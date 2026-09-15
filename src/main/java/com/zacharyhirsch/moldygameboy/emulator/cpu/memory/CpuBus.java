package com.zacharyhirsch.moldygameboy.emulator.cpu.memory;

import com.zacharyhirsch.moldygameboy.emulator.memory.BootRom;
import com.zacharyhirsch.moldygameboy.emulator.memory.EchoRam;
import com.zacharyhirsch.moldygameboy.emulator.memory.Hram;
import com.zacharyhirsch.moldygameboy.emulator.memory.IoRegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemoryRegion;
import com.zacharyhirsch.moldygameboy.emulator.memory.Oam;
import com.zacharyhirsch.moldygameboy.emulator.memory.Ram;
import com.zacharyhirsch.moldygameboy.emulator.memory.Rom;
import com.zacharyhirsch.moldygameboy.emulator.memory.Vram;
import com.zacharyhirsch.moldygameboy.emulator.memory.Wram;

public final class CpuBus {

  private final BootRom boot;
  private final Rom rom;
  private final Vram vram;
  private final Ram ram;
  private final Wram wram;
  private final EchoRam echoRam;
  private final Oam oam;
  private final Hram hram;
  private final IoRegisters ioregs;

  private byte ie = 0;

  public CpuBus(
      BootRom boot,
      Rom rom,
      Vram vram,
      Ram ram,
      Wram wram,
      Oam oam,
      Hram hram,
      IoRegisters ioregs) {
    this.boot = boot;
    this.rom = rom;
    this.vram = vram;
    this.ram = ram;
    this.wram = wram;
    this.echoRam = new EchoRam(wram);
    this.oam = oam;
    this.hram = hram;
    this.ioregs = ioregs;
  }

  public int getEnabledInterrupts() {
    return Byte.toUnsignedInt(ie);
  }

  public byte read(short address) {
    int addr = Short.toUnsignedInt(address);
    if (ioregs.dma.isActive() && 0x0000 <= addr && addr <= 0xfeff) {
      return (byte) 0xff;
    }
    return readDirect(address);
  }

  public void write(short address, byte data) {
    int addr = Short.toUnsignedInt(address);
    if (ioregs.dma.isActive() && 0x0000 <= addr && addr <= 0xfeff) {
      return;
    }
    writeDirect(address, data);
  }

  public byte readDirect(short address) {
    return getMemoryRegion(address).read(address);
  }

  public void writeDirect(short address, byte data) {
    getMemoryRegion(address).write(address, data);
  }

  private MemoryRegion getMemoryRegion(short address) {
    int addr = Short.toUnsignedInt(address);
    if (0x0000 <= addr && addr <= 0x00ff) {
      return ioregs.bank.get() == 0 ? boot : rom;
    }
    if (0x0100 <= addr && addr <= 0x7fff) {
      return rom;
    }
    if (0x8000 <= addr && addr <= 0x9fff) {
      return vram;
    }
    if (0xa000 <= addr && addr <= 0xbfff) {
      return ram;
    }
    if (0xc000 <= addr && addr <= 0xdfff) {
      return wram;
    }
    if (0xe000 <= addr && addr <= 0xfdff) {
      return echoRam;
    }
    if (0xfe00 <= addr && addr <= 0xfe9f) {
      if (addr == 0xfe00) {
        IO.println("!");
      }
      return oam;
    }
    if (0xfea0 <= addr && addr <= 0xfeff) {
      return new MemoryRegion() {
        @Override
        public byte read(short address) {
          return (byte) 0xff;
        }

        @Override
        public void write(short address, byte value) {}
      };
    }
    if (0x0ff00 <= addr && addr <= 0xff7f) {
      return ioregs;
    }
    if (0xff80 <= addr && addr <= 0xfffe) {
      return hram;
    }
    if (addr == 0xffff) {
      return new MemoryRegion() {
        @Override
        public byte read(short address) {
          return ie;
        }

        @Override
        public void write(short address, byte value) {
          ie = value;
        }
      };
    }
    throw new IllegalArgumentException("%04x".formatted(address));
  }
}
