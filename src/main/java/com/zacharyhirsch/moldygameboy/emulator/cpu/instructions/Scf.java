package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Scf extends AbstractInstruction1 {

  private final Registers registers;

  public Scf(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0() {
    registers.f().n().set(false);
    registers.f().h().set(false);
    registers.f().c().set(true);
    return Mem.read(registers.pc().getAndIncrement(), registers.ir()::write);
  }
}
