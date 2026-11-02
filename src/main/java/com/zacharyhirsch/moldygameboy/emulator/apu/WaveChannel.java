package com.zacharyhirsch.moldygameboy.emulator.apu;

import java.util.function.BooleanSupplier;

/**
 * Channel 3: plays the 32 4-bit samples in wave RAM.
 *
 * <p>While the channel is on, CPU accesses to wave RAM go to the byte being played rather than the
 * one addressed. On DMG, they only get through just after the channel has read a sample;
 * otherwise reads return $FF and writes are ignored. Also on DMG, retriggering the channel just
 * before it reads a sample corrupts the start of wave RAM. The timings are fitted to blargg
 * dmg_sound 09-wave read while on, 10-wave trigger while on and 12-wave write while on.
 */
final class WaveChannel {

  private static final int[] VOLUME_SHIFT = {4, 0, 1, 2};
  // T-cycles from a trigger to the first sample read, on top of the period.
  private static final int TRIGGER_DELAY = 6;
  // T-cycles after a sample read during which the CPU can access wave RAM.
  private static final int ACCESS_WINDOW = 2;
  // A retrigger within this many T-cycles of a sample read corrupts wave RAM.
  private static final int CORRUPT_WINDOW = 2;

  private final BooleanSupplier nextStepClocksLength;
  private final BooleanSupplier dmg;
  private final LengthCounter length = new LengthCounter(256);
  private final int[] waveRam = new int[16];

  private boolean enabled = false;
  private boolean dacOn = false;
  private int volume = 0;
  private int frequency = 0;
  private int frequencyTimer = 0;
  private int position = 0;
  private int sampleBuffer = 0;
  private int ticksSinceRead = 1000;

  WaveChannel(BooleanSupplier nextStepClocksLength, BooleanSupplier dmg) {
    this.nextStepClocksLength = nextStepClocksLength;
    this.dmg = dmg;
  }

  /** NR30: the DAC. */
  final SoundRegister nrx0 =
      new SoundRegister() {
        @Override
        public int get() {
          return dacOn ? 0x80 : 0x00;
        }

        @Override
        public void set(int value) {
          dacOn = (value & 0x80) != 0;
          if (!dacOn) {
            enabled = false;
          }
        }
      };

  /** NR31: the length (write-only). */
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
          length.load(value);
        }
      };

  /** NR32: the output level. */
  final SoundRegister nrx2 =
      new SoundRegister() {
        @Override
        public int get() {
          return volume << 5;
        }

        @Override
        public void set(int value) {
          volume = (value >>> 5) & 0x03;
        }
      };

  /** NR33: the low 8 bits of the frequency. */
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

  /** NR34: the trigger, length enable and the high 3 bits of the frequency. */
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
            if (dmg.getAsBoolean() && enabled && frequencyTimer <= CORRUPT_WINDOW) {
              corruptWaveRam();
            }
            enabled = dacOn;
            position = 0;
            frequencyTimer = period() + TRIGGER_DELAY;
          }
        }
      };

  int readWaveRam(int index) {
    if (enabled) {
      return canAccessCurrentByte() ? waveRam[position >>> 1] : 0xff;
    }
    return waveRam[index];
  }

  void writeWaveRam(int index, int value) {
    if (enabled) {
      if (canAccessCurrentByte()) {
        waveRam[position >>> 1] = value;
      }
      return;
    }
    waveRam[index] = value;
  }

  private boolean canAccessCurrentByte() {
    return !dmg.getAsBoolean() || ticksSinceRead < ACCESS_WINDOW;
  }

  /**
   * The byte about to be read is copied to the start of wave RAM: on its own if it's one of the
   * first 4 bytes, otherwise along with the rest of its 4-byte block.
   */
  private void corruptWaveRam() {
    int index = ((position + 1) & 0x1f) >>> 1;
    if (index < 4) {
      waveRam[0] = waveRam[index];
    } else {
      System.arraycopy(waveRam, index & ~3, waveRam, 0, 4);
    }
  }

  boolean isEnabled() {
    return enabled;
  }

  boolean isDacOn() {
    return dacOn;
  }

  /** The current output level, 0-15. */
  int output() {
    return enabled ? sampleBuffer >>> VOLUME_SHIFT[volume] : 0;
  }

  void tick() {
    ticksSinceRead++;
    if (!enabled || --frequencyTimer > 0) {
      return;
    }
    ticksSinceRead = 0;
    frequencyTimer += period();
    position = (position + 1) & 0x1f;
    int sample = waveRam[position >>> 1];
    sampleBuffer = (position & 1) == 0 ? sample >>> 4 : sample & 0x0f;
  }

  void clockLength() {
    if (length.clock()) {
      enabled = false;
    }
  }

  void powerOff(boolean dmg) {
    enabled = false;
    dacOn = false;
    volume = 0;
    frequency = 0;
    length.powerOff(dmg);
  }

  void powerOn() {
    position = 0;
    sampleBuffer = 0;
  }

  private int period() {
    return (2048 - frequency) * 2;
  }
}
