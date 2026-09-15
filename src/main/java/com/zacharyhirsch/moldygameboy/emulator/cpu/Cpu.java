package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.arch.InterruptRequestLine;
import com.zacharyhirsch.moldygameboy.emulator.arch.InterruptType;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.AbstractInstruction5;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.Instruction;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.Mem;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.Nop;
import com.zacharyhirsch.moldygameboy.emulator.cpu.memory.CpuBus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.BootRom;
import com.zacharyhirsch.moldygameboy.emulator.memory.Hram;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.Oam;
import com.zacharyhirsch.moldygameboy.emulator.memory.Ram;
import com.zacharyhirsch.moldygameboy.emulator.memory.Rom;
import com.zacharyhirsch.moldygameboy.emulator.memory.Vram;
import com.zacharyhirsch.moldygameboy.emulator.memory.Wram;

public final class Cpu {

  private static final InterruptType[] INTERRUPTS = {
    InterruptType.VBLANK,
    InterruptType.LCD,
    InterruptType.TIMER,
    InterruptType.SERIAL,
    InterruptType.JOYPAD,
  };

  private final CpuBus bus;
  private final IoRegisters ioregs;
  private final Registers registers;
  private final InterruptRequestLine vblank;
  private final InterruptRequestLine lcd;
  private final InterruptRequestLine timer;
  private final InterruptRequestLine serial;
  private final InterruptRequestLine joypad;
  private final CpuDecoder decoder;

  private Instruction instruction;
  private boolean halted = false;

  public Cpu(
      BootRom boot,
      Rom rom,
      Vram vram,
      Ram ram,
      Wram wram,
      Oam oam,
      Hram hram,
      IoRegisters ioregs,
      Registers registers,
      InterruptRequestLine vblank,
      InterruptRequestLine lcd,
      InterruptRequestLine timer,
      InterruptRequestLine serial,
      InterruptRequestLine joypad) {
    this.bus = new CpuBus(boot, rom, vram, ram, wram, oam, hram, ioregs);
    this.ioregs = ioregs;
    this.registers = registers;
    this.vblank = vblank;
    this.lcd = lcd;
    this.timer = timer;
    this.serial = serial;
    this.joypad = joypad;
    this.decoder = new CpuDecoder(registers);
    this.instruction = new Nop(registers);
  }

  public void tick() {
    ioregs.dma.tick(bus::readDirect, bus::writeDirect);
    if (vblank.get()) {
      ioregs.if_.set(ioregs.if_.get() | InterruptType.VBLANK.mask());
    }
    if (lcd.get()) {
      ioregs.if_.set(ioregs.if_.get() | InterruptType.LCD.mask());
    }
    if (timer.get()) {
      ioregs.if_.set(ioregs.if_.get() | InterruptType.TIMER.mask());
    }
    if (serial.get()) {
      ioregs.if_.set(ioregs.if_.get() | InterruptType.SERIAL.mask());
    }
    if (joypad.get()) {
      ioregs.if_.set(ioregs.if_.get() | InterruptType.JOYPAD.mask());
    }
    halted = halted && getPendingInterrupts() == 0;
    if (halted) {
      return;
    }
    Mem mem = instruction.tick();
    if (mem == null) {
      instruction = next();
      mem = instruction.tick();
    }
    mem.execute(bus);
  }

  private Instruction next() {
    if (registers.ime().read() != 0 && getPendingInterrupts() != 0) {
      return new Isr(registers);
    }
    return decoder.decode();
  }

  private int getPendingInterrupts() {
    return bus.getEnabledInterrupts() & ioregs.if_.get();
  }

  private boolean isInterruptPending(InterruptType interrupt) {
    return (getPendingInterrupts() & interrupt.mask()) != 0;
  }

  private final class Isr extends AbstractInstruction5 {

    private final Registers registers;

    Isr(Registers registers) {
      this.registers = registers;
    }

    @Override
    protected Mem execute0() {
      return Mem.none(registers.pc().getAndDecrement());
    }

    @Override
    protected Mem execute1() {
      return Mem.none(registers.sp().getAndDecrement());
    }

    @Override
    protected Mem execute2() {
      return Mem.write(registers.sp().getAndDecrement(), registers.pc().hi()::read);
    }

    @Override
    protected Mem execute3() {
      return Mem.write(registers.sp().read(), registers.pc().lo()::read);
    }

    @Override
    protected Mem execute4() {
      for (InterruptType interrupt : INTERRUPTS) {
        if (!isInterruptPending(interrupt)) {
          continue;
        }
        registers.ime().write((byte) 0);
        ioregs.if_.set(ioregs.if_.get() | ~interrupt.mask());
        registers.pc().write(interrupt.vector());
        break;
      }
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::write);
    }
  }
}
