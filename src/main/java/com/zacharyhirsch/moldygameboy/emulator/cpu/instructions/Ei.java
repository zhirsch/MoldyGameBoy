package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Ei implements Instruction {

  @Override
  public MCycle start(Registers registers, Bus bus) {
    registers.ime().set((byte) 1);
    return new FetchOpcode();
  }
}
