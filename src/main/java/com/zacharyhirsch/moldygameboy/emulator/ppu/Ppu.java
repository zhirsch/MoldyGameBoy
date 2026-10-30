package com.zacharyhirsch.moldygameboy.emulator.ppu;

import com.zacharyhirsch.moldygameboy.emulator.arch.Irq;
import com.zacharyhirsch.moldygameboy.emulator.io.Color;
import com.zacharyhirsch.moldygameboy.emulator.io.Video;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public final class Ppu {

  private static final Color[] PALETTE = {
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
  private final Video video;
  private final Irq vblank;
  private final Irq lcd;
  private final Queue<Integer> fifo;
  private final PixelFetcher fetcher;

  private Mode mode = Mode.OAM_SCAN;
  private int dot = 0;
  private int screenX = 0;
  private int pixelsToDiscard = 0;

  // Window state: wyTriggered latches once LY == WY during a frame; windowLine is the window's
  // own line counter, which only advances on lines where the window was actually drawn.
  private boolean wyTriggered = false;
  private boolean windowActive = false;
  private int windowLine = 0;

  public Ppu(IoRegisters ioregs, ByteBuffer vram, Video video, Irq vblank, Irq lcd) {
    this.ioregs = ioregs;
    this.video = video;
    this.vblank = vblank;
    this.lcd = lcd;
    this.fifo = new ArrayDeque<>();
    this.fetcher = new PixelFetcher(ioregs, vram, fifo);
  }

  public void tick() {
    if (!ioregs.lcdc.bit(7)) {
      // LCD off: the PPU is held in reset. LY reads 0, STAT reports mode 0. Line 0 after
      // enabling is 4 dots short (see mooneye ppu/lcdon_timing-GS), so start it at dot 4.
      mode = Mode.OAM_SCAN;
      dot = 4;
      screenX = 0;
      wyTriggered = false;
      windowActive = false;
      windowLine = 0;
      fetcher.reset();
      fifo.clear();
      ioregs.ly.set(0);
      ioregs.stat.set(ioregs.stat.get() & 0b1111_1000);
      vblank.set(false);
      lcd.set(false);
      return;
    }
    assert 0 <= dot && dot <= 455;
    assert 0 <= ioregs.ly.get() && ioregs.ly.get() <= 153;
    switch (mode) {
      case OAM_SCAN -> {
        assert 0 <= dot && dot < 80;
        tickOamScan();
      }
      case DRAWING -> {
        assert 80 <= dot && dot < 369;
        tickDrawing();
      }
      case HBLANK -> {
        assert 252 <= dot && dot < 456;
        tickHBlank();
      }
      case VBLANK -> {
        assert 144 <= ioregs.ly.get() && ioregs.ly.get() <= 153;
        tickVBlank();
      }
    }
    vblank.set(mode == Mode.VBLANK);
    ioregs.stat.set(
        (ioregs.stat.get() & 0b1111_1000)
            | (ioregs.ly.get() == ioregs.lyc.get() ? 0b0000_0100 : 0b0000_0000)
            | mode.bits);
    lcd.set(
        (ioregs.stat.bit(5) && mode == Mode.OAM_SCAN)
            || (ioregs.stat.bit(4) && mode == Mode.VBLANK)
            || (ioregs.stat.bit(3) && mode == Mode.HBLANK)
            || (ioregs.stat.bit(6) && ioregs.stat.bit(2)));
    if (dot == 455) {
      dot = 0;
      screenX = 0;
      if (windowActive) {
        windowActive = false;
        windowLine++;
      }
      fetcher.reset();
      fifo.clear();
      if (ioregs.ly.get() == 153) {
        ioregs.ly.set(0);
        wyTriggered = false;
        windowLine = 0;
        video.present();
      } else {
        ioregs.ly.set(ioregs.ly.get() + 0b0000_0001);
      }
      mode = ioregs.ly.get() <= 143 ? Mode.OAM_SCAN : Mode.VBLANK;
    } else {
      dot++;
      if (mode == Mode.OAM_SCAN && dot == 80) {
        mode = Mode.DRAWING;
      } else if (mode == Mode.DRAWING && screenX == 160) {
        mode = Mode.HBLANK;
      }
    }
  }

  private void tickOamScan() {
    if (ioregs.ly.get() == ioregs.wy.get()) {
      wyTriggered = true;
    }
  }

  private void tickDrawing() {
    if (dot == 80) {
      pixelsToDiscard = (ioregs.scx.get() & 0b0000_0111) >>> 0;
      fetcher.latchScroll();
    }
    if (80 <= dot && dot < 84) {
    } else {
      if (shouldStartWindow()) {
        // Starting the window throws away the background pixels in flight and restarts the
        // fetcher on the window tile map. WX < 7 starts the window partially off-screen.
        windowActive = true;
        fifo.clear();
        fetcher.startWindow(windowLine);
        pixelsToDiscard = Math.max(0, 7 - ioregs.wx.get());
      }
      if (!fifo.isEmpty()) {
        render(fifo.remove());
      }
      fetcher.tick();
    }
  }

  private boolean shouldStartWindow() {
    return !windowActive
        && wyTriggered
        && ioregs.lcdc.bit(5)
        && screenX + 7 >= ioregs.wx.get();
  }

  private static void tickHBlank() {}

  private static void tickVBlank() {}

  private void render(int pixel) {
    if (pixelsToDiscard > 0) {
      pixelsToDiscard--;
      return;
    }
    if (!ioregs.lcdc.bit(0)) {
      // On DMG, LCDC.0 clear blanks both the background and the window to color 0.
      pixel = 0;
    }
    int colorIdx =
        switch (pixel) {
          case 0 -> (ioregs.bgp.get() & 0b0000_0011) >>> 0;
          case 1 -> (ioregs.bgp.get() & 0b0000_1100) >>> 2;
          case 2 -> (ioregs.bgp.get() & 0b0011_0000) >>> 4;
          case 3 -> (ioregs.bgp.get() & 0b1100_0000) >>> 6;
          default -> throw new IllegalStateException();
        };
    video.writeVideoPixel(screenX++, ioregs.ly.get(), PALETTE[colorIdx]);
  }
}
