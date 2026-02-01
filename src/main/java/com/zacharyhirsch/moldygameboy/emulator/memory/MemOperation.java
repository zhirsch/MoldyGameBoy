package com.zacharyhirsch.moldygameboy.emulator.memory;

import com.zacharyhirsch.moldygameboy.emulator.bus.AddressBus;
import com.zacharyhirsch.moldygameboy.emulator.bus.DataBus;

public interface MemOperation {

  void execute(Memory memory, AddressBus addressBus, DataBus dataBus);
}
