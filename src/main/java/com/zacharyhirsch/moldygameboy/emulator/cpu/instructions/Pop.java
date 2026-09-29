package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.RR;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Pop implements Instruction {

  private final RR register;

  public Pop(RR register) {
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
    public void t1(Registers registers, Bus bus) {
      bus.addressBus().set(registers.sp().get());
    }

    @Override
    public void t2(Registers registers, Bus bus) {
      bus.rd().set(true);
    }

    @Override
    public void t3(Registers registers, Bus bus) {}

    @Override
    public MCycle t4(Registers registers, Bus bus) {
      register.lo().set(bus.dataBus().get());
      registers.sp().increment();
      return new M3(register);
    }
  }

  private static final class M3 implements MCycle {

    private final RR register;

    private M3(RR register) {
      this.register = register;
    }

    @Override
    public void t1(Registers registers, Bus bus) {
      bus.addressBus().set(registers.sp().get());
    }

    @Override
    public void t2(Registers registers, Bus bus) {
      bus.rd().set(true);
    }

    @Override
    public void t3(Registers registers, Bus bus) {}

    @Override
    public MCycle t4(Registers registers, Bus bus) {
      register.hi().set(bus.dataBus().get());
      registers.sp().increment();
      return new FetchOpcode();
    }
  }
}
