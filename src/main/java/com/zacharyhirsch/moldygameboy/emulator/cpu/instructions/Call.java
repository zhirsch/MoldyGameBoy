package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;


public final class Call extends AbstractInstruction {

  private final Registers registers;

  private byte w = 0;
  private byte z = 0;

  public Call(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0(byte data) {
    return Mem.read(registers.pc().getAndIncrement());
  }

  @Override
  protected Mem execute1(byte data) {
    z = data;
    return Mem.read(registers.pc().getAndIncrement());
  }

  @Override
  protected Mem execute2(byte data) {
    w = data;
    return Mem.read(registers.sp().getAndDecrement());
  }

  @Override
  protected Mem execute3(byte data) {
    return Mem.write(registers.sp().getAndDecrement(), registers.pc().hi().get());
  }

  @Override
  protected Mem execute4(byte data) {
    return Mem.write(registers.sp().get(), registers.pc().lo().get());
  }

  @Override
  protected Mem execute5(byte data) {
    registers.pc().set(w, z);
    return Mem.read(registers.pc().getAndIncrement());
  }

  @Override
  protected Mem execute6(byte data) {
    registers.ir().set(data);
    return null;
  }
}
