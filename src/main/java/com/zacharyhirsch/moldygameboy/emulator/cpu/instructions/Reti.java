package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;

public final class Reti extends AbstractInstruction {

  private final Registers registers;

  private byte w = 0;
  private byte z = 0;

  public Reti(Registers registers) {
    this.registers = registers;
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
    return new MemRead((short) 0);
  }

  @Override
  protected MemOperation execute3(byte data) {
    registers.pc().set(w, z);
    registers.ime().set();
    return new MemRead(registers.pc().getAndIncrement());
  }

  @Override
  protected MemOperation execute4(byte data) {
    registers.ir().set(data);
    return null;
  }
}
