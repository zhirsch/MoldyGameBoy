package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.arch.InterruptType;
import com.zacharyhirsch.moldygameboy.emulator.arch.Irqs;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.CpuOpcodeDecoder;
import com.zacharyhirsch.moldygameboy.emulator.cpu.instructions.MCycle;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.Mapper;
import java.nio.ByteBuffer;

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
    T4,
  }

  private final CpuMemoryController memory;
  private final IoRegisters ioregs;
  private final Irqs irqs;
  private final Registers registers;

  private MCycle mcycle;
  private TCycle tcycle = TCycle.T1;

  public Cpu(ByteBuffer boot, ByteBuffer rom, ByteBuffer vram, IoRegisters ioregs, Irqs irqs) {
    this.memory = new CpuMemoryController(Mapper.create(rom), boot, vram, ioregs);
    this.ioregs = ioregs;
    this.irqs = irqs;
    this.registers = new Registers();

    this.mcycle = new FetchOpcode(ioregs, 0);
  }

  public Registers registers() {
    return registers;
  }

  public void tick(Bus bus) {
    if (registers.halted().get()) {
      tcycle =
          switch (tcycle) {
            case T1 -> TCycle.T2;
            case T2 -> TCycle.T3;
            case T3 -> TCycle.T4;
            case T4 -> {
              registers
                  .halted()
                  .set((ioregs.if_.get() & memory.getEnabledInterrupts() & 0x1f) == 0);
              if (!registers.halted().get()) {
                mcycle = new FetchOpcode(ioregs, getPendingInterrupts());
              }
              yield TCycle.T1;
            }
          };
    } else {
      tcycle =
          switch (tcycle) {
            case T1 -> tickT1(bus);
            case T2 -> tickT2(bus);
            case T3 -> tickT3(bus);
            case T4 -> tickT4(bus);
          };
    }
  }

  private TCycle tickT1(Bus bus) {
    mcycle.t1(registers, bus);
    return TCycle.T2;
  }

  private TCycle tickT2(Bus bus) {
    mcycle.t2(registers, bus);
    return TCycle.T3;
  }

  private TCycle tickT3(Bus bus) {
    memory.tick(bus);
    mcycle.t3(registers, bus);
    return TCycle.T4;
  }

  private TCycle tickT4(Bus bus) {
    mcycle = mcycle.t4(registers, bus);
    if (mcycle == null) {
      registers.ime().tick();
      mcycle = new FetchOpcode(ioregs, getPendingInterrupts());
    }
    return TCycle.T1;
  }

  private int getPendingInterrupts() {
    return registers.ime().get() ? ioregs.if_.get() & memory.getEnabledInterrupts() & 0x1f : 0;
  }

  private static final class Isr {

    private record M2(IoRegisters ioregs, InterruptType interrupt) implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {}

      @Override
      public void t2(Registers registers, Bus bus) {}

      @Override
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        return new M3(ioregs, interrupt);
      }
    }

    private record M3(IoRegisters ioregs, InterruptType interrupt) implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {
        registers.sp().decrement();
        bus.addressBus().set(registers.sp().get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.dataBus().set(registers.pc().hi().get());
        bus.wr().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.wr().set(false);
        return new M4(ioregs, interrupt);
      }
    }

    private record M4(IoRegisters ioregs, InterruptType interrupt) implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {
        registers.sp().decrement();
        bus.addressBus().set(registers.sp().get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.dataBus().set(registers.pc().lo().get());
        bus.wr().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.wr().set(false);
        return new M5(ioregs, interrupt);
      }
    }

    private record M5(IoRegisters ioregs, InterruptType interrupt) implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {}

      @Override
      public void t2(Registers registers, Bus bus) {
        ioregs.if_.set(ioregs.if_.get() & ~interrupt.mask());
      }

      @Override
      public void t3(Registers registers, Bus bus) {
        registers.pc().set(interrupt.vector());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        return null;
      }
    }
  }

  private static final class FetchOpcode implements MCycle {

    private final IoRegisters ioregs;
    private final int pendingInterrupts;

    private InterruptType pendingInterrupt;

    FetchOpcode(IoRegisters ioregs, int pendingInterrupts) {
      this.ioregs = ioregs;
      this.pendingInterrupts = pendingInterrupts;
    }

    @Override
    public void t1(Registers registers, Bus bus) {
      if (pendingInterrupts != 0) {
        return;
      }
      bus.addressBus().set(registers.pc().get());
    }

    @Override
    public void t2(Registers registers, Bus bus) {
      if (pendingInterrupts != 0) {
        return;
      }
      bus.rd().set(true);
      registers.pc().increment();
    }

    @Override
    public void t3(Registers registers, Bus bus) {
      if (pendingInterrupts != 0) {
        for (InterruptType interrupt : INTERRUPTS) {
          if ((pendingInterrupts & interrupt.mask()) != 0) {
            pendingInterrupt = interrupt;
            return;
          }
        }
        throw new IllegalStateException("No active interrupt");
      }
      registers.ir().set(bus.dataBus().get());
    }

    @Override
    public MCycle t4(Registers registers, Bus bus) {
      if (pendingInterrupts != 0) {
        registers.ime().set(false, 0);
        return new Isr.M2(ioregs, pendingInterrupt);
      }
      bus.rd().set(false);
      if (Byte.toUnsignedInt(registers.ir().get()) == 0xcb) {
        return new FetchOpcodePrefixed();
      }
      return CpuOpcodeDecoder.decode(registers).start(registers, bus);
    }

    private static final class FetchOpcodePrefixed implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set(registers.pc().get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.rd().set(true);
        registers.pc().increment();
      }

      @Override
      public void t3(Registers registers, Bus bus) {
        registers.ir().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return CpuOpcodeDecoder.decodePrefixed(registers).start(registers, bus);
      }
    }
  }
}
