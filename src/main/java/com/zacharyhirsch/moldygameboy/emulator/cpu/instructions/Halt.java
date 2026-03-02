package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;


public final class Halt extends AbstractInstruction {

  private final Registers registers;

  public Halt(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0(byte data) {
    return Mem.read(registers.pc().get());
  }

  @Override
  protected Mem execute1(byte data) {
    return Mem.read(registers.pc().getAndIncrement());
  }

  @Override
  protected Mem execute2(byte data) {
    registers.ir().set(data);
    return null;
  }
}
