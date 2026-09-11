package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.nio.ByteBuffer;

public final class Hram implements MemoryRegion {

  private static final int BASE = 0xff80;
  private static final int SIZE = 0x007f;

  private final ByteBuffer hram;

  public Hram() {
    this.hram = ByteBuffer.allocate(SIZE);
  }

  @Override
  public byte read(short address) {
    return at(address).get();
  }

  @Override
  public void write(short address, byte value) {
    at(address).put(value);
  }

  private ByteBuffer at(short address) {
    int addr = Short.toUnsignedInt(address);
    if (BASE <= addr && addr < BASE + SIZE) {
      return hram.slice(addr - BASE, 1);
    }
    throw new IllegalArgumentException("%04x".formatted(addr));
  }
}
