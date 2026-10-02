package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.arch.InterruptType;
import com.zacharyhirsch.moldygameboy.emulator.arch.Irqs;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.FetchOpcode;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.MCycle;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.BootRom;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.MainMemoryController;

public final class Cpu {

  private static final InterruptType[] INTERRUPTS = {
    InterruptType.VBLANK,
    InterruptType.LCD,
    InterruptType.TIMER,
    InterruptType.SERIAL,
    InterruptType.JOYPAD,
  };

  private enum TCycle {
    T1,
    T2,
    T3,
    T4;
  }

  private final CpuMemoryController memory;
  private final Registers registers;
  private final Irqs irqs;

  private MCycle mcycle = new FetchOpcode();
  private TCycle tcycle = TCycle.T1;
  private boolean halted = false;

  public Cpu(
      MainMemoryController memory,
      BootRom boot,
      IoRegisters ioregs,
      Registers registers,
      Irqs irqs) {
    this.memory = new CpuMemoryController(memory, boot, ioregs);
    this.registers = registers;
    this.irqs = irqs;
  }

  public void tick(Bus bus) {
    switch (tcycle) {
      case T1 -> {
        mcycle.t1(registers, bus);
        tcycle = TCycle.T2;
      }
      case T2 -> {
        mcycle.t2(registers, bus);
        tcycle = TCycle.T3;
      }
      case T3 -> {
        memory.tick(bus);
        mcycle.t3(registers, bus);
        tcycle = TCycle.T4;
      }
      case T4 -> {
        mcycle = mcycle.t4(registers, bus);
        tcycle = TCycle.T1;
      }
    }
    //    if (vblank.get()) {
    //      ioregs.if_.set(ioregs.if_.get() | InterruptType.VBLANK.mask());
    //    }
    //    if (lcd.get()) {
    //      ioregs.if_.set(ioregs.if_.get() | InterruptType.LCD.mask());
    //    }
    //    if (timer.get()) {
    //      ioregs.if_.set(ioregs.if_.get() | InterruptType.TIMER.mask());
    //    }
    //    if (serial.get()) {
    //      ioregs.if_.set(ioregs.if_.get() | InterruptType.SERIAL.mask());
    //    }
    //    if (joypad.get()) {
    //      ioregs.if_.set(ioregs.if_.get() | InterruptType.JOYPAD.mask());
    //    }
    //    halted = halted && getPendingInterrupts() == 0;
    //    if (halted) {
    //      return;
    //    }
    //    Mem mem = instruction.tick();
    //    if (mem == null) {
    //      instruction = next();
    //      mem = instruction.tick();
    //    }
    //    ioregs.dma.prepare(bus::readDirect);
    //    mem.prepare(bus);
    //    ioregs.dma.commit(bus::writeDirect);
    //    mem.commit(bus);
  }

  //  private int getPendingInterrupts() {
  //    return bus.getEnabledInterrupts() & ioregs.if_.get();
  //  }
  //
  //  private boolean isInterruptPending(InterruptType interrupt) {
  //    return (getPendingInterrupts() & interrupt.mask()) != 0;
  //  }
  //
  //  private final class Isr extends AbstractInstruction5 {
  //
  //    private final Registers registers;
  //
  //    Isr(Registers registers) {
  //      this.registers = registers;
  //    }
  //
  //    @Override
  //    protected Mem execute0() {
  //      return Mem.none(registers.pc().getAndDecrement());
  //    }
  //
  //    @Override
  //    protected Mem execute1() {
  //      return Mem.none(registers.sp().getAndDecrement());
  //    }
  //
  //    @Override
  //    protected Mem execute2() {
  //      return Mem.write(registers.sp().getAndDecrement(), registers.pc().hi()::read);
  //    }
  //
  //    @Override
  //    protected Mem execute3() {
  //      return Mem.write(registers.sp().read(), registers.pc().lo()::read);
  //    }
  //
  //    @Override
  //    protected Mem execute4() {
  //      for (InterruptType interrupt : INTERRUPTS) {
  //        if (!isInterruptPending(interrupt)) {
  //          continue;
  //        }
  //        registers.ime().write((byte) 0);
  //        ioregs.if_.set(ioregs.if_.get() | ~interrupt.mask());
  //        registers.pc().write(interrupt.vector());
  //        break;
  //      }
  //      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::write);
  //    }
  //  }
}
