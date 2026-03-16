package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Rrca extends AbstractInstruction1 {

  private final Registers registers;

  public Rrca(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0() {
    Alu.Result result = Alu.rr(registers.a().get(), (registers.a().get() & 0x01) != 0);
    registers.a().set(result.result());
    registers.f().z().set(false);
    registers.f().n().set(false);
    registers.f().h().set(false);
    registers.f().c().set(result.c());
    return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
  }
}
