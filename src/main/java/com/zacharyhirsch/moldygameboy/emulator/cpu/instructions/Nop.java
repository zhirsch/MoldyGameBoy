package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Nop implements Instruction {

  @Override
  public MCycle start(Registers registers, Bus bus) {
    return null;
  }
}
