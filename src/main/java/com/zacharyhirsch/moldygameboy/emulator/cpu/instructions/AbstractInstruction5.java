package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

public abstract class AbstractInstruction5 implements Instruction {

  private int i = 0;

  @Override
  public Mem tick() {
    return switch (i++) {
      case 0 -> execute0();
      case 1 -> execute1();
      case 2 -> execute2();
      case 3 -> execute3();
      case 4 -> execute4();
      case 5 -> null;
      default -> throw new IllegalStateException();
    };
  }

  protected abstract Mem execute0();

  protected abstract Mem execute1();

  protected abstract Mem execute2();

  protected abstract Mem execute3();

  protected abstract Mem execute4();
}
