package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Rrca implements Instruction {

  @Override
  public MCycle start(Registers registers, Bus bus) {
    Alu.Result<Byte> result = Alu.rr(registers.a().get(), (registers.a().get() & 0x01) != 0);
    registers.a().set(result.result());
    registers.f().z().set(false);
    registers.f().n().set(false);
    registers.f().h().set(false);
    registers.f().c().set(result.c());
    return new FetchOpcode();
  }
}
