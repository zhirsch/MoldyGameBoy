package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Scf implements Instruction {

  @Override
  public MCycle start(Registers registers, Bus bus) {
    registers.f().n().set(false);
    registers.f().h().set(false);
    registers.f().c().set(true);
    return new FetchOpcode();
  }
}
