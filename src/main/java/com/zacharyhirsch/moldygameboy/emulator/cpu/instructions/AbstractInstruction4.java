package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

public abstract class AbstractInstruction4 implements Instruction {

  private int i = 0;

  @Override
  public final Mem tick() {
    return switch (i++) {
      case 0 -> execute0();
      case 1 -> execute1();
      case 2 -> execute2();
      case 3 -> execute3();
      case 4 -> null;
      default -> throw new IllegalStateException();
    };
  }

  protected abstract Mem execute0();

  protected abstract Mem execute1();

  protected abstract Mem execute2();

  protected abstract Mem execute3();
}
