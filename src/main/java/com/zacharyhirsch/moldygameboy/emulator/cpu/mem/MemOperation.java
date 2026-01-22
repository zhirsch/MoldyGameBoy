package com.zacharyhirsch.moldygameboy.emulator.cpu.mem;

import com.zacharyhirsch.moldygameboy.emulator.bus.AddressBus;
import com.zacharyhirsch.moldygameboy.emulator.bus.DataBus;
import com.zacharyhirsch.moldygameboy.emulator.memory.CpuMemory;

public interface MemOperation {

  void execute(CpuMemory memory, AddressBus addressBus, DataBus dataBus);
}
