package com.zacharyhirsch.moldygameboy.emulator.timer;

import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;

public final class Divider {

  private final Memory memory;

  private int count = 0;

  public Divider(Memory memory) {
    this.memory = memory;
  }

  public void tick() {
    count = (count + 1) % 64;
    if (count == 0) {
      memory.setDiv((byte) (memory.getDiv() + 1));
    }
  }
}
