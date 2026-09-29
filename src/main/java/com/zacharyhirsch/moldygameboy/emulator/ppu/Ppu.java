package com.zacharyhirsch.moldygameboy.emulator.ppu;

import com.zacharyhirsch.moldygameboy.emulator.arch.Irqs;
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

  enum State {
    GET_TILE_ID,
    GET_TILE_DATA_LO,
    GET_TILE_DATA_HI,
    PUSH_TO_FIFO,
  }

  private final IoRegisters ioregs;
  private final Vram vram;
  private final Video video;
  private final Irqs.Line vblank;
  private final Irqs.Line lcd;
  private final Queue<Integer> fifo;

  private int dot = 0;
  private State state = State.GET_TILE_ID;

  private int fetcherX = 0;
  private byte tileId;
  private byte tileDataLo;
  private byte tileDataHi;
  private int screenX = 0;
  private int fineScx = 0;
  private int coarseScx = 0;

  public Ppu(
      IoRegisters ioregs,
      Vram vram,
      Video video,
      Irqs.Line vblank,
      Irqs.Line lcd) {
    this.ioregs = ioregs;
    this.vram = vram;
    this.video = video;
    this.vblank = vblank;
    this.lcd = lcd;
    this.fifo = new ArrayDeque<>();
  }

  public void tick() {
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
      fetcherX = 0;
      state = State.GET_TILE_ID;
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
      coarseScx = (ioregs.scx.get() & 0b1111_1000) >>> 3;
    }
    if (80 <= dot && dot < 84) {
    } else {
      if (!fifo.isEmpty()) {
        render(fifo.remove());
      }
      switch (state) {
        case GET_TILE_ID -> getTileId();
        case GET_TILE_DATA_LO -> getTileDataLo();
        case GET_TILE_DATA_HI -> getTileDataHi();
        case PUSH_TO_FIFO -> pushToFifo();
      }
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

  private void getTileId() {
    if (dot % 2 == 0) {
      return;
    }
    int base = ioregs.lcdc.bit(3) ? 0x9c00 : 0x9800;
    int mapY = (ioregs.ly.get() + ioregs.scy.get()) & 0xff;
    int tileY = mapY / 8;
    int tileX = (fetcherX + coarseScx) & 0x1f;
    int address = base + (tileY * 32) + tileX;
    tileId = vram.read((short) address);
    state = State.GET_TILE_DATA_LO;
  }

  private void getTileDataLo() {
    if (dot % 2 == 0) {
      return;
    }
    tileDataLo = vram.read((short) computeTileAddress());
    state = State.GET_TILE_DATA_HI;
  }

  private void getTileDataHi() {
    if (dot % 2 == 0) {
      return;
    }
    tileDataHi = vram.read((short) (computeTileAddress() + 1));
    state = State.PUSH_TO_FIFO;
  }

  private void pushToFifo() {
    if (dot % 2 == 0) {
      return;
    }
    if (fifo.size() >= 8) {
      return;
    }
    for (int bit = 7; bit >= 0; bit--) {
      int loBit = (tileDataLo >> bit) & 0x01;
      int hiBit = (tileDataHi >> bit) & 0x01;
      fifo.add((hiBit << 1) | loBit);
    }
    fetcherX++;
    state = State.GET_TILE_ID;
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
