package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

final class InvalidOpcodeError extends RuntimeException {

  InvalidOpcodeError(Byte... bytes) {
    super(stream(bytes).map("%02x"::formatted).collect(joining(" ", "Unknown opcode: ", "")));
  }
}
