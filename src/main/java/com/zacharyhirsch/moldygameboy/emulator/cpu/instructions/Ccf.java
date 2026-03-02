package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;


public final class Ccf extends AbstractInstruction {

  private final Registers registers;

  public Ccf(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0(byte data) {
    registers.f().setN(false);
    registers.f().setH(false);
    registers.f().setC(!registers.f().getC());
    return Mem.read(registers.pc().getAndIncrement());
  }

  @Override
  protected Mem execute1(byte data) {
    registers.ir().set(data);
    return null;
  }
}
