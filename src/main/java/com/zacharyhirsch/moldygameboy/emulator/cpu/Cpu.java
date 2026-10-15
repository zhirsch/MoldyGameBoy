package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.arch.InterruptType;
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

  private final CpuMemoryController memory;
  private final IoRegisters ioregs;
  private final Registers registers;
  private final Bus bus;

  private MCycle mcycle;
  private TCycle tcycle = TCycle.T1;

  public Cpu(ByteBuffer boot, ByteBuffer rom, ByteBuffer vram, IoRegisters ioregs) {
    this.memory = new CpuMemoryController(Mapper.create(rom), boot, vram, ioregs);
    this.ioregs = ioregs;
    this.registers = new Registers();
    this.bus = new Bus();

    this.mcycle = new FetchOpcode(ioregs, 0);
  }

  public Registers registers() {
    return registers;
  }

  public void tick() {
    if (registers.halted().get()) {
      switch (tcycle) {
        case T1, T2, T3 -> {}
        case T4 -> {
          if (getPendingInterrupts() != 0) {
            registers.halted().set(false);
            mcycle = new FetchOpcode(ioregs, registers.ime().get() ? getPendingInterrupts() : 0);
          }
        }
      }
    } else {
      switch (tcycle) {
        case T1 -> tickT1();
        case T2 -> tickT2();
        case T3 -> tickT3();
        case T4 -> tickT4();
      }
    }
    tcycle = tcycle.next();
  }

  private void tickT1() {
    mcycle.t1(registers, bus);
  }

  private void tickT2() {
    mcycle.t2(registers, bus);
  }

  private void tickT3() {
    memory.tick(bus);
    mcycle.t3(registers, bus);
  }

  private void tickT4() {
    mcycle = mcycle.t4(registers, bus);
    if (mcycle == null) {
      registers.ime().tick();
      mcycle = new FetchOpcode(ioregs, registers.ime().get() ? getPendingInterrupts() : 0);
    }
  }

  private int getPendingInterrupts() {
    return ioregs.if_.get() & memory.getEnabledInterrupts() & 0x1f;
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
