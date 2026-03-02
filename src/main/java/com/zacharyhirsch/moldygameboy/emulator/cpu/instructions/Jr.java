package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;


public final class Jr extends AbstractInstruction{

  private final Registers registers;

  private byte z;

  public Jr(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0(byte data) {
    return Mem.read(registers.pc().getAndIncrement());
  }

  @Override
  protected Mem execute1(byte data) {
    z = data;
    return Mem.read(registers.pc().get());
  }

  @Override
  protected Mem execute2(byte data) {
    short wz = (short) (Short.toUnsignedInt(registers.pc().get()) + z);
    registers.pc().set(wz);
    return Mem.read(registers.pc().getAndIncrement());
  }

  @Override
  protected Mem execute3(byte data) {
    registers.ir().set(data);
    return null;
  }
}
