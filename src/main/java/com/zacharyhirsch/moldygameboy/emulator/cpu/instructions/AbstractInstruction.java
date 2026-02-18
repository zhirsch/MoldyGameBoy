package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;

public abstract class AbstractInstruction implements Instruction {

  private int i = 0;

  @Override
  public final MemOperation tick(byte data) {
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

  protected MemOperation execute0(byte data) {
    throw new IllegalStateException();
  }

  protected MemOperation execute1(byte data) {
    throw new IllegalStateException();
  }

  protected MemOperation execute2(byte data) {
    throw new IllegalStateException();
  }

  protected MemOperation execute3(byte data) {
    throw new IllegalStateException();
  }

  protected MemOperation execute4(byte data) {
    throw new IllegalStateException();
  }

  protected MemOperation execute5(byte data) {
    throw new IllegalStateException();
  }

  protected MemOperation execute6(byte data) {
    throw new IllegalStateException();
  }
}
