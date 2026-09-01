package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.FlagsRegister;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import java.util.function.Predicate;

public final class Callcc implements Instruction {

  private final Registers registers;
  private final Predicate<FlagsRegister> predicate;

  private int i = 0;
  private byte w;
  private byte z;

  public Callcc(Registers registers, Predicate<FlagsRegister> predicate) {
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
      case 5 -> execute5();
      case 6 -> null;
      default -> throw new IllegalStateException();
    };
  }

  private Mem execute0() {
    return Mem.read(registers.pc().getAndIncrement(), data -> z = data);
  }

  private Mem execute1() {
    return Mem.read(registers.pc().getAndIncrement(), data -> w = data);
  }

  private Mem execute2() {
    if (!predicate.test(registers.f())) {
      i = 6;
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::write);
    }
    return Mem.none(registers.sp().getAndDecrement());
  }

  private Mem execute3() {
    return Mem.write(registers.sp().getAndDecrement(), registers.pc().hi()::read);
  }

  private Mem execute4() {
    return Mem.write(registers.sp().read(), registers.pc().lo()::read);
  }

  private Mem execute5() {
    registers.pc().write(w, z);
    return Mem.read(registers.pc().getAndIncrement(), registers.ir()::write);
  }
}
