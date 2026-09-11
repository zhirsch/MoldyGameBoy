package com.zacharyhirsch.moldygameboy.emulator;

import com.zacharyhirsch.moldygameboy.emulator.arch.InterruptRequestLine;
import com.zacharyhirsch.moldygameboy.emulator.cpu.Cpu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.memory.BootRom;
import com.zacharyhirsch.moldygameboy.emulator.memory.Hram;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.Oam;
import com.zacharyhirsch.moldygameboy.emulator.memory.Ram;
import com.zacharyhirsch.moldygameboy.emulator.memory.Rom;
import com.zacharyhirsch.moldygameboy.emulator.memory.Vram;
import com.zacharyhirsch.moldygameboy.emulator.memory.Wram;
import com.zacharyhirsch.moldygameboy.emulator.ppu.Ppu;
import com.zacharyhirsch.moldygameboy.emulator.timer.Timer;

final class MoldyGameBoy {

  private final Timer timer;
  private final Cpu cpu;
  private final Ppu ppu;

  public MoldyGameBoy(BootRom boot, Rom rom, Registers registers, Io io) {
    Vram vram = new Vram();
    Ram ram = new Ram();
    Wram wram = new Wram();
    Oam oam = new Oam();
    Hram hram = new Hram();
    IoRegisters ioregisters = new IoRegisters();

    InterruptRequestLine vblank = new InterruptRequestLine();
    InterruptRequestLine lcd = new InterruptRequestLine();
    InterruptRequestLine timer = new InterruptRequestLine();
    InterruptRequestLine serial = new InterruptRequestLine();
    InterruptRequestLine joypad = new InterruptRequestLine();

    this.timer = new Timer(ioregisters, timer);
    this.cpu =
        new Cpu(
            boot,
            rom,
            vram,
            ram,
            wram,
            oam,
            hram,
            ioregisters,
            registers,
            vblank,
            lcd,
            timer,
            serial,
            joypad);
    this.ppu = new Ppu(ioregisters, vram, io.video(), vblank, lcd);

  }

  public void tick() {
    ppu.tick();
    ppu.tick();
    ppu.tick();
    ppu.tick();
    cpu.tick();
    timer.tick();
  }

  //  private static final class UnusedIoRegister implements IoRegister {
  //
  //    @Override
  //    public byte read() {
  //      return (byte) 0xff;
  //    }
  //
  //    @Override
  //    public void write(byte value) {}
  //  }
}
