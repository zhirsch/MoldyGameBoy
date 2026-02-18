package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;

public final class Rra extends AbstractInstruction {

  private final Registers registers;

  public Rra(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected MemRead execute0(byte data) {
    boolean b0 = (registers.a().get() & 0x01) != 0;
    byte carry = (byte) (registers.f().getC() ? 0x80 : 0x00);
    byte result = (byte) ((Byte.toUnsignedInt(registers.a().get()) >>> 1) | carry);
    registers.a().set(result);
    registers.f().setZ(false);
    registers.f().setN(false);
    registers.f().setH(false);
    registers.f().setC(b0);
    return new MemRead(registers.pc().getAndIncrement());
  }

  @Override
  protected MemOperation execute1(byte data) {
    registers.ir().set(data);
    return null;
  }
}
