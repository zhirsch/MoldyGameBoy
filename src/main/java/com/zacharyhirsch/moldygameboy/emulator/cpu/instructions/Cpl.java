package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Cpl implements Instruction {

  @Override
  public MCycle start(Registers registers, Bus bus) {
    Alu.Result<Byte> result = Alu.cpl(registers.a().get());
    registers.a().set(result.result());
    registers.f().n().set(result.n());
    registers.f().h().set(result.h());
    return new FetchOpcode();
  }
}
