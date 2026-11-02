package com.zacharyhirsch.moldygameboy.emulator.apu;

import com.zacharyhirsch.moldygameboy.emulator.arch.IoRegister;
import com.zacharyhirsch.moldygameboy.emulator.arch.Model;
import com.zacharyhirsch.moldygameboy.emulator.io.Audio;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

/**
 * The APU: four sound channels, mixed to left and right outputs (NR51 routes each channel to
 * either or both, and NR50 sets each output's volume) and written to {@link Audio} as one stereo
 * sample per M-cycle.
 *
 * <p>The frame sequencer steps on each falling edge of bit 12 of the system counter (bit 4 of DIV,
 * 512 Hz; bit 13 in double speed, where the counter runs twice as fast), clocking the length
 * counters on even steps, the sweep on steps 2 and 6, and the envelopes on step 7. While the APU is
 * off, the sound registers read as 0 and ignore writes, except that the length counters can still
 * be loaded on DMG (on CGB, they're cleared instead).
 */
public final class Apu {

  private static final int SAMPLES_PER_PRESENT = 4096;
  // The high-pass filter's charge factor per sample, 0.999958 per T-cycle (as on DMG).
  private static final double HIGH_PASS_CHARGE = Math.pow(0.999958, 4);

  private final Model model;
  private final IntSupplier counter;
  private final BooleanSupplier doubleSpeed;
  private final Audio audio;

  private final SquareChannel square1 = new SquareChannel(true, this::nextStepClocksLength);
  private final SquareChannel square2 = new SquareChannel(false, this::nextStepClocksLength);
  private final WaveChannel wave = new WaveChannel(this::nextStepClocksLength, this::isDmg);
  private final NoiseChannel noise = new NoiseChannel(this::nextStepClocksLength);

  private final IoRegister nr10 = register("nr10", 0xff10, square1.nrx0);
  private final IoRegister nr11 = register("nr11", 0xff11, square1.nrx1);
  private final IoRegister nr12 = register("nr12", 0xff12, square1.nrx2);
  private final IoRegister nr13 = register("nr13", 0xff13, square1.nrx3);
  private final IoRegister nr14 = register("nr14", 0xff14, square1.nrx4);

  private final IoRegister nr21 = register("nr21", 0xff16, square2.nrx1);
  private final IoRegister nr22 = register("nr22", 0xff17, square2.nrx2);
  private final IoRegister nr23 = register("nr23", 0xff18, square2.nrx3);
  private final IoRegister nr24 = register("nr24", 0xff19, square2.nrx4);

  private final IoRegister nr30 = register("nr30", 0xff1a, wave.nrx0);
  private final IoRegister nr31 = register("nr31", 0xff1b, wave.nrx1);
  private final IoRegister nr32 = register("nr32", 0xff1c, wave.nrx2);
  private final IoRegister nr33 = register("nr33", 0xff1d, wave.nrx3);
  private final IoRegister nr34 = register("nr34", 0xff1e, wave.nrx4);

  private final IoRegister nr41 = register("nr41", 0xff20, noise.nrx1);
  private final IoRegister nr42 = register("nr42", 0xff21, noise.nrx2);
  private final IoRegister nr43 = register("nr43", 0xff22, noise.nrx3);
  private final IoRegister nr44 = register("nr44", 0xff23, noise.nrx4);

  private final IoRegister nr50 = register("nr50", 0xff24, masterVolume());
  private final IoRegister nr51 = register("nr51", 0xff25, panning());
  private final IoRegister nr52 = soundOnOff();

  private final IoRegister wave0 = waveRam(0);
  private final IoRegister wave1 = waveRam(1);
  private final IoRegister wave2 = waveRam(2);
  private final IoRegister wave3 = waveRam(3);
  private final IoRegister wave4 = waveRam(4);
  private final IoRegister wave5 = waveRam(5);
  private final IoRegister wave6 = waveRam(6);
  private final IoRegister wave7 = waveRam(7);
  private final IoRegister wave8 = waveRam(8);
  private final IoRegister wave9 = waveRam(9);
  private final IoRegister wavea = waveRam(10);
  private final IoRegister waveb = waveRam(11);
  private final IoRegister wavec = waveRam(12);
  private final IoRegister waved = waveRam(13);
  private final IoRegister wavee = waveRam(14);
  private final IoRegister wavef = waveRam(15);

  private boolean powered = false;
  private int nr50Value = 0;
  private int nr51Value = 0;

