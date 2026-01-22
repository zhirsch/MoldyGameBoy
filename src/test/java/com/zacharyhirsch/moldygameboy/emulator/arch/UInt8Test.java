package com.zacharyhirsch.moldygameboy.emulator.arch;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

final class UInt8Test {

  @Test
  void testXor() {

    assertThat(new UInt8(0xfa).xor(new UInt8(0x00))).isEqualTo(new UInt8(0xfa));
    assertThat(new UInt8(0xfa).xor(new UInt8(0xff))).isEqualTo(new UInt8(0x05));
    assertThat(new UInt8(0xfa).xor(new UInt8(0xaa))).isEqualTo(new UInt8(0x50));
  }

  @Test
  void testComplement() {
    assertThat(new UInt8(0x00).complement()).isEqualTo(new UInt8(0xff));
    assertThat(new UInt8(0xff).complement()).isEqualTo(new UInt8(0x00));
    assertThat(new UInt8(0xaa).complement()).isEqualTo(new UInt8(0x55));
  }
}
