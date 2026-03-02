package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

public abstract class AbstractInstruction implements Instruction {

  private int i = 0;

  @Override
  public final Mem tick(byte data) {
    return switch (i++) {
      case 0 -> execute0(data);
      case 1 -> execute1(data);
      case 2 -> execute2(data);
      case 3 -> execute3(data);
      case 4 -> execute4(data);
      case 5 -> execute5(data);
      case 6 -> execute6(data);
      default -> throw new IllegalStateException();
    };
  }

  protected Mem execute0(byte data) {
    throw new IllegalStateException();
  }

  protected Mem execute1(byte data) {
    throw new IllegalStateException();
  }

  protected Mem execute2(byte data) {
    throw new IllegalStateException();
  }

  protected Mem execute3(byte data) {
    throw new IllegalStateException();
  }

  protected Mem execute4(byte data) {
    throw new IllegalStateException();
  }

  protected Mem execute5(byte data) {
    throw new IllegalStateException();
  }

  protected Mem execute6(byte data) {
    throw new IllegalStateException();
  }
}
