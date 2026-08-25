package com.zacharyhirsch.moldygameboy.emulator;

import com.zacharyhirsch.moldygameboy.emulator.arch.InterruptRequestLine;
import com.zacharyhirsch.moldygameboy.emulator.cpu.Cpu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import com.zacharyhirsch.moldygameboy.emulator.ppu.Ppu;
import com.zacharyhirsch.moldygameboy.emulator.timer.Divider;
import com.zacharyhirsch.moldygameboy.emulator.timer.Timer;

final class MoldyGameBoy {

  private final Divider divider;
  private final Timer timer;
  private final Cpu cpu;
  private final Ppu ppu;

  public MoldyGameBoy(Memory memory, Registers registers, Io io) {
    InterruptRequestLine vblank = new InterruptRequestLine();
    InterruptRequestLine lcd = new InterruptRequestLine();
    InterruptRequestLine timer = new InterruptRequestLine();
    InterruptRequestLine serial = new InterruptRequestLine();
    InterruptRequestLine joypad = new InterruptRequestLine();
    this.divider = new Divider(memory);
    this.timer = new Timer(memory, timer);
    this.cpu = new Cpu(registers, memory, vblank, lcd, timer, serial, joypad);
    this.ppu = new Ppu(memory, io.video(), vblank, lcd);
  }

  public void tick() {
    ppu.tick();
    ppu.tick();
    ppu.tick();
    ppu.tick();
    divider.tick();
    timer.tick();
    cpu.tick();
  }
}
