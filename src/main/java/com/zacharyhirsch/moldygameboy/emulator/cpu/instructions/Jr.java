package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;

public final class Jr extends AbstractInstruction{

  private final Registers registers;

  private byte z;

  public Jr(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected MemOperation execute0(byte data) {
    return new MemRead(registers.pc().getAndIncrement());
  }

  @Override
  protected MemOperation execute1(byte data) {
    z = data;
    return new MemRead(registers.pc().get());
  }

  @Override
  protected MemOperation execute2(byte data) {
    short wz = (short) (Short.toUnsignedInt(registers.pc().get()) + z);
    registers.pc().set(wz);
    return new MemRead(registers.pc().getAndIncrement());
  }

  @Override
  protected MemOperation execute3(byte data) {
    registers.ir().set(data);
    return null;
  }
}
