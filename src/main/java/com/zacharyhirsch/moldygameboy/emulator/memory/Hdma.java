package com.zacharyhirsch.moldygameboy.emulator.memory;

import com.zacharyhirsch.moldygameboy.emulator.arch.IoRegister;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

/**
 * The CGB's VRAM DMA, which copies 16-byte blocks to VRAM from HDMA1-2 (source) to HDMA3-4
 * (destination). Writing HDMA5 starts a transfer of (bits 0-6) + 1 blocks: with bit 7 clear, a
 * general-purpose transfer copies them all at once; with bit 7 set, an HBlank transfer copies one
 * block at the start of each HBlank (or straight away if the LCD is off). Either way, the CPU is
 * stopped while bytes are being copied, at 2 bytes per M-cycle.
 */
public final class Hdma {

  private static final int BLOCK_SIZE = 16;

  private final Memory memory;
  private final BooleanSupplier lcdOn;

  private int source = 0;
  private int destination = 0;
  private int blocksLeft = 0;
  private boolean active = false;
  private boolean hblank = false;
  private int bytesToCopy = 0;

  public Hdma(Memory memory, BooleanSupplier lcdOn) {
    this.memory = memory;
    this.lcdOn = lcdOn;
  }

  /** HDMA1: the high byte of the source address. */
  public IoRegister hdma1() {
    return register("hdma1", 0xff51, v -> source = (v << 8) | (source & 0x00f0));
  }

  /** HDMA2: the low byte of the source address (the low 4 bits are ignored). */
  public IoRegister hdma2() {
    return register("hdma2", 0xff52, v -> source = (source & 0xff00) | (v & 0xf0));
  }

  /** HDMA3: the high byte of the destination address in VRAM (the upper 3 bits are ignored). */
  public IoRegister hdma3() {
    return register(
        "hdma3", 0xff53, v -> destination = ((v & 0x1f) << 8) | (destination & 0xf0));
  }

  /** HDMA4: the low byte of the destination address (the low 4 bits are ignored). */
  public IoRegister hdma4() {
    return register("hdma4", 0xff54, v -> destination = (destination & 0x1f00) | (v & 0xf0));
  }

  /**
   * HDMA5: starts a transfer, or stops an HBlank transfer when written with bit 7 clear. Reads give
   * the number of blocks left minus 1, with bit 7 clear while a transfer is active ($FF when done).
   */
  public IoRegister hdma5() {
    return new IoRegister() {
      @Override
      public int get() {
        return (active ? 0x00 : 0x80) | ((blocksLeft - 1) & 0x7f);
      }

      @Override
      public void set(int value) {
        if (active && hblank && (value & 0x80) == 0) {
          active = false;
          return;
        }
        blocksLeft = (value & 0x7f) + 1;
        hblank = (value & 0x80) != 0;
        active = true;
        if (!hblank) {
          bytesToCopy = blocksLeft * BLOCK_SIZE;
        } else if (!lcdOn.getAsBoolean()) {
          bytesToCopy = BLOCK_SIZE;
        }
      }

      @Override
      public String toString() {
        return "hdma5 @ ff55 = %02x".formatted(get());
      }
    };
  }

  /** Whether bytes are being copied, which stops the CPU. */
  public boolean isTransferring() {
    return bytesToCopy > 0;
  }

  /** Called by the PPU at the start of each HBlank. */
  public void onHBlank() {
    if (active && hblank && bytesToCopy == 0) {
      bytesToCopy = BLOCK_SIZE;
    }
  }

  /** Advances by one M-cycle. */
  public void tick() {
    for (int i = 0; i < 2 && bytesToCopy > 0; i++) {
      copyByte();
    }
  }

  private void copyByte() {
    byte value = source < 0xe000 ? memory.read((short) source) : (byte) 0xff;
    memory.write((short) (0x8000 | destination), value);
    source = (source + 1) & 0xffff;
    destination++;
    bytesToCopy--;
    if ((destination & 0x0f) == 0) {
      blocksLeft--;
      // The transfer also ends if the destination runs past the end of VRAM.
      if (blocksLeft == 0 || destination > 0x1fff) {
        active = false;
        bytesToCopy = 0;
        destination &= 0x1fff;
      }
    }
  }

  private static IoRegister register(String name, int address, IntConsumer set) {
    return new IoRegister() {
      @Override
      public int get() {
        return 0xff;
      }

      @Override
      public void set(int value) {
        set.accept(value & 0xff);
      }

      @Override
      public String toString() {
        return "%s @ %04x".formatted(name, address);
      }
    };
  }
}
