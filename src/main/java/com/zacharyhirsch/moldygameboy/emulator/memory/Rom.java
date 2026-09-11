package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.nio.ByteBuffer;

public class Rom implements MemoryRegion {

  private static final int BASE = 0x0000;
  private static final int SIZE = 0x8000;
  private static final int BANK_SIZE = 0x4000;

  private final ByteBuffer[] banks;

  public Rom(ByteBuffer... banks) {
    this.banks = banks;
  }

  @Override
  public byte read(short address) {
    return at(address).get();
  }

  @Override
  public void write(short address, byte value) {
//    throw new IllegalArgumentException("can't write to rom at %04x".formatted(address));
  }

  private ByteBuffer at(short address) {
    int addr = Short.toUnsignedInt(address);
    if (BASE <= addr && addr < BASE + SIZE) {
      int bank = getBank(addr);
      return banks[bank].slice(addr - (BASE + BANK_SIZE * bank), 1);
    }
    throw new IllegalArgumentException("%04x".formatted(addr));
  }

  private int getBank(int addr) {
    if (0x0000 <= addr && addr <= 0x3fff) {
      return 0;
    }
    if (0x4000 <= addr && addr <= 0x7fff) {
      return banks.length == 1 ? 0 : 1;
    }
    throw new IllegalArgumentException("%04x".formatted(addr));
  }
}
