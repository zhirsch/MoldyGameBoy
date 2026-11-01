package com.zacharyhirsch.moldygameboy.emulator.ppu;

import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import java.nio.ByteBuffer;

final class ObjPixelFetcher {

  private enum State {
    GET_TILE_ID,
    GET_TILE_DATA_LO,
    GET_TILE_DATA_HI,
    DONE,
  }

  private final IoRegisters ioregs;
  private final ByteBuffer vram;
  private final ByteBuffer oam;

  private int slot = 0;
  private Sprite sprite = null;
  private State state = null;
  private boolean secondDot = false;
  private int tile;
  private int attr;
  private int lo;
  private int hi;

  ObjPixelFetcher(IoRegisters ioregs, ByteBuffer vram, ByteBuffer oam) {
    this.ioregs = ioregs;
    this.vram = vram;
    this.oam = oam;
  }

  boolean isActive() {
    return state != null;
  }

  boolean isDone() {
    return state == State.DONE;
  }

  Sprite sprite() {
    return sprite;
  }

  int attr() {
    return attr;
  }

  int lo() {
    return lo;
  }

  int hi() {
    return hi;
  }

  int slot() {
    return slot;
  }

  void start(int slot, Sprite sprite) {
    this.slot = slot;
    this.sprite = sprite;
    this.state = State.GET_TILE_ID;
    this.secondDot = false;
  }

  void reset() {
    this.slot = 0;
    this.sprite = null;
    this.state = null;
    this.secondDot = false;
  }

  void tick() {
    if (!secondDot) {
      secondDot = true;
      return;
    }
    secondDot = false;
    switch (state) {
      case GET_TILE_ID -> {
        tile = Byte.toUnsignedInt(oam.get(sprite.oamIndex() * 4 + 2));
        attr = Byte.toUnsignedInt(oam.get(sprite.oamIndex() * 4 + 3));
        state = State.GET_TILE_DATA_LO;
      }
      case GET_TILE_DATA_LO -> {
        lo = Byte.toUnsignedInt(vram.get(rowAddress()));
        state = State.GET_TILE_DATA_HI;
      }
      case GET_TILE_DATA_HI -> {
        hi = Byte.toUnsignedInt(vram.get(rowAddress() + 1));
        state = State.DONE;
      }
      case DONE -> {}
    }
  }

  private int rowAddress() {
    int height = ioregs.lcdc.bit(2) ? 16 : 8;
    int row = ioregs.ly.get() + 16 - sprite.y();
    if (bit(attr, 6) != 0) {
      row = height - 1 - row;
    }
    int t = height == 16 ? (tile & 0xfe) : tile;
    return t * 16 + row * 2;
  }

  private static int bit(int value, int index) {
    return (value & (1 << index)) != 0 ? 1 : 0;
  }
}
