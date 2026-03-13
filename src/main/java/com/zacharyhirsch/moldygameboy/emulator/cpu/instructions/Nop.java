package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Nop implements Instruction {

  private final Registers registers;

  private int i = 0;

  public Nop(Registers registers) {
    this.registers = registers;
  }

  @Override
  public Mem tick() {
    return switch (i++) {
      case 0 -> Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
      case 1 -> null;
      default -> throw new IllegalStateException();
    };
  }
}
