package com.zacharyhirsch.moldygameboy.emulator.timer;

import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;

public final class Timer {

  private final Memory memory;

  private int count = 0;

  public Timer(Memory memory) {
    this.memory = memory;
  }

  public void tick() {
    if ((memory.registers().tac().get() & 0b0000_0100) == 0) {
      return;
    }
    int period =
        switch (memory.registers().tac().get() & 0b0000_0011) {
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
    if (memory.registers().tima().get() == (byte) 0xff) {
      memory.registers().if_().set((byte) (memory.registers().if_().get() | 0b0000_0100));
      memory.registers().tima().set(memory.registers().tma().get());
      return;
    }
    memory.registers().tima().set((byte) (memory.registers().tima().get() + 1));
  }
}
