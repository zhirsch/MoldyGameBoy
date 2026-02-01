package com.zacharyhirsch.moldygameboy.emulator.memory;

import com.zacharyhirsch.moldygameboy.emulator.bus.AddressBus;
import com.zacharyhirsch.moldygameboy.emulator.bus.DataBus;
import java.nio.ByteBuffer;

public final class Memory {

  private final AddressBus addressBus;
  private final DataBus dataBus;
  private final ByteBuffer boot;
  private final ByteBuffer rom;
  private final ByteBuffer vram;
  private final ByteBuffer wram;
  private final ByteBuffer oam;
  private final ByteBuffer hram;
  private final ByteBuffer waveRam;

  private byte sb = 0; // ff01
  private byte sc = 0; // ff02
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

  public Memory(AddressBus addressBus, DataBus dataBus, ByteBuffer boot, ByteBuffer rom) {
    this.boot = boot;
    this.rom = rom;
    this.addressBus = addressBus;
    this.dataBus = dataBus;
    this.vram = ByteBuffer.allocate(0x4000);
    this.wram = ByteBuffer.allocate(0x8000);
    this.oam = ByteBuffer.allocate(0xa0);
    this.hram = ByteBuffer.allocate(0x7f);
    this.waveRam = ByteBuffer.allocate(0x10);
  }

  public byte read(int address) {
    assert 0x0000 <= address && address <= 0xffff;
    if (bank == 1) {
      if (0x0000 <= address && address <= 0x00ff) {
        // boot rom (lower)
        return boot.get(address);
      }
      if (0x0100 <= address && address <= 0x01ff) {
        // cartridge rom hole
        return rom.get(address);
      }
      if (0x0200 <= address && address <= 0x08ff) {
        // boot rom (upper)
        return boot.get(address);
      }
    } else {
      if (0x0000 <= address && address <= 0x08ff) {
        // cartridge rom
        return rom.get(address);
      }
    }
    if (0x0900 <= address && address <= 0x3fff) {
      throw new IllegalStateException("%04x".formatted(addressBus.get()));
    }
    if (0x4000 <= address && address <= 0x7fff) {
      // TODO: switchable bank
      return rom.get(address);
    }
    if (0x8000 <= address && address <= 0x9fff) {
      return vram.get(address - 0x8000);
    }
    if (0xa000 <= address && address <= 0xbfff) {
      // cartridge ram
      throw new IllegalStateException("%04x".formatted(addressBus.get()));
    }
    if (0xc000 <= address && address <= 0xcfff) {
      return wram.get(address - 0xc000);
    }
    if (0xd000 <= address && address <= 0xdfff) {
      return wram.get(svbk * 0x1000 + (address - 0xd000));
    }
    if (0xe000 <= address && address <= 0xfdff) {
      // echo ram (unusable)
      throw new IllegalStateException("%04x".formatted(addressBus.get()));
    }
    if (0xfe00 <= address && address <= 0xfe9f) {
      return oam.get(address - 0xfe00);
    }
    if (0xfea0 <= address && address <= 0xfeff) {
      // unusable
      throw new IllegalStateException("%04x".formatted(addressBus.get()));
    }
    if (0x0ff00 <= address && address <= 0xff7f) {
      // i/o registers
      return switch (address) {
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
        default -> throw new IllegalStateException("%04x".formatted(addressBus.get()));
      };
    }
    if (0xff80 <= address && address <= 0xfffe) {
      return hram.get(address - 0xff80);
    }
    if (address == 0xffff) {
      return ie;
    }
    throw new IllegalStateException("%04x".formatted(addressBus.get()));
  }

