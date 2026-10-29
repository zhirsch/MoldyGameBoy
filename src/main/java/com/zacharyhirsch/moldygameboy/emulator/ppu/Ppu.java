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

  private final IoRegisters ioregs;
  private final Video video;
  private final Irq vblank;
  private final Irq lcd;
  private final Queue<Integer> fifo;
  private final PixelFetcher fetcher;

  private int dot = 0;
  private int screenX = 0;
  private int fineScx = 0;

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
      dot = 4;
      screenX = 0;
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
    int mode;
    if (0 <= ioregs.ly.get() && ioregs.ly.get() <= 143) {
      assert 0 <= dot && dot < 456;
      if (0 <= dot && dot < 80) {
        mode = 0b0000_0010;
        tickOamScan();
      } else if (0 <= screenX && screenX < 160) {
        assert 80 <= dot && dot < 369;
        mode = 0b0000_0011;
        tickDrawing();
      } else if (252 <= dot && dot < 456) {
        mode = 0b0000_0000;
        tickHBlank();
      } else {
        throw new IllegalStateException("dot=%03d screenX=%03d".formatted(dot, screenX));
      }
    } else {
      mode = 0b0000_0001;
      tickVBlank();
    }
    vblank.set(mode == 0b0000_0001);
    ioregs.stat.set(
        (ioregs.stat.get() & 0b1111_1000)
            | (ioregs.ly.get() == ioregs.lyc.get() ? 0b0000_0100 : 0b0000_0000)
            | mode);
    lcd.set(
        (ioregs.stat.bit(5) && mode == 0b0000_0010)
            || (ioregs.stat.bit(4) && mode == 0b0000_0001)
            || (ioregs.stat.bit(3) && mode == 0b0000_0000)
            || (ioregs.stat.bit(6) && ioregs.stat.bit(2)));
    if (dot == 455) {
      dot = 0;
      screenX = 0;
      fetcher.reset();
      fifo.clear();
      if (ioregs.ly.get() == 153) {
        ioregs.ly.set(0);
        video.present();
      } else {
        ioregs.ly.set(ioregs.ly.get() + 0b0000_0001);
      }
    } else {
      dot++;
    }
  }

  private static void tickOamScan() {}

  private void tickDrawing() {
    if (dot == 80) {
      fineScx = (ioregs.scx.get() & 0b0000_0111) >>> 0;
      fetcher.latchScroll();
    }
    if (80 <= dot && dot < 84) {
    } else {
      if (!fifo.isEmpty()) {
        render(fifo.remove());
      }
      fetcher.tick();
    }
  }

  private static void tickHBlank() {}

  private static void tickVBlank() {}

  private void render(int pixel) {
    if (fineScx > 0) {
      fineScx--;
      return;
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