  // The next frame sequencer step, 0-7.
  private int frameSequencerStep = 0;
  private boolean frameSequencerBit = false;

  private int tcycle = 0;
  private int samples = 0;
  // The high-pass filters' capacitors, one per output.
  private double leftCapacitor = 0;
  private double rightCapacitor = 0;

  public Apu(Model model, IntSupplier counter, BooleanSupplier doubleSpeed, Audio audio) {
    this.model = model;
    this.counter = counter;
    this.doubleSpeed = doubleSpeed;
    this.audio = audio;
  }

  public IoRegister nr10() {
    return nr10;
  }

  public IoRegister nr11() {
    return nr11;
  }

  public IoRegister nr12() {
    return nr12;
  }

  public IoRegister nr13() {
    return nr13;
  }

  public IoRegister nr14() {
    return nr14;
  }

  public IoRegister nr21() {
    return nr21;
  }

  public IoRegister nr22() {
    return nr22;
  }

  public IoRegister nr23() {
    return nr23;
  }

  public IoRegister nr24() {
    return nr24;
  }

  public IoRegister nr30() {
    return nr30;
  }

  public IoRegister nr31() {
    return nr31;
  }

  public IoRegister nr32() {
    return nr32;
  }

  public IoRegister nr33() {
    return nr33;
  }

  public IoRegister nr34() {
    return nr34;
  }

  public IoRegister nr41() {
    return nr41;
  }

  public IoRegister nr42() {
    return nr42;
  }

  public IoRegister nr43() {
    return nr43;
  }

  public IoRegister nr44() {
    return nr44;
  }

  public IoRegister nr50() {
    return nr50;
  }

  public IoRegister nr51() {
    return nr51;
  }

  /** PCM12 (CGB only): the current output levels of channels 1 (low nibble) and 2 (high). */
  public IoRegister pcm12() {
    return pcm("pcm12", 0xff76, square1::output, square2::output);
  }

  /** PCM34 (CGB only): the current output levels of channels 3 (low nibble) and 4 (high). */
  public IoRegister pcm34() {
    return pcm("pcm34", 0xff77, wave::output, noise::output);
  }

  public IoRegister nr52() {
    return nr52;
  }

  public IoRegister wave0() {
    return wave0;
  }

  public IoRegister wave1() {
    return wave1;
  }

  public IoRegister wave2() {
    return wave2;
  }

  public IoRegister wave3() {
    return wave3;
  }

  public IoRegister wave4() {
    return wave4;
  }

  public IoRegister wave5() {
    return wave5;
  }

  public IoRegister wave6() {
    return wave6;
  }

  public IoRegister wave7() {
    return wave7;
  }

  public IoRegister wave8() {
    return wave8;
  }

  public IoRegister wave9() {
    return wave9;
  }

  public IoRegister wavea() {
    return wavea;
  }

  public IoRegister waveb() {
    return waveb;
  }

  public IoRegister wavec() {
    return wavec;
  }

  public IoRegister waved() {
    return waved;
  }

  public IoRegister wavee() {
    return wavee;
  }

  public IoRegister wavef() {
    return wavef;
  }

  /** Advances the APU by one T-cycle. */
  public void tick() {
    tickFrameSequencer();
    if (powered) {
      square1.tick();
      square2.tick();
      wave.tick();
      noise.tick();
    }
    if (++tcycle == 4) {
      tcycle = 0;
      writeSample();
    }
  }

  private void tickFrameSequencer() {
    int mask = doubleSpeed.getAsBoolean() ? 1 << 13 : 1 << 12;
    boolean bit = (counter.getAsInt() & mask) != 0;
    if (frameSequencerBit && !bit && powered) {
      if ((frameSequencerStep & 1) == 0) {
        square1.clockLength();
        square2.clockLength();
        wave.clockLength();
        noise.clockLength();
      }
      if (frameSequencerStep == 2 || frameSequencerStep == 6) {
        square1.clockSweep();
      }
      if (frameSequencerStep == 7) {
        square1.clockEnvelope();
        square2.clockEnvelope();
        noise.clockEnvelope();
      }
      frameSequencerStep = (frameSequencerStep + 1) & 0x07;
    }
    frameSequencerBit = bit;
  }

  private boolean isDmg() {
    return model == Model.DMG;
  }

  private boolean nextStepClocksLength() {
    return (frameSequencerStep & 1) == 0;
  }

