package com.zacharyhirsch.moldygameboy.emulator.ppu;

import com.zacharyhirsch.moldygameboy.emulator.arch.Irq;
import com.zacharyhirsch.moldygameboy.emulator.io.Color;
import com.zacharyhirsch.moldygameboy.emulator.io.Video;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public final class Ppu {

  private static final Color[] COLORS = {
    new Color((byte) 0xff, (byte) 0xff, (byte) 0xff), // white
    new Color((byte) 0xaa, (byte) 0xaa, (byte) 0xaa), // light gray
    new Color((byte) 0x55, (byte) 0x55, (byte) 0x55), // dark gray
    new Color((byte) 0x00, (byte) 0x00, (byte) 0x00), // black
  };

  enum Mode {
    HBLANK(0b0000_0000),
    VBLANK(0b0000_0001),
    OAM_SCAN(0b0000_0010),
    DRAWING(0b0000_0011);

    final int bits;

    Mode(int bits) {
      this.bits = bits;
    }
  }

  private final IoRegisters ioregs;
  private final ByteBuffer oam;
  private final OamScanner oamScanner;
  private final Video video;
  private final Irq vblank;
  private final Irq lcd;
  private final Queue<Integer> bgFifo;
  private final List<ObjPixel> objFifo;
  private final BgPixelFetcher bgFetcher;
  private final ObjPixelFetcher objFetcher;
  private final boolean[] objsFetched;

  private Mode mode = Mode.OAM_SCAN;
  private int dot = 0;
  private int screenX = 0;
  private int pixelsToDiscard = 0;

  // Window state: wyTriggered latches once LY == WY during a frame; windowLine is the window's
  // own line counter, which only advances on lines where the window was actually drawn.
  private boolean wyTriggered = false;
  private boolean windowActive = false;
  private int windowLine = 0;

  public Ppu(
      IoRegisters ioregs, ByteBuffer vram, ByteBuffer oam, Video video, Irq vblank, Irq lcd) {
    this.ioregs = ioregs;
    this.oam = oam;
    this.oamScanner = new OamScanner(ioregs, oam);
    this.video = video;
    this.vblank = vblank;
    this.lcd = lcd;
    this.bgFifo = new ArrayDeque<>();
    this.objFifo = new ArrayList<>();
    this.bgFetcher = new BgPixelFetcher(ioregs, vram, bgFifo);
    this.objFetcher = new ObjPixelFetcher(ioregs, vram, oam);
    this.objsFetched = new boolean[10];
  }

  public void tick() {
    if (!ioregs.lcdc.bit(7)) {
      // LCD off: the PPU is held in reset. LY reads 0, STAT reports mode 0. Line 0 after
      // enabling is 4 dots short (see mooneye ppu/lcdon_timing-GS), so start it at dot 4.
      startLine(0, 4);
      updateStatAndIrqs();
      return;
    }
    switch (mode) {
      case OAM_SCAN -> tickOamScan();
      case DRAWING -> tickDrawing();
      case HBLANK -> tickHBlank();
      case VBLANK -> tickVBlank();
    }
    dot++;
    if (dot == 456) {
      int nextLy = (ioregs.ly.get() == 153) ? 0 : (ioregs.ly.get() + 1);
      startLine(nextLy, 0);
      if (nextLy == 0) {
        video.present();
      }
    } else if (dot == 80) {
      if (mode == Mode.OAM_SCAN) {
        mode = Mode.DRAWING;
      }
    } else if (screenX == 160) {
      if (mode == Mode.DRAWING) {
        mode = Mode.HBLANK;
      }
    }
    updateStatAndIrqs();
  }

  private void startLine(int line, int firstDot) {
    mode = line <= 143 ? Mode.OAM_SCAN : Mode.VBLANK;
    dot = firstDot;
    screenX = 0;
    oamScanner.reset();
    bgFifo.clear();
    objFifo.clear();
    bgFetcher.reset();
    objFetcher.reset();
    Arrays.fill(objsFetched, false);
    ioregs.ly.set(line);
    if (windowActive) {
      windowLine++;
      windowActive = false;
    }
    if (line == 0) {
      windowLine = 0;
      wyTriggered = false;
    }
  }

  private void updateStatAndIrqs() {
    if (!ioregs.lcdc.bit(7)) {
      ioregs.stat.set(ioregs.stat.get() & 0b1111_1000);
      vblank.set(false);
      lcd.set(false);
    } else {
      ioregs.stat.set(
          (ioregs.stat.get() & 0b1111_1000)
              | (ioregs.ly.get() == ioregs.lyc.get() ? 0b0000_0100 : 0b0000_0000)
              | mode.bits);
      vblank.set(mode == Mode.VBLANK);
      lcd.set(
          (ioregs.stat.bit(5) && mode == Mode.OAM_SCAN)
              || (ioregs.stat.bit(4) && mode == Mode.VBLANK)
              || (ioregs.stat.bit(3) && mode == Mode.HBLANK)
              || (ioregs.stat.bit(6) && ioregs.stat.bit(2)));
    }
  }

  private void tickOamScan() {
    if (ioregs.ly.get() == ioregs.wy.get()) {
      wyTriggered = true;
    }
    oamScanner.tick();
  }

  private void tickDrawing() {
    if (dot == 80) {
      pixelsToDiscard = ioregs.scx.get() & 0b0000_0111;
      bgFetcher.latchScroll();
    }
    if (dot < 84) {
      return;
    }

    if (objFetcher.isActive()) {
      objFetcher.tick();
      if (objFetcher.isDone()) {
        mergeIntoObjFifo(objFetcher.sprite(), objFetcher.attr(), objFetcher.lo(), objFetcher.hi());
        objsFetched[objFetcher.slot()] = true;
        objFetcher.reset();
      }
      return;
    }

    if (shouldStartWindow()) {
      // Starting the window throws away the background pixels in flight and restarts the
      // fetcher on the window tile map. WX < 7 starts the window partially off-screen.
      windowActive = true;
      bgFifo.clear();
      bgFetcher.startWindow(windowLine);
      pixelsToDiscard = Math.max(0, 7 - ioregs.wx.get());
    }

    int slot = nextSpriteSlot();
    if (slot >= 0) {
      if (bgFetcher.isBetweenTiles() && !bgFifo.isEmpty()) {
        objFetcher.start(slot, oamScanner.selected().get(slot));
      } else {
        bgFetcher.tick();
      }
      return;
    }

    if (!bgFifo.isEmpty()) {
      int bgPixel = bgFifo.remove();
      if (pixelsToDiscard > 0) {
        pixelsToDiscard--;
      } else {
        ObjPixel objPixel = objFifo.isEmpty() ? null : objFifo.removeFirst();
        video.writeVideoPixel(screenX++, ioregs.ly.get(), mix(bgPixel, objPixel));
      }
    }

    bgFetcher.tick();
  }

  private static void tickHBlank() {}

  private static void tickVBlank() {}

  private int nextSpriteSlot() {
    List<Sprite> sprites = oamScanner.selected();
    for (int i = 0; i < sprites.size(); i++) {
      if (!objsFetched[i] && sprites.get(i).x() <= screenX + 8) {
        return i;
      }
    }
    return -1;
  }

  private void mergeIntoObjFifo(Sprite sprite, int attr, int lo, int hi) {
    boolean xflip = bit(attr, 5) != 0;
    boolean obp1 = bit(attr, 4) != 0;
    boolean behindBg = bit(attr, 7) != 0;
    int dropLeft = Math.max(0, screenX + 8 - sprite.x()); // clips sprites with OAM X < 8
    for (int i = dropLeft; i < 8; i++) {
      int slot = i - dropLeft;
      while (objFifo.size() <= slot) {
        objFifo.add(new ObjPixel(0, false, false)); // pad with color 0
      }
      if (objFifo.get(slot).color() == 0) { // earlier sprite's opaque pixel wins
        int b = xflip ? i : 7 - i;
        int color = (bit(hi, b) << 1) | bit(lo, b);
        objFifo.set(slot, new ObjPixel(color, obp1, behindBg));
      }
    }
  }

  private Color mix(int bg, ObjPixel obj) {
    if (!ioregs.lcdc.bit(0)) {
      bg = 0;
    }
    return obj == null || !ioregs.lcdc.bit(1) || obj.color() == 0 || (obj.behindBg() && bg != 0)
        ? applyPalette(ioregs.bgp.get(), bg)
        : applyPalette(obj.obp1() ? ioregs.obp1.get() : ioregs.obp0.get(), obj.color());
  }

  private Color applyPalette(int palette, int color) {
    return switch (color) {
      case 0 -> COLORS[(palette & 0b0000_0011) >>> 0];
      case 1 -> COLORS[(palette & 0b0000_1100) >>> 2];
      case 2 -> COLORS[(palette & 0b0011_0000) >>> 4];
      case 3 -> COLORS[(palette & 0b1100_0000) >>> 6];
      default -> throw new IllegalStateException("%d".formatted(color));
    };
  }

  private boolean shouldStartWindow() {
    return !windowActive && wyTriggered && ioregs.lcdc.bit(5) && screenX + 7 >= ioregs.wx.get();
  }

  private static int bit(int value, int index) {
    return (value & (1 << index)) != 0 ? 1 : 0;
  }
}
