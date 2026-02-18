package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;

public interface Instruction {

  MemOperation tick(byte data);
}
