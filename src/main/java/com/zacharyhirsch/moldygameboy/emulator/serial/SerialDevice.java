package com.zacharyhirsch.moldygameboy.emulator.serial;

/** Whatever is on the other end of the link cable. */
public interface SerialDevice {

  /** Nothing connected: the data line is pulled up, so every received bit is 1. */
  SerialDevice DISCONNECTED = bit -> 1;

  /**
   * Called on each clock edge of a transfer using the internal clock, with the bit shifted out of
   * SB (most significant first). Returns the bit shifted into SB.
   */
  int exchange(int bit);
}
