package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Rst extends AbstractInstruction4 {

  private final Registers registers;
  private final short address;

  public Rst(Registers registers, short address) {
    this.registers = registers;
    this.address = address;
  }

  @Override
  protected Mem execute0() {
    return Mem.none(registers.sp().getAndDecrement());
  }

  @Override
  protected Mem execute1() {
    return Mem.write(registers.sp().getAndDecrement(), registers.pc().hi()::read);
  }

  @Override
  protected Mem execute2() {
    return Mem.write(registers.sp().read(), registers.pc().lo()::read);
  }

  @Override
  protected Mem execute3() {
    registers.pc().write(address);
    return Mem.read(registers.pc().getAndIncrement(), registers.ir()::write);
  }
}
