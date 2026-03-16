package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Ret extends AbstractInstruction4 {

  private final Registers registers;

  private byte w;
  private byte z;

  public Ret(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0() {
    return Mem.read(registers.sp().getAndIncrement(), data -> z = data);
  }

  @Override
  protected Mem execute1() {
    return Mem.read(registers.sp().getAndIncrement(), data -> w = data);
  }

  @Override
  protected Mem execute2() {
    return Mem.none((short) 0);
  }

  @Override
  protected Mem execute3() {
    registers.pc().set(w, z);
    return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
  }
}
