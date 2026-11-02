package com.zacharyhirsch.moldygameboy.emulator.apu;

import java.util.function.BooleanSupplier;

/** Channels 1 and 2: a square wave, with a frequency sweep on channel 1. */
final class SquareChannel {

  private static final int[][] DUTY = {
    {0, 0, 0, 0, 0, 0, 0, 1}, // 12.5%
    {1, 0, 0, 0, 0, 0, 0, 1}, // 25%
    {1, 0, 0, 0, 0, 1, 1, 1}, // 50%
    {0, 1, 1, 1, 1, 1, 1, 0}, // 75%
  };

  private final boolean hasSweep;
  private final BooleanSupplier nextStepClocksLength;
  private final LengthCounter length = new LengthCounter(64);
  private final Envelope envelope = new Envelope();

  private boolean enabled = false;
  private int duty = 0;
  private int frequency = 0;
  private int frequencyTimer = 0;
  private int dutyPosition = 0;

  // Sweep (channel 1 only), controlled by NR10.
  private int sweep = 0;
  private int shadowFrequency = 0;
  private int sweepTimer = 0;
  private boolean sweepEnabled = false;
  private boolean negateUsed = false;

  SquareChannel(boolean hasSweep, BooleanSupplier nextStepClocksLength) {
    this.hasSweep = hasSweep;
    this.nextStepClocksLength = nextStepClocksLength;
  }

  /** NR10: the sweep (channel 1 only). */
  final SoundRegister nrx0 =
      new SoundRegister() {
        @Override
        public int get() {
          return sweep;
        }

        @Override
        public void set(int value) {
          // Clearing the negate bit after a sweep calculation used it turns the channel off (see
          // blargg dmg_sound 05-sweep details).
          if (negateUsed && (sweep & 0x08) != 0 && (value & 0x08) == 0) {
            enabled = false;
          }
          sweep = value;
        }
      };

  /** NRx1: the duty cycle and length. */
  final SoundRegister nrx1 =
      new SoundRegister() {
        @Override
        public int get() {
          return duty << 6;
        }

        @Override
        public void set(int value) {
          duty = value >>> 6;
          setLength(value);
        }

        @Override
        public void setLength(int value) {
          length.load(value & 0x3f);
        }
      };

  /** NRx2: the volume envelope. */
  final SoundRegister nrx2 =
      new SoundRegister() {
        @Override
        public int get() {
          return envelope.nrx2();
        }

        @Override
        public void set(int value) {
          envelope.setNrx2(value);
          if (!envelope.isDacOn()) {
            enabled = false;
          }
        }
      };

  /** NRx3: the low 8 bits of the frequency. */
  final SoundRegister nrx3 =
      new SoundRegister() {
        @Override
        public int get() {
          return frequency & 0xff;
        }

        @Override
        public void set(int value) {
          frequency = (frequency & 0x700) | value;
        }
      };

  /** NRx4: the trigger, length enable and the high 3 bits of the frequency. */
  final SoundRegister nrx4 =
      new SoundRegister() {
        @Override
        public int get() {
          return (length.isEnabled() ? 0x40 : 0x00) | (frequency >>> 8);
        }

        @Override
        public void set(int value) {
          frequency = (frequency & 0xff) | ((value & 0x07) << 8);
          if (length.write(value, nextStepClocksLength.getAsBoolean())) {
            enabled = false;
          }
          if ((value & 0x80) != 0) {
            trigger();
          }
        }
      };

  boolean isEnabled() {
    return enabled;
  }

  boolean isDacOn() {
    return envelope.isDacOn();
  }

  /** The current output level, 0-15. */
  int output() {
    return enabled ? DUTY[duty][dutyPosition] * envelope.volume() : 0;
  }

  void tick() {
    if (--frequencyTimer <= 0) {
      frequencyTimer += period();
      dutyPosition = (dutyPosition + 1) & 0x07;
    }
  }

  void clockLength() {
    if (length.clock()) {
      enabled = false;
    }
  }

  void clockEnvelope() {
    envelope.clock();
  }

  void clockSweep() {
    if (!hasSweep || --sweepTimer > 0) {
      return;
    }
    sweepTimer = sweepPeriod() == 0 ? 8 : sweepPeriod();
    if (sweepEnabled && sweepPeriod() != 0) {
      int newFrequency = calculateSweep();
      if (newFrequency <= 2047 && sweepShift() != 0) {
        shadowFrequency = newFrequency;
        frequency = newFrequency;
        calculateSweep();
      }
    }
  }

  void powerOff(boolean dmg) {
    enabled = false;
    duty = 0;
    frequency = 0;
    sweep = 0;
    sweepEnabled = false;
    negateUsed = false;
    length.powerOff(dmg);
    envelope.powerOff();
  }

  void powerOn() {
    dutyPosition = 0;
  }

  private void trigger() {
    enabled = envelope.isDacOn();
    frequencyTimer = period();
    envelope.trigger();
    if (hasSweep) {
      shadowFrequency = frequency;
      sweepTimer = sweepPeriod() == 0 ? 8 : sweepPeriod();
      sweepEnabled = sweepPeriod() != 0 || sweepShift() != 0;
      negateUsed = false;
      if (sweepShift() != 0) {
        calculateSweep();
      }
    }
  }

  /** Computes the next swept frequency, turning the channel off if it overflows. */
  private int calculateSweep() {
    int delta = shadowFrequency >>> sweepShift();
    int newFrequency;
    if ((sweep & 0x08) != 0) {
      newFrequency = shadowFrequency - delta;
      negateUsed = true;
    } else {
      newFrequency = shadowFrequency + delta;
    }
    if (newFrequency > 2047) {
      enabled = false;
    }
    return newFrequency;
  }

  private int period() {
    return (2048 - frequency) * 4;
  }

  private int sweepPeriod() {
    return (sweep >>> 4) & 0x07;
  }

  private int sweepShift() {
    return sweep & 0x07;
  }
}
