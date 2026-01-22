package com.zacharyhirsch.moldygameboy.emulator.cpu.idu;

import com.zacharyhirsch.moldygameboy.emulator.arch.UInt16Output;
import com.zacharyhirsch.moldygameboy.emulator.bus.AddressBus;

public final class IduDecrement implements IduOperation {

  private final UInt16Output output;

  public IduDecrement(UInt16Output output) {
    this.output = output;
  }

  @Override
  public void execute(AddressBus addressBus) {
    output.set((short) (addressBus.get() - 1));
  }
}
