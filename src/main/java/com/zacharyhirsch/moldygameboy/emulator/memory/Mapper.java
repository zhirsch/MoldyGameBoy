package com.zacharyhirsch.moldygameboy.emulator.memory;

/** A cartridge's memory bank controller, which maps its ROM and RAM banks into memory. */
interface Mapper {

  /** Advances by one dot, for cartridges with a clock. */
  default void tick() {}

  MemoryRegion mapRom();

  MemoryRegion mapRam();
}
