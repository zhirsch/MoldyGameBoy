package com.zacharyhirsch.moldygameboy.emulator.cpu.alu;

import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8Input;
import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8Output;

public final class AluXor implements AluOperation {

  private final UInt8Input input1;
  private final UInt8Input input2;
  private final UInt8Output output;

  public AluXor(UInt8Input input1, UInt8Input input2, UInt8Output output) {
    this.input1 = input1;
    this.input2 = input2;
    this.output = output;
  }

  @Override
  public <T extends UInt8Input & UInt8Output> void execute(T flags) {
    byte result = (byte) (input1.get() ^ input2.get());
    output.set(result);
    flags.set(AluFlags.apply(flags.get(), result == 0, false, false, false));
  }
}
