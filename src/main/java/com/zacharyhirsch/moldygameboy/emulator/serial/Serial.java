package com.zacharyhirsch.moldygameboy.emulator.serial;

import com.zacharyhirsch.moldygameboy.emulator.arch.IoRegister;
import com.zacharyhirsch.moldygameboy.emulator.arch.Irq;
import java.util.function.IntSupplier;

/**
 * The serial port, with nothing connected to it. A transfer using the internal clock shifts one
 * bit every 512 dots (8192 Hz), on edges divided from the system counter, so they're aligned to
 * the reset time rather than to the SC write. The edges come 4 dots before bit 8 of the system
 * counter falls (see mooneye serial/boot_sclk_align-dmgABCmgb). With no other Game Boy attached,
 * every received bit is 1, and transfers using the external clock never finish.
 */
public final class Serial {

  private static final int CLOCK_BIT = 1 << 8;
  private static final int CLOCK_OFFSET = 4;

  private final IntSupplier counter;
  private final Irq irq;

  private int sb = 0;
  private int sc = 0;
  private int bitsLeft = 0;
  private boolean clock = false;

  public Serial(IntSupplier counter, Irq irq) {
    this.counter = counter;
    this.irq = irq;
  }

  public IoRegister sb() {
    return new IoRegister() {
      @Override
      public int get() {
        return sb;
      }

      @Override
      public void set(int value) {
        sb = Byte.toUnsignedInt((byte) value);
      }

      @Override
      public String toString() {
        return "sb @ ff01 = %02x".formatted(get());
      }
    };
  }

  public IoRegister sc() {
    return new IoRegister() {
      @Override
      public int get() {
        return sc;
      }

      @Override
      public void set(int value) {
        sc = Byte.toUnsignedInt((byte) value);
        bitsLeft = (sc & 0x81) == 0x81 ? 8 : 0;
      }

      @Override
      public String toString() {
        return "sc @ ff02 = %02x".formatted(get());
      }
    };
  }

  public void tick() {
    boolean newClock = ((counter.getAsInt() + CLOCK_OFFSET) & CLOCK_BIT) != 0;
    if (clock && !newClock && bitsLeft > 0) {
      sb = ((sb << 1) | 1) & 0xff;
      if (--bitsLeft == 0) {
        sc &= 0x7f;
        irq.set(true);
      }
    } else {
      irq.set(false);
    }
    clock = newClock;
  }
}
