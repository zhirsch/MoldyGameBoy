package com.zacharyhirsch.moldygameboy.emulator;

import java.nio.ByteBuffer;

/** Chooses which boot ROM (and so which model) to run a cartridge with. */
final class BootRoms {

  private BootRoms() {}

  /**
   * The CGB boot ROM for cartridges whose header says they support the CGB (bit 7 of $0143, set
   * both for CGB-only games and for games that also run on a DMG), and the DMG's for the rest.
   */
  static String forCartridge(ByteBuffer rom) {
    return (rom.get(0x0143) & 0x80) != 0 ? "cgb.bin" : "dmg.bin";
  }
}
