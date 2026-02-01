package com.zacharyhirsch.moldygameboy.emulator.memory;

import com.zacharyhirsch.moldygameboy.emulator.bus.AddressBus;
import com.zacharyhirsch.moldygameboy.emulator.bus.DataBus;

public record MemWrite(short address, byte data) implements MemOperation {

  @Override
  public void execute(Memory memory, AddressBus addressBus, DataBus dataBus) {
    addressBus.set(address);
    dataBus.set(data);
    memory.write();
  }
}
