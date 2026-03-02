package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;


public final class Cpl extends AbstractInstruction {

  private final Registers registers;

  public Cpl(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0(byte data) {
    registers.a().set((byte) ~registers.a().get());
    registers.f().setN(true);
    registers.f().setH(true);
    return Mem.read(registers.pc().getAndIncrement());
  }

  @Override
  protected Mem execute1(byte data) {
    registers.ir().set(data);
    return null;
  }
}
