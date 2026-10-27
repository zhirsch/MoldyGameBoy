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

  private enum TCycle {
    T1,
    T2,
    T3,
    T4,
    ;

    TCycle next() {
      return switch (this) {
        case T1 -> T2;
        case T2 -> T3;
        case T3 -> T4;
        case T4 -> T1;
      };
    }
  }

  private final Cpu cpu;
  private final Ppu ppu;
  private final Timer timer;

  private TCycle tcycle = TCycle.T1;

  public MoldyGameBoy(ByteBuffer boot, ByteBuffer rom, Io io) {
    ByteBuffer vram = ByteBuffer.allocate(0x2000);
    IoRegister if_ = IoRegister.create("if", (short) 0xff0f);
    Irqs irqs = new Irqs(if_);
    this.timer = new Timer(irqs.timer());
    IoRegisters ioregs = new IoRegisters(timer.div(), timer.tima(), timer.tma(), timer.tac(), if_);

    this.cpu = new Cpu(boot, rom, vram, ioregs);
    this.ppu = new Ppu(ioregs, vram, io.video(), irqs.vblank(), irqs.lcd());
  }

  public void tick() {
    switch (tcycle) {
      case T1 -> {
        ppu.tick();
        cpu.tickT1();
        timer.tickT1();
      }
      case T2 -> {
        ppu.tick();
        cpu.tickT2();
        timer.tickT2();
      }
      case T3 -> {
        ppu.tick();
        cpu.tickT3();
        timer.tickT3();
      }
      case T4 -> {
        ppu.tick();
        cpu.tickT4();
        timer.tickT4();
      }
    }
    tcycle = tcycle.next();
  }

  public Cpu cpu() {
    return cpu;
  }
}
