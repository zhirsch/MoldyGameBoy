package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register16;
import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;

public final class Pop extends AbstractInstruction {

  private final Registers registers;
  private final Register16<? extends UInt8, ? extends UInt8> register;

  private byte z;
  private byte w;

  public Pop(Registers registers, Register16<? extends UInt8, ? extends UInt8> register) {
    this.registers = registers;
    this.register = register;
  }

  @Override
  protected MemOperation execute0(byte data) {
    return new MemRead(registers.sp().getAndIncrement());
  }

  @Override
  protected MemOperation execute1(byte data) {
    z = data;
    return new MemRead(registers.sp().getAndIncrement());
  }

  @Override
  protected MemOperation execute2(byte data) {
    w = data;
    register.set(w, z);
    return new MemRead(registers.pc().getAndIncrement());
  }

  @Override
  protected MemOperation execute3(byte data) {
    registers.ir().set(data);
    return null;
  }
}
