package com.zacharyhirsch.moldygameboy.emulator.memory;

import com.zacharyhirsch.moldygameboy.emulator.bus.AddressBus;
import com.zacharyhirsch.moldygameboy.emulator.bus.DataBus;

public record MemRead(short address) implements MemOperation {

  @Override
  public void execute(Memory memory, AddressBus addressBus, DataBus dataBus) {
    addressBus.set(address);
    dataBus.set(memory.read(Short.toUnsignedInt(address)));
  }
}
