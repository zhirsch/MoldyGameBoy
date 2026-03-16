package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

public abstract class AbstractInstruction1 implements Instruction {

  private int i = 0;

  @Override
  public final Mem tick() {
    return switch (i++) {
      case 0 -> execute0();
      case 1 -> null;
      default -> throw new IllegalStateException();
    };
  }

  protected abstract Mem execute0();
}
