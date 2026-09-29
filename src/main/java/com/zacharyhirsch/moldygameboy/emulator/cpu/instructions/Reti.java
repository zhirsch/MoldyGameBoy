package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Reti implements Instruction {

  @Override
  public MCycle start(Registers registers, Bus bus) {
    return new M2();
  }

  private static final class M2 implements MCycle {

    @Override
    public void t1(Registers registers, Bus bus) {
      bus.addressBus().set(registers.sp().get());
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
      registers.sp().increment();
      return new M3();
    }
  }

  private static final class M3 implements MCycle {

    @Override
    public void t1(Registers registers, Bus bus) {
      bus.addressBus().set(registers.sp().get());
    }

    @Override
    public void t2(Registers registers, Bus bus) {
      bus.rd().set(true);
    }

    @Override
    public void t3(Registers registers, Bus bus) {
      registers.w().set(bus.dataBus().get());
    }

    @Override
    public MCycle t4(Registers registers, Bus bus) {
      bus.rd().set(false);
      registers.sp().increment();
      return new M4();
    }
  }

  private static final class M4 implements MCycle {

    @Override
    public void t1(Registers registers, Bus bus) {}

    @Override
    public void t2(Registers registers, Bus bus) {
      registers.pc().set(registers.wz().get());
    }

    @Override
    public void t3(Registers registers, Bus bus) {
      registers.ime().set((byte) 1);
    }

    @Override
    public MCycle t4(Registers registers, Bus bus) {
      return new FetchOpcode();
    }
  }
}
