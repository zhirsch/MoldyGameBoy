package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.nio.ByteBuffer;

final class Mbc1Mapper implements Mapper {

  private final ByteBuffer rom;
  private final ByteBuffer ram;
  private final int romBankCount;
  private final int ramBankCount;
  private final boolean multicart;

  private byte ramEnable = 0;
  private byte bank1 = 0;
  private byte bank2 = 0;
  private byte mode = 0;

  Mbc1Mapper(ByteBuffer rom, ByteBuffer ram, boolean multicart) {
    this.rom = rom;
    this.ram = ram;
    this.romBankCount = rom.limit() / 0x4000;
    this.ramBankCount = ram.limit() / 0x2000;
    this.multicart = multicart;
  }

  @Override
  public MemoryRegion mapRom() {
    return new MemoryRegion() {
      @Override
      public byte read(short address) {
        int addr = Short.toUnsignedInt(address);
        assert 0x0000 <= addr && addr <= 0x7fff;
        if (0x0000 <= addr && addr <= 0x3fff) {
          return rom.get((getRomBank0() << 14) | (addr - 0x0000));
        }
        if (0x4000 <= addr && addr <= 0x7fff) {
          int bank = bank1 == 0 ? 1 : bank1;
          return rom.get((getRomBank(bank) << 14) | (addr - 0x4000));
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
          bank1 = (byte) (value & 0b0001_1111);
          return;
        }
        if (0x4000 <= addr && addr <= 0x5fff) {
          bank2 = (byte) (value & 0b0000_0011);
          return;
        }
        if (0x6000 <= addr && addr <= 0x7fff) {
          mode = (byte) (value & 0b0000_0001);
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
        return ram.get((getRamBank() << 13) | (addr - 0xa000));
      }

      @Override
      public void write(short address, byte value) {
        int addr = Short.toUnsignedInt(address);
        assert 0xa000 <= addr && addr <= 0xbfff;
        ram.put((getRamBank() << 13) | (addr - 0xa000), value);
      }
    };
  }

  private int getRomBank0() {
    return switch (mode) {
      case 0 -> 0;
      case 1 -> getRomBank(0);
      default -> throw new IllegalStateException();
    };
  }

  private int getRomBank(int bank) {
    if (multicart) {
      return ((bank2 << 4) | (bank & 0b0000_1111)) & (romBankCount - 1);
    } else {
      return ((bank2 << 5) | (bank & 0b0001_1111)) & (romBankCount - 1);
    }
  }

  private int getRamBank() {
    return switch (mode) {
      case 0 -> 0;
      case 1 -> bank2 & (ramBankCount - 1);
      default -> throw new IllegalStateException();
    };
  }
}
