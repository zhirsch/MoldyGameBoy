package com.zacharyhirsch.moldygameboy.emulator.apu;

/** The volume envelope of channels 1, 2 and 4, controlled by NRx2. */
final class Envelope {

  private int nrx2 = 0;
  private int volume = 0;
  private int timer = 0;

  int nrx2() {
    return nrx2;
  }

  void setNrx2(int value) {
    nrx2 = value;
  }

  /** The upper 5 bits of NRx2 being clear turns the channel's DAC off. */
  boolean isDacOn() {
    return (nrx2 & 0xf8) != 0;
  }

  int volume() {
    return volume;
  }

  void trigger() {
    volume = nrx2 >>> 4;
    timer = period() == 0 ? 8 : period();
  }

  /** Clocked by the frame sequencer. */
  void clock() {
    if (period() == 0 || --timer > 0) {
      return;
    }
    timer = period();
    if ((nrx2 & 0x08) != 0 && volume < 15) {
      volume++;
    } else if ((nrx2 & 0x08) == 0 && volume > 0) {
      volume--;
    }
  }

  void powerOff() {
    nrx2 = 0;
    volume = 0;
    timer = 0;
  }

  private int period() {
    return nrx2 & 0x07;
  }
}
