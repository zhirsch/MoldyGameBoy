package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;


public final class Rst extends AbstractInstruction {

  private final Registers registers;
  private final short address;

  private final byte w = 0;
  private final byte z = 0;

  public Rst(Registers registers, short address) {
    this.registers = registers;
    this.address = address;
  }

  @Override
  protected Mem execute0(byte data) {
    return Mem.read(registers.sp().getAndDecrement());
  }

  @Override
  protected Mem execute1(byte data) {
    return Mem.write(registers.sp().getAndDecrement(), registers.pc().hi().get());
  }

  @Override
  protected Mem execute2(byte data) {
    return Mem.write(registers.sp().get(), registers.pc().lo().get());
  }

  @Override
  protected Mem execute3(byte data) {
    registers.pc().set(address);
    return Mem.read(registers.pc().getAndIncrement());
  }

  @Override
  protected Mem execute4(byte data) {
    registers.ir().set(data);
    return null;
  }
}
