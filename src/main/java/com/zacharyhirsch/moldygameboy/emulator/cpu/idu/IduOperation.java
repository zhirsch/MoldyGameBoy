package com.zacharyhirsch.moldygameboy.emulator.cpu.idu;

import com.zacharyhirsch.moldygameboy.emulator.bus.AddressBus;

public interface IduOperation {

  void execute(AddressBus addressBus);
}
