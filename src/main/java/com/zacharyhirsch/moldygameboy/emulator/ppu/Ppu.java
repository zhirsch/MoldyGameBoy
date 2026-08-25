package com.zacharyhirsch.moldygameboy.emulator.ppu;

import com.zacharyhirsch.moldygameboy.emulator.arch.InterruptRequestLine;
import com.zacharyhirsch.moldygameboy.emulator.io.Color;
import com.zacharyhirsch.moldygameboy.emulator.io.Video;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import java.util.ArrayDeque;
import java.util.Queue;

public final class Ppu {

  private static final Color[] PALETTE = {
    new Color((byte) 0xff, (byte) 0xff, (byte) 0xff), // white
    new Color((byte) 0xaa, (byte) 0xaa, (byte) 0xaa), // light gray
    new Color((byte) 0x55, (byte) 0x55, (byte) 0x55), // dark gray
    new Color((byte) 0x00, (byte) 0x00, (byte) 0x00), // black
  };

  private enum Mode {
    MODE_0_HBLANK((byte) 0),
    MODE_1_VBLANK((byte) 1),
    MODE_2_OAM_SCAN((byte) 2),
    MODE_3_DRAWING((byte) 3),
    ;

    private final byte value;

    Mode(byte value) {
      this.value = value;
    }

    public byte getValue() {
      return value;
    }
  }

  private final Memory memory;
  private final Video video;
  private final InterruptRequestLine vblank;
  private final InterruptRequestLine lcd;
  private final Queue<Integer> fifo;
  private final PixelFetcher fetcher;

  private int dot = 0;
  private int drawnDots = 0;
  private int pushedPixels = 0;
  private Mode mode = Mode.MODE_2_OAM_SCAN;
  private boolean yCondition = false;
  private boolean isStalledForSprite = false;
  private boolean windowWasDrawnThisLine = false;

  public Ppu(Memory memory, Video video, InterruptRequestLine vblank, InterruptRequestLine lcd) {
    this.memory = memory;
    this.video = video;
    this.vblank = vblank;
    this.lcd = lcd;
    this.fifo = new ArrayDeque<>();
    this.fetcher = new PixelFetcher(memory);
  }

  public void tick() {
    dot++;
    switch (mode) {
      case MODE_2_OAM_SCAN -> tickOamScan();
      case MODE_3_DRAWING -> tickDrawing();
      case MODE_0_HBLANK -> tickHBlank();
      case MODE_1_VBLANK -> tickVBlank();
    }
    boolean lyEqualsLyc = memory.registers().ly().get() == memory.registers().lyc().get();
    memory.registers().stat().setLyEqualsLyc(lyEqualsLyc);
    memory.registers().stat().setMode(mode.getValue());
    if (dot == 456) {
      dot = 0;
    }
    vblank.set(mode == Mode.MODE_1_VBLANK);
    lcd.set((memory.registers().stat().get() & 0b0111_1100) != 0);
  }

  private void tickOamScan() {
    if (dot == 80) {
      mode = Mode.MODE_3_DRAWING;
    }
  }

  private void tickDrawing() {
    if (!fifo.isEmpty() && !isStalledForSprite) {
      render(fifo.remove());
      pushedPixels++;
    }
    boolean isWindowEnabled = memory.registers().lcdc().isWindowEnabled();
    if (isWindowEnabled && yCondition && !fetcher.isInWindow() && (memory.registers().wx().get() & 0xff) - 7 == drawnDots) {
      fetcher.startWindow();
      fifo.clear();
      windowWasDrawnThisLine = true;
    }
    fetcher.tick(fifo);
    if (pushedPixels == 160) {
      pushedPixels = 0;
      mode = Mode.MODE_0_HBLANK;
      fetcher.nextLine(windowWasDrawnThisLine);
    }
  }

  private void tickHBlank() {
    if (dot == 456) {
      int nextLy = Byte.toUnsignedInt(memory.registers().ly().get()) + 1;
      if (nextLy == 144) {
        mode = Mode.MODE_1_VBLANK;
      } else {
        mode = Mode.MODE_2_OAM_SCAN;
      }
      memory.registers().ly().set((byte) nextLy);
      drawnDots = 0;
      windowWasDrawnThisLine = false;
      yCondition = nextLy >= Byte.toUnsignedInt(memory.registers().wy().get());
    }
  }

  private void tickVBlank() {
    if (dot == 456) {
      int nextLy = Byte.toUnsignedInt(memory.registers().ly().get()) + 1;
      if (nextLy == 154) {
        video.present();
        mode = Mode.MODE_2_OAM_SCAN;
        nextLy = 0;
        yCondition = false;
        fetcher.nextFrame();
      }
      memory.registers().ly().set((byte) nextLy);
    }
  }

  private void render(int pixel) {
    int colorIdx =
        switch (pixel) {
          case 0 -> (memory.registers().bgp().get() & 0b0000_0011) >>> 0;
          case 1 -> (memory.registers().bgp().get() & 0b0000_1100) >>> 2;
          case 2 -> (memory.registers().bgp().get() & 0b0011_0000) >>> 4;
          case 3 -> (memory.registers().bgp().get() & 0b1100_0000) >>> 6;
          default -> throw new IllegalStateException();
        };
    int y = Byte.toUnsignedInt(memory.registers().ly().get());
    video.writeVideoPixel(drawnDots++, y, PALETTE[colorIdx]);
  }
}
