package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

public abstract class AbstractInstruction3 implements Instruction {

  private int i = 0;

  @Override
  public final Mem tick() {
    return switch (i++) {
      case 0 -> execute0();
      case 1 -> execute1();
      case 2 -> execute2();
      case 3 -> null;
      default -> throw new IllegalStateException();
    };
  }

  protected abstract Mem execute0();

  protected abstract Mem execute1();

  protected abstract Mem execute2();
}
