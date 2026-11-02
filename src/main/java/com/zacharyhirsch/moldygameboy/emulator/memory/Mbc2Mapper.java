package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.nio.ByteBuffer;

/**
 * MBC2: up to 16 ROM banks, and 512 4-bit RAM cells built into the chip, which repeat throughout
 * $A000-$BFFF and read with the upper 4 bits set. Writes to $0000-$3FFF set the RAM enable when
 * address bit 8 is clear, and the ROM bank (where 0 means 1) when it's set.
 */
final class Mbc2Mapper implements Mapper {

  private final ByteBuffer rom;
  private final ByteBuffer ram = ByteBuffer.allocate(0x200);
  private final int romBankCount;

  private boolean ramEnabled = false;
  private int romBankNumber = 1;

  Mbc2Mapper(ByteBuffer rom) {
    this.rom = rom;
    this.romBankCount = rom.limit() / 0x4000;
  }

  @Override
  public MemoryRegion mapRom() {
    return new MemoryRegion() {
      @Override
      public byte read(short address) {
        int addr = Short.toUnsignedInt(address);
        assert 0x0000 <= addr && addr <= 0x7fff;
        if (0x0000 <= addr && addr <= 0x3fff) {
          return rom.get(addr);
        }
        int bank = romBankNumber & (romBankCount - 1);
        return rom.get((bank << 14) | (addr - 0x4000));
      }

      @Override
      public void write(short address, byte value) {
        int addr = Short.toUnsignedInt(address);
        assert 0x0000 <= addr && addr <= 0x7fff;
        if (addr > 0x3fff) {
          return;
        }
        if ((addr & 0x0100) == 0) {
          ramEnabled = (value & 0x0f) == 0x0a;
        } else {
          romBankNumber = Math.max(1, value & 0x0f);
        }
      }
    };
  }

  @Override
  public MemoryRegion mapRam() {
    if (!ramEnabled) {
      return new OpenBus();
    }
    return new MemoryRegion() {
      @Override
      public byte read(short address) {
        return (byte) (0xf0 | ram.get(Short.toUnsignedInt(address) & 0x1ff));
      }

      @Override
      public void write(short address, byte value) {
        ram.put(Short.toUnsignedInt(address) & 0x1ff, (byte) (value & 0x0f));
      }
    };
  }
}
