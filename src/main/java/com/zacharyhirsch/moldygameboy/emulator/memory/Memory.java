package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.nio.ByteBuffer;

public final class Memory {

  private final ByteBuffer boot;
  private final ByteBuffer rom;
  private final ByteBuffer vram;
  private final ByteBuffer wram;
  private final ByteBuffer oam;
  private final ByteBuffer hram;
  private final ByteBuffer waveRam;

  private byte sb = 0; // ff01
  private byte sc = 0; // ff02
  private byte div = 0; // ff04
  private byte tima = 0; // ff05
  private byte tma = 0; // ff06
  private byte tac = 0; // ff07
  private byte if_ = 0; // ff0f
  private byte nr11 = 0; // ff11
  private byte nr12 = 0; // ff12
  private byte nr50 = 0; // ff24
  private byte nr51 = 0; // ff25
  private byte nr52 = 0; // ff26
  private byte lcdc = 0; // ff40
  private byte scy = 0; // ff42
  private byte scx = 0; // ff43
  private byte bgp = 0; // ff47
  private byte obp0 = 0; // ff48
  private byte obp1 = 0; // ff49
  private byte vbk = 0; // ff4f
  private byte bank = 1; // ff50
  private byte bcps = 0; // ff68
  private byte svbk = 0; // ff70
  private byte ie = 0; // ffff

  public Memory(ByteBuffer boot, ByteBuffer rom) {
    this.boot = boot;
    this.rom = rom;
    this.vram = ByteBuffer.allocate(0x4000);
    this.wram = ByteBuffer.allocate(0x8000);
    this.oam = ByteBuffer.allocate(0xa0);
    this.hram = ByteBuffer.allocate(0x7f);
    this.waveRam = ByteBuffer.allocate(0x10);
  }

  public byte read(short address) {
    int addr = Short.toUnsignedInt(address);
    assert 0x0000 <= addr && addr <= 0xffff;
    if (bank == 1) {
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
    } else {
      if (0x0000 <= addr && addr <= 0x08ff) {
        // cartridge rom
        return rom.get(addr);
      }
    }
    if (0x0900 <= addr && addr <= 0x3fff) {
      throw new IllegalStateException("%04x".formatted(address));
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
      return wram.get(svbk * 0x1000 + (addr - 0xd000));
    }
    if (0xe000 <= addr && addr <= 0xfdff) {
      // echo ram (unusable)
      throw new IllegalStateException("%04x".formatted(address));
    }
    if (0xfe00 <= addr && addr <= 0xfe9f) {
      return oam.get(addr - 0xfe00);
    }
    if (0xfea0 <= addr && addr <= 0xfeff) {
      // unusable
      throw new IllegalStateException("%04x".formatted(address));
    }
    if (0x0ff00 <= addr && addr <= 0xff7f) {
      // i/o registers
      return switch (addr) {
        case 0xff0f -> if_;
        case 0xff11 -> (byte) (nr11 & 0b1100_0000);
        case 0xff12 -> nr12;
        case 0xff24 -> nr50;
        case 0xff25 -> nr51;
        case 0xff26 -> nr52;
        case 0xff30 -> waveRam.get(0x0);
        case 0xff31 -> waveRam.get(0x1);
        case 0xff32 -> waveRam.get(0x2);
        case 0xff33 -> waveRam.get(0x3);
        case 0xff34 -> waveRam.get(0x4);
        case 0xff35 -> waveRam.get(0x5);
        case 0xff36 -> waveRam.get(0x6);
        case 0xff37 -> waveRam.get(0x7);
        case 0xff38 -> waveRam.get(0x8);
        case 0xff39 -> waveRam.get(0x9);
        case 0xff3a -> waveRam.get(0xa);
        case 0xff3b -> waveRam.get(0xb);
        case 0xff3c -> waveRam.get(0xc);
        case 0xff3d -> waveRam.get(0xd);
        case 0xff3e -> waveRam.get(0xe);
        case 0xff3f -> waveRam.get(0xf);
        case 0xff44 -> (byte) 0x90; // TODO;
        case 0xff47 -> bgp;
        case 0xff48 -> obp0;
        case 0xff49 -> obp1;
        case 0xff4f -> (byte) (0b1111_1110 | vbk);
        case 0xff70 -> svbk;
        default -> throw new IllegalStateException("%04x".formatted(address));
      };
    }
    if (0xff80 <= addr && addr <= 0xfffe) {
      return hram.get(addr - 0xff80);
    }
    if (addr == 0xffff) {
      return ie;
    }
    throw new IllegalStateException("%04x".formatted(address));
  }

