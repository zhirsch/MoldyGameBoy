package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;

public final class Cpl extends AbstractInstruction {

  private final Registers registers;

  public Cpl(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected MemOperation execute0(byte data) {
    registers.a().set((byte) ~registers.a().get());
    registers.f().setN(true);
    registers.f().setH(true);
    return new MemRead(registers.pc().getAndIncrement());
  }

  @Override
  protected MemOperation execute1(byte data) {
    registers.ir().set(data);
    return null;
  }
}
