package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Ccf extends AbstractInstruction1 {

  private final Registers registers;

  public Ccf(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0() {
    Alu.Result result = Alu.ccf(registers.f().c().get());
    registers.f().n().set(result.n());
    registers.f().h().set(result.h());
    registers.f().c().set(result.c());
    return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
  }
}
