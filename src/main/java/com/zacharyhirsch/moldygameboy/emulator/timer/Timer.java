package com.zacharyhirsch.moldygameboy.emulator.timer;

import com.zacharyhirsch.moldygameboy.emulator.arch.Memory;

public final class Timer {

  private final Memory memory;

  private int count = 0;

  public Timer(Memory memory) {
    this.memory = memory;
  }

  public void tick() {
    byte tac = memory.read(Memory.Register.TAC);
    if ((tac & 0b0000_0100) == 0) {
      return;
    }
    int period =
        switch (tac & 0b0000_0011) {
          case 0 -> 256;
          case 1 -> 4;
          case 2 -> 16;
          case 3 -> 64;
          default -> throw new IllegalStateException();
        };
    count = (count + 1) % period;
    if (count != 0) {
      return;
    }
    byte tima = memory.read(Memory.Register.TIMA);
    if (tima == (byte) 0xff) {
      memory.write(Memory.Register.IF, (byte) (memory.read(Memory.Register.IF) | 0b0000_0100));
      memory.write(Memory.Register.TIMA, memory.read(Memory.Register.TMA));
      return;
    }
    memory.write(Memory.Register.TIMA, (byte) (tima + 1));
  }
}
