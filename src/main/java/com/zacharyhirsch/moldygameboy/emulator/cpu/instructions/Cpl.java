package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Cpl extends AbstractInstruction1 {

  private final Registers registers;

  public Cpl(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0() {
    Alu.Result result = Alu.cpl(registers.a().get());
    registers.a().set(result.result());
    registers.f().n().set(result.n());
    registers.f().h().set(result.h());
    return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
  }
}
