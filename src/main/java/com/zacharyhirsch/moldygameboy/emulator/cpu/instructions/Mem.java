package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.memory.CpuBus;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Mem {

  void execute(CpuBus bus);

  static Mem read(short address, Consumer<Byte> dst) {
    return bus -> dst.accept(bus.read(address));
  }

  static Mem write(short address, Supplier<Byte> src) {
    return bus -> bus.write(address, src.get());
  }

  static Mem none(short address) {
    return _ -> {};
  }
}
