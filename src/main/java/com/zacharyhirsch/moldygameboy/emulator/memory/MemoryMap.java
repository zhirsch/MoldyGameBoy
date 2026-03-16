package com.zacharyhirsch.moldygameboy.emulator.memory;

import com.zacharyhirsch.moldygameboy.emulator.arch.Memory;
import java.nio.ByteBuffer;

public final class MemoryMap implements Memory {

  private final ByteBuffer boot;
  private final ByteBuffer rom;
  private final ByteBuffer vram;
  private final ByteBuffer wram;
  private final ByteBuffer oam;
  private final ByteBuffer registers;
  private final ByteBuffer hram;
  private final ByteBuffer ie;

  public MemoryMap(ByteBuffer boot, ByteBuffer rom) {
    this.boot = boot;
    this.rom = rom;
    this.vram = ByteBuffer.allocate(0x4000);
    this.wram = ByteBuffer.allocate(0x8000);
    this.oam = ByteBuffer.allocate(0xa0);
    this.registers = ByteBuffer.allocate(0x80);
    this.hram = ByteBuffer.allocate(0x7f);
    this.ie = ByteBuffer.allocate(0x01);
  }

  @Override
  public byte read(short address) {
    int addr = Short.toUnsignedInt(address);
    assert 0x0000 <= addr && addr <= 0xffff;
    switch (registers.get(Memory.Register.BANK.index())) {
      case 0 -> {
        if (0x0000 <= addr && addr <= 0x00ff) {
          // cartridge rom
          return rom.get(addr);
        }
        if (0x0100 <= addr && addr <= 0x01ff) {
          // cartridge rom
          return rom.get(addr);
        }
        if (0x0200 <= addr && addr <= 0x08ff) {
          // cartridge rom
          return rom.get(addr);
        }
      }
      case 1 -> {
        if (0x0000 <= addr && addr <= 0x00ff) {
          // boot rom (lower)
          return boot.get(addr);
        }
        if (0x0100 <= addr && addr <= 0x01ff) {
          // cartridge rom hole
          return rom.get(addr);
        }
        if (0x0200 <= addr && addr <= 0x08ff) {
          // boot rom (upper)
          return boot.get(addr);
        }
      }
      default -> throw new IllegalStateException();
    }
    if (0x0900 <= addr && addr <= 0x3fff) {
      // cartridge rom
      return rom.get(addr);
    }
    if (0x4000 <= addr && addr <= 0x7fff) {
      // TODO: switchable bank
      return rom.get(addr);
    }
    if (0x8000 <= addr && addr <= 0x9fff) {
      return vram.get(addr - 0x8000);
    }
    if (0xa000 <= addr && addr <= 0xbfff) {
      // cartridge ram
      throw new IllegalStateException("%04x".formatted(address));
    }
    if (0xc000 <= addr && addr <= 0xcfff) {
      return wram.get(addr - 0xc000);
    }
    if (0xd000 <= addr && addr <= 0xdfff) {
      return wram.get(getWramBank() * 0x1000 + (addr - 0xd000));
    }
    if (0xe000 <= addr && addr <= 0xfdff) {
      // echo ram (unusable?)
      // throw new IllegalStateException("%04x".formatted(address));
      return (byte) 0xff;
    }
    if (0xfe00 <= addr && addr <= 0xfe9f) {
      return oam.get(addr - 0xfe00);
    }
    if (0xfea0 <= addr && addr <= 0xfeff) {
      // unusable
      throw new IllegalStateException("%04x".formatted(address));
    }
    if (0x0ff00 <= addr && addr <= 0xff7f) {
      return registers.get(addr - 0xff00);
    }
    if (0xff80 <= addr && addr <= 0xfffe) {
      return hram.get(addr - 0xff80);
    }
    if (addr == 0xffff) {
      return ie.get(addr - 0xffff);
    }
    throw new IllegalStateException("%04x".formatted(address));
  }

  @Override
  public void write(short address, byte data) {
    int addr = Short.toUnsignedInt(address);
    assert 0x0000 <= addr && addr <= 0xffff;
    if (0x0000 <= addr && addr <= 0x7fff) {
      // throw new IllegalStateException("%04x".formatted(address));
      return;
    }
    if (0x8000 <= addr && addr <= 0x9fff) {
      vram.put(addr - 0x8000, data);
      return;
    }
    if (0xa000 <= addr && addr <= 0xbfff) {
      // cartridge ram
      throw new IllegalStateException("%04x".formatted(address));
    }
    if (0xc000 <= addr && addr <= 0xcfff) {
      wram.put(addr - 0xc000, data);
      return;
    }
    if (0xd000 <= addr && addr <= 0xdfff) {
      wram.put(getWramBank() * 0x1000 + (addr - 0xd000), data);
      return;
    }
    if (0xe000 <= addr && addr <= 0xfdff) {
      // echo ram (unusable?)
      throw new IllegalStateException("%04x".formatted(address));
    }
    if (0xfe00 <= addr && addr <= 0xfe9f) {
      oam.put(addr - 0xfe00, data);
      return;
    }
    if (0xfea0 <= addr && addr <= 0xfeff) {
      // unusable
      throw new IllegalStateException("%04x".formatted(address));
    }
    if (0x0ff00 <= addr && addr <= 0xff7f) {
      registers.put(addr - 0xff00, data);
      return;
    }
    if (0xff80 <= addr && addr <= 0xfffe) {
      hram.put(addr - 0xff80, data);
      return;
    }
    if (addr == 0xffff) {
      ie.put(addr - 0xffff, data);
      return;
    }
    throw new IllegalStateException("%04x".formatted(address));
  }

  @Override
  public void none(short address) {}

  private int getWramBank() {
    return switch (ie.get(0)) {
      case 0, 1 -> 1;
      case 2 -> 2;
      case 3 -> 3;
      case 4 -> 4;
      case 5 -> 5;
      case 6 -> 6;
      case 7 -> 7;
      default -> throw new IllegalStateException();
    };
  }
}
