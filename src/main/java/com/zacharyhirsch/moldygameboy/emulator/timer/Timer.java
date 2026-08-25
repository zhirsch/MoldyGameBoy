package com.zacharyhirsch.moldygameboy.emulator.timer;

import com.zacharyhirsch.moldygameboy.emulator.arch.InterruptRequestLine;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;

public final class Timer {

  private final Memory memory;
  private final InterruptRequestLine timer;

  private int count = 0;

  public Timer(Memory memory, InterruptRequestLine timer) {
    this.memory = memory;
    this.timer = timer;
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
    int nextTima;
    if (memory.registers().tima().get() == (byte) 0xff) {
      nextTima = memory.registers().tma().get();
      timer.set(true);
    } else {
      nextTima = memory.registers().tima().get() + 1;
      timer.set(false);
    }
    memory.registers().tima().set((byte) nextTima);
  }
}
