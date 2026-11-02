package com.zacharyhirsch.moldygameboy.emulator.ppu;

import com.zacharyhirsch.moldygameboy.emulator.arch.Irq;
import com.zacharyhirsch.moldygameboy.emulator.io.Color;
import com.zacharyhirsch.moldygameboy.emulator.io.Video;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.VideoMemoryLock;
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

  // LY changes and the mode 2 interrupt fires at dot 0, but STAT reports mode 0 (with the LY=LYC
  // flag clear) until OAM_SCAN_START. Mode 3 starts at DRAWING_START (see mooneye
  // ppu/lcdon_timing-GS and ppu/intr_2_mode3_timing).
  private static final int OAM_SCAN_START = 4;
  private static final int DRAWING_START = 84;

  private final IoRegisters ioregs;
  private final VideoMemoryLock videoMemoryLock;
  private final OamScanner oamScanner;
  private final Video video;
  private final Irq vblank;
  private final Irq lcd;
  private final Runnable onHBlank;
  private final PaletteRam bgPalettes;
  private final PaletteRam objPalettes;
  private final Queue<BgPixel> bgFifo;
  private final List<ObjPixel> objFifo;
  private final BgPixelFetcher bgFetcher;
  private final ObjPixelFetcher objFetcher;
  private final boolean[] objsFetched;

  private Mode mode = Mode.OAM_SCAN;
  private int dot = 0;
  private int screenX = 0;
  private int pixelsToDiscard = 0;
  private int junkPixels = 0;

  // Window state: wyTriggered latches once LY == WY during a frame; windowLine is the window's
  // own line counter, which only advances on lines where the window was actually drawn.
  private boolean wyTriggered = false;
  private boolean windowActive = false;
  private int windowLine = 0;

  // The first line after the LCD is enabled has no real OAM scan: STAT reports mode 0, OAM stays
  // accessible and no mode 2 interrupt fires (see mooneye ppu/lcdon_timing-GS).
  private boolean firstLineAfterLcdOn = false;

  public Ppu(
      IoRegisters ioregs,
      ByteBuffer vram,
      ByteBuffer oam,
      VideoMemoryLock videoMemoryLock,
      Video video,
      Irq vblank,
      Irq lcd,
      Runnable onHBlank,
      PaletteRam bgPalettes,
      PaletteRam objPalettes) {
    this.ioregs = ioregs;
    this.videoMemoryLock = videoMemoryLock;
    this.oamScanner = new OamScanner(ioregs, oam);
    this.video = video;
    this.vblank = vblank;
    this.lcd = lcd;
    this.onHBlank = onHBlank;
    this.bgPalettes = bgPalettes;
    this.objPalettes = objPalettes;
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
      firstLineAfterLcdOn = true;
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
      firstLineAfterLcdOn = false;
      int nextLy = (ioregs.ly.get() == 153) ? 0 : (ioregs.ly.get() + 1);
      startLine(nextLy, 0);
      if (nextLy == 0) {
        video.present();
      }
    } else if (dot == DRAWING_START) {
      if (mode == Mode.OAM_SCAN) {
        mode = Mode.DRAWING;
      }
    } else if (screenX == 160) {
      if (mode == Mode.DRAWING) {
        mode = Mode.HBLANK;
        onHBlank.run();
      }
    }
    updateStatAndIrqs();
  }

  private void startLine(int line, int firstDot) {
    mode = line <= 143 ? Mode.OAM_SCAN : Mode.VBLANK;
    dot = firstDot;
    screenX = 0;
    junkPixels = 0;
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
      // While the LCD is off, STAT reports mode 0 but the LY=LYC flag keeps its last value, and
      // the STAT interrupt line stays where it was, so turning the LCD back on only interrupts if
      // the line goes from low to high (see mooneye ppu/stat_lyc_onoff).
      ioregs.stat.set(ioregs.stat.get() & 0b1111_1100);
      vblank.set(false);
      videoMemoryLock.set(false, false, false, false);
      return;
    }
    boolean oamScan = mode == Mode.OAM_SCAN && !firstLineAfterLcdOn;
    boolean lineStart = mode != Mode.VBLANK && dot < OAM_SCAN_START;
    boolean lyc = !lineStart && ioregs.ly.get() == ioregs.lyc.get();
    boolean drawing = mode == Mode.DRAWING;
    boolean lastOamScanCycle = oamScan && dot >= DRAWING_START - 4;
    videoMemoryLock.set(
        drawing || oamScan,
        drawing || (oamScan && !lineStart && !lastOamScanCycle),
        drawing || lastOamScanCycle,
        drawing);
    ioregs.stat.set(
        (ioregs.stat.get() & 0b1111_1000)
            | (lyc ? 0b0000_0100 : 0b0000_0000)
            | (mode == Mode.OAM_SCAN && (firstLineAfterLcdOn || lineStart) ? 0 : mode.bits));
    vblank.set(mode == Mode.VBLANK);
    // The mode 2 STAT condition is also raised for the first M-cycle of line 144, at the same
    // time as the VBlank interrupt (see mooneye ppu/vblank_stat_intr-GS). On a CGB it's raised
    // one M-cycle earlier, at the end of line 143 (see mooneye misc/ppu/vblank_stat_intr-C).
    boolean line144OamCondition =
        (ioregs.ly.get() == 144 && dot < 4)
            || (ioregs.isCgb() && ioregs.ly.get() == 143 && dot >= 456 - 4);
    boolean oamCondition = oamScan || line144OamCondition;
    lcd.set(
        (ioregs.stat.bit(5) && oamCondition)
            || (ioregs.stat.bit(4) && mode == Mode.VBLANK)
            || (ioregs.stat.bit(3) && mode == Mode.HBLANK)
            || (ioregs.stat.bit(6) && ioregs.stat.bit(2)));
  }

  private void tickOamScan() {
    if (ioregs.ly.get() == ioregs.wy.get()) {
      wyTriggered = true;
    }
    if (dot >= OAM_SCAN_START) {
      oamScanner.tick();
    }
  }

  private void tickDrawing() {
    if (dot == DRAWING_START) {
      pixelsToDiscard = ioregs.scx.get() & 0b0000_0111;
      bgFetcher.latchScroll();
      // The line starts with 8 junk pixels that are shifted out but never shown, while the
      // fetcher fetches the first real tile. Sprites with X < 8 are hit during these pixels, so
      // they stall for the same 5 - min(5, X % 8) dots as any other sprite. Mode 3 is 169 dots
      // without SCX or sprites (see mooneye ppu/intr_2_mode0_timing_sprites,
      // ppu/intr_2_0_timing and ppu/hblank_ly_scx_timing-GS).
      for (int i = 0; i < 8; i++) {
        bgFifo.add(new BgPixel(0, 0, false));
      }
      junkPixels = 8;
      return;
    }

    if (objFetcher.isActive()) {
      tickObjFetcher();
      return;
    }

    if (junkPixels == 0 && shouldStartWindow()) {
      // Starting the window throws away the background pixels in flight and restarts the
      // fetcher on the window tile map. WX < 7 starts the window partially off-screen.
      windowActive = true;
      bgFifo.clear();
      bgFetcher.startWindow(windowLine);
      pixelsToDiscard = Math.max(0, 7 - ioregs.wx.get());
    }

    int slot = nextSpriteSlot();
    if (slot >= 0) {
      // Output stops while the background fetcher finishes the tile it's working on; the sprite
      // fetch can start on the same dot that the tile's data arrives, and then takes 6 dots.
      bgFetcher.tick();
      if (bgFetcher.hasTileData() && !bgFifo.isEmpty()) {
        objFetcher.start(slot, oamScanner.selected().get(slot));
        tickObjFetcher();
      }
      return;
    }

    if (!bgFifo.isEmpty()) {
      BgPixel bgPixel = bgFifo.remove();
      if (junkPixels > 0) {
        junkPixels--;
      } else if (pixelsToDiscard > 0) {
        pixelsToDiscard--;
      } else {
        ObjPixel objPixel = objFifo.isEmpty() ? null : objFifo.removeFirst();
        video.writeVideoPixel(screenX++, ioregs.ly.get(), mix(bgPixel, objPixel));
      }
    }

    bgFetcher.tick();
  }

  private void tickObjFetcher() {
    objFetcher.tick();
    if (objFetcher.isDone()) {
      mergeIntoObjFifo(objFetcher.sprite(), objFetcher.attr(), objFetcher.lo(), objFetcher.hi());
      objsFetched[objFetcher.slot()] = true;
      objFetcher.reset();
    }
  }

  private static void tickHBlank() {}

  private static void tickVBlank() {}

  private int nextSpriteSlot() {
    List<Sprite> sprites = oamScanner.selected();
    for (int i = 0; i < sprites.size(); i++) {
      if (!objsFetched[i] && sprites.get(i).x() <= screenX + 8 - junkPixels) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Merges a fetched object's pixels into the object FIFO. Where objects overlap, the one already
   * there wins (sprites are fetched left to right, so the leftmost wins), except in CGB mode with
   * OPRI clear, where the one earlier in OAM wins.
   */
  private void mergeIntoObjFifo(Sprite sprite, int attr, int lo, int hi) {
    boolean cgb = ioregs.isCgbMode();
    boolean oamPriority = cgb && !ioregs.opri.bit(0);
    boolean xflip = bit(attr, 5) != 0;
    int palette = cgb ? attr & 0x07 : bit(attr, 4);
    boolean behindBg = bit(attr, 7) != 0;
    int dropLeft = Math.max(0, screenX + 8 - sprite.x()); // clips sprites with OAM X < 8
    for (int i = dropLeft; i < 8; i++) {
      int slot = i - dropLeft;
      while (objFifo.size() <= slot) {
        objFifo.add(new ObjPixel(0, 0, false, 0)); // pad with color 0
      }
      int b = xflip ? i : 7 - i;
      int color = (bit(hi, b) << 1) | bit(lo, b);
      ObjPixel existing = objFifo.get(slot);
      boolean wins =
          existing.color() == 0
              || (oamPriority && color != 0 && sprite.oamIndex() < existing.oamIndex());
      if (wins) {
        objFifo.set(slot, new ObjPixel(color, palette, behindBg, sprite.oamIndex()));
      }
    }
  }

  private Color mix(BgPixel bg, ObjPixel obj) {
    boolean objVisible = obj != null && ioregs.lcdc.bit(1) && obj.color() != 0;
    if (ioregs.isCgbMode()) {
      // LCDC bit 0 clear puts objects above the background whatever the priority bits say.
      boolean bgWins =
          objVisible
              && ioregs.lcdc.bit(0)
              && bg.color() != 0
              && (bg.priority() || obj.behindBg());
      return objVisible && !bgWins
          ? objPalettes.color(obj.palette(), obj.color())
          : bgPalettes.color(bg.palette(), bg.color());
    }
    int bgColor = ioregs.lcdc.bit(0) ? bg.color() : 0;
    if (objVisible && !(obj.behindBg() && bgColor != 0)) {
      int obp = obj.palette() == 1 ? ioregs.obp1.get() : ioregs.obp0.get();
      return dmgColor(obp, obj.color(), objPalettes, obj.palette());
    }
    return dmgColor(ioregs.bgp.get(), bgColor, bgPalettes, 0);
  }

  /**
   * Maps a color through a DMG palette register to one of 4 shades. A DMG shows the shade as gray;
   * a CGB in DMG compatibility mode looks it up in CGB palette 0 (or 1 for OBP1), which the boot
   * ROM sets up.
   */
  private Color dmgColor(int palette, int color, PaletteRam cgbPalettes, int cgbPalette) {
    int shade = (palette >>> (color * 2)) & 0b11;
    return ioregs.isCgb() ? cgbPalettes.color(cgbPalette, shade) : COLORS[shade];
  }

  private boolean shouldStartWindow() {
    return !windowActive && wyTriggered && ioregs.lcdc.bit(5) && screenX + 7 >= ioregs.wx.get();
  }

  private static int bit(int value, int index) {
    return (value & (1 << index)) != 0 ? 1 : 0;
  }
}
