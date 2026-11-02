package com.zacharyhirsch.moldygameboy.emulator.cpu;

import com.zacharyhirsch.moldygameboy.emulator.arch.Bus;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import com.zacharyhirsch.moldygameboy.emulator.memory.VideoMemoryLock;
import java.nio.ByteBuffer;

public final class CpuMemoryController {

  private final ByteBuffer boot;
  private final ByteBuffer hram;
  private final IoRegisters ioregs;
  private final Memory memory;
  private final VideoMemoryLock videoMemoryLock;

  private byte ie = 0;

  public CpuMemoryController(
      ByteBuffer boot, Memory memory, IoRegisters ioregs, VideoMemoryLock videoMemoryLock) {
    this.boot = boot;
    this.hram = ByteBuffer.allocate(0x007f);
    this.ioregs = ioregs;
    this.memory = memory;
    this.videoMemoryLock = videoMemoryLock;
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
    if (isBootRom(addr)) {
      return boot.get(addr);
    }
    if (0x0000 <= addr && addr <= 0xfdff) {
      if (ioregs.dma.isActive() && isVramBus(address) == isVramBus(ioregs.dma.getActiveBase())) {
        return ioregs.dma.getCurrentValue();
      }
      if (isVramBus(address) && videoMemoryLock.isVramReadLocked()) {
        return (byte) 0xff;
      }
      return readExternal(address);
    }
    if (0xfe00 <= addr && addr <= 0xfeff) {
      if (ioregs.dma.isActive() || videoMemoryLock.isOamReadLocked()) {
        return (byte) 0xff;
      }
      return readExternal(address);
    }
    if (isPaletteData(addr) && videoMemoryLock.isVramWriteLocked()) {
      return (byte) 0xff;
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
    if (isBootRom(addr)) {
      throw new UnsupportedOperationException();
    }
    if (0x0000 <= addr && addr <= 0xfdff) {
      if (ioregs.dma.isActive() && isVramBus(address) == isVramBus(ioregs.dma.getActiveBase())) {
        return;
      }
      if (isVramBus(address) && videoMemoryLock.isVramWriteLocked()) {
        return;
      }
      writeExternal(address, value);
      return;
    }
    if (0xfe00 <= addr && addr <= 0xfeff) {
      if (ioregs.dma.isActive() || videoMemoryLock.isOamWriteLocked()) {
        return;
      }
      writeExternal(address, value);
      return;
    }
    if (isPaletteData(addr) && videoMemoryLock.isVramWriteLocked()) {
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

  /**
   * The DMG boot ROM covers $0000-$00FF. The CGB's also covers $0200-$08FF, leaving the cartridge
   * header visible in between.
   */
  private boolean isBootRom(int addr) {
    return ioregs.bank.get() == 0 && (addr <= 0x00ff || (0x0200 <= addr && addr < boot.limit()));
  }

  /** BCPD and OCPD, which (like VRAM) the CPU can't access while the PPU is drawing. */
  private static boolean isPaletteData(int addr) {
    return addr == 0xff69 || addr == 0xff6b;
  }

  private static boolean isVramBus(short address) {
    int addr = Short.toUnsignedInt(address);
    return 0x8000 <= addr && addr <= 0x9fff;
  }
}
