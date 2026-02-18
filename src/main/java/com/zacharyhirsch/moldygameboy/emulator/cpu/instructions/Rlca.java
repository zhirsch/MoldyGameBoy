package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;

public final class Rlca extends AbstractInstruction {

  private final Registers registers;

  public Rlca(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected MemRead execute0(byte data) {
    boolean b7 = (registers.a().get() & 0x80) != 0;
    registers.a().set((byte) ((registers.a().get() << 1) | (b7 ? 1 : 0)));
    registers.f().setZ(false);
    registers.f().setN(false);
    registers.f().setH(false);
    registers.f().setC(b7);
    return new MemRead(registers.pc().getAndIncrement());
  }

  @Override
  protected MemOperation execute1(byte data) {
    registers.ir().set(data);
    return null;
  }
}
