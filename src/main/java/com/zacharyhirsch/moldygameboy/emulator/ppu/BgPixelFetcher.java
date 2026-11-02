package com.zacharyhirsch.moldygameboy.emulator.ppu;

import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import java.nio.ByteBuffer;
import java.util.Queue;

final class BgPixelFetcher {

  private enum State {
    GET_TILE_ID,
    GET_TILE_DATA_LO,
    GET_TILE_DATA_HI,
    PUSH_TO_FIFO,
  }

  private final IoRegisters ioregs;
  private final ByteBuffer vram;
  private final Queue<BgPixel> fifo;

  private State state = State.GET_TILE_ID;
  // Each fetcher step takes two dots; the step's work happens on the second dot.
  private boolean secondDot = false;

  private int fetcherX = 0;
  private int coarseScx = 0;
  private boolean window = false;
  private int windowLine = 0;
  private byte tileId;
  // CGB mode: the tile's attributes from VRAM bank 1 (palette, bank, flips, priority).
  private int attributes;
  private byte tileDataLo;
  private byte tileDataHi;

  BgPixelFetcher(IoRegisters ioregs, ByteBuffer vram, Queue<BgPixel> fifo) {
    this.ioregs = ioregs;
    this.vram = vram;
    this.fifo = fifo;
  }

  void reset() {
    state = State.GET_TILE_ID;
    secondDot = false;
    fetcherX = 0;
    window = false;
  }

  void startWindow(int windowLine) {
    reset();
    this.window = true;
    this.windowLine = windowLine;
  }

  /** Whether the fetcher has a whole tile row fetched and is waiting to push it. */
  boolean hasTileData() {
    return state == State.PUSH_TO_FIFO;
  }

  void latchScroll() {
    coarseScx = (ioregs.scx.get() & 0b1111_1000) >>> 3;
  }

  void tick() {
    // Pushing isn't a two-dot step: the fetcher retries every dot until the FIFO is empty.
    if (state == State.PUSH_TO_FIFO) {
      pushToFifo();
      return;
    }
    if (!secondDot) {
      secondDot = true;
      return;
    }
    secondDot = false;
    switch (state) {
      case GET_TILE_ID -> getTileId();
      case GET_TILE_DATA_LO -> getTileDataLo();
      case GET_TILE_DATA_HI -> getTileDataHi();
      case PUSH_TO_FIFO -> throw new IllegalStateException();
    }
  }

  private void getTileId() {
    int base = ioregs.lcdc.bit(window ? 6 : 3) ? 0x9c00 : 0x9800;
    int tileY = mapY() / 8;
    int tileX = (window ? fetcherX : fetcherX + coarseScx) & 0x1f;
    int address = base + (tileY * 32) + tileX;
    tileId = vram.get(address - 0x8000);
    attributes = ioregs.isCgbMode() ? Byte.toUnsignedInt(vram.get(0x2000 + address - 0x8000)) : 0;
    state = State.GET_TILE_DATA_LO;
  }

  private void getTileDataLo() {
    tileDataLo = vram.get(computeTileAddress());
    state = State.GET_TILE_DATA_HI;
  }

  private void getTileDataHi() {
    tileDataHi = vram.get(computeTileAddress() + 1);
    state = State.PUSH_TO_FIFO;
  }

  private void pushToFifo() {
    if (!fifo.isEmpty()) {
      return;
    }
    boolean xFlip = (attributes & 0x20) != 0;
    int palette = attributes & 0x07;
    boolean priority = (attributes & 0x80) != 0;
    for (int i = 0; i < 8; i++) {
      int bit = xFlip ? i : 7 - i;
      int loBit = (tileDataLo >> bit) & 0x01;
      int hiBit = (tileDataHi >> bit) & 0x01;
      fifo.add(new BgPixel((hiBit << 1) | loBit, palette, priority));
    }
    fetcherX++;
    state = State.GET_TILE_ID;
  }

  private int computeTileAddress() {
    // LCDC.4 set: tiles 0-255 at $8000-$8FFF. Clear: tiles 0-127 at $9000-$97FF and 128-255 at
    // $8800-$8FFF, i.e. only tiles 0-127 move.
    int base = !ioregs.lcdc.bit(4) && Byte.toUnsignedInt(tileId) <= 127 ? 0x1000 : 0x0000;
    int tileOffset = Byte.toUnsignedInt(tileId) * 16;
    int tileLine = (attributes & 0x40) != 0 ? 7 - mapY() % 8 : mapY() % 8;
    int bank = (attributes & 0x08) != 0 ? 0x2000 : 0x0000;
    return bank + base + tileOffset + (tileLine * 2);
  }

  private int mapY() {
    return window ? windowLine : (ioregs.ly.get() + ioregs.scy.get()) & 0xff;
  }
}
