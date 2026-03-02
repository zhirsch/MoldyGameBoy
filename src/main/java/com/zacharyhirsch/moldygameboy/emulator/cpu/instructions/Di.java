package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;


public final class Di extends AbstractInstruction {

  private final Registers registers;

  public Di(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0(byte data) {
    registers.ime().reset();
    return Mem.read(registers.pc().getAndIncrement());
  }

  @Override
  protected Mem execute1(byte data) {
    registers.ir().set(data);
    return null;
  }
}
