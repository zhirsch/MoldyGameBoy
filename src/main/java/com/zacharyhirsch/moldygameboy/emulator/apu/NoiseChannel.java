package com.zacharyhirsch.moldygameboy.emulator.apu;

import java.util.function.BooleanSupplier;

/** Channel 4: pseudo-random noise from a linear feedback shift register. */
final class NoiseChannel {

  private final BooleanSupplier nextStepClocksLength;
  private final LengthCounter length = new LengthCounter(64);
  private final Envelope envelope = new Envelope();

  private boolean enabled = false;
  private int polynomial = 0;
  private int frequencyTimer = 0;
  private int lfsr = 0x7fff;

  NoiseChannel(BooleanSupplier nextStepClocksLength) {
    this.nextStepClocksLength = nextStepClocksLength;
  }

  /** NR41: the length (write-only). */
  final SoundRegister nrx1 =
      new SoundRegister() {
        @Override
        public int get() {
          return 0;
        }

        @Override
        public void set(int value) {
          setLength(value);
        }

        @Override
        public void setLength(int value) {
          length.load(value & 0x3f);
        }
      };

  /** NR42: the volume envelope. */
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

  /** NR43: the LFSR clock shift, width and divisor. */
  final SoundRegister nrx3 =
      new SoundRegister() {
        @Override
        public int get() {
          return polynomial;
        }

        @Override
        public void set(int value) {
          polynomial = value;
        }
      };

  /** NR44: the trigger and length enable. */
  final SoundRegister nrx4 =
      new SoundRegister() {
        @Override
        public int get() {
          return length.isEnabled() ? 0x40 : 0x00;
        }

        @Override
        public void set(int value) {
          if (length.write(value, nextStepClocksLength.getAsBoolean())) {
            enabled = false;
          }
          if ((value & 0x80) != 0) {
            enabled = envelope.isDacOn();
            frequencyTimer = period();
            lfsr = 0x7fff;
            envelope.trigger();
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
    return enabled && (lfsr & 1) == 0 ? envelope.volume() : 0;
  }

  void tick() {
    if (--frequencyTimer > 0) {
      return;
    }
    frequencyTimer += period();
    // Shifts of 14 and 15 stop the LFSR.
    if ((polynomial >>> 4) >= 14) {
      return;
    }
    int feedback = (lfsr ^ (lfsr >>> 1)) & 1;
    lfsr = (lfsr >>> 1) | (feedback << 14);
    if ((polynomial & 0x08) != 0) {
      lfsr = (lfsr & ~0x40) | (feedback << 6);
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

  void powerOff() {
    enabled = false;
    polynomial = 0;
    length.powerOff();
    envelope.powerOff();
  }

  private int period() {
    int divisor = (polynomial & 0x07) == 0 ? 8 : (polynomial & 0x07) * 16;
    return divisor << (polynomial >>> 4);
  }
}
