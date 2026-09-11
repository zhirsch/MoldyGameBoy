package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.nio.ByteBuffer;

public final class Ram implements MemoryRegion {

  private static final int BASE = 0xa000;
  private static final int SIZE = 0x2000;

  private final ByteBuffer sram;

  public Ram() {
    this.sram = ByteBuffer.allocate(SIZE);
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
      return sram.slice(addr - BASE, 1);
    }
    throw new IllegalArgumentException("%04x".formatted(addr));
  }
}
