package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Reti extends AbstractInstruction4 {

  private final Registers registers;

  private byte w;
  private byte z;

  public Reti(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0() {
    return Mem.read(registers.sp().getAndIncrement(), data -> z = data);
  }

  @Override
  protected Mem execute1() {
    return Mem.read(registers.sp().getAndIncrement(), data -> w = data);
  }

  @Override
  protected Mem execute2() {
    return Mem.none((short) 0);
  }

  @Override
  protected Mem execute3() {
    registers.pc().write(w, z);
    registers.ime().write((byte) 1);
    return Mem.read(registers.pc().getAndIncrement(), registers.ir()::write);
  }
}
