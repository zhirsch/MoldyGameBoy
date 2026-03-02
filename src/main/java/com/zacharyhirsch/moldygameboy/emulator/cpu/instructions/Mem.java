package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.MemoryRange;

public interface Mem {

  byte execute(MemoryRange memory);

  static Mem read(short address) {
    return memory -> memory.read(address);
  }

  static Mem write(short address, byte data) {
    return memory -> {
      memory.write(address, data);
      return data;
    };
  }
}
