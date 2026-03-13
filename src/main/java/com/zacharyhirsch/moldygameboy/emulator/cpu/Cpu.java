package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.arch.MemoryRange;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.AbstractInstruction5;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.Instruction;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.Mem;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.Nop;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.IORegisters;

public final class Cpu {

  private static final InterruptType[] INTERRUPTS = {
    InterruptType.VBLANK,
    InterruptType.LCD,
    InterruptType.TIMER,
    InterruptType.SERIAL,
    InterruptType.JOYPAD,
  };

  private final Registers registers;
  private final IORegisters ioRegisters;
  private final MemoryRange memory;
  private final CpuDecoder decoder;

  private Instruction instruction;
  private boolean halted = false;

  public Cpu(Registers registers, IORegisters ioRegisters, MemoryRange memory) {
    this.registers = registers;
    this.ioRegisters = ioRegisters;
    this.memory = memory;
    this.decoder = new CpuDecoder(registers);
    this.instruction = new Nop(registers);
  }

  public void tick() {
    halted = halted && (ioRegisters.ie().get() & ioRegisters.if_().get()) == 0;
    if (halted) {
      return;
    }
    Mem mem = instruction.tick();
    if (mem == null) {
      instruction = next();
      mem = instruction.tick();
    }
    mem.execute(memory);
  }

  private Instruction next() {
    if (registers.ime().get() != 0 && (ioRegisters.ie().get() & ioRegisters.if_().get()) != 0) {
      return new Isr(registers, ioRegisters);
    }
    return decoder.decode();
  }

  private static final class Isr extends AbstractInstruction5 {

    private final Registers registers;
    private final IORegisters ioRegisters;

    Isr(Registers registers, IORegisters ioRegisters) {
      this.registers = registers;
      this.ioRegisters = ioRegisters;
    }

    @Override
    protected Mem execute0() {
      return Mem.read(registers.pc().getAndDecrement(), _ -> {});
    }

    @Override
    protected Mem execute1() {
      return Mem.read(registers.sp().getAndDecrement(), _ -> {});
    }

    @Override
    protected Mem execute2() {
      return Mem.write(registers.sp().getAndDecrement(), registers.pc().hi()::get);
    }

    @Override
    protected Mem execute3() {
      return Mem.write(registers.sp().get(), registers.pc().lo()::get);
    }

    @Override
    protected Mem execute4() {
      for (InterruptType interrupt : INTERRUPTS) {
        if ((ioRegisters.ie().get() & ioRegisters.if_().get() & interrupt.getMask()) != 0) {
          registers.ime().set((byte) 0);
          ioRegisters.if_().set((byte) (ioRegisters.if_().get() & ~interrupt.getMask()));
          registers.pc().set(interrupt.getVector());
          break;
        }
      }
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }
}
