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
  private int windowFetcherX = 0;
  private boolean inWindow = false;
  private int windowLy = 0;
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

  boolean isInWindow() {
    return inWindow;
  }

  void startWindow() {
    inWindow = true;
    windowFetcherX = 0;
    state = State.GET_TILE_ID;
    clock = 0;
  }

  void nextLine(boolean windowWasDrawnThisLine) {
    fetcherX = 0;
    windowFetcherX = 0;
    if (windowWasDrawnThisLine) {
      windowLy++;
    }
    inWindow = false;
  }

  void nextFrame() {
    windowLy = 0;
    inWindow = false;
  }

  private void getTileId() {
    if (clock == 0) {
      clock++;
      return;
    }
    if (memory.registers().lcdc().isWindowEnabled() && inWindow) {
      int base = memory.registers().lcdc().getWindowTileIdBase() & 0xffff;
      int row = (windowLy / 8) & 0x1f;
      int tileX = windowFetcherX & 0x1f;
      int address = base + (row * 32) + tileX;
      tileId = memory.read((short) address);
      state = State.GET_TILE_DATA_LO;
    } else if (memory.registers().lcdc().isBackgroundEnabled()) {
      int base = memory.registers().lcdc().getBackgroundTileIdBase() & 0xffff;
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
    if (inWindow) {
      windowFetcherX++;
    } else {
      fetcherX++;
    }
    state = State.GET_TILE_ID;
    clock = 0;
  }

  private int computeTileAddress() {
    boolean isUnsigned = memory.registers().lcdc().isTileDataBase8000();

    int base;
    int tileOffset;
    if (isUnsigned) {
      base = 0x8000;
      tileOffset = Byte.toUnsignedInt(tileId) * 16;
    } else {
      base = 0x9000;
      tileOffset = tileId * 16;
    }

    int tileLine;
    if (inWindow) {
      tileLine = windowLy % 8;
    } else {
      byte ly = memory.registers().ly().get();
      byte scy = memory.registers().scy().get();
      tileLine = ((ly & 0xff) + (scy & 0xff)) % 8;
    }

    return base + tileOffset + (tileLine * 2);
  }
}
