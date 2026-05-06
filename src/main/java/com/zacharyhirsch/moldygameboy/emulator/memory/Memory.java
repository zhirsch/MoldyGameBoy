package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.nio.ByteBuffer;

public final class Memory {

  private final ByteBuffer boot;
  private final ByteBuffer rom;
  private final ByteBuffer vram;
  private final ByteBuffer ram;
  private final ByteBuffer wram;
  private final ByteBuffer oam;
  private final Registers registers;
  private final ByteBuffer hram;
  private final ByteBuffer ie;

  public Memory(ByteBuffer boot, ByteBuffer rom) {
    this.boot = boot;
    this.rom = rom;
    this.vram = ByteBuffer.allocate(0x4000);
    this.ram = ByteBuffer.allocate(0x2000);
    this.wram = ByteBuffer.allocate(0x8000);
    this.oam = ByteBuffer.allocate(0xa0);
    this.registers = new Registers();
    this.hram = ByteBuffer.allocate(0x7f);
    this.ie = ByteBuffer.allocate(0x01);
  }

  public Registers registers() {
    return registers;
  }

  public byte ie() {
    return ie.get(0);
  }

  public byte read(short address) {
    int addr = Short.toUnsignedInt(address);
    assert 0x0000 <= addr && addr <= 0xffff;
    if (0x0000 <= addr && addr <= 0x00ff) {
      return getRomBank().get(addr);
    }
    if (0x0100 <= addr && addr <= 0x01ff) {
      return rom.get(addr);
    }
    if (0x0200 <= addr && addr <= 0x08ff) {
      return getRomBank().get(addr);
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
      return ram.get(addr - 0xa000);
    }
    if (0xc000 <= addr && addr <= 0xcfff) {
      return wram.get(addr - 0xc000);
    }
    if (0xd000 <= addr && addr <= 0xdfff) {
      return getWramBank().get(addr - 0xd000);
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
      return registers.at(addr - 0xff00).read();
    }
    if (0xff80 <= addr && addr <= 0xfffe) {
      return hram.get(addr - 0xff80);
    }
    if (addr == 0xffff) {
      return ie.get(addr - 0xffff);
    }
    throw new IllegalStateException("%04x".formatted(address));
  }

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
      getWramBank().put(addr - 0xd000, data);
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
      registers.at(addr - 0xff00).write(data);
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

  public void none(short ignored) {}

  private ByteBuffer getRomBank() {
    return registers.bank().get() == 0 ? boot : rom;
  }

  private ByteBuffer getWramBank() {
    int index =
        switch (registers.svbk().get()) {
          case 0, 1 -> 1;
          case 2 -> 2;
          case 3 -> 3;
          case 4 -> 4;
          case 5 -> 5;
          case 6 -> 6;
          case 7 -> 7;
          default -> throw new IllegalStateException();
        };
    return wram.slice(index * 0x1000, 0x1000);
  }

}
