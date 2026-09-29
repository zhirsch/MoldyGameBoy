package com.zacharyhirsch.moldygameboy.emulator;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.arch.Irqs;
import com.zacharyhirsch.moldygameboy.emulator.cpu.Cpu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.memory.BootRom;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.MainMemoryController;
import com.zacharyhirsch.moldygameboy.emulator.memory.Rom;
import com.zacharyhirsch.moldygameboy.emulator.memory.Vram;
import com.zacharyhirsch.moldygameboy.emulator.ppu.Ppu;
import com.zacharyhirsch.moldygameboy.emulator.timer.Timer;

final class MoldyGameBoy {

  private final Timer timer;
  private final Cpu cpu;
  private final Ppu ppu;
  private final Bus bus;

  public MoldyGameBoy(BootRom boot, Rom rom, Registers registers, Io io) {
    Vram vram = new Vram();
    IoRegisters ioregs = new IoRegisters();
    MainMemoryController memory = new MainMemoryController(rom, vram, ioregs.dma);
    Irqs irqs = new Irqs();

    this.timer = new Timer(ioregs, irqs.timer());
    this.cpu = new Cpu(memory, boot, ioregs, registers, irqs);
    this.ppu = new Ppu(ioregs, vram, io.video(), irqs.vblank(), irqs.lcd());
    this.bus = new Bus();
  }

  public void tick() {
    ppu.tick();
    cpu.tick(bus);
    timer.tick();
  }
}
