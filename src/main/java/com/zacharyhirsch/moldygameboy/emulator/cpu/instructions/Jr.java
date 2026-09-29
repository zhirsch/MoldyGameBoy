package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;

public final class Jr implements Instruction {

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
      registers.wz().set(registers.z().get()); // sign extend 8-bit to 16-bit.
      return new M3();
    }
  }

  private static final class M3 implements MCycle {

    @Override
    public void t1(Registers registers, Bus bus) {}

    @Override
    public void t2(Registers registers, Bus bus) {}

    @Override
    public void t3(Registers registers, Bus bus) {
      registers.pc().set((short) (registers.pc().get() + registers.wz().get()));
    }

    @Override
    public MCycle t4(Registers registers, Bus bus) {
      return new FetchOpcode();
    }
  }
}
