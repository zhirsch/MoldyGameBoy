package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.nio.ByteBuffer;

public final class BootRom implements MemoryRegion {

  private static final int BASE = 0x0000;
  private static final int SIZE = 0x0100;

  private final ByteBuffer boot;

  public BootRom(ByteBuffer boot) {
    this.boot = boot;
  }

  @Override
  public byte read(short address) {
    return boot.get(normalize(address));
  }

  @Override
  public void write(short address, byte value) {
    throw new UnsupportedOperationException();
  }

  private static int normalize(short address) {
    int addr = Short.toUnsignedInt(address);
    if (!(BASE <= addr && addr < BASE + SIZE)) {
      throw new IllegalArgumentException("%04x".formatted(addr));
    }
    return addr - BASE;
  }
}
