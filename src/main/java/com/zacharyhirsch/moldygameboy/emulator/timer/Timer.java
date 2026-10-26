package com.zacharyhirsch.moldygameboy.emulator.timer;

import com.zacharyhirsch.moldygameboy.emulator.arch.Irqs;

public final class Timer {

  private final Irqs.Line irq;

  private short counter = 0;
  private byte tima = 0;
  private byte tma = 0;
  private byte tac = 0;
  private boolean prevSignal = false;
  private boolean reload = false;
  private int reloadDelay = 0;

  public Timer(Irqs.Line irq) {
    this.irq = irq;
  }

  public byte getDiv() {
    return (byte) (counter >>> 8);
  }

  public void resetDiv() {
    counter = 0;
  }

  public byte getTima() {
    return tima;
  }

  public void setTima(byte value) {
    this.tima = value;
    if (reload && reloadDelay > 1) {
      reload = false;
    }
  }

  public byte getTma() {
    return tma;
  }

  public void setTma(byte value) {
    this.tma = value;
  }

  public byte getTac() {
    return tac;
  }

  public void setTac(byte value) {
    this.tac = value;
  }

  public void tick() {
    if (reload) {
      reloadDelay--;
      if (reloadDelay == 0) {
        reload = false;
        tima = tma;
        irq.set(true);
      }
    } else {
      irq.set(false);
    }

    counter++;

    int bit =
        switch (tac & 0b0000_0011) {
          case 0b00 -> 9;
          case 0b01 -> 3;
          case 0b10 -> 5;
          case 0b11 -> 7;
          default -> throw new IllegalStateException();
        };
    boolean signal = bit16(counter, bit) && bit8(tac, 2);
    if (prevSignal && !signal) {
      if (Byte.toUnsignedInt(tima) == 0xff) {
        reload = true;
        reloadDelay = 4;
      }
      tima = (byte) (tima + 1);
    }
    prevSignal = signal;
  }

  private static boolean bit8(byte value, int index) {
    assert 0 <= index && index < 8;
    return (value & (1 << index)) != 0;
  }

  private static boolean bit16(short value, int index) {
    assert 0 <= index && index < 16;
    return (value & (1 << index)) != 0;
  }
}
