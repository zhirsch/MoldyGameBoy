package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.arch.InterruptRequestLine;
import com.zacharyhirsch.moldygameboy.emulator.arch.InterruptType;
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
  private final InterruptRequestLine vblank;
  private final InterruptRequestLine lcd;
  private final InterruptRequestLine timer;
  private final InterruptRequestLine serial;
  private final InterruptRequestLine joypad;
  private final CpuDecoder decoder;

  private Instruction instruction;
  private boolean halted = false;

  public Cpu(
      Registers registers,
      Memory memory,
      InterruptRequestLine vblank,
      InterruptRequestLine lcd,
      InterruptRequestLine timer,
      InterruptRequestLine serial,
      InterruptRequestLine joypad) {
    this.registers = registers;
    this.memory = memory;
    this.vblank = vblank;
    this.lcd = lcd;
    this.timer = timer;
    this.serial = serial;
    this.joypad = joypad;
    this.decoder = new CpuDecoder(registers);
    this.instruction = new Nop(registers);
  }

  public void tick() {
    if (vblank.get()) {
      memory.registers().if_().request(InterruptType.VBLANK);
    }
    if (lcd.get()) {
      memory.registers().if_().request(InterruptType.LCD);
    }
    if (timer.get()) {
      memory.registers().if_().request(InterruptType.TIMER);
    }
    if (serial.get()) {
      memory.registers().if_().request(InterruptType.SERIAL);
    }
    if (joypad.get()) {
      memory.registers().if_().request(InterruptType.JOYPAD);
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
    mem.execute(memory);
  }

  private Instruction next() {
    //    System.out.printf("%04x  %02x\n", registers.pc().get(), registers.ir().get());
    if (registers.ime().read() != 0 && getPendingInterrupts() != 0) {
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
        if (interrupt != InterruptType.LCD) {
          IO.println("Interrupt: " + interrupt);
        }
        registers.ime().write((byte) 0);
        memory.registers().if_().clear(interrupt);
        registers.pc().write(interrupt.vector());
        break;
      }
      return Mem.read(registers.pc().getAndIncrement(), registers.ir()::write);
    }
  }
}
