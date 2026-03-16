package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.FlagsRegister;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import java.util.function.Predicate;

public final class Retcc implements Instruction {

  private final Registers registers;
  private final Predicate<FlagsRegister> predicate;

  private int i = 0;
  private byte w;
  private byte z;

  public Retcc(Registers registers, Predicate<FlagsRegister> predicate) {
    this.registers = registers;
    this.predicate = predicate;
  }

  @Override
  public Mem tick() {
    return switch (i++) {
      case 0 -> execute0();
      case 1 -> execute1();
      case 2 -> execute2();
      case 3 -> execute3();
      case 4 -> execute4();
      case 5 -> null;
      default -> throw new IllegalStateException();
    };
  }

  private Mem execute0() {
    return Mem.none((short) 0x0000);
  }

  private Mem execute1() {
    if (!predicate.test(registers.f())) {
      i = 5;
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
    return Mem.read(registers.sp().getAndIncrement(), data -> z = data);
  }

  private Mem execute2() {
    return Mem.read(registers.sp().getAndIncrement(), data -> w = data);
  }

  private Mem execute3() {
    registers.pc().set(w, z);
    return Mem.none((short) 0x0000);
  }

  private Mem execute4() {
    registers.pc().set(w, z);
    return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
  }
}
