package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.Mapper;
import com.zacharyhirsch.moldygameboy.emulator.memory.OpenBus;
import java.nio.ByteBuffer;

public final class CpuMemoryController {

  private final Mapper mapper;
  private final ByteBuffer boot;
  private final ByteBuffer vram;
  private final ByteBuffer wram;
  private final ByteBuffer oam;
  private final ByteBuffer hram;
  private final IoRegisters ioregs;

  private byte ie = 0;

  public CpuMemoryController(Mapper mapper, ByteBuffer boot, ByteBuffer vram, IoRegisters ioregs) {
    this.mapper = mapper;
    this.boot = boot;
    this.vram = vram;
    this.wram = ByteBuffer.allocate(0x2000);
    this.oam = ByteBuffer.allocate(0x00a0);
    this.hram = ByteBuffer.allocate(0x007f);
    this.ioregs = ioregs;
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
    if (0x0000 <= addr && addr <= 0xfeff && ioregs.dma.isActive()) {
      return new OpenBus().read(address);
    }
    if (0x0000 <= addr && addr <= 0x7fff) {
      return mapper.mapRom().read(address);
    }
    if (0x8000 <= addr && addr <= 0x9fff) {
      return vram.get(addr - 0x8000);
    }
    if (0xa000 <= addr && addr <= 0xbfff) {
      return mapper.mapRam().read(address);
    }
    if (0xc000 <= addr && addr <= 0xfdff) {
      return wram.get((addr & 0xdfff) - 0xc000);
    }
    if (0xfe00 <= addr && addr <= 0xfe9f) {
      return oam.get(addr - 0xfe00);
    }
    if (0xfea0 <= addr && addr <= 0xfeff) {
      return new OpenBus().read(address);
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
    if (0x0000 <= addr && addr <= 0xfeff && ioregs.dma.isActive()) {
      new OpenBus().write(address, value);
      return;
    }
    if (0x0000 <= addr && addr <= 0x7fff) {
      mapper.mapRom().write(address, value);
      return;
    }
    if (0x8000 <= addr && addr <= 0x9fff) {
      vram.put(addr - 0x8000, value);
      return;
    }
    if (0xa000 <= addr && addr <= 0xbfff) {
      mapper.mapRam().write(address, value);
      return;
    }
    if (0xc000 <= addr && addr <= 0xfdff) {
      wram.put((addr & 0xdfff) - 0xc000, value);
      return;
    }
    if (0xfe00 <= addr && addr <= 0xfe9f) {
      oam.put(addr - 0xfe00, value);
      return;
    }
    if (0xfea0 <= addr && addr <= 0xfeff) {
      new OpenBus().write(address, value);
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
}
