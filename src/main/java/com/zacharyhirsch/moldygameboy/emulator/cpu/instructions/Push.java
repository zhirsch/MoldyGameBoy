package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.RR;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Push implements Instruction {

  private final RR register;

  public Push(RR register) {
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
    public void t2(Registers registers, Bus bus) {
      registers.sp().decrement();
    }

    @Override
    public void t3(Registers registers, Bus bus) {}

    @Override
    public MCycle t4(Registers registers, Bus bus) {
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
      bus.dataBus().set(register.hi().get());
      bus.wr().set(true);
    }

    @Override
    public void t3(Registers registers, Bus bus) {}

    @Override
    public MCycle t4(Registers registers, Bus bus) {
      bus.wr().set(false);
      registers.sp().decrement();
      return new M4(register);
    }
  }

  private static final class M4 implements MCycle {

    private final RR register;

    private M4(RR register) {
      this.register = register;
    }

    @Override
    public void t1(Registers registers, Bus bus) {
      bus.addressBus().set(registers.sp().get());
    }

    @Override
    public void t2(Registers registers, Bus bus) {
      bus.dataBus().set(register.lo().get());
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
