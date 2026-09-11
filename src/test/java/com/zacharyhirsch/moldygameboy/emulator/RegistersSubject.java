package com.zacharyhirsch.moldygameboy.emulator;

import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.Fact;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import java.util.ArrayList;
import java.util.List;

final class RegistersSubject extends Subject {

  private final Registers actual;

  private RegistersSubject(FailureMetadata metadata, Registers actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  public static RegistersSubject assertThat(Registers registers) {
    return assertAbout(RegistersSubject::new).that(registers);
  }

  public void isMooneyeSuccess() {
    if (actual == null) {
      failWithoutActual(Fact.simpleFact("expected to be non-null"));
      return;
    }

    List<Fact> failures = new ArrayList<>();

    if (actual.b().read() != (byte) 0x03) {
      failures.add(Fact.fact("b", "expected: 03, actual: %02x".formatted(actual.b().read())));
    }
    if (actual.c().read() != (byte) 0x05) {
      failures.add(Fact.fact("c", "expected: 05, actual: %02x".formatted(actual.c().read())));
    }
    if (actual.d().read() != (byte) 0x08) {
      failures.add(Fact.fact("d", "expected: 08, actual: %02x".formatted(actual.d().read())));
    }
    if (actual.e().read() != (byte) 0x0d) {
      failures.add(Fact.fact("e", "expected: 0d, actual: %02x".formatted(actual.e().read())));
    }
    if (actual.h().read() != (byte) 0x15) {
      failures.add(Fact.fact("h", "expected: 15, actual: %02x".formatted(actual.h().read())));
    }
    if (actual.l().read() != (byte) 0x22) {
      failures.add(Fact.fact("l", "expected: 22, actual: %02x".formatted(actual.l().read())));
    }

    if (!failures.isEmpty()) {
      failWithActual(
          Fact.simpleFact("cpu registers are not set to values indicating success"),
          failures.toArray(new Fact[0]));
    }
  }
}
