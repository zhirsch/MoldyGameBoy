package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.Mem;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.AbstractInstruction;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.Halt;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.Instruction;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.Nop;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.IORegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemoryMap;
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
  private final MemoryMap memory;
  private final CpuDecoder decoder;
  private final Writer writer;

  private Instruction instruction = null;
  private byte data = 0;
  private boolean halted = false;
  private boolean skipLoggingNextInstruction = false;

  public Cpu(Registers registers, IORegisters ioRegisters, MemoryMap memory, Writer writer) {
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
    Mem mem = instruction.tick(data);
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
    if (registers.ime().get() && (ioRegisters.ie().get() & ioRegisters.if_().get()) != 0) {
      skipLoggingNextInstruction = true;
      return new Isr(registers, ioRegisters);
    }
    skipLoggingNextInstruction = !decoder.isPrefixed() && registers.ir().get() == (byte) 0xcb;
    Instruction instruction = decoder.decode();
    halted = instruction instanceof Halt;
    return instruction;
  }

  private static final class Isr extends AbstractInstruction {

    private final Registers registers;
    private final IORegisters ioRegisters;

    Isr(Registers registers, IORegisters ioRegisters) {
      this.registers = registers;
      this.ioRegisters = ioRegisters;
    }

    @Override
    protected Mem execute0(byte data) {
      return Mem.read(registers.pc().getAndDecrement());
    }

    @Override
    protected Mem execute1(byte data) {
      return Mem.read(registers.sp().getAndDecrement());
    }

    @Override
    protected Mem execute2(byte data) {
      return Mem.write(registers.sp().getAndDecrement(), registers.pc().hi().get());
    }

    @Override
    protected Mem execute3(byte data) {
      return Mem.write(registers.sp().get(), registers.pc().lo().get());
    }

    @Override
    protected Mem execute4(byte data) {
      for (InterruptType interrupt : INTERRUPTS) {
        if ((ioRegisters.ie().get() & ioRegisters.if_().get() & interrupt.getMask()) != 0) {
          registers.ime().reset();
          ioRegisters.if_().set((byte) (ioRegisters.if_().get() & ~interrupt.getMask()));
          registers.pc().set(interrupt.getVector());
          break;
        }
      }
      return Mem.read(registers.pc().getAndIncrement());
    }

    @Override
    protected Mem execute5(byte data) {
      registers.ir().set(data);
      return null;
    }

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
