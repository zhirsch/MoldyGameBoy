package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.AbstractInstruction5;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.Instruction;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.Mem;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.Nop;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;

public final class Cpu {

  private static final InterruptType[] INTERRUPTS = {
    InterruptType.VBLANK,
    InterruptType.LCD,
    InterruptType.TIMER,
    InterruptType.SERIAL,
    InterruptType.JOYPAD,
  };

  private final Registers registers;
  private final Memory memory;
  private final CpuDecoder decoder;

  private Instruction instruction;
  private boolean halted = false;

  public Cpu(Registers registers, Memory memory) {
    this.registers = registers;
    this.memory = memory;
    this.decoder = new CpuDecoder(registers);
    this.instruction = new Nop(registers);
  }

  public void tick() {
    halted = halted && getPendingInterrupts() == 0;
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
    //    System.out.printf("%04x  %02x\n", registers.pc().get(), registers.ir().get());
    if (registers.ime().get() != 0 && getPendingInterrupts() != 0) {
      return new Isr(registers);
    }
    return decoder.decode();
  }

  private int getPendingInterrupts() {
    return memory.ie() & memory.registers().if_().get();
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
      return Mem.write(registers.sp().getAndDecrement(), registers.pc().hi()::get);
    }

    @Override
    protected Mem execute3() {
      return Mem.write(registers.sp().get(), registers.pc().lo()::get);
    }

    @Override
    protected Mem execute4() {
      for (InterruptType interrupt : INTERRUPTS) {
        if (!isInterruptPending(interrupt)) {
          continue;
        }
        registers.ime().set((byte) 0);
        memory.registers().if_().set((byte) (memory.registers().if_().get() & ~interrupt.mask()));
        registers.pc().set(interrupt.vector());
        break;
      }
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::set);
    }
  }
}
