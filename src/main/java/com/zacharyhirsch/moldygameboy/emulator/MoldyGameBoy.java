package com.zacharyhirsch.moldygameboy.emulator;

import com.zacharyhirsch.moldygameboy.emulator.arch.Memory;
import com.zacharyhirsch.moldygameboy.emulator.cpu.Cpu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.timer.Divider;
import com.zacharyhirsch.moldygameboy.emulator.timer.Timer;

final class MoldyGameBoy {

  private final Divider divider;
  private final Timer timer;
  private final Cpu cpu;

  public MoldyGameBoy(Memory memory, Registers registers) {
    this.divider = new Divider(memory);
    this.timer = new Timer(memory);
    this.cpu = new Cpu(registers, memory);
  }

  public void tick() {
    divider.tick();
    timer.tick();
    cpu.tick();
  }
}
