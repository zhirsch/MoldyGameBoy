package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.nio.ByteBuffer;

public final class Oam implements MemoryRegion {

  private static final int BASE = 0xfe00;
  private static final int SIZE = 0x00a0;

  private final ByteBuffer oam;

  public Oam() {
    this.oam = ByteBuffer.allocate(SIZE);
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
      return oam.slice(addr - BASE, 1);
    }
    throw new IllegalArgumentException("%04x".formatted(addr));
  }
}
