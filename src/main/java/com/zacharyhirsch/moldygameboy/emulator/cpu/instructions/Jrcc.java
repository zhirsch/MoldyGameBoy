package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.FlagsRegister;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import java.util.function.Predicate;

public final class Jrcc implements Instruction {

  private final Registers registers;
  private final Predicate<FlagsRegister> predicate;

  private int i = 0;
  private byte z;

  public Jrcc(Registers registers, Predicate<FlagsRegister> predicate) {
    this.registers = registers;
    this.predicate = predicate;
  }

  @Override
  public Mem tick() {
    return switch (i++) {
      case 0 -> execute0();
      case 1 -> execute1();
      case 2 -> execute2();
      case 3 -> null;
      default -> throw new IllegalStateException();
    };
  }

  private Mem execute0() {
    return Mem.read(registers.pc().getAndIncrement(), data -> z = data);
  }

  private Mem execute1() {
    if (!predicate.test(registers.f())) {
      i = 3;
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
    registers.pc().set((short) (registers.pc().get() + z));
    return Mem.none(registers.pc().get());
  }

  private Mem execute2() {
    return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
  }
}
