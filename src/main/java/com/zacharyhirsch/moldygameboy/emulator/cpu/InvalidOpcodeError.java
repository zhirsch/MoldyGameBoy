package com.zacharyhirsch.moldygameboy.emulator.cpu;

final class InvalidOpcodeError extends RuntimeException {

  InvalidOpcodeError(byte opcode) {
    super("Unknown opcode: %02x".formatted(opcode));
  }

  InvalidOpcodeError(byte prefix, byte opcode) {
    super("Unknown opcode: %02x %02x".formatted(prefix, opcode));
  }
}
