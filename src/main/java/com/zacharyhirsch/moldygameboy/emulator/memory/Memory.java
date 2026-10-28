package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.nio.ByteBuffer;

public class Memory {

  private final Mapper mapper;
  private final ByteBuffer oam;
  private final ByteBuffer vram;
  private final ByteBuffer wram;

  public Memory(Mapper mapper, ByteBuffer vram) {
    this.mapper = mapper;
    this.oam = ByteBuffer.allocate(0x00a0);
    this.vram = vram;
    this.wram = ByteBuffer.allocate(0x2000);
  }

  public byte read(short address) {
    int addr = Short.toUnsignedInt(address);
    assert 0x0000 <= addr && addr <= 0xfeff;
    if (0x0000 <= addr && addr <= 0x7fff) {
      return this.mapper.mapRom().read(address);
    }
    if (0x8000 <= addr && addr <= 0x9fff) {
      return this.vram.get(addr - 0x8000);
    }
    if (0xa000 <= addr && addr <= 0xbfff) {
      return this.mapper.mapRam().read(address);
    }
    if (0xc000 <= addr && addr <= 0xfdff) {
      return this.wram.get((addr & 0xdfff) - 0xc000);
    }
    if (0xfe00 <= addr && addr <= 0xfe9f) {
      return this.oam.get(addr - 0xfe00);
    }
    if (0xfea0 <= addr && addr <= 0xfeff) {
      return new OpenBus().read(address);
    }
    throw new IllegalArgumentException("%04x".formatted(address));
  }

  public void write(short address, byte value) {
    int addr = Short.toUnsignedInt(address);
    assert 0x0000 <= addr && addr <= 0xfeff;
    if (0x0000 <= addr && addr <= 0x7fff) {
      this.mapper.mapRom().write(address, value);
      return;
    }
    if (0x8000 <= addr && addr <= 0x9fff) {
      this.vram.put(addr - 0x8000, value);
      return;
    }
    if (0xa000 <= addr && addr <= 0xbfff) {
      this.mapper.mapRam().write(address, value);
      return;
    }
    if (0xc000 <= addr && addr <= 0xfdff) {
      this.wram.put((addr & 0xdfff) - 0xc000, value);
      return;
    }
    if (0xfe00 <= addr && addr <= 0xfe9f) {
      this.oam.put(addr - 0xfe00, value);
      return;
    }
    if (0xfea0 <= addr && addr <= 0xfeff) {
      new OpenBus().write(address, value);
      return;
    }
    throw new IllegalArgumentException("%04x".formatted(address));
  }
}
