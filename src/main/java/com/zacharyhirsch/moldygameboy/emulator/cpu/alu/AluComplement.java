package com.zacharyhirsch.moldygameboy.emulator.cpu.alu;

import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8Input;
import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8Output;

public final class AluComplement implements AluOperation {

  private final UInt8Input input;
  private final UInt8Output output;

  public AluComplement(UInt8Input input, UInt8Output output) {
    this.input = input;
    this.output = output;
  }

  @Override
  public <T extends UInt8Input & UInt8Output> void execute(T flags) {
    byte result = (byte) (~input.get());
    output.set(result);
    flags.set(AluFlags.apply(flags.get(), null, true, true, null));
  }
}
