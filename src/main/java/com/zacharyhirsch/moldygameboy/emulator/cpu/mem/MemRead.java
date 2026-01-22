package com.zacharyhirsch.moldygameboy.emulator.cpu.mem;

import com.zacharyhirsch.moldygameboy.emulator.arch.UInt16Input;
import com.zacharyhirsch.moldygameboy.emulator.bus.AddressBus;
import com.zacharyhirsch.moldygameboy.emulator.bus.DataBus;
import com.zacharyhirsch.moldygameboy.emulator.memory.CpuMemory;

public class MemRead implements MemOperation {

  private final short address;

  public MemRead(UInt16Input address) {
    this.address = address.get();
  }

  @Override
  public void execute(CpuMemory memory, AddressBus addressBus, DataBus dataBus) {
    addressBus.set(address);
    memory.read();
  }
}
