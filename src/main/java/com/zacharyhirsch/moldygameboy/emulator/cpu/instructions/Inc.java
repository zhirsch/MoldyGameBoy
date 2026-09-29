package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.RR;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.R;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Inc {

  public static final class Indirect implements Instruction {

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
        registers.z().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        Alu.Result<Byte> result = Alu.inc(registers.z().get());
        registers.z().set(result.result());
        registers.f().z().set(result.z());
        registers.f().n().set(result.n());
        registers.f().h().set(result.h());
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
        return new FetchOpcode();
      }
    }
  }

  public static final class R8 implements Instruction {

    private final R register;

    public R8(R register) {
      this.register = register;
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      Alu.Result<Byte> result = Alu.inc(register.get());
      register.set(result.result());
      registers.f().z().set(result.z());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      return new FetchOpcode();
    }
  }

  public static final class R16 implements Instruction {

    private final RR register;

    public R16(RR register) {
      this.register = register;
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M2(register);
    }

    private static final class M2 implements MCycle {

      private final RR register;

      private M2(RR register) {
        this.register = register;
      }

      @Override
      public void t1(Registers registers, Bus bus) {}

      @Override
      public void t2(Registers registers, Bus bus) {}

      @Override
      public void t3(Registers registers, Bus bus) {
        register.increment();
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        return new FetchOpcode();
      }
    }
  }
}
