package com.zacharyhirsch.moldygameboy.emulator.memory;

import com.zacharyhirsch.moldygameboy.emulator.arch.MemoryRange;
import java.nio.ByteBuffer;

public final class MemoryMap implements MemoryRange {

  private final ByteBuffer boot;
  private final ByteBuffer rom;
  private final ByteBuffer vram;
  private final ByteBuffer wram;
  private final ByteBuffer oam;
  private final ByteBuffer hram;
  private final ByteBuffer waveRam;
  private final IORegisters ioRegisters;

  public MemoryMap(ByteBuffer boot, ByteBuffer rom, IORegisters ioRegisters) {
    this.boot = boot;
    this.rom = rom;
    this.vram = ByteBuffer.allocate(0x4000);
    this.wram = ByteBuffer.allocate(0x8000);
    this.oam = ByteBuffer.allocate(0xa0);
    this.hram = ByteBuffer.allocate(0x7f);
    this.waveRam = ByteBuffer.allocate(0x10);
    this.ioRegisters = ioRegisters;
  }

  @Override
  public byte read(short address) {
    int addr = Short.toUnsignedInt(address);
    assert 0x0000 <= addr && addr <= 0xffff;
    if (ioRegisters.bank().get() == 1) {
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
    if (0x0000 <= addr && addr <= 0x3fff) {
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
      // echo ram (unusable)
      //      throw new IllegalStateException("%04x".formatted(address));
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
      // i/o registers
      // TODO: unreadable bits always return 1.
      return switch (addr) {
        //        case 0xff0f -> (byte) (0b1110_0000 | (ioRegisters.if_().get() & 0b0001_1111));
        case 0xff0f -> (byte) (ioRegisters.if_().get() & 0b0001_1111);
        case 0xff11 -> (byte) (ioRegisters.nr11().get() & 0b1100_0000);
        case 0xff12 -> ioRegisters.nr12().get();
        case 0xff24 -> ioRegisters.nr50().get();
        case 0xff25 -> ioRegisters.nr51().get();
        case 0xff26 -> (byte) (0b0111_0000 | (ioRegisters.nr52().get() & 0b1000_1111));
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
        case 0xff47 -> ioRegisters.bgp().get();
        case 0xff48 -> ioRegisters.obp0().get();
        case 0xff49 -> ioRegisters.obp1().get();
        case 0xff4d -> (byte) 0xff; // TODO // ioRegisters.key1().get();
        case 0xff4f -> (byte) (0b1111_1100 | (ioRegisters.vbk().get() & 0b0000_0011));
        case 0xff70 -> (byte) (0b1111_1000 | (ioRegisters.svbk().get() & 0b0000_0111));
        default -> throw new IllegalStateException("%04x".formatted(address));
      };
    }
    if (0xff80 <= addr && addr <= 0xfffe) {
      return hram.get(addr - 0xff80);
    }
    if (addr == 0xffff) {
      return ioRegisters.ie().get();
    }
    throw new IllegalStateException("%04x".formatted(address));
  }

  @Override
  public void write(short address, byte data) {
    int addr = Short.toUnsignedInt(address);
    assert 0x0000 <= addr && addr <= 0xffff;
    if (0x0000 <= addr && addr <= 0x7fff) {
      //      throw new IllegalStateException("%04x".formatted(address));
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
          ioRegisters.sb().set(data);
          System.out.printf("%s", (char) data);
        }
        case 0xff02 -> ioRegisters.sc().set(data);
        case 0xff04 -> ioRegisters.div().set((byte) 0);
        case 0xff05 -> ioRegisters.tima().set(data);
        case 0xff06 -> ioRegisters.tma().set(data);
        case 0xff07 -> ioRegisters.tac().set((byte) (data & 0b0000_0111));
        case 0xff0f -> ioRegisters.if_().set((byte) (data & 0b0001_1111));
        case 0xff11 -> ioRegisters.nr11().set(data);
        case 0xff12 -> ioRegisters.nr12().set(data);
        case 0xff24 -> ioRegisters.nr50().set(data);
        case 0xff25 -> ioRegisters.nr51().set(data);
        case 0xff26 ->
            ioRegisters
                .nr52()
                .set((byte) ((data & 0b1000_0000) | (ioRegisters.nr52().get() & 0b0111_1111)));
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
        case 0xff40 -> ioRegisters.lcdc().set(data);
        case 0xff41 -> {} // TODO
        case 0xff42 -> ioRegisters.scy().set(data);
        case 0xff43 -> ioRegisters.scx().set(data);
        case 0xff47 -> ioRegisters.bgp().set(data);
        case 0xff48 -> ioRegisters.obp0().set(data);
        case 0xff49 -> ioRegisters.obp1().set(data);
        case 0xff4f -> ioRegisters.vbk().set((byte) (data & 0b0000_0011));
        case 0xff68 -> ioRegisters.bcps().set((byte) (data & 0b1011_1111));
        case 0xff69 -> {} // TODO: write background color palette at address in bcps
        case 0xff70 -> ioRegisters.svbk().set((byte) (data & 0b0000_0111));
        default -> throw new IllegalStateException("%04x".formatted(address));
      }
      return;
    }
    if (0xff80 <= addr && addr <= 0xfffe) {
      hram.put(addr - 0xff80, data);
      return;
    }
    if (addr == 0xffff) {
      ioRegisters.ie().set(data);
      return;
    }
    throw new IllegalStateException("%04x".formatted(address));
  }

  @Override
  public void none(short address) {}

  private int getWramBank() {
    return switch (ioRegisters.svbk().get()) {
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
