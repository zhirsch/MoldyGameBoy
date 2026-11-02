package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * A cartridge: its RAM (which a battery may keep, and which is then saved), and the mapper that
 * banks its ROM and RAM into the address space.
 */
public final class Cartridge {

  private final Mapper mapper;
  private final Ram ram;

  Cartridge(Mapper mapper, Ram ram) {
    this.mapper = mapper;
    this.ram = ram;
  }

  /** Builds the cartridge described by the ROM's header (its mapper, RAM size and battery). */
  public static Cartridge create(ByteBuffer rom) {
    byte type = rom.get(0x0147);
    return switch (type) {
      case 0x00 -> create(new VolatileRam(0x2000), ram -> new NullMapper(rom, ram));
      case 0x01 -> create(new VolatileRam(0), ram -> new Mbc1Mapper(rom, ram, isMbc1M(rom)));
      case 0x02 ->
          create(new VolatileRam(ramSize(rom)), ram -> new Mbc1Mapper(rom, ram, isMbc1M(rom)));
      case 0x03 ->
          create(new BatteryBackedRam(ramSize(rom)), ram -> new Mbc1Mapper(rom, ram, isMbc1M(rom)));
      case 0x05 -> create(new VolatileRam(Mbc2Mapper.RAM_SIZE), ram -> new Mbc2Mapper(rom, ram));
      case 0x06 ->
          create(new BatteryBackedRam(Mbc2Mapper.RAM_SIZE), ram -> new Mbc2Mapper(rom, ram));
      case 0x0f -> mbc3WithClock(rom, 0);
      case 0x10 -> mbc3WithClock(rom, ramSize(rom));
      case 0x11 -> create(new VolatileRam(0), ram -> new Mbc3Mapper(rom, ram, null));
      case 0x12 -> create(new VolatileRam(ramSize(rom)), ram -> new Mbc3Mapper(rom, ram, null));
      case 0x13 ->
          create(new BatteryBackedRam(ramSize(rom)), ram -> new Mbc3Mapper(rom, ram, null));
      case 0x19 -> create(new VolatileRam(0), ram -> new Mbc5Mapper(rom, ram, false));
      case 0x1a -> create(new VolatileRam(ramSize(rom)), ram -> new Mbc5Mapper(rom, ram, false));
      case 0x1b ->
          create(new BatteryBackedRam(ramSize(rom)), ram -> new Mbc5Mapper(rom, ram, false));
      case 0x1c -> create(new VolatileRam(0), ram -> new Mbc5Mapper(rom, ram, true));
      case 0x1d -> create(new VolatileRam(ramSize(rom)), ram -> new Mbc5Mapper(rom, ram, true));
      case 0x1e ->
          create(new BatteryBackedRam(ramSize(rom)), ram -> new Mbc5Mapper(rom, ram, true));
      default -> throw new IllegalArgumentException("Unknown mapper %02x".formatted(type));
    };
  }

  Mapper mapper() {
    return mapper;
  }

  /** Advances by one dot, for cartridges with a clock. */
  void tick() {
    mapper.tick();
  }

  /**
   * Writes the cartridge's battery-backed state (RAM, and MBC3's clock) to {@code sink}, if it has
   * a battery and the state has changed since it was last saved; otherwise does nothing.
   */
  public void save(SaveSink sink) throws IOException {
    ram.save(sink);
  }

  /** Restores the battery-backed state from a save file; does nothing without a battery. */
  public void load(byte[] save) {
    ram.load(save);
  }

  private static Cartridge create(Ram ram, Function<Ram, Mapper> mapper) {
    return new Cartridge(mapper.apply(ram), ram);
  }

  /** MBC3 with a clock, which the battery keeps along with the RAM. */
  private static Cartridge mbc3WithClock(ByteBuffer rom, int ramSize) {
    RealTimeClock clock = new RealTimeClock();
    return create(new BatteryBackedRam(ramSize, clock), ram -> new Mbc3Mapper(rom, ram, clock));
  }

  private static boolean isMbc1M(ByteBuffer rom) {
    if (rom.limit() != 0x10_0000) {
      return false;
    }
    // 1 MiB ROM with a second Nintendo header at bank 0x10.
    return rom.slice(0x0104, 0x30).equals(rom.slice(0x10 * 0x4000 + 0x0104, 0x30));
  }

  private static int ramSize(ByteBuffer rom) {
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
}
