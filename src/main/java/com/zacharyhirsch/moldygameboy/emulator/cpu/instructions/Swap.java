package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.R;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Swap {

  public static final class Register implements Instruction {

    private final R register;

    public Register(R register) {
      this.register = register;
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      Alu.Result<Byte> result = Alu.swap(register.get());
      register.set(result.result());
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
      return new M3();
    }

    private static final class M3 implements MCycle {

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
        Alu.Result<Byte> result = Alu.swap(registers.z().get());
        registers.z().set(result.result());
        registers.f().z().set(result.z());
        registers.f().n().set(result.n());
        registers.f().h().set(result.h());
        registers.f().c().set(result.c());
        return new M4();
      }
    }

    private static final class M4 implements MCycle {

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
}
