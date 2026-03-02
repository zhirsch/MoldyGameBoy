package com.zacharyhirsch.moldygameboy.emulator.arch;

public interface MemoryRange {

  byte read(short address);

  void write(short address, byte data);
}
