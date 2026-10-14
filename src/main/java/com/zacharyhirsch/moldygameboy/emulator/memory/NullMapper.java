package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.nio.ByteBuffer;

final class NullMapper implements Mapper {

  private final ByteBuffer rom;
  private final ByteBuffer ram;

  NullMapper(ByteBuffer rom) {
    this.rom = rom;
    this.ram = ByteBuffer.allocate(0x2000);
  }

  @Override
  public MemoryRegion mapRom() {
    return new MemoryRegion() {
      @Override
      public byte read(short address) {
        int addr = Short.toUnsignedInt(address);
        assert 0x0000 <= addr && addr <= 0x7fff;
        if (0x0000 <= addr && addr <= 0x3fff) {
          return rom.get(0b0000_0000_0000_0000 | (addr - 0x0000));
        }
        if (0x4000 <= addr && addr <= 0x7fff) {
          return rom.get(0b0100_0000_0000_0000 | (addr - 0x4000));
        }
        throw new IllegalArgumentException("%04x".formatted(address));
      }

      @Override
      public void write(short address, byte value) {}
    };
  }

  @Override
  public MemoryRegion mapRam() {
    return new MemoryRegion() {
      @Override
      public byte read(short address) {
        int addr = Short.toUnsignedInt(address);
        assert 0xa000 <= addr && addr <= 0xbfff;
        return ram.get((short) (addr - 0xa000));
      }

      @Override
      public void write(short address, byte value) {
        int addr = Short.toUnsignedInt(address);
        assert 0xa000 <= addr && addr <= 0xbfff;
        ram.put((short) (addr - 0xa000), value);
      }
    };
  }
}
