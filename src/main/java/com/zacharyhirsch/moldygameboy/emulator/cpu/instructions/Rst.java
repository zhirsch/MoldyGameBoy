package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemWrite;

public final class Rst extends AbstractInstruction {

  private final Registers registers;
  private final short address;

  private byte w = 0;
  private byte z = 0;

  public Rst(Registers registers, short address) {
    this.registers = registers;
    this.address = address;
  }

  @Override
  protected MemOperation execute0(byte data) {
    return new MemRead(registers.sp().getAndDecrement());
  }

  @Override
  protected MemOperation execute1(byte data) {
    return new MemWrite(registers.sp().getAndDecrement(), registers.pc().hi().get());
  }

  @Override
  protected MemOperation execute2(byte data) {
    return new MemWrite(registers.sp().get(), registers.pc().lo().get());
  }

  @Override
  protected MemOperation execute3(byte data) {
    registers.pc().set(address);
    return new MemRead(registers.pc().getAndIncrement());
  }

  @Override
  protected MemOperation execute4(byte data) {
    registers.ir().set(data);
    return null;
  }
}
