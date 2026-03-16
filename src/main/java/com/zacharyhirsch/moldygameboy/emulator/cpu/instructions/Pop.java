package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register16;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Pop extends AbstractInstruction3 {

  private final Registers registers;
  private final Register16 register;

  private byte z;
  private byte w;

  public Pop(Registers registers, Register16 register) {
    this.registers = registers;
    this.register = register;
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
    register.set(w, z);
    return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
  }
}
