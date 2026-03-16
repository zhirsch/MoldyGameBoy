package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register16;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Push extends AbstractInstruction4 {

  private final Registers registers;
  private final Register16 register;

  public Push(Registers registers, Register16 register) {
    this.registers = registers;
    this.register = register;
  }

  @Override
  protected Mem execute0() {
    return Mem.none(registers.sp().getAndDecrement());
  }

  @Override
  protected Mem execute1() {
    return Mem.write(registers.sp().getAndDecrement(), register.hi()::get);
  }

  @Override
  protected Mem execute2() {
    return Mem.write(registers.sp().get(), register.lo()::get);
  }

  @Override
  protected Mem execute3() {
    return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
  }
}
