package com.zacharyhirsch.moldygameboy.emulator.timer;

import com.zacharyhirsch.moldygameboy.emulator.arch.Irqs;

public final class Timer {

  private final Irqs.Line irq;

  private short counter = 0;
  private byte tima = 0;
  private byte tma = 0;
  private byte tac = 0;
  private boolean prevSignal = false;

  // Overflow/reload state machine, counted in T-ticks after TIMA overflows:
  //   8..5  TIMA reads 00; a TIMA write cancels the reload and the interrupt
  //   4..1  reload cycle; TIMA is loaded from TMA, TIMA writes are ignored
  //   0     idle
  private int reloadState = 0;

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
    if (1 <= reloadState && reloadState <= 4) {
      return;
    }
    reloadState = 0;
    tima = value;
  }

  public byte getTma() {
    return tma;
  }

  public void setTma(byte value) {
    tma = value;
    if (1 <= reloadState && reloadState <= 4) {
      tima = value;
    }
  }

  public byte getTac() {
    return tac;
  }

  public void setTac(byte value) {
    tac = value;
  }

  public void tick() {
    if (reloadState > 0) {
      reloadState--;
      if (reloadState == 4) {
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
        reloadState = 8;
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
