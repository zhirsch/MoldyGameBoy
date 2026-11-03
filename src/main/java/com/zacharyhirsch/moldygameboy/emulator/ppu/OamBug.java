package com.zacharyhirsch.moldygameboy.emulator.ppu;

import com.zacharyhirsch.moldygameboy.emulator.arch.Model;
import java.nio.ByteBuffer;
import java.util.function.IntSupplier;

/**
 * The DMG's OAM corruption bug. While the PPU scans OAM in mode 2 (one 8-byte row per M-cycle),
 * the CPU putting an OAM address on the bus (by reading or writing it, or by incrementing or
 * decrementing a register pair that holds it) corrupts the row being scanned, using words from the
 * row before it. Row 0 is never corrupted, and the CGB isn't affected. See the Pan Docs' "OAM
 * Corruption Bug" and blargg oam_bug.
 */
public final class OamBug {

  private static final int ROWS = 20;

  private final boolean affected;
  private final ByteBuffer oam;
  private final IntSupplier scanRow;

  /** {@code scanRow} gives the row the PPU is scanning, or -1 when it isn't scanning OAM. */
  public OamBug(Model model, ByteBuffer oam, IntSupplier scanRow) {
    this.affected = model == Model.DMG;
    this.oam = oam;
    this.scanRow = scanRow;
  }

  /** A write to {@code address}, or an increment or decrement of a register pair holding it. */
  public void write(short address) {
    int row = corruptibleRow(address);
    if (row > 0) {
      int a = word(row, 0);
      int b = word(row - 1, 0);
      int c = word(row - 1, 2);
      corrupt(row, ((a ^ c) & (b ^ c)) ^ c);
    }
  }

  /** A read from {@code address}. */
  public void read(short address) {
    int row = corruptibleRow(address);
    if (row > 0) {
      read(row);
    }
  }

  /**
   * A read from {@code address} in the same M-cycle as an increment or decrement of the register
   * pair holding it (LD A,(HL+/-), POP). Unless the row is one of the first four or the last, the
   * row before it is corrupted and copied over this row and the one two before; then it's
   * corrupted like a read.
   */
  public void readDuringIncrementOrDecrement(short address) {
    int row = corruptibleRow(address);
    if (row <= 0) {
      return;
    }
    if (4 <= row && row < ROWS - 1) {
      int a = word(row - 2, 0);
      int b = word(row - 1, 0);
      int c = word(row, 0);
      int d = word(row - 1, 2);
      setWord(row - 1, 0, (b & (a | c | d)) | (a & c & d));
      copyRow(row - 1, row);
      copyRow(row - 1, row - 2);
    }
    read(row);
  }

  private void read(int row) {
    int a = word(row, 0);
    int b = word(row - 1, 0);
    int c = word(row - 1, 2);
    corrupt(row, b | (a & c));
  }

  /** Replaces the row's first word, and copies the rest of the row from the row before. */
  private void corrupt(int row, int firstWord) {
    setWord(row, 0, firstWord);
    for (int i = 2; i < 8; i++) {
      oam.put(row * 8 + i, oam.get((row - 1) * 8 + i));
    }
  }

  /** The row the PPU is scanning if the bug applies to this access, or -1. */
  private int corruptibleRow(short address) {
    int addr = Short.toUnsignedInt(address);
    if (!affected || addr < 0xfe00 || addr > 0xfeff) {
      return -1;
    }
    return scanRow.getAsInt();
  }

  private void copyRow(int from, int to) {
    for (int i = 0; i < 8; i++) {
      oam.put(to * 8 + i, oam.get(from * 8 + i));
    }
  }

  private int word(int row, int index) {
    int i = row * 8 + index * 2;
    return Byte.toUnsignedInt(oam.get(i)) | (Byte.toUnsignedInt(oam.get(i + 1)) << 8);
  }

  private void setWord(int row, int index, int value) {
    int i = row * 8 + index * 2;
    oam.put(i, (byte) value);
    oam.put(i + 1, (byte) (value >>> 8));
  }
}
