package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Call extends AbstractInstruction6 {

  private final Registers registers;

  private byte w;
  private byte z;

  public Call(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0() {
    return Mem.read(registers.pc().getAndIncrement(), data -> z = data);
  }

  @Override
  protected Mem execute1() {
    return Mem.read(registers.pc().getAndIncrement(), data -> w = data);
  }

  @Override
  protected Mem execute2() {
    return Mem.none(registers.sp().getAndDecrement());
  }

  @Override
  protected Mem execute3() {
    return Mem.write(registers.sp().getAndDecrement(), registers.pc().hi()::get);
  }

  @Override
  protected Mem execute4() {
    return Mem.write(registers.sp().get(), registers.pc().lo()::get);
  }

  @Override
  protected Mem execute5() {
    registers.pc().set(w, z);
    return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
  }
}
