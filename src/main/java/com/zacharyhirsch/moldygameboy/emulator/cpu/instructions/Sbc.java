package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.R;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Sbc {

  public static final class Register implements Instruction {

    private final R register;

    public Register(R register) {
      this.register = register;
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      Alu.Result<Byte> result = Alu.sub(registers.a().get(), register.get(), registers.f().c().get());
      registers.a().set(result.result());
      registers.f().z().set(result.z());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      registers.f().c().set(result.c());
      return new FetchOpcode();
    }
  }

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
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        Alu.Result<Byte> result =
            Alu.sub(registers.a().get(), bus.dataBus().get(), registers.f().c().get());
        registers.a().set(result.result());
        registers.f().z().set(result.z());
        registers.f().n().set(result.n());
        registers.f().h().set(result.h());
        registers.f().c().set(result.c());
        return new FetchOpcode();
      }
    }
  }

  public static final class Immediate implements Instruction {

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
      public void t3(Registers registers, Bus bus) {}

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        Alu.Result<Byte> result =
            Alu.sub(registers.a().get(), bus.dataBus().get(), registers.f().c().get());
        R r = registers.a();
        r.set(result.result());
        registers.f().z().set(result.z());
        registers.f().n().set(result.n());
        registers.f().h().set(result.h());
        registers.f().c().set(result.c());
        return new FetchOpcode();
      }
    }
  }
}
