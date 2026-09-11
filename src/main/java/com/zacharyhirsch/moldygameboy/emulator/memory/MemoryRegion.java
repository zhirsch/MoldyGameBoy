package com.zacharyhirsch.moldygameboy.emulator.memory;

public interface MemoryRegion {

  byte read(short address);

  void write(short address, byte value);
}
