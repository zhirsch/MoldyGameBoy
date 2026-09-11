package com.zacharyhirsch.moldygameboy.emulator.ppu;

import com.zacharyhirsch.moldygameboy.emulator.arch.InterruptRequestLine;
import com.zacharyhirsch.moldygameboy.emulator.io.Color;
import com.zacharyhirsch.moldygameboy.emulator.io.Video;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.Vram;
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

  enum State {
    GET_TILE_ID,
    GET_TILE_DATA_LO,
    GET_TILE_DATA_HI,
    SLEEP,
    PUSH_TO_FIFO,
  }

  private final IoRegisters ioregs;
  private final Vram vram;
  private final Video video;
  private final InterruptRequestLine vblank;
  private final InterruptRequestLine lcd;
  private final Queue<Integer> fifo;

  private int dot = 0;
  private int drawnDots = 0;
  private int pushedPixels = 0;
  private Mode mode = Mode.MODE_2_OAM_SCAN;
  private State state = State.GET_TILE_ID;

  private int clock = 0;
  private int fetcherX = 0;
  private byte tileId;
  private byte tileDataLo;
  private byte tileDataHi;

  public Ppu(
      IoRegisters ioregs,
      Vram vram,
      Video video,
      InterruptRequestLine vblank,
      InterruptRequestLine lcd) {
    this.ioregs = ioregs;
    this.vram = vram;
    this.video = video;
    this.vblank = vblank;
    this.lcd = lcd;
    this.fifo = new ArrayDeque<>();
  }

  public void tick() {
    if (dot == 0) {
      drawnDots = 0;
    }
    dot++;
    switch (mode) {
      case MODE_2_OAM_SCAN -> tickOamScan();
      case MODE_3_DRAWING -> tickDrawing();
      case MODE_0_HBLANK -> tickHBlank();
      case MODE_1_VBLANK -> tickVBlank();
    }
    ioregs.stat.set((byte) (ioregs.stat.get() & 0xf8));
    ioregs.stat.set((byte) (ioregs.stat.get() | (mode.getValue() & 0x03)));
    ioregs.stat.set((byte) (ioregs.stat.get() | (ioregs.ly.get() == ioregs.lyc.get() ? 0x04 : 0)));
    if (dot == 456) {
      dot = 0;
    }
    vblank.set(mode == Mode.MODE_1_VBLANK);
    lcd.set(
        (mode == Mode.MODE_0_HBLANK && ioregs.stat.bit(3))
            || (mode == Mode.MODE_1_VBLANK && ioregs.stat.bit(4))
            || (mode == Mode.MODE_2_OAM_SCAN && ioregs.stat.bit(5))
            || (ioregs.ly.get() == ioregs.lyc.get() && ioregs.stat.bit(6)));
  }

  private void tickOamScan() {
    if (dot == 80) {
      fifo.clear();
      mode = Mode.MODE_3_DRAWING;
    }
  }

  private void tickDrawing() {
    if (!fifo.isEmpty()) {
      render(fifo.remove());
      pushedPixels++;
    }
    switch (state) {
      case GET_TILE_ID -> getTileId();
      case GET_TILE_DATA_LO -> getTileDataLo();
      case GET_TILE_DATA_HI -> getTileDataHi();
      case SLEEP -> sleep();
      case PUSH_TO_FIFO -> pushToFifo();
    }
    if (pushedPixels == 160) {
      pushedPixels = 0;
      mode = Mode.MODE_0_HBLANK;
      fetcherX = 0;
    }
  }

  private void tickHBlank() {
    if (dot == 456) {
      ioregs.ly.set((byte) (ioregs.ly.get() + 1));
      mode = ioregs.ly.get() == 144 ? Mode.MODE_1_VBLANK : Mode.MODE_2_OAM_SCAN;
    }
  }

  private void tickVBlank() {
    if (dot == 456) {
      ioregs.ly.set((byte) (ioregs.ly.get() + 1));
      if (ioregs.ly.get() == 154) {
        video.present();
        ioregs.ly.set(0);
        mode = Mode.MODE_2_OAM_SCAN;
      }
    }
  }

  private void render(int pixel) {
    int colorIdx =
        switch (pixel) {
          case 0 -> (ioregs.bgp.get() & 0b0000_0011) >>> 0;
          case 1 -> (ioregs.bgp.get() & 0b0000_1100) >>> 2;
          case 2 -> (ioregs.bgp.get() & 0b0011_0000) >>> 4;
          case 3 -> (ioregs.bgp.get() & 0b1100_0000) >>> 6;
          default -> throw new IllegalStateException();
        };
    video.writeVideoPixel(drawnDots++, ioregs.ly.get(), PALETTE[colorIdx]);
  }

  private void getTileId() {
    if (clock == 0) {
      clock++;
      return;
    }
    int base = ioregs.lcdc.bit(3) ? 0x9c00 : 0x9800;
    int tileX = (fetcherX + (ioregs.scx.get() / 8)) & 0x1f;
    int tileY = (ioregs.ly.get() + ioregs.scy.get()) & 0xff;
    int address = base + (tileY / 8 * 32) + tileX;
    tileId = vram.read((short) address);
    state = State.GET_TILE_DATA_LO;
    clock = 0;
  }

  private void getTileDataLo() {
    if (clock == 0) {
      clock++;
      return;
    }
    tileDataLo = vram.read((short) computeTileAddress());
    state = State.GET_TILE_DATA_HI;
    clock = 0;
  }

  private void getTileDataHi() {
    if (clock == 0) {
      clock++;
      return;
    }
    tileDataHi = vram.read((short) (computeTileAddress() + 1));

    // if (fifo.size() > 8) {
    // return;
    // }
    // for (int bit = 7; bit >= 0; bit--) {
    // int loBit = (tileDataLo >> bit) & 0x01;
    // int hiBit = (tileDataHi >> bit) & 0x01;
    // fifo.add((hiBit << 1) | loBit);
    // }
    // if (inWindow) {
    // windowFetcherX++;
    // } else {
    // fetcherX++;
    // }

    state = State.SLEEP;
    clock = 0;
  }

  private void sleep() {
    if (clock == 0) {
      clock++;
      return;
    }
    state = State.PUSH_TO_FIFO;
    clock = 0;
  }

  private void pushToFifo() {
    if (clock == 0) {
      clock++;
      return;
    }
    if (fifo.size() > 8) {
      return;
    }
    for (int bit = 7; bit >= 0; bit--) {
      int loBit = (tileDataLo >> bit) & 0x01;
      int hiBit = (tileDataHi >> bit) & 0x01;
      fifo.add((hiBit << 1) | loBit);
    }
    fetcherX++;
    state = State.GET_TILE_ID;
    clock = 0;
  }

  private int computeTileAddress() {
    int base;
    if (ioregs.lcdc.bit(4)) {
      base = Byte.toUnsignedInt(tileId) <= 127 ? 0x8000 : 0x8800;
    } else {
      base = Byte.toUnsignedInt(tileId) <= 127 ? 0x9000 : 0x8800;
    }
    int tileOffset = Byte.toUnsignedInt(tileId) * 16;
    int tileLine = (ioregs.ly.get() + ioregs.scy.get()) % 8;

    return base + tileOffset + (tileLine * 2);
  }
}
