package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.nio.ByteBuffer;

/**
 * MBC3: up to 128 ROM banks, 4 RAM banks and an optional real-time clock. $4000-$5FFF selects a RAM
 * bank (0-3) or a clock register ($08-$0C) to appear at $A000-$BFFF, and writing $00 then $01 to
 * $6000-$7FFF latches the clock.
 */
final class Mbc3Mapper implements Mapper {

  private final ByteBuffer rom;
  private final Ram ram;
  private final RealTimeClock clock;
  private final int romBankCount;
  private final int ramBankCount;

  private boolean ramEnabled = false;
  private int romBankNumber = 1;
  private int ramBankOrClockRegister = 0;
  private int lastLatchWrite = 0xff;

  /** {@code clock} is null for cartridges without one. */
  Mbc3Mapper(ByteBuffer rom, Ram ram, RealTimeClock clock) {
    this.ram = ram;
    this.rom = rom;
    this.clock = clock;
    this.romBankCount = rom.limit() / 0x4000;
    this.ramBankCount = Math.max(1, ram.size() / 0x2000);
  }

  @Override
  public void tick() {
    if (clock != null) {
      clock.tick();
    }
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
        int v = Byte.toUnsignedInt(value);
        assert 0x0000 <= addr && addr <= 0x7fff;
        if (addr <= 0x1fff) {
          ramEnabled = (v & 0x0f) == 0x0a;
        } else if (addr <= 0x3fff) {
          romBankNumber = Math.max(1, v & 0x7f);
        } else if (addr <= 0x5fff) {
          ramBankOrClockRegister = v & 0x0f;
        } else {
          if (lastLatchWrite == 0x00 && v == 0x01 && clock != null) {
            clock.latch();
          }
          lastLatchWrite = v;
        }
      }
    };
  }

  @Override
  public MemoryRegion mapRam() {
    if (!ramEnabled) {
      return new OpenBus();
    }
    if (ramBankOrClockRegister >= RealTimeClock.SECONDS) {
      if (clock == null || ramBankOrClockRegister > RealTimeClock.DAYS_HIGH) {
        return new OpenBus();
      }
      return new MemoryRegion() {
        @Override
        public byte read(short address) {
          return (byte) clock.read(ramBankOrClockRegister);
        }

        @Override
        public void write(short address, byte value) {
          clock.write(ramBankOrClockRegister, Byte.toUnsignedInt(value));
        }
      };
    }
    if (ram.size() == 0) {
      return new OpenBus();
    }
    return new MemoryRegion() {
      @Override
      public byte read(short address) {
        return ram.read(offset(address));
      }

      @Override
      public void write(short address, byte value) {
        ram.write(offset(address), value);
      }
    };
  }

  private int offset(short address) {
    int bank = ramBankOrClockRegister & (ramBankCount - 1);
    return (bank << 13) | (Short.toUnsignedInt(address) - 0xa000);
  }
}
