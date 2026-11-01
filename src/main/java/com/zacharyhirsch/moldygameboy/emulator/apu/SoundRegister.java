package com.zacharyhirsch.moldygameboy.emulator.apu;

/** A sound register, backed by the state of a channel or the APU. */
interface SoundRegister {

  int get();

  /** Writes the register while the APU is on. */
  void set(int value);

  /**
   * Writes the register while the APU is off, which only loads the length counter of an NRx1
   * register (on DMG); other registers ignore it.
   */
  default void setLength(int value) {}
}
