package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import com.zacharyhirsch.moldygameboy.emulator.memory.OpenBus;
import java.nio.ByteBuffer;

public final class CpuMemoryController {

  private final ByteBuffer boot;
  private final ByteBuffer hram;
  private final IoRegisters ioregs;
  private final Memory memory;

  private byte ie = 0;

  public CpuMemoryController(ByteBuffer boot, Memory memory, IoRegisters ioregs) {
    this.boot = boot;
    this.hram = ByteBuffer.allocate(0x007f);
    this.ioregs = ioregs;
    this.memory = memory;
  }

  public int getEnabledInterrupts() {
    return Byte.toUnsignedInt(ie);
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
    int addr = Short.toUnsignedInt(address);
    if (0x0000 <= addr && addr <= 0x00ff && ioregs.bank.get() == 0) {
      return boot.get(address);
    }
    if (0x0000 <= addr && addr <= 0xfeff) {
      if (ioregs.dma.isActive()) {
        return new OpenBus().read(address);
      }
      return readExternal(address);
    }
    if (0x0ff00 <= addr && addr <= 0xff7f) {
      return ioregs.read(address);
    }
    if (0xff80 <= addr && addr <= 0xfffe) {
      return hram.get(addr - 0xff80);
    }
    if (addr == 0xffff) {
      return ie;
    }
    throw new IllegalArgumentException("%04x".formatted(address));
  }

  public void write(short address, byte value) {
    int addr = Short.toUnsignedInt(address);
    if (0x0000 <= addr && addr <= 0x00ff && ioregs.bank.get() == 0) {
      throw new UnsupportedOperationException();
    }
    if (0x0000 <= addr && addr <= 0xfeff) {
      if (ioregs.dma.isActive()) {
        new OpenBus().write(address, value);
        return;
      }
      writeExternal(address, value);
      return;
    }
    if (0x0ff00 <= addr && addr <= 0xff7f) {
      ioregs.write(address, value);
      return;
    }
    if (0xff80 <= addr && addr <= 0xfffe) {
      hram.put(addr - 0xff80, value);
      return;
    }
    if (addr == 0xffff) {
      ie = value;
      return;
    }
    throw new IllegalArgumentException("%04x".formatted(address));
  }

  public byte readExternal(short address) {
    return memory.read(address);
  }

  public void writeExternal(short address, byte value) {
    memory.write(address, value);
  }
}
