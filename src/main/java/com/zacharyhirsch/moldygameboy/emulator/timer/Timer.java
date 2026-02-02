package com.zacharyhirsch.moldygameboy.emulator.timer;

import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;

public final class Timer {

  private final Memory memory;

  private int count = 0;

  public Timer(Memory memory) {
    this.memory = memory;
  }

  public void tick() {
    if ((memory.getTac() & 0b0000_0100) == 0) {
      return;
    }
    int period =
        switch (memory.getTac() & 0b0000_0011) {
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
    if (memory.getTima() == (byte) 0xff) {
      memory.setIf((byte) (memory.getIf() | 0b0000_0100));
      memory.setTima(memory.getTma());
      return;
    }
    memory.setTima((byte) (memory.getTima() + 1));
  }
}
