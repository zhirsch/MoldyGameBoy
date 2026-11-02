package com.zacharyhirsch.moldygameboy.emulator;

import static com.google.common.truth.Truth.assertThat;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

final class BootRomsTest {

  @Test
  void cgbCartridgesUseTheCgbBootRom() {
    assertThat(BootRoms.forCartridge(rom(0xc0))).isEqualTo("cgb.bin"); // CGB only
    assertThat(BootRoms.forCartridge(rom(0x80))).isEqualTo("cgb.bin"); // CGB and DMG
  }

  @Test
  void dmgCartridgesUseTheDmgBootRom() {
    assertThat(BootRoms.forCartridge(rom(0x00))).isEqualTo("dmg.bin");
  }

  private static ByteBuffer rom(int cgbFlag) {
    ByteBuffer rom = ByteBuffer.allocate(0x8000);
    rom.put(0x0143, (byte) cgbFlag);
    return rom;
  }
}
