package com.zacharyhirsch.moldygameboy.emulator.cpu.alu;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

class AluTest {

  @Test
  void testAdc() {
    assertThat(Alu.adc((byte) 0, (byte) 0, false))
        .isEqualTo(new Alu.Result((byte) 0, true, false, false, false));
    assertThat(Alu.adc((byte) 0x0f, (byte) 0, false))
        .isEqualTo(new Alu.Result((byte) 0x0f, false, false, false, false));
    assertThat(Alu.adc((byte) 0x0f, (byte) 1, false))
        .isEqualTo(new Alu.Result((byte) 0x10, false, false, true, false));
    assertThat(Alu.adc((byte) 0x0f, (byte) 0, true))
        .isEqualTo(new Alu.Result((byte) 0x10, false, false, true, false));
    assertThat(Alu.adc((byte) 0x0e, (byte) 1, true))
        .isEqualTo(new Alu.Result((byte) 0x10, false, false, true, false));
    assertThat(Alu.adc((byte) 1, (byte) 0x0f, false))
        .isEqualTo(new Alu.Result((byte) 0x10, false, false, true, false));
    assertThat(Alu.adc((byte) 0xff, (byte) 1, false))
        .isEqualTo(new Alu.Result((byte) 0, true, false, true, true));
    assertThat(Alu.adc((byte) 0xff, (byte) 0, true))
        .isEqualTo(new Alu.Result((byte) 0, true, false, true, true));
    assertThat(Alu.adc((byte) 0xfe, (byte) 1, true))
        .isEqualTo(new Alu.Result((byte) 0, true, false, true, true));
    assertThat(Alu.adc((byte) 0xff, (byte) 0xff, true))
        .isEqualTo(new Alu.Result((byte) 0xff, false, false, true, true));
  }

  @Test
  void testAnd() {
    assertThat(Alu.and((byte) 0, (byte) 0))
        .isEqualTo(new Alu.Result((byte) 0, true, false, true, false));
    assertThat(Alu.and((byte) 1, (byte) 0))
        .isEqualTo(new Alu.Result((byte) 0, true, false, true, false));
    assertThat(Alu.and((byte) 0b1111_0000, (byte) 0b0000_1111))
        .isEqualTo(new Alu.Result((byte) 0, true, false, true, false));
    assertThat(Alu.and((byte) 0b1111_1000, (byte) 0b0001_1111))
        .isEqualTo(new Alu.Result((byte) 0b0001_1000, false, false, true, false));
  }

  @Test
  void testCcf() {
    assertThat(Alu.ccf(false))
        .isEqualTo(new Alu.Result((byte) 1, false, false, false, true));
    assertThat(Alu.ccf(true))
        .isEqualTo(new Alu.Result((byte) 0, true, false, false, false));
  }
}
