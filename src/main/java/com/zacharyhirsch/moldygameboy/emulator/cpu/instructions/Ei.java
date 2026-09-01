package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Ei extends AbstractInstruction1 {

  private final Registers registers;

  public Ei(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0() {
    registers.ime().write((byte) 1);
    return Mem.read(registers.pc().getAndIncrement(), registers.ir()::write);
  }
}
