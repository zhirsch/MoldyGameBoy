package com.zacharyhirsch.moldygameboy.emulator.ppu;

import com.zacharyhirsch.moldygameboy.emulator.arch.IoRegister;
import com.zacharyhirsch.moldygameboy.emulator.io.Color;

/**
 * One of the CGB's two palette RAMs (background or object): 8 palettes of 4 colors, each color 2
 * bytes of little-endian RGB555. It's accessed through an index register (BCPS/OCPS), whose bit 7
 * makes the index advance after each write to the data register (BCPD/OCPD).
 */
public final class PaletteRam {

  private final String name;
  private final int address;
  private final byte[] ram = new byte[64];

  private int index = 0;
  private boolean autoIncrement = false;

  public PaletteRam(String name, int address) {
    this.name = name;
    this.address = address;
  }

  /**
   * The color for a pixel, converting each 5-bit component to 8 bits with {@code (c << 3) | (c >>
   * 2)}.
   */
  Color color(int palette, int color) {
    int i = palette * 8 + color * 2;
    int rgb555 = Byte.toUnsignedInt(ram[i]) | (Byte.toUnsignedInt(ram[i + 1]) << 8);
    return new Color(
        component(rgb555), component(rgb555 >>> 5), component(rgb555 >>> 10));
  }

  private static byte component(int value) {
    int c = value & 0x1f;
    return (byte) ((c << 3) | (c >>> 2));
  }

  /** BCPS/OCPS: the index into palette RAM, and the auto-increment flag. */
  public IoRegister spec() {
    return new IoRegister() {
      @Override
      public int get() {
        return (autoIncrement ? 0x80 : 0x00) | index;
      }

      @Override
      public void set(int value) {
        autoIncrement = (value & 0x80) != 0;
        index = value & 0x3f;
      }

      @Override
      public String toString() {
        return "%sps @ %04x = %02x".formatted(name, address, get());
      }
    };
  }

  /** BCPD/OCPD: the palette RAM byte at the index. */
  public IoRegister data() {
    return new IoRegister() {
      @Override
      public int get() {
        return Byte.toUnsignedInt(ram[index]);
      }

      @Override
      public void set(int value) {
        ram[index] = (byte) value;
        if (autoIncrement) {
          index = (index + 1) & 0x3f;
        }
      }

      @Override
      public String toString() {
        return "%spd @ %04x = %02x".formatted(name, address + 1, get());
      }
    };
  }
}