  public void write(short address, byte data) {
    int addr = Short.toUnsignedInt(address);
    assert 0x0000 <= addr && addr <= 0xffff;
    if (0x0000 <= addr && addr <= 0x7fff) {
      throw new IllegalStateException("%04x".formatted(address));
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
      wram.put(svbk * 0x1000 + (addr - 0xd000), data);
      return;
    }
    if (0xe000 <= addr && addr <= 0xfdff) {
      // echo ram (unusable)
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
      // i/o registers
      switch (addr) {
        case 0xff01 -> {
          sb = data;
          System.out.printf("%s", (char) sb);
        }
        case 0xff02 -> sc = data;
        case 0xff04 -> div = 0;
        case 0xff05 -> tima = data;
        case 0xff06 -> tma = data;
        case 0xff07 -> tac = (byte) (data & 0b0000_0111);
        case 0xff0f -> if_ = (byte) (data & 0b0001_1111);
        case 0xff11 -> nr11 = data;
        case 0xff12 -> nr12 = data;
        case 0xff24 -> nr50 = data;
        case 0xff25 -> nr51 = data;
        case 0xff26 -> nr52 = (byte) ((data & 0b1000_0000) | (nr52 & 0b0111_1111));
        case 0xff30 -> waveRam.put(0x0, data);
        case 0xff31 -> waveRam.put(0x1, data);
        case 0xff32 -> waveRam.put(0x2, data);
        case 0xff33 -> waveRam.put(0x3, data);
        case 0xff34 -> waveRam.put(0x4, data);
        case 0xff35 -> waveRam.put(0x5, data);
        case 0xff36 -> waveRam.put(0x6, data);
        case 0xff37 -> waveRam.put(0x7, data);
        case 0xff38 -> waveRam.put(0x8, data);
        case 0xff39 -> waveRam.put(0x9, data);
        case 0xff3a -> waveRam.put(0xa, data);
        case 0xff3b -> waveRam.put(0xb, data);
        case 0xff3c -> waveRam.put(0xc, data);
        case 0xff3d -> waveRam.put(0xd, data);
        case 0xff3e -> waveRam.put(0xe, data);
        case 0xff3f -> waveRam.put(0xf, data);
        case 0xff40 -> lcdc = data;
        case 0xff42 -> scy = data;
        case 0xff43 -> scx = data;
        case 0xff47 -> bgp = data;
        case 0xff48 -> obp0 = data;
        case 0xff49 -> obp1 = data;
        case 0xff4f -> vbk = (byte) (data & 0b0000_0001);
        case 0xff68 -> bcps = (byte) (data & 0b1011_1111);
        case 0xff69 -> {} // TODO: write background color palette at address in bcps
        case 0xff70 -> svbk = (byte) (data & 0b0000_0111);
        default -> throw new IllegalStateException("%04x".formatted(address));
      }
      return;
    }
    if (0xff80 <= addr && addr <= 0xfffe) {
      hram.put(addr - 0xff80, data);
      return;
    }
    if (addr == 0xffff) {
      ie = data;
      return;
    }
    throw new IllegalStateException("%04x".formatted(address));
  }

  public byte getDiv() {
    return div;
  }

  public void setDiv(byte div) {
    this.div = div;
  }

  public byte getTac() {
    return tac;
  }

  public void setTac(byte tac) {
    this.tac = tac;
  }

  public byte getTma() {
    return tma;
  }

  public void setTma(byte tma) {
    this.tma = tma;
  }

  public byte getTima() {
    return tima;
  }

  public void setTima(byte tima) {
    this.tima = tima;
  }

  public byte getIf() {
    return if_;
  }

  public void setIf(byte if_) {
    this.if_ = if_;
  }

  public byte getIe() {
    return ie;
  }

  public void setIe(byte ie) {
    this.ie = ie;
  }
}
