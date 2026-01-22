package com.zacharyhirsch.moldygameboy.emulator.cpu;

final class InvalidOpcodeError extends RuntimeException {

  InvalidOpcodeError(byte opcode) {
    super("Unknown opcode: %02x".formatted(opcode));
  }
}
