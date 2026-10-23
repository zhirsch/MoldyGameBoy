package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.nio.ByteBuffer;

final class Mbc1Mapper implements Mapper {

  private final ByteBuffer rom;
  private final ByteBuffer ram;

  private byte ramEnable = 0;
  private byte romBankNumber = 0;
  private byte ramBankNumber = 0;
  private byte bankingModeSelect = 0;

  Mbc1Mapper(ByteBuffer rom, ByteBuffer ram) {
    this.rom = rom;
    this.ram = ram;
  }

  @Override
  public MemoryRegion mapRom() {
    return new MemoryRegion() {
      @Override
      public byte read(short address) {
        int addr = Short.toUnsignedInt(address);
        assert 0x0000 <= addr && addr <= 0x7fff;
        assert 0 <= romBankNumber && romBankNumber <= 31;
        if (0x0000 <= addr && addr <= 0x3fff) {
          int bank = 0;
          return rom.get((bank << 14) | (addr - 0x0000));
        }
        if (0x4000 <= addr && addr <= 0x7fff) {
          int bank = romBankNumber == 0 ? 1 : romBankNumber;
          return rom.get((bank << 14) | (addr - 0x4000));
        }
        throw new IllegalArgumentException("%04x".formatted(address));
      }

      @Override
      public void write(short address, byte value) {
        int addr = Short.toUnsignedInt(address);
        assert 0x0000 <= addr && addr <= 0x7fff;
        if (0x0000 <= addr && addr <= 0x1fff) {
          ramEnable = (byte) ((value & 0x0f) == 0x0a ? 0x01 : 0x00);
          return;
        }
        if (0x2000 <= addr && addr <= 0x3fff) {
          romBankNumber = (byte) (value & 0b0001_1111);
          return;
        }
        if (0x4000 <= addr && addr <= 0x5fff) {
          ramBankNumber = (byte) (value & 0b0000_0011);
          return;
        }
        if (0x6000 <= addr && addr <= 0x7fff) {
          bankingModeSelect = (byte) (value & 0b0000_0001);
          if (bankingModeSelect != 0) {
            throw new UnsupportedOperationException();
          }
          return;
        }
        throw new IllegalArgumentException("%04x".formatted(address));
      }
    };
  }

  @Override
  public MemoryRegion mapRam() {
    if (ram == null || ram.limit() == 0 || ramEnable == 0) {
      return new OpenBus();
    }
    return new MemoryRegion() {
      @Override
      public byte read(short address) {
        int addr = Short.toUnsignedInt(address);
        assert 0xa000 <= addr && addr <= 0xbfff;
        assert 0 <= ramBankNumber && ramBankNumber <= 3;
        return ram.get((ramBankNumber << 13) | (addr - 0xa000));
      }

      @Override
      public void write(short address, byte value) {
        int addr = Short.toUnsignedInt(address);
        assert 0xa000 <= addr && addr <= 0xbfff;
        assert 0 <= ramBankNumber && ramBankNumber <= 3;
        ram.put((ramBankNumber << 13) | (addr - 0xa000), value);
      }
    };
  }
}
