package com.zacharyhirsch.moldygameboy.emulator.memory;

import com.zacharyhirsch.moldygameboy.emulator.arch.IoRegister;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The memory on the cartridge and video buses. The CGB has two banks of VRAM, selected by VBK, and
 * eight of WRAM: $C000-$CFFF is always bank 0, and $D000-$DFFF is the bank selected by SVBK (where
 * 0 selects bank 1). On a DMG, VBK and SVBK stay 0, so VRAM bank 0 and WRAM banks 0-1 are used.
 */
public class Memory {

  public static final int VRAM_SIZE = 0x4000;

  private final Cartridge cartridge;
  private final Mapper mapper;
  private final ByteBuffer oam;
  private final ByteBuffer vram;
  private final ByteBuffer wram;

  private int vramBank = 0;
  private int wramBank = 0;

  public Memory(Cartridge cartridge, ByteBuffer vram, ByteBuffer oam) {
    this.cartridge = cartridge;
    this.mapper = cartridge.mapper();
    this.oam = oam;
    this.vram = vram;
    this.wram = ByteBuffer.allocate(0x8000);
  }

  /** Advances the cartridge by one dot. */
  public void tick() {
    cartridge.tick();
  }

  /**
   * Writes the cartridge's battery-backed state to {@code sink}, if it has a battery and the state
   * has changed since it was last saved; otherwise does nothing.
   */
  public void save(SaveSink sink) throws IOException {
    cartridge.save(sink);
  }

  /** Restores the cartridge's battery-backed state from a save file. */
  public void load(byte[] save) {
    cartridge.load(save);
  }

  /** VBK: which VRAM bank the CPU sees at $8000-$9FFF. */
  public IoRegister vbk() {
    return new IoRegister() {
      @Override
      public int get() {
        return vramBank;
      }

      @Override
      public void set(int value) {
        vramBank = value & 0x01;
      }

      @Override
      public String toString() {
        return "vbk @ ff4f = %02x".formatted(get());
      }
    };
  }

  /** SVBK: which WRAM bank the CPU sees at $D000-$DFFF. */
  public IoRegister svbk() {
    return new IoRegister() {
      @Override
      public int get() {
        return wramBank;
      }

      @Override
      public void set(int value) {
        wramBank = value & 0x07;
      }

      @Override
      public String toString() {
        return "svbk @ ff70 = %02x".formatted(get());
      }
    };
  }

  public byte read(short address) {
    int addr = Short.toUnsignedInt(address);
    assert 0x0000 <= addr && addr <= 0xfeff;
    if (0x0000 <= addr && addr <= 0x7fff) {
      return this.mapper.mapRom().read(address);
    }
    if (0x8000 <= addr && addr <= 0x9fff) {
      return this.vram.get(vramOffset(addr));
    }
    if (0xa000 <= addr && addr <= 0xbfff) {
      return this.mapper.mapRam().read(address);
    }
    if (0xc000 <= addr && addr <= 0xfdff) {
      return this.wram.get(wramOffset(addr));
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
      this.vram.put(vramOffset(addr), value);
      return;
    }
    if (0xa000 <= addr && addr <= 0xbfff) {
      this.mapper.mapRam().write(address, value);
      return;
    }
    if (0xc000 <= addr && addr <= 0xfdff) {
      this.wram.put(wramOffset(addr), value);
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

  private int vramOffset(int addr) {
    return vramBank * 0x2000 + (addr - 0x8000);
  }

  /** $E000-$FDFF echoes $C000-$DDFF. */
  private int wramOffset(int addr) {
    int offset = (addr & 0xdfff) - 0xc000;
    if (offset < 0x1000) {
      return offset;
    }
    return Math.max(1, wramBank) * 0x1000 + (offset - 0x1000);
  }
}
