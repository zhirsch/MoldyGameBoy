package com.zacharyhirsch.moldygameboy.emulator.arch;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

final class UInt16Test {

  @Test
  void testAdd() {
    UInt16 value = new UInt16(0);

    assertThat(value).isEqualTo(new UInt16(0));
    assertThat(value.add(1)).isEqualTo(new UInt16(1));
    assertThat(value.add(0x7fff)).isEqualTo(new UInt16(0x7fff));
    assertThat(value.add(0x7fff).add(1)).isEqualTo(new UInt16(0x8000));
    assertThat(value.add(0xffff)).isEqualTo(new UInt16(0xffff));
    assertThat(value.add(0xffff).add(1)).isEqualTo(new UInt16(0));
    assertThat(value.add(0xffff).add(0xffff)).isEqualTo(new UInt16(0xfffe));
  }

  @Test
  void testHiLo() {
    UInt8 hi = new UInt8(0xfe);
    UInt8 lo = new UInt8(0xdc);
    UInt16 value = new UInt16(hi, lo);

    assertThat(value).isEqualTo(new UInt16(0xfedc));
    assertThat(value.hi()).isEqualTo(hi);
    assertThat(value.lo()).isEqualTo(lo);
  }
}
