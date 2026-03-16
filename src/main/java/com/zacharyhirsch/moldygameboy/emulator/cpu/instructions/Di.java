package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Di extends AbstractInstruction1 {

  private final Registers registers;

  public Di(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0() {
    registers.ime().set((byte) 0);
    return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
  }
}
