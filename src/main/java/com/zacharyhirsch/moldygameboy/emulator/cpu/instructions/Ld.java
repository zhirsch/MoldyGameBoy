package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.RR;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.R;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Ld {

  private Ld() {}

  public static final class RegisterRegister implements Instruction {

    private final R src;
    private final R dst;

    public RegisterRegister(R src, R dst) {
      this.src = src;
      this.dst = dst;
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      dst.set(src.get());
      return null;
    }
  }

  public static final class RegisterRegister16 implements Instruction {

    private final RR src;
    private final RR dst;

    public RegisterRegister16(
        RR src, RR dst) {
      this.src = src;
      this.dst = dst;
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2(src, dst);
    }

    // LD SP,HL takes 2 M-cycles: the second is an internal delay with no bus access.
    private static final class M2 implements MCycle {

      private final RR src;
      private final RR dst;

      private M2(RR src, RR dst) {
        this.src = src;
        this.dst = dst;
      }

      @Override
      public void t1(Registers registers, Bus bus) {}

      @Override
      public void t2(Registers registers, Bus bus) {}

      @Override
      public void t3(Registers registers, Bus bus) {
        dst.set(src.get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        return null;
      }
    }
  }

  public static final class RegisterIndirect implements Instruction {

    private final R dst;

    public RegisterIndirect(R dst) {
      this.dst = dst;
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2(dst);
    }

    private static final class M2 implements MCycle {

      private final R dst;

      private M2(R dst) {
        this.dst = dst;
      }

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set(registers.hl().get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.rd().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {
        dst.set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return null;
      }
    }
  }

  public static final class AccumulatorIndirectIncrement implements Instruction {

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2();
    }

    private static final class M2 implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set(registers.hl().get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.rd().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {
        registers.a().set(bus.dataBus().get());
        registers.hl().increment();
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return null;
      }
    }
  }

  public static final class AccumulatorIndirectDecrement implements Instruction {

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2();
    }

    private static final class M2 implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set(registers.hl().get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.rd().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {
        registers.a().set(bus.dataBus().get());
        registers.hl().decrement();
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return null;
      }
    }
  }

  public static final class AccumulatorIndirect implements Instruction {

    private final RR src;

    public AccumulatorIndirect(RR src) {
      this.src = src;
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2(src);
    }

    private static final class M2 implements MCycle {

      private final RR src;

      public M2(RR src) {
        this.src = src;
      }

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set(src.get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.rd().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {
        registers.a().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return null;
      }
    }
  }

  public static final class AccumulatorIndirectHi implements Instruction {

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2();
    }

    private static final class M2 implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set((short) (0xff00 | registers.c().get()));
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.rd().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {
        registers.a().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return null;
      }
    }
  }

  public static final class ImmediateIndirect implements Instruction {

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2();
    }

    private static final class M2 implements MCycle {

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
        registers.z().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return new M3();
      }
    }

    private static final class M3 implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set(registers.hl().get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.dataBus().set(registers.z().get());
        bus.wr().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.wr().set(false);
        return null;
      }
    }
  }

  public static final class IndirectRegister implements Instruction {

    private final R src;
    private final RR dst;

    public IndirectRegister(R src, RR dst) {
      this.src = src;
      this.dst = dst;
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2(src, dst);
    }

    private static final class M2 implements MCycle {

      private final R src;
      private final RR dst;

      public M2(R src, RR dst) {
        this.src = src;
        this.dst = dst;
      }

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set(dst.get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.dataBus().set(src.get());
        bus.wr().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.wr().set(false);
        return null;
      }
    }
  }

  public static final class IndirectIncrementAccumulator implements Instruction {

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2();
    }

    private static final class M2 implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set(registers.hl().get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.dataBus().set(registers.a().get());
        bus.wr().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {
        registers.hl().increment();
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.wr().set(false);
        return null;
      }
    }
  }

  public static final class IndirectDecrementAccumulator implements Instruction {

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2();
    }

    private static final class M2 implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set(registers.hl().get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.dataBus().set(registers.a().get());
        bus.wr().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {
        registers.hl().decrement();
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.wr().set(false);
        return null;
      }
    }
  }

  public static final class RegisterImmediate8 implements Instruction {

    private final R dst;

    public RegisterImmediate8(R dst) {
      this.dst = dst;
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2(dst);
    }

    private static final class M2 implements MCycle {

      private final R dst;

      public M2(R dst) {
        this.dst = dst;
      }

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
        dst.set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return null;
      }
    }
  }

  public static final class RegisterImmediate16 implements Instruction {

    private final RR dst;

    public RegisterImmediate16(RR dst) {
      this.dst = dst;
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2(dst);
    }

    private static final class M2 implements MCycle {

      private final RR dst;

      public M2(RR dst) {
        this.dst = dst;
      }

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
        dst.lo().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return new M3(dst);
      }
    }

    private static final class M3 implements MCycle {

      private final RR dst;

      public M3(RR dst) {
        this.dst = dst;
      }

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
        dst.hi().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return null;
      }
    }
  }

  public static final class DirectAccumulator implements Instruction {

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2();
    }

    private static final class M2 implements MCycle {

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
        registers.z().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return new M3();
      }
    }

    private static final class M3 implements MCycle {

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
        registers.w().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return new M4();
      }
    }

    private static final class M4 implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set(registers.wz().get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.dataBus().set(registers.a().get());
        bus.wr().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.wr().set(false);
        return null;
      }
    }
  }

  public static final class DirectStackPointer implements Instruction {

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2();
    }

    private static final class M2 implements MCycle {

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
        registers.z().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return new M3();
      }
    }

    private static final class M3 implements MCycle {

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
        registers.w().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return new M4();
      }
    }

    private static final class M4 implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set(registers.wz().get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.dataBus().set(registers.sp().lo().get());
        bus.wr().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.wr().set(false);
        registers.wz().increment();
        return new M5();
      }
    }

    private static final class M5 implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set(registers.wz().get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.dataBus().set(registers.sp().hi().get());
        bus.wr().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.wr().set(false);
        return null;
      }
    }
  }

  public static final class DirectHiAccumulator implements Instruction {

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2();
    }

    private static final class M2 implements MCycle {

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
        registers.z().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        registers.w().set((byte) 0xff);
        return new M3();
      }
    }

    private static final class M3 implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set(registers.wz().get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.dataBus().set(registers.a().get());
        bus.wr().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.wr().set(false);
        return null;
      }
    }
  }

  public static final class IndirectHiAccumulator implements Instruction {

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2();
    }

    private static final class M2 implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set((short) (0xff00 | registers.c().get()));
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.dataBus().set(registers.a().get());
        bus.wr().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.wr().set(false);
        return null;
      }
    }
  }

  public static final class AccumulatorDirect implements Instruction {

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2();
    }

    private static final class M2 implements MCycle {

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
        registers.z().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return new M3();
      }
    }

    private static final class M3 implements MCycle {

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
        registers.w().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return new M4();
      }
    }

    private static final class M4 implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set(registers.wz().get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.rd().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {
        registers.a().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return null;
      }
    }
  }

  public static final class AccumulatorDirectHi implements Instruction {

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2();
    }

    private static final class M2 implements MCycle {

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
        registers.z().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        registers.w().set((byte) 0xff);
        return new M3();
      }
    }

    private static final class M3 implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set(registers.wz().get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.rd().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {
        registers.a().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return null;
      }
    }
  }

  public static final class StackOffset implements Instruction {

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2();
    }

    private static final class M2 implements MCycle {

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
        var sp = registers.sp();
        registers.hl().set((short) (sp.get() + bus.dataBus().get()));
        registers.f().z().set(false);
        registers.f().n().set(false);
        registers.f().h().set((sp.lo().get() & 0x0f) + (bus.dataBus().get() & 0x0f) > 0x0f);
        registers.f().c().set((sp.lo().get() & 0xff) + (bus.dataBus().get() & 0xff) > 0xff);
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return new M3();
      }
    }

    // LD HL,SP+e takes 3 M-cycles: the third is an internal delay with no bus access.
    private static final class M3 implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {}

      @Override
      public void t2(Registers registers, Bus bus) {}

      @Override
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        return null;
      }
    }
  }
}
