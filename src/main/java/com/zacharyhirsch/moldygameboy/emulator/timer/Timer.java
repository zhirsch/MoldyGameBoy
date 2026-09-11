package com.zacharyhirsch.moldygameboy.emulator.timer;

import com.zacharyhirsch.moldygameboy.emulator.arch.InterruptRequestLine;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;

public final class Timer {

  private final IoRegisters ioregs;
  private final InterruptRequestLine timer;

  private boolean prevSignal = false;
  private boolean reload = false;

  public Timer(IoRegisters ioregs, InterruptRequestLine timer) {
    this.ioregs = ioregs;
    this.timer = timer;
  }

  public void tick() {
    if (reload) {
      reload = false;
      ioregs.tima.set(ioregs.tma.get());
      timer.set(true);
    } else {
      timer.set(false);
    }

    ioregs.div.increment();
    ioregs.div.increment();
    ioregs.div.increment();
    ioregs.div.increment();

    int bit =
        switch (ioregs.tac.get() & 0b0000_0011) {
          case 0b00 -> 9;
          case 0b01 -> 3;
          case 0b10 -> 5;
          case 0b11 -> 7;
          default -> throw new IllegalStateException();
        };
    boolean signal = ioregs.div.bit(bit) && ioregs.tac.bit(2);
    if (prevSignal && !signal) {
      reload = ioregs.tima.get() == 0xff;
      ioregs.tima.set((byte) (ioregs.tima.get() + 1));
    }
    prevSignal = signal;
  }
}