  public void write() {
    int address = Short.toUnsignedInt(addressBus.get());
    assert 0x0000 <= address && address <= 0xffff;
    if (0x0000 <= address && address <= 0x7fff) {
      throw new IllegalStateException("%04x".formatted(addressBus.get()));
    }
    if (0x8000 <= address && address <= 0x9fff) {
      vram.put(address - 0x8000, dataBus.get());
      return;
    }
    if (0xa000 <= address && address <= 0xbfff) {
      // cartridge ram
      throw new IllegalStateException("%04x".formatted(addressBus.get()));
    }
    if (0xc000 <= address && address <= 0xcfff) {
      wram.put(address - 0xc000, dataBus.get());
      return;
    }
    if (0xd000 <= address && address <= 0xdfff) {
      wram.put(svbk * 0x1000 + (address - 0xd000), dataBus.get());
      return;
    }
    if (0xe000 <= address && address <= 0xfdff) {
      // echo ram (unusable)
      throw new IllegalStateException("%04x".formatted(addressBus.get()));
    }
    if (0xfe00 <= address && address <= 0xfe9f) {
      oam.put(address - 0xfe00, dataBus.get());
      return;
    }
    if (0xfea0 <= address && address <= 0xfeff) {
      // unusable
      throw new IllegalStateException("%04x".formatted(addressBus.get()));
    }
    if (0x0ff00 <= address && address <= 0xff7f) {
      // i/o registers
      switch (address) {
        case 0xff01 -> {
          sb = dataBus.get();
          System.out.printf("%s", (char) sb);
        }
        case 0xff02 -> sc = dataBus.get();
        case 0xff07 -> tac = (byte) (dataBus.get() & 0b0000_0111);
        case 0xff0f -> if_ = (byte) (dataBus.get() & 0b0001_1111);
        case 0xff11 -> nr11 = dataBus.get();
        case 0xff12 -> nr12 = dataBus.get();
        case 0xff24 -> nr50 = dataBus.get();
        case 0xff25 -> nr51 = dataBus.get();
        case 0xff26 -> nr52 = (byte) ((dataBus.get() & 0b1000_0000) | (nr52 & 0b0111_1111));
        case 0xff30 -> waveRam.put(0x0, dataBus.get());
        case 0xff31 -> waveRam.put(0x1, dataBus.get());
        case 0xff32 -> waveRam.put(0x2, dataBus.get());
        case 0xff33 -> waveRam.put(0x3, dataBus.get());
        case 0xff34 -> waveRam.put(0x4, dataBus.get());
        case 0xff35 -> waveRam.put(0x5, dataBus.get());
        case 0xff36 -> waveRam.put(0x6, dataBus.get());
        case 0xff37 -> waveRam.put(0x7, dataBus.get());
        case 0xff38 -> waveRam.put(0x8, dataBus.get());
        case 0xff39 -> waveRam.put(0x9, dataBus.get());
        case 0xff3a -> waveRam.put(0xa, dataBus.get());
        case 0xff3b -> waveRam.put(0xb, dataBus.get());
        case 0xff3c -> waveRam.put(0xc, dataBus.get());
        case 0xff3d -> waveRam.put(0xd, dataBus.get());
        case 0xff3e -> waveRam.put(0xe, dataBus.get());
        case 0xff3f -> waveRam.put(0xf, dataBus.get());
        case 0xff40 -> lcdc = dataBus.get();
        case 0xff42 -> scy = dataBus.get();
        case 0xff43 -> scx = dataBus.get();
        case 0xff47 -> bgp = dataBus.get();
        case 0xff48 -> obp0 = dataBus.get();
        case 0xff49 -> obp1 = dataBus.get();
        case 0xff4f -> vbk = (byte) (dataBus.get() & 0b0000_0001);
        case 0xff68 -> bcps = (byte) (dataBus.get() & 0b1011_1111);
        case 0xff69 -> {} // TODO: write background color palette at address in bcps
        case 0xff70 -> svbk = (byte) (dataBus.get() & 0b0000_0111);
        default -> throw new IllegalStateException("%04x".formatted(addressBus.get()));
      }
      return;
    }
    if (0xff80 <= address && address <= 0xfffe) {
      hram.put(address - 0xff80, dataBus.get());
      return;
    }
    if (address == 0xffff) {
      ie = dataBus.get();
      return;
    }
    throw new IllegalStateException("%04x".formatted(addressBus.get()));
  }
}
