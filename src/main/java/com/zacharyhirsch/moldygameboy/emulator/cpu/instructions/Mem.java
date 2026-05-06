package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Mem {

  void execute(Memory memory);

  static Mem read(short address, Consumer<Byte> dst) {
    return memory -> dst.accept(memory.read(address));
  }

  static Mem write(short address, Supplier<Byte> src) {
    return memory -> memory.write(address, src.get());
  }

  static Mem none(short address) {
    return memory -> memory.none(address);
  }
}
