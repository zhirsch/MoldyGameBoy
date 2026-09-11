package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.nio.ByteBuffer;

public final class Wram implements MemoryRegion {

  private static final int BASE = 0xc000;
  private static final int SIZE = 0x2000;
  private static final int BANK_SIZE = 0x1000;

  private final ByteBuffer[] banks;

  public Wram() {
    this.banks = new ByteBuffer[] {ByteBuffer.allocate(BANK_SIZE), ByteBuffer.allocate(BANK_SIZE)};
  }

  @Override
  public byte read(short address) {
    return at(address).get(0);
  }

  @Override
  public void write(short address, byte value) {
    at(address).put(0, value);
  }

  private ByteBuffer at(short address) {
    int addr = Short.toUnsignedInt(address);
    if (BASE <= addr && addr < BASE + SIZE) {
      int bank = getBank(addr);
      return banks[bank].slice(addr - (BASE + BANK_SIZE * bank), 1);
    }
    throw new IllegalArgumentException("%04x".formatted(addr));
  }

  private static int getBank(int addr) {
    if (0xc000 <= addr && addr <= 0xcfff) {
      return 0;
    }
    if (0xd000 <= addr && addr <= 0xdfff) {
      return 1;
    }
    throw new IllegalArgumentException("%04x".formatted(addr));
  }
}
