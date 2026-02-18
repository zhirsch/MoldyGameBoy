package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.AbstractInstruction;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.Halt;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.Instruction;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.Nop;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.IORegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemOperation;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemRead;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemWrite;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import java.io.IOException;
import java.io.Writer;

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
  private final Memory memory;
  private final CpuDecoder decoder;
  private final Writer writer;

  private Instruction instruction = null;
  private byte data = 0;
  private boolean halted = false;
  private boolean skipLoggingNextInstruction = false;

  public Cpu(Registers registers, IORegisters ioRegisters, Memory memory, Writer writer) {
    this.registers = registers;
    this.ioRegisters = ioRegisters;
    this.memory = memory;
    this.decoder = new CpuDecoder(registers);
    this.writer = writer;
    this.instruction = new Nop(registers);
    logState();
  }

  public void tick() {
    halted = halted && (ioRegisters.ie().get() & ioRegisters.if_().get()) == 0;
    if (halted) {
      return;
    }
    MemOperation mem = instruction.tick(data);
    if (mem == null) {
      instruction = next();
      mem = instruction.tick(data);
    }
    data = mem.execute(memory);
  }

  private Instruction next() {
    if (!skipLoggingNextInstruction) {
      logState();
    }
    for (InterruptType interrupt : INTERRUPTS) {
      if (isInterruptRequested(interrupt)) {
        skipLoggingNextInstruction = true;
        return new Isr(registers, ioRegisters, interrupt);
      }
    }
    skipLoggingNextInstruction = !decoder.isPrefixed() && registers.ir().get() == (byte) 0xcb;
    Instruction instruction = decoder.decode();
    halted = instruction instanceof Halt;
    return instruction;
  }

  private static final class Isr extends AbstractInstruction {

    private final Registers registers;
    private final IORegisters ioRegisters;
    private final InterruptType interrupt;

    Isr(Registers registers, IORegisters ioRegisters, InterruptType interrupt) {
      this.registers = registers;
      this.ioRegisters = ioRegisters;
      this.interrupt = interrupt;
    }

    @Override
    protected MemOperation execute0(byte data) {
      return new MemRead(registers.pc().getAndDecrement());
    }

    @Override
    protected MemOperation execute1(byte data) {
      return new MemRead(registers.sp().getAndDecrement());
    }

    @Override
    protected MemOperation execute2(byte data) {
      return new MemWrite(registers.sp().getAndDecrement(), registers.pc().hi().get());
    }

    @Override
    protected MemOperation execute3(byte data) {
      return new MemWrite(registers.sp().get(), registers.pc().lo().get());
    }

    @Override
    protected MemOperation execute4(byte data) {
      registers.ime().reset();
      ioRegisters.if_().set((byte) (ioRegisters.if_().get() & ~interrupt.getMask()));
      registers.pc().set(interrupt.getVector());
      return new MemRead(registers.pc().getAndIncrement());
    }

    @Override
    protected MemOperation execute5(byte data) {
      registers.ir().set(data);
      return null;
    }
  }

  private boolean isInterruptRequested(InterruptType interrupt) {
    // TODO: ime only suppresses the jump and setting `if`. This matters for STOP and HALT?
    return registers.ime().get()
        && (ioRegisters.ie().get() & interrupt.getMask()) != 0
        && (ioRegisters.if_().get() & interrupt.getMask()) != 0;
  }

  private void logState() {
    if (writer == null) {
      return;
    }
    short pc = (short) (Short.toUnsignedInt(registers.pc().get()) - 1);
    try {
      writer.write(
          "A:%02x F:%02x B:%02x C:%02x D:%02x E:%02x H:%02x L:%02x SP:%04x PC:%04x PCMEM:%02x,%02x,%02x,%02x\n"
              .formatted(
                  registers.a().get(),
                  registers.f().get(),
                  registers.b().get(),
                  registers.c().get(),
                  registers.d().get(),
                  registers.e().get(),
                  registers.h().get(),
                  registers.l().get(),
                  registers.sp().get(),
                  pc,
                  memory.read(pc),
                  memory.read((short) (pc + 1)),
                  memory.read((short) (pc + 2)),
                  memory.read((short) (pc + 3))));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
