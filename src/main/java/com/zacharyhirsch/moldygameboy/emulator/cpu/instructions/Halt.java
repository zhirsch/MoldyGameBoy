package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Halt implements Instruction {

  @Override
  public MCycle start(Registers registers, Bus bus) {
    registers.halted().set(true);
    return new Halted();
  }

  private static final class Halted implements MCycle {

    @Override
    public void t1(Registers registers, Bus bus) {}

    @Override
    public void t2(Registers registers, Bus bus) {}

    @Override
    public void t3(Registers registers, Bus bus) {}

    @Override
    public MCycle t4(Registers registers, Bus bus) {
      return this;
    }
  }
}
