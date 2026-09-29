package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.memory.BootRom;
import com.zacharyhirsch.moldygameboy.emulator.memory.Hram;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.MainMemoryController;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemoryRegion;

public final class CpuMemoryController {

  private final MainMemoryController main;
  private final BootRom boot;
  private final Hram hram;
  private final IoRegisters ioregs;
  private final IeRegister ie;

  public CpuMemoryController(MainMemoryController main, BootRom boot, IoRegisters ioregs) {
    this.main = main;
    this.boot = boot;
    this.hram = new Hram();
    this.ioregs = ioregs;
    this.ie = new IeRegister();
  }

  public int getEnabledInterrupts() {
    return ie.get();
  }

  public void tick(Bus bus) {
    if (!bus.rd().get() && !bus.wr().get()) {
      return;
    }
    if (bus.rd().get() && bus.wr().get()) {
      throw new IllegalStateException();
    }
    if (bus.rd().get()) {
      bus.dataBus().set(read(bus.addressBus().get()));
    } else {
      write(bus.addressBus().get(), bus.dataBus().get());
    }
  }

  public byte read(short address) {
    return getMemoryRegion(address).read(address);
  }

  public void write(short address, byte data) {
    getMemoryRegion(address).write(address, data);
  }

  private MemoryRegion getMemoryRegion(short address) {
    int addr = Short.toUnsignedInt(address);
    if (0x0000 <= addr && addr <= 0x00ff) {
      return ioregs.bank.get() == 0 ? boot : main.getMemoryRegion(address);
    }
    if (0x0100 <= addr && addr <= 0xfeff) {
      return main.getMemoryRegion(address);
    }
    if (0x0ff00 <= addr && addr <= 0xff7f) {
      return ioregs;
    }
    if (0xff80 <= addr && addr <= 0xfffe) {
      return hram;
    }
    if (addr == 0xffff) {
      return ie;
    }
    throw new IllegalArgumentException("%04x".formatted(address));
  }

  private static class IeRegister implements MemoryRegion {

    private byte value = 0;

    public int get() {
      return Short.toUnsignedInt(value);
    }

    @Override
    public byte read(short address) {
      return value;
    }

    @Override
    public void write(short address, byte value) {
      this.value = value;
    }
  }
}
