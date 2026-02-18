package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;

public final class Scf extends AbstractInstruction {

  private final Registers registers;

  public Scf(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected MemRead execute0(byte data) {
    registers.f().setN(false);
    registers.f().setH(false);
    registers.f().setC(true);
    return new MemRead(registers.pc().getAndIncrement());
  }

  @Override
  protected MemOperation execute1(byte data) {
    registers.ir().set(data);
    return null;
  }
}
