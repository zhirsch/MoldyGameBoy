package com.zacharyhirsch.moldygameboy.emulator;

import com.zacharyhirsch.moldygameboy.emulator.arch.MemoryRange;
import com.zacharyhirsch.moldygameboy.emulator.cpu.Cpu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.IORegisters;
import com.zacharyhirsch.moldygameboy.emulator.timer.Divider;
import com.zacharyhirsch.moldygameboy.emulator.timer.Timer;

final class MoldyGameBoy {

  private final Divider divider;
  private final Timer timer;
  private final Cpu cpu;

  public MoldyGameBoy(MemoryRange memory, Registers registers, IORegisters ioRegisters) {
    this.divider = new Divider(ioRegisters);
    this.timer = new Timer(ioRegisters);
    this.cpu = new Cpu(registers, ioRegisters, memory);
  }

  public void tick() {
    divider.tick();
    timer.tick();
    cpu.tick();
  }
}
