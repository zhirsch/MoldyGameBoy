package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

public abstract class AbstractInstruction2 implements Instruction {

  private int i = 0;

  @Override
  public Mem tick() {
    return switch (i++) {
      case 0 -> execute0();
      case 1 -> execute1();
      case 2 -> null;
      default -> throw new IllegalStateException();
    };
  }

  protected abstract Mem execute0();

  protected abstract Mem execute1();
}
