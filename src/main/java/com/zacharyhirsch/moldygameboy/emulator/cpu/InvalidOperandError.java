package com.zacharyhirsch.moldygameboy.emulator.cpu;

final class InvalidOperandError extends RuntimeException {

  InvalidOperandError(byte opcode, CpuDecoder.R8 arg) {
    super("Invalid operand for opcode %02x: %s".formatted(opcode, arg));
  }

  InvalidOperandError(byte opcode, CpuDecoder.R16 arg) {
    super("Invalid operand for opcode %02x: %s".formatted(opcode, arg));
  }
}
