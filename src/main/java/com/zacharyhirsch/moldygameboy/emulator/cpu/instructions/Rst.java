package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Rst implements Instruction {

  private final short address;

  public Rst(short address) {
    this.address = address;
  }

  @Override
  public MCycle start(Registers registers, Bus bus) {
    return new M2(address);
  }

  private static final class M2 implements MCycle {

    private final short address;

    private M2(short address) {
      this.address = address;
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
      return new M3(address);
    }
  }

  private static final class M3 implements MCycle {

    private final short address;

    private M3(short address) {
      this.address = address;
    }

    @Override
    public void t1(Registers registers, Bus bus) {
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
      registers.sp().decrement();
      return new M4(address);
    }
  }

  private static final class M4 implements MCycle {

    private final short address;

    private M4(short address) {
      this.address = address;
    }

    @Override
    public void t1(Registers registers, Bus bus) {
      bus.addressBus().set(registers.sp().get());
    }

    @Override
    public void t2(Registers registers, Bus bus) {
      bus.dataBus().set(registers.pc().lo().get());
      bus.wr().set(true);
    }

    @Override
    public void t3(Registers registers, Bus bus) {
      registers.pc().set(address);
    }

    @Override
    public MCycle t4(Registers registers, Bus bus) {
      bus.wr().set(false);
      return null;
    }
  }
}
