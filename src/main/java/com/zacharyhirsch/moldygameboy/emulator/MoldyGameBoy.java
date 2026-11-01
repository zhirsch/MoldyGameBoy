package com.zacharyhirsch.moldygameboy.emulator;

import com.zacharyhirsch.moldygameboy.emulator.apu.Apu;
import com.zacharyhirsch.moldygameboy.emulator.arch.Irqs;
import com.zacharyhirsch.moldygameboy.emulator.cpu.Cpu;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.Mapper;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import com.zacharyhirsch.moldygameboy.emulator.memory.VideoMemoryLock;
import com.zacharyhirsch.moldygameboy.emulator.ppu.Ppu;
import com.zacharyhirsch.moldygameboy.emulator.serial.Serial;
import com.zacharyhirsch.moldygameboy.emulator.serial.SerialDevice;
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

  private final Memory memory;
  private final Timer timer;
  private final Serial serial;
  private final Apu apu;
  private final IoRegisters ioregs;
  private final Cpu cpu;
  private final Ppu ppu;

  private TCycle tcycle = TCycle.T1;

  public MoldyGameBoy(ByteBuffer boot, ByteBuffer rom, Io io) {
    ByteBuffer vram = ByteBuffer.allocate(0x2000);
    ByteBuffer oam = ByteBuffer.allocate(0x00a0);
    this.memory = new Memory(Mapper.create(rom), vram, oam);
    VideoMemoryLock videoMemoryLock = new VideoMemoryLock();
    Irqs irqs = new Irqs();
    this.timer = new Timer(irqs.timer());
    this.serial = new Serial(timer::counter, irqs.serial());
    this.apu = new Apu(timer::counter, io.audio());
    this.ioregs =
        new IoRegisters(
            serial.sb(),
            serial.sc(),
            timer.div(),
            timer.tima(),
            timer.tma(),
            timer.tac(),
            irqs.if_(),
            apu.nr10(),
            apu.nr11(),
            apu.nr12(),
            apu.nr13(),
            apu.nr14(),
            apu.nr21(),
            apu.nr22(),
            apu.nr23(),
            apu.nr24(),
            apu.nr30(),
            apu.nr31(),
            apu.nr32(),
            apu.nr33(),
            apu.nr34(),
            apu.nr41(),
            apu.nr42(),
            apu.nr43(),
            apu.nr44(),
            apu.nr50(),
            apu.nr51(),
            apu.nr52(),
            apu.wave0(),
            apu.wave1(),
            apu.wave2(),
            apu.wave3(),
            apu.wave4(),
            apu.wave5(),
            apu.wave6(),
            apu.wave7(),
            apu.wave8(),
            apu.wave9(),
            apu.wavea(),
            apu.waveb(),
            apu.wavec(),
            apu.waved(),
            apu.wavee(),
            apu.wavef(),
            memory);
    this.cpu = new Cpu(boot, memory, ioregs, videoMemoryLock);
    this.ppu =
        new Ppu(ioregs, vram, oam, videoMemoryLock, io.video(), irqs.vblank(), irqs.lcd());
  }

  public void tick() {
    switch (tcycle) {
      case T1 -> {
        ppu.tick();
        ioregs.dma.tickT1();
        cpu.tickT1();
        timer.tickT1();
        serial.tick();
        apu.tick();
      }
      case T2 -> {
        ppu.tick();
        ioregs.dma.tickT2();
        cpu.tickT2();
        timer.tickT2();
        serial.tick();
        apu.tick();
      }
      case T3 -> {
        ppu.tick();
        ioregs.dma.tickT3();
        cpu.tickT3();
        timer.tickT3();
        serial.tick();
        apu.tick();
      }
      case T4 -> {
        ppu.tick();
        ioregs.dma.tickT4();
        cpu.tickT4();
        timer.tickT4();
        serial.tick();
        apu.tick();
      }
    }
    tcycle = tcycle.next();
  }

  public Cpu cpu() {
    return cpu;
  }

  /** Reads memory as the cartridge and video buses see it, bypassing the CPU. */
  byte read(short address) {
    return memory.read(address);
  }

  /** Plugs a device into the link port. */
  void connectSerial(SerialDevice device) {
    serial.connect(device);
  }
}