  private void writeSample() {
    double[] dac = {
      dac(square1.isDacOn(), square1.output()),
      dac(square2.isDacOn(), square2.output()),
      dac(wave.isDacOn(), wave.output()),
      dac(noise.isDacOn(), noise.output()),
    };
    double left = 0;
    double right = 0;
    for (int i = 0; i < 4; i++) {
      if ((nr51Value & (0x10 << i)) != 0) {
        left += dac[i];
      }
      if ((nr51Value & (0x01 << i)) != 0) {
        right += dac[i];
      }
    }
    // Each side is at most 4 channels * 8 volume steps; scale to [-1, 1].
    left *= (((nr50Value >>> 4) & 0x07) + 1) / 32.0;
    right *= ((nr50Value & 0x07) + 1) / 32.0;
    double leftOut = left - leftCapacitor;
    leftCapacitor = left - leftOut * HIGH_PASS_CHARGE;
    double rightOut = right - rightCapacitor;
    rightCapacitor = right - rightOut * HIGH_PASS_CHARGE;
    audio.writeAudioSample((float) leftOut, (float) rightOut);
    if (++samples == SAMPLES_PER_PRESENT) {
      samples = 0;
      audio.present();
    }
  }

  /** A channel's DAC turns its 0-15 output into a level from 1 to -1, or 0 when it's off. */
  private static double dac(boolean on, int output) {
    return on ? 1 - output / 7.5 : 0;
  }

  private void powerOff() {
    square1.powerOff(isDmg());
    square2.powerOff(isDmg());
    wave.powerOff(isDmg());
    noise.powerOff(isDmg());
    nr50Value = 0;
    nr51Value = 0;
  }

  private void powerOn() {
    frameSequencerStep = 0;
    square1.powerOn();
    square2.powerOn();
    wave.powerOn();
  }

  /** A sound register: while the APU is off, it only takes length writes (on DMG). */
  private IoRegister register(String name, int address, SoundRegister register) {
    return new IoRegister() {
      @Override
      public int get() {
        return register.get();
      }

      @Override
      public void set(int value) {
        int v = Byte.toUnsignedInt((byte) value);
        if (powered) {
          register.set(v);
        } else {
          if (isDmg()) {
            register.setLength(v);
          }
        }
      }

      @Override
      public String toString() {
        return "%s @ %04x = %02x".formatted(name, address, get());
      }
    };
  }

  /** NR50: the left and right master volume. */
  private SoundRegister masterVolume() {
    return new SoundRegister() {
      @Override
      public int get() {
        return nr50Value;
      }

      @Override
      public void set(int value) {
        nr50Value = value;
      }
    };
  }

  /** NR51: which channels go to the left and right outputs. */
  private SoundRegister panning() {
    return new SoundRegister() {
      @Override
      public int get() {
        return nr51Value;
      }

      @Override
      public void set(int value) {
        nr51Value = value;
      }
    };
  }

  private static IoRegister pcm(String name, int address, IntSupplier low, IntSupplier high) {
    return new IoRegister() {
      @Override
      public int get() {
        return (high.getAsInt() << 4) | low.getAsInt();
      }

      @Override
      public void set(int value) {}

      @Override
      public String toString() {
        return "%s @ %04x = %02x".formatted(name, address, get());
      }
    };
  }

  /** Wave RAM can be accessed whether or not the APU is on. */
  private IoRegister waveRam(int index) {
    return new IoRegister() {
      @Override
      public int get() {
        return wave.readWaveRam(index);
      }

      @Override
      public void set(int value) {
        wave.writeWaveRam(index, Byte.toUnsignedInt((byte) value));
      }

      @Override
      public String toString() {
        return "wave%x @ %04x = %02x".formatted(index, 0xff30 + index, get());
      }
    };
  }

  /** NR52: bit 7 turns the APU on or off; bits 0-3 report which channels are on. */
  private IoRegister soundOnOff() {
    return new IoRegister() {
      @Override
      public int get() {
        return (powered ? 0x80 : 0x00)
            | (square1.isEnabled() ? 0x01 : 0x00)
            | (square2.isEnabled() ? 0x02 : 0x00)
            | (wave.isEnabled() ? 0x04 : 0x00)
            | (noise.isEnabled() ? 0x08 : 0x00);
      }

      @Override
      public void set(int value) {
        boolean on = (value & 0x80) != 0;
        if (powered && !on) {
          powerOff();
        } else if (!powered && on) {
          powerOn();
        }
        powered = on;
      }

      @Override
      public String toString() {
        return "nr52 @ ff26 = %02x".formatted(get());
      }
    };
  }
}
