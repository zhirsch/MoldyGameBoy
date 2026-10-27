package com.zacharyhirsch.moldygameboy.emulator.timer;

import com.zacharyhirsch.moldygameboy.emulator.arch.IoRegister;
import com.zacharyhirsch.moldygameboy.emulator.arch.Irqs;

public final class Timer {

  private enum State {
    RUNNING,
    RELOADING, // TIMA reads 00, TMA not yet loaded visible, IF not yet set
    RELOADED, // reload cycle
  }

  private static final int[] TRIGGER_BITS = {1 << 9, 1 << 3, 1 << 5, 1 << 7};

  private final Irqs.Line irq;

  private int counter = 0; // 16-bit system counter, advances 4 per M-cycle
  private int tima = 0;
  private int tma = 0;
  private int tac = 0;
  private State state = State.RUNNING;

  public Timer(Irqs.Line irq) {
    this.irq = irq;
  }

  public IoRegister div() {
    return new IoRegister() {
      @Override
      public int get() {
        return Byte.toUnsignedInt((byte) (counter >>> 8));
      }

      @Override
      public void set(int value) {
        setCounter((short) 0);
      }

      @Override
      public String toString() {
        return "div @ ff04 = %02x".formatted(get());
      }
    };
  }

  public IoRegister tima() {
    return new IoRegister() {
      @Override
      public int get() {
        return state == State.RELOADING ? 0 : tima;
      }

      @Override
      public void set(int value) {
        if (state != State.RELOADED) {
          tima = value;
        }
      }

      @Override
      public String toString() {
        return "tima @ ff05 = %02x".formatted(get());
      }
    };
  }

  public IoRegister tma() {
    return new IoRegister() {
      @Override
      public int get() {
        return tma;
      }

      @Override
      public void set(int value) {
        tma = value;
        if (state != State.RUNNING) {
          tima = tma;
        }
      }

      @Override
      public String toString() {
        return "tma @ ff06 = %02x".formatted(get());
      }
    };
  }

  public IoRegister tac() {
    return new IoRegister() {
      @Override
      public int get() {
        return tac;
      }

      @Override
      public void set(int value) {
        int newTac = Byte.toUnsignedInt((byte) value);
        if (isTimerEnabled(tac) && isFrequencyBitSet(counter, tac)) {
          if (!isTimerEnabled(newTac)) {
            increaseTima();
          }
          if (isTimerEnabled(newTac) && !isFrequencyBitSet(counter, newTac)) {
            increaseTima();
          }
        }
        tac = newTac;
      }

      @Override
      public String toString() {
        return "tac @ ff07 = %02x".formatted(get());
      }
    };
  }

  public void tickT1() {
    setCounter((short) (counter + 1));
  }

  public void tickT2() {
    setCounter((short) (counter + 1));
  }

  public void tickT3() {
    switch (state) {
      case RELOADED -> {
        irq.set(false);
        state = State.RUNNING;
      }
      case RELOADING -> {
        irq.set(true);
        state = State.RELOADED;
      }
    }
    setCounter((short) (counter + 1));
  }

  public void tickT4() {
    setCounter((short) (counter + 1));
  }

  private void setCounter(short value) {
    int newCounter = Short.toUnsignedInt(value);
    if (isTimerEnabled(tac) && isFrequencyBitSet(counter & ~newCounter, tac)) {
      increaseTima();
    }
    counter = newCounter;
  }

  private void increaseTima() {
    tima = Byte.toUnsignedInt((byte) (tima + 1));
    if (tima == 0) {
      tima = tma;
      state = State.RELOADING;
    }
  }

  private static boolean isTimerEnabled(int tac) {
    return (tac & 0b0000_0100) != 0;
  }

  private static boolean isFrequencyBitSet(int counter, int tac) {
    return ((counter & 0xffff) & TRIGGER_BITS[tac & 0b0000_0011]) != 0;
  }
}
