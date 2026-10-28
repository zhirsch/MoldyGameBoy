package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Mapper {

  static Mapper create(ByteBuffer rom) {
    byte type = rom.get(0x0147);
    return switch (type) {
      case 0x00 -> new NullMapper(rom);
      case 0x01 -> new Mbc1Mapper(rom, ByteBuffer.allocate(0), isMbc1M(rom));
      case 0x02 -> new Mbc1Mapper(rom, newRamBuffer(rom), isMbc1M(rom));
      case 0x03 -> new Mbc1Mapper(rom, loadRamBuffer(rom), isMbc1M(rom));
      case 0x19 -> new Mbc5Mapper(rom, ByteBuffer.allocate(0), false);
      case 0x1a -> new Mbc5Mapper(rom, newRamBuffer(rom), false);
      case 0x1b -> new Mbc5Mapper(rom, loadRamBuffer(rom), false);
      case 0x1c -> new Mbc5Mapper(rom, ByteBuffer.allocate(0), true);
      case 0x1d -> new Mbc5Mapper(rom, newRamBuffer(rom), true);
      case 0x1e -> new Mbc5Mapper(rom, loadRamBuffer(rom), true);
      default -> throw new IllegalArgumentException("Unknown mapper %02x".formatted(type));
    };
  }

  private static boolean isMbc1M(ByteBuffer rom) {
    if (rom.limit() != 0x10_0000) {
      return false;
    }
    // 1 MiB ROM with a second Nintendo header at bank 0x10.
    return rom.slice(0x0104, 0x30).equals(rom.slice(0x10 * 0x4000 + 0x0104, 0x30));
  }

  private static ByteBuffer newRamBuffer(ByteBuffer rom) {
    return ByteBuffer.allocate(getRamBufferSize(rom));
  }

  private static ByteBuffer loadRamBuffer(ByteBuffer rom) {
    Path path = Path.of("save.bin");
    if (!Files.exists(path)) {
      return newRamBuffer(rom);
    }
    try {
      byte[] save = Files.readAllBytes(path);
      int size = getRamBufferSize(rom);
      if (size != save.length) {
        throw new IllegalArgumentException(
            "bad save file size (%04x != %04x".formatted(size, save.length));
      }
      return ByteBuffer.wrap(save);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static int getRamBufferSize(ByteBuffer rom) {
    return switch (rom.get(0x0149)) {
      case 0 -> 0;
      case 1 -> throw new UnsupportedOperationException();
      case 2 -> 0x2000;
      case 3 -> 4 * 0x2000;
      case 4 -> 16 * 0x2000;
      case 5 -> 8 * 0x2000;
      default -> throw new IllegalArgumentException();
    };
  }

  MemoryRegion mapRom();

  MemoryRegion mapRam();
}
