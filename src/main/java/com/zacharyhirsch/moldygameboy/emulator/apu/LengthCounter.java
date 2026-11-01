package com.zacharyhirsch.moldygameboy.emulator.apu;

/** A channel's length counter, which turns the channel off once it counts down to 0. */
final class LengthCounter {

  private final int max;

  private int counter = 0;
  private boolean enabled = false;

  LengthCounter(int max) {
    this.max = max;
  }

  /** Loads the counter from the length bits of NRx1. */
  void load(int length) {
    counter = max - length;
  }

  boolean isEnabled() {
    return enabled;
  }

  /** Clocked by the frame sequencer. Returns whether the counter just ran out. */
  boolean clock() {
    return enabled && counter > 0 && --counter == 0;
  }

  /**
   * Handles a write to NRx4. Returns whether the counter ran out, which turns the channel off.
   *
   * <p>If the next frame sequencer step won't clock the length counter, enabling it clocks it once
   * straight away, and a trigger that reloads it to the maximum also takes one off (see blargg
   * dmg_sound 03-trigger).
   */
  boolean write(int nrx4, boolean nextStepClocksLength) {
    boolean wasEnabled = enabled;
    enabled = (nrx4 & 0x40) != 0;
    boolean ranOut = false;
    if (!wasEnabled && enabled && !nextStepClocksLength && counter > 0) {
      ranOut = --counter == 0;
    }
    if ((nrx4 & 0x80) != 0 && counter == 0) {
      counter = max;
      if (enabled && !nextStepClocksLength) {
        counter--;
      }
      ranOut = false;
    }
    return ranOut;
  }

  /** On DMG, powering the APU off leaves the counter alone. */
  void powerOff() {
    enabled = false;
  }
}
