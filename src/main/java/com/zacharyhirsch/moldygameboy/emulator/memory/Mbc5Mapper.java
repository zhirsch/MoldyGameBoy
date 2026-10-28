package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.nio.ByteBuffer;

final class Mbc5Mapper implements Mapper {

  private final ByteBuffer rom;
  private final ByteBuffer ram;
  private final boolean rumblePresent;
  private final int romBankCount;
  private final int ramBankCount;

  private int ramEnable = 0;
  private int romBankNumber = 1;
  private int ramBankNumber = 0;

  Mbc5Mapper(ByteBuffer rom, ByteBuffer ram, boolean rumblePresent) {
    this.rom = rom;
    this.ram = ram;
    this.rumblePresent = rumblePresent;
    this.romBankCount = rom.limit() / 0x4000;
    this.ramBankCount = ram.limit() / 0x2000;
  }

  @Override
  public MemoryRegion mapRom() {
    return new MemoryRegion() {
      @Override
      public byte read(short address) {
        int addr = Short.toUnsignedInt(address);
        assert 0x0000 <= addr && addr <= 0x7fff;
        assert 0 <= romBankNumber && romBankNumber <= 511;
        if (0x0000 <= addr && addr <= 0x3fff) {
          int bank = 0 & (romBankCount - 1);
          return rom.get((bank << 14) | (addr - 0x0000));
        }
        if (0x4000 <= addr && addr <= 0x7fff) {
          int bank = romBankNumber & (romBankCount - 1);
          return rom.get((bank << 14) | (addr - 0x4000));
        }
        throw new IllegalArgumentException("%04x".formatted(address));
      }

      @Override
      public void write(short address, byte value) {
        int addr = Short.toUnsignedInt(address);
        assert 0x0000 <= addr && addr <= 0x7fff;
        if (0x0000 <= addr && addr <= 0x1fff) {
          ramEnable = (value & 0x0f) == 0x0a ? 0x01 : 0x00;
          return;
        }
        if (0x2000 <= addr && addr <= 0x2fff) {
          romBankNumber = (romBankNumber & 0x0100) | ((value << 0) & 0x00ff);
          return;
        }
        if (0x3000 <= addr && addr <= 0x3fff) {
          romBankNumber = (romBankNumber & 0x00ff) | ((value << 8) & 0x0100);
          return;
        }
        if (0x4000 <= addr && addr <= 0x5fff) {
          ramBankNumber = value & (rumblePresent ? 0x07 : 0x0f);
          return;
        }
        if (0x6000 <= addr && addr <= 0x7fff) {
          return;
        }
        throw new IllegalArgumentException("%04x".formatted(address));
      }
    };
  }

  @Override
  public MemoryRegion mapRam() {
    if (ram.limit() == 0 || ramEnable == 0) {
      return new OpenBus();
    }
    return new MemoryRegion() {
      @Override
      public byte read(short address) {
        int addr = Short.toUnsignedInt(address);
        assert 0xa000 <= addr && addr <= 0xbfff;
        assert 0 <= ramBankNumber && ramBankNumber <= 0x0f;
        int bank = ramBankNumber & (ramBankCount - 1);
        return ram.get((bank << 13) | (addr - 0xa000));
      }

      @Override
      public void write(short address, byte value) {
        int addr = Short.toUnsignedInt(address);
        assert 0xa000 <= addr && addr <= 0xbfff;
        assert 0 <= ramBankNumber && ramBankNumber <= 0x0f;
        int bank = ramBankNumber & (ramBankCount - 1);
        ram.put((bank << 13) | (addr - 0xa000), value);
      }
    };
  }
}
