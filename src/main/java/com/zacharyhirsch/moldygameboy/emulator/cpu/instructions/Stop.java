package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

/**
 * STOP, which is followed by a byte that's skipped. The CPU then either switches speed (on a CGB
 * with KEY1 armed) or stops until a button is pressed; see {@code Cpu}.
 */
public final class Stop implements Instruction {

  @Override
  public MCycle start(Registers registers, Bus bus) {
    registers.pc().increment();
    registers.stopped().set(true);
    return null;
  }
}
