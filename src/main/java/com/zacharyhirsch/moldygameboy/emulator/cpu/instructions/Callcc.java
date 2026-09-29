package com.zacharyhirsch.moldygameboy.emulator.cpu.instructions;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.FlagsRegister;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import java.util.function.Predicate;

public final class Callcc implements Instruction {

  private final Predicate<FlagsRegister> predicate;

  public Callcc(Predicate<FlagsRegister> predicate) {
    this.predicate = predicate;
  }

  @Override
  public MCycle start(Registers registers, Bus bus) {
    return predicate.test(registers.f()) ? new Callcc.Take.M2() : new Callcc.Skip.M2();
  }

  private static final class Take {

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
        return new M3();
      }
    }

    private static final class M3 implements MCycle {

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
        registers.w().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return new M4();
      }
    }

    private static final class M4 implements MCycle {

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
        return new M5();
      }
    }

    private static final class M5 implements MCycle {

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
        return new M6();
      }
    }

    private static final class M6 implements MCycle {

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
        registers.pc().set(registers.wz().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.wr().set(false);
        return new FetchOpcode();
      }
    }
  }

  private static final class Skip {

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
        return new M3();
      }
    }

    private static final class M3 implements MCycle {

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
        registers.w().set(bus.dataBus().get());
      }

      @Override
      public MCycle t4(Registers registers, Bus bus) {
        bus.rd().set(false);
        return new FetchOpcode();
      }
    }
  }
}
