package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register16;
import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;


public final class Push extends AbstractInstruction {

  private final Registers registers;
  private final Register16<? extends UInt8, ? extends UInt8> register;

  public Push(Registers registers, Register16<? extends UInt8, ? extends UInt8> register) {
    this.registers = registers;
    this.register = register;
  }

  @Override
  protected Mem execute0(byte data) {
    return Mem.read(registers.sp().getAndDecrement());
  }

  @Override
  protected Mem execute1(byte data) {
    return Mem.write(registers.sp().getAndDecrement(), register.hi().get());
  }

  @Override
  protected Mem execute2(byte data) {
    return Mem.write(registers.sp().get(), register.lo().get());
  }

  @Override
  protected Mem execute3(byte data) {
    return Mem.read(registers.pc().getAndIncrement());
  }

  @Override
  protected Mem execute4(byte data) {
    registers.ir().set(data);
    return null;
  }
}
