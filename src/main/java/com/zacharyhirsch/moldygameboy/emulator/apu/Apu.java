package com.zacharyhirsch.moldygameboy.emulator.apu;

import com.zacharyhirsch.moldygameboy.emulator.arch.IoRegister;
import java.util.function.IntConsumer;

/**
 * The APU, which owns the sound registers ($FF10-$FF26) and wave RAM ($FF30-$FF3F).
 *
 * <p>No sound is generated yet, so NR52 only tracks which channels are on: a channel turns on when
 * it's triggered with its DAC on, and off when its DAC is turned off. Length timers aren't
 * modelled.
 */
public final class Apu {

  private final IoRegister nr10 = IoRegister.create("nr10", (short) 0xff10);
  private final IoRegister nr11 = IoRegister.create("nr11", (short) 0xff11);
  private final IoRegister nr12 = envelope("nr12", (short) 0xff12, 0);
  private final IoRegister nr13 = IoRegister.create("nr13", (short) 0xff13);
  private final IoRegister nr14 = control("nr14", (short) 0xff14, 0);

  private final IoRegister nr21 = IoRegister.create("nr21", (short) 0xff16);
  private final IoRegister nr22 = envelope("nr22", (short) 0xff17, 1);
  private final IoRegister nr23 = IoRegister.create("nr23", (short) 0xff18);
  private final IoRegister nr24 = control("nr24", (short) 0xff19, 1);

  private final IoRegister nr30 = dacEnable("nr30", (short) 0xff1a, 2);
  private final IoRegister nr31 = IoRegister.create("nr31", (short) 0xff1b);
  private final IoRegister nr32 = IoRegister.create("nr32", (short) 0xff1c);
  private final IoRegister nr33 = IoRegister.create("nr33", (short) 0xff1d);
  private final IoRegister nr34 = control("nr34", (short) 0xff1e, 2);

  private final IoRegister nr41 = IoRegister.create("nr41", (short) 0xff20);
  private final IoRegister nr42 = envelope("nr42", (short) 0xff21, 3);
  private final IoRegister nr43 = IoRegister.create("nr43", (short) 0xff22);
  private final IoRegister nr44 = control("nr44", (short) 0xff23, 3);

  private final IoRegister nr50 = IoRegister.create("nr50", (short) 0xff24);
  private final IoRegister nr51 = IoRegister.create("nr51", (short) 0xff25);
  private final IoRegister nr52 = soundOnOff();

  private final IoRegister wave0 = IoRegister.create("wave0", (short) 0xff30);
  private final IoRegister wave1 = IoRegister.create("wave1", (short) 0xff31);
  private final IoRegister wave2 = IoRegister.create("wave2", (short) 0xff32);
  private final IoRegister wave3 = IoRegister.create("wave3", (short) 0xff33);
  private final IoRegister wave4 = IoRegister.create("wave4", (short) 0xff34);
  private final IoRegister wave5 = IoRegister.create("wave5", (short) 0xff35);
  private final IoRegister wave6 = IoRegister.create("wave6", (short) 0xff36);
  private final IoRegister wave7 = IoRegister.create("wave7", (short) 0xff37);
  private final IoRegister wave8 = IoRegister.create("wave8", (short) 0xff38);
  private final IoRegister wave9 = IoRegister.create("wave9", (short) 0xff39);
  private final IoRegister wavea = IoRegister.create("wavea", (short) 0xff3a);
  private final IoRegister waveb = IoRegister.create("waveb", (short) 0xff3b);
  private final IoRegister wavec = IoRegister.create("wavec", (short) 0xff3c);
  private final IoRegister waved = IoRegister.create("waved", (short) 0xff3d);
  private final IoRegister wavee = IoRegister.create("wavee", (short) 0xff3e);
  private final IoRegister wavef = IoRegister.create("wavef", (short) 0xff3f);

  // Which channels are on (NR52 bits 0-3), and whether the APU is on (NR52 bit 7).
  private int channelsOn = 0;
  private boolean powered = false;

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

  /** A plain register that also runs {@code onWrite} with each value written to it. */
  private static IoRegister register(String name, short address, IntConsumer onWrite) {
    IoRegister register = IoRegister.create(name, address);
    return new IoRegister() {
      @Override
      public int get() {
        return register.get();
      }

      @Override
      public void set(int value) {
        register.set(value);
        onWrite.accept(value);
      }

      @Override
      public String toString() {
        return register.toString();
      }
    };
  }

  /** NRx2 for channels 1, 2 and 4: the upper 5 bits being clear turns the channel's DAC off. */
  private IoRegister envelope(String name, short address, int channel) {
    return register(
        name,
        address,
        value -> {
          if ((value & 0xf8) == 0) {
            channelsOn &= ~(1 << channel);
          }
        });
  }

  /** NR30: bit 7 turns channel 3's DAC on or off. */
  private IoRegister dacEnable(String name, short address, int channel) {
    return register(
        name,
        address,
        value -> {
          if ((value & 0x80) == 0) {
            channelsOn &= ~(1 << channel);
          }
        });
  }

  /** NRx4: writing bit 7 triggers the channel, which turns it on if its DAC is on. */
  private IoRegister control(String name, short address, int channel) {
    return register(
        name,
        address,
        value -> {
          if ((value & 0x80) != 0 && powered && isDacOn(channel)) {
            channelsOn |= 1 << channel;
          }
        });
  }

  /** NR52: bit 7 turns the APU on or off; bits 0-3 report which channels are on. */
  private IoRegister soundOnOff() {
    return new IoRegister() {
      @Override
      public int get() {
        return (powered ? 0x80 : 0x00) | channelsOn;
      }

      @Override
      public void set(int value) {
        powered = (value & 0x80) != 0;
        if (!powered) {
          // Turning the APU off also turns off every channel.
          channelsOn = 0;
        }
      }

      @Override
      public String toString() {
        return "nr52 @ ff26 = %02x".formatted(get());
      }
    };
  }

  private boolean isDacOn(int channel) {
    return switch (channel) {
      case 0 -> (nr12.get() & 0xf8) != 0;
      case 1 -> (nr22.get() & 0xf8) != 0;
      case 2 -> nr30.bit(7);
      case 3 -> (nr42.get() & 0xf8) != 0;
      default -> throw new IllegalArgumentException("channel %d".formatted(channel));
    };
  }
}
