package com.zacharyhirsch.moldygameboy.emulator.cpu.alu;

import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8Input;
import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8Output;

public final class AluDecrement implements AluOperation {

  private final UInt8Input input;
  private final UInt8Output output;

  public AluDecrement(UInt8Input input, UInt8Output output) {
    this.input = input;
    this.output = output;
  }

  @Override
  public <T extends UInt8Input & UInt8Output> void execute(T flags) {
    byte oldValue = input.get();
    byte newValue = (byte) (oldValue - 1);
    output.set(newValue);
    flags.set(AluFlags.apply(flags.get(), newValue == 0, true, (oldValue & 0x0f) == 0, null));
  }
}
