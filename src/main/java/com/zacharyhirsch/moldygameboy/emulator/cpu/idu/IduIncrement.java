package com.zacharyhirsch.moldygameboy.emulator.cpu.idu;

import com.zacharyhirsch.moldygameboy.emulator.arch.UInt16Output;
import com.zacharyhirsch.moldygameboy.emulator.bus.AddressBus;

public final class IduIncrement implements IduOperation {

  private final UInt16Output output;

  public IduIncrement(UInt16Output output) {
    this.output = output;
  }

  @Override
  public void execute(AddressBus addressBus) {
    output.set((short) (addressBus.get() + 1));
  }
}
