package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Stop extends AbstractInstruction2 {

  private final Registers registers;

  public Stop(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0() {
    return Mem.none(registers.pc().read());
  }

  @Override
  protected Mem execute1() {
    return Mem.none(registers.pc().getAndIncrement());
  }
}
