package com.zacharyhirsch.moldygameboy.emulator.cpu.mem;

import com.zacharyhirsch.moldygameboy.emulator.arch.UInt16Input;
import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8Input;
import com.zacharyhirsch.moldygameboy.emulator.bus.AddressBus;
import com.zacharyhirsch.moldygameboy.emulator.bus.DataBus;
import com.zacharyhirsch.moldygameboy.emulator.memory.CpuMemory;

public class MemWrite implements MemOperation {

  private final short address;
  private final byte data;

  public MemWrite(UInt16Input address, UInt8Input data) {
    this.address = address.get();
    this.data = data.get();
  }

  public MemWrite(byte adh, byte adl, UInt8Input data) {
    this(UInt16Input.of(adh, adl), data);
  }

  @Override
  public void execute(CpuMemory memory, AddressBus addressBus, DataBus dataBus) {
    addressBus.set(address);
    dataBus.set(data);
    memory.write();
  }
}
