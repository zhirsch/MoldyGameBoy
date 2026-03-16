package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Jr extends AbstractInstruction3 {

  private final Registers registers;

  private byte z;

  public Jr(Registers registers) {
    this.registers = registers;
  }

  @Override
  protected Mem execute0() {
    return Mem.read(registers.pc().getAndIncrement(), data -> z = data);
  }

  @Override
  protected Mem execute1() {
    registers.pc().set((short) (registers.pc().get() + z));
    return Mem.none(registers.pc().get());
  }

  @Override
  protected Mem execute2() {
    return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
  }
}
