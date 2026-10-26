package com.zacharyhirsch.moldygameboy.emulator;

import com.zacharyhirsch.moldygameboy.emulator.arch.IoRegister;
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
    IoRegister if_ = IoRegister.create("if", (short) 0xff0f);
    Irqs irqs = new Irqs(if_);
    this.timer = new Timer(irqs.timer());
    IoRegisters ioregs =
        new IoRegisters(
            IoRegister.wrap("div", (short) 0xff04, timer::getDiv, _ -> timer.resetDiv()),
            IoRegister.wrap("tima", (short) 0xff05, timer::getTima, timer::setTima),
            IoRegister.wrap("tma", (short) 0xff06, timer::getTma, timer::setTma),
            IoRegister.wrap("tac", (short) 0xff07, timer::getTac, timer::setTac),
            if_);

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
