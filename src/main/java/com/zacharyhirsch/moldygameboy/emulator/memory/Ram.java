package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.io.IOException;

/** A cartridge's RAM, which a battery may keep (and which then gets saved). */
interface Ram {

  int size();

  byte read(int offset);

  void write(int offset, byte value);

  /**
   * Writes the RAM to {@code sink} if a battery keeps it and it has changed since it was last
   * saved; otherwise does nothing.
   */
  void save(SaveSink sink) throws IOException;

  /** Restores the RAM from a save file, if a battery keeps it; otherwise does nothing. */
  void load(byte[] save);
}
