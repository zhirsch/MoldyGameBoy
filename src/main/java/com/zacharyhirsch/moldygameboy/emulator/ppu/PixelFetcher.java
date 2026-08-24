package com.zacharyhirsch.moldygameboy.emulator.ppu;

import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import java.util.Queue;

final class PixelFetcher {

  private enum State {
    GET_TILE_ID,
    GET_TILE_DATA_LO,
    GET_TILE_DATA_HI,
    PUSH_TO_FIFO,
  }

  private final Memory memory;

  private State state = State.GET_TILE_ID;
  private int clock = 0;
  private int fetcherX = 0;
  private byte tileId;
  private byte tileDataLo;
  private byte tileDataHi;

  PixelFetcher(Memory memory) {
    this.memory = memory;
  }

  void tick(Queue<Integer> fifo) {
    switch (state) {
      case GET_TILE_ID -> getTileId();
      case GET_TILE_DATA_LO -> getTileDataLo();
      case GET_TILE_DATA_HI -> getTileDataHi();
      case PUSH_TO_FIFO -> pushToFifo(fifo);
    }
  }

  public void nextLine() {
    fetcherX = 0;
  }

  private void getTileId() {
    if (clock == 0) {
      clock++;
      return;
    }
    if (memory.registers().lcdc().isWindowEnabled() && isInWindow()) {
    } else if (memory.registers().lcdc().isBackgroundEnabled()) {
      int base = memory.registers().lcdc().getTileIdBase() & 0xffff;
      int ly = memory.registers().ly().get() & 0xff;
      int scx = memory.registers().scx().get() & 0xff;
      int scy = memory.registers().scy().get() & 0xff;
      int tileY = (ly + scy) & 0xff;
      int tileX = (fetcherX + (scx / 8)) & 0x1f;
      int row = (tileY / 8) & 0x1f;
      int address = base + (row * 32) + tileX;
      tileId = memory.read((short) address);
      state = State.GET_TILE_DATA_LO;
    }
    clock = 0;
  }

  private boolean isInWindow() {
    return false;
  }

  private void getTileDataLo() {
    if (clock == 0) {
      clock++;
      return;
    }

    tileDataLo = memory.read((short) computeTileAddress());
    state = State.GET_TILE_DATA_HI;
    clock = 0;
  }

  private void getTileDataHi() {
    if (clock == 0) {
      clock++;
      return;
    }

    tileDataHi = memory.read((short) (computeTileAddress() + 1));
    state = State.PUSH_TO_FIFO;
    clock = 0;
  }

  private void pushToFifo(Queue<Integer> fifo) {
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
    int base = memory.registers().lcdc().getTileDataBase() & 0xffff;

    int ly = memory.registers().ly().get() & 0xff;
    int scy = memory.registers().scy().get() & 0xff;
    int tileLine = (ly + scy) % 8;
    int offset = (tileId & 0xff) * 16 + (tileLine * 2);

    return base + offset;
  }
}
