package com.zacharyhirsch.moldygameboy.emulator;

import com.zacharyhirsch.moldygameboy.emulator.arch.Irqs;
import com.zacharyhirsch.moldygameboy.emulator.cpu.Cpu;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import com.zacharyhirsch.moldygameboy.emulator.ppu.Ppu;
import com.zacharyhirsch.moldygameboy.emulator.timer.Timer;
import java.nio.ByteBuffer;

final class MoldyGameBoy {

  private final Timer timer;
  private final Cpu cpu;
  private final Ppu ppu;

  public MoldyGameBoy(ByteBuffer boot, ByteBuffer rom, Io io) {
    ByteBuffer vram = ByteBuffer.allocate(0x2000);
    IoRegisters ioregs = new IoRegisters();
    Irqs irqs = new Irqs(ioregs.if_);

    this.timer = new Timer(ioregs, irqs.timer());
    this.cpu = new Cpu(boot, rom, vram, ioregs);
    this.ppu = new Ppu(ioregs, vram, io.video(), irqs.vblank(), irqs.lcd());
  }

  public void tick() {
    ppu.tick();
    cpu.tick();
    timer.tick();
  }

  public Cpu cpu() {
    return cpu;
  }
}
