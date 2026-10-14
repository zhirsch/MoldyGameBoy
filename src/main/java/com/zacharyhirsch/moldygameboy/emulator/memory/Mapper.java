package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.nio.ByteBuffer;

public interface Mapper {

  static Mapper create(ByteBuffer rom) {
    byte type = rom.get(0x0147);
    return switch (type) {
      case 0x00 -> new NullMapper(rom);
      case 0x01 -> new Mbc1Mapper(rom);
      default -> throw new IllegalArgumentException("Unknown mapper %02x".formatted(type));
    };
  }

  MemoryRegion mapRom();

  MemoryRegion mapRam();
}
