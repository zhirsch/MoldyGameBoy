package com.zacharyhirsch.moldygameboy.emulator;

import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.CpuRegister8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

final class RegistersSubject extends Subject {

  private final Registers actual;

  private RegistersSubject(FailureMetadata metadata, Registers actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  public static RegistersSubject assertThat(Registers registers) {
    return assertAbout(RegistersSubject::new).that(registers);
  }

  public RegistersSubject hasA(byte expected) {
    check("a").that(actual.a()).isEqualTo(new CpuRegister8(expected));
    return this;
  }

  public RegistersSubject hasF(byte expected) {
    check("f").that(actual.f()).isEqualTo(new CpuRegister8(expected));
    return this;
  }

  public RegistersSubject hasB(byte expected) {
    check("b").that(actual.b()).isEqualTo(new CpuRegister8(expected));
    return this;
  }

  public RegistersSubject hasC(byte expected) {
    check("c").that(actual.c()).isEqualTo(new CpuRegister8(expected));
    return this;
  }

  public RegistersSubject hasD(byte expected) {
    check("d").that(actual.d()).isEqualTo(new CpuRegister8(expected));
    return this;
  }

  public RegistersSubject hasE(byte expected) {
    check("e").that(actual.e()).isEqualTo(new CpuRegister8(expected));
    return this;
  }

  public RegistersSubject hasH(byte expected) {
    check("h").that(actual.h()).isEqualTo(new CpuRegister8(expected));
    return this;
  }

  public RegistersSubject hasL(byte expected) {
    check("l").that(actual.l()).isEqualTo(new CpuRegister8(expected));
    return this;
  }
}
