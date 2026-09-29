package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.common.Function3;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.R;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.RR;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import java.util.function.BiFunction;

public class AluOperation {

  public static final class A_R implements Instruction {

    private final R rhs;
    private final Function3<Byte, Byte, Boolean, Alu.Result<Byte>> op;

    public A_R(R rhs, Function3<Byte, Byte, Boolean, Alu.Result<Byte>> op) {
      this.rhs = rhs;
      this.op = op;
    }

    public A_R(R rhs, BiFunction<Byte, Byte, Alu.Result<Byte>> op) {
      this(rhs, (x, y, c) -> op.apply(x, y));
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      var result = op.apply(registers.a().get(), rhs.get(), registers.f().c().get());
      registers.a().set(result.result());
      registers.f().z().set(result.z());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      registers.f().c().set(result.c());
      return new FetchOpcode();
    }
  }

  public static final class A_IndirectHL implements Instruction {

    private final Function3<Byte, Byte, Boolean, Alu.Result<Byte>> op;

    public A_IndirectHL(Function3<Byte, Byte, Boolean, Alu.Result<Byte>> op) {
      this.op = op;
    }

    public A_IndirectHL(BiFunction<Byte, Byte, Alu.Result<Byte>> op) {
      this((x, y, c) -> op.apply(x, y));
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2(op);
    }

    private record M2(Function3<Byte, Byte, Boolean, Alu.Result<Byte>> op) implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {
        bus.addressBus().set(registers.hl().get());
      }

      @Override
      public void t2(Registers registers, Bus bus) {
        bus.rd().set(true);
      }

      @Override
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        var result = op.apply(registers.a().get(), bus.dataBus().get(), registers.f().c().get());
        registers.a().set(result.result());
        registers.f().z().set(result.z());
        registers.f().n().set(result.n());
        registers.f().h().set(result.h());
        registers.f().c().set(result.c());
        return new FetchOpcode();
      }
    }
  }

  public static final class A_N8 implements Instruction {

    private final Function3<Byte, Byte, Boolean, Alu.Result<Byte>> op;

    public A_N8(Function3<Byte, Byte, Boolean, Alu.Result<Byte>> op) {
      this.op = op;
    }

    public A_N8(BiFunction<Byte, Byte, Alu.Result<Byte>> op) {
      this((x, y, c) -> op.apply(x, y));
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2(op);
    }

    private record M2(Function3<Byte, Byte, Boolean, Alu.Result<Byte>> op) implements MCycle {

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
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        var result = op.apply(registers.a().get(), bus.dataBus().get(), registers.f().c().get());
        registers.a().set(result.result());
        registers.f().z().set(result.z());
        registers.f().n().set(result.n());
        registers.f().h().set(result.h());
        registers.f().c().set(result.c());
        return new FetchOpcode();
      }
    }
  }

  public static final class HL_RR implements Instruction {

    private final RR rhs;
    private final BiFunction<Short, Short, Alu.Result<Short>> op;

    public HL_RR(RR rhs, BiFunction<Short, Short, Alu.Result<Short>> op) {
      this.rhs = rhs;
      this.op = op;
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2(rhs, op);
    }

    private record M2(RR rhs, BiFunction<Short, Short, Alu.Result<Short>> op) implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {}

      @Override
      public void t2(Registers registers, Bus bus) {}

      @Override
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        Alu.Result<Short> result = op.apply(registers.hl().get(), rhs.get());
        registers.hl().set(result.result());
        registers.f().n().set(result.n());
        registers.f().h().set(result.h());
        registers.f().c().set(result.c());
        return new FetchOpcode();
      }
    }
  }

  public static final class SP_E8 implements Instruction {

    private final BiFunction<Short, Byte, Alu.Result<Short>> op;

    public SP_E8(BiFunction<Short, Byte, Alu.Result<Short>> op) {
      this.op = op;
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2(op);
    }

    private record M2(BiFunction<Short, Byte, Alu.Result<Short>> op) implements MCycle {

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
        registers.wz().set(registers.z().get()); // sign extend 8-bit to 16-bit.
        return new M3(op);
      }
    }

    private record M3(BiFunction<Short, Byte, Alu.Result<Short>> op) implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {}

      @Override
      public void t2(Registers registers, Bus bus) {}

      @Override
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        var result = op.apply(registers.sp().get(), registers.z().get());
        registers.wz().set(result.result());
        registers.f().z().set(result.z());
        registers.f().n().set(result.n());
        registers.f().h().set(result.h());
        registers.f().c().set(result.c());
        return new M4();
      }
    }

    private static final class M4 implements MCycle {

      @Override
      public void t1(Registers registers, Bus bus) {}

      @Override
      public void t2(Registers registers, Bus bus) {
        registers.sp().set(registers.wz().get());
      }

      @Override
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        return new FetchOpcode();
      }
    }
  }
}
