package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Ccf implements Instruction {

  @Override
  public MCycle start(Registers registers, Bus bus) {
    Alu.Result<Byte> result = Alu.ccf(registers.f().c().get());
    registers.f().n().set(result.n());
    registers.f().h().set(result.h());
    registers.f().c().set(result.c());
    return new FetchOpcode();
  }
}
