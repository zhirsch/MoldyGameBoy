package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.R;
import com.zacharyhirsch.moldygameboy.emulator.cpu.alu.Alu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Bit {

  public static final class Register implements Instruction {

    private final R register;
    private final int index;

    public Register(R register, int index) {
      this.register = register;
      this.index = index;
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      Alu.Result<Byte> result = Alu.bit(register.get(), index);
      registers.f().z().set(result.z());
      registers.f().n().set(result.n());
      registers.f().h().set(result.h());
      return new FetchOpcode();
    }
  }

  public static final class Indirect implements Instruction {

    private final int index;

    public Indirect(int index) {
      this.index = index;
    }

    @Override
    public MCycle start(Registers registers, Bus bus) {
      return new M3(index);
    }

    private static final class M3 implements MCycle {

      private final int index;

      private M3(int index) {
        this.index = index;
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
        registers.z().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        Alu.Result<Byte> result = Alu.bit(registers.z().get(), index);
        registers.f().z().set(result.z());
        registers.f().n().set(result.n());
        registers.f().h().set(result.h());
        return new FetchOpcode();
      }
    }
  }
}
