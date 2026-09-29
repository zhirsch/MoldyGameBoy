package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public interface MCycle {

  void t1(Registers registers, Bus bus);

  void t2(Registers registers, Bus bus);

  void t3(Registers registers, Bus bus);

  MCycle t4(Registers registers, Bus bus);
}
