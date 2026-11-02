package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.nio.ByteBuffer;
import java.util.function.LongSupplier;

/**
 * MBC3's real-time clock: seconds, minutes, hours and a 9-bit day counter, counted by a 32768 Hz
 * crystal. DH holds bit 8 of the day counter (bit 0), the halt flag (bit 6, which stops the clock)
 * and the day counter's carry (bit 7, which stays set once the days overflow). Reads give the
 * values copied by the last latch; writes go to the clock itself.
 *
 * <p>The clock runs on emulated time, at one crystal tick per 128 dots. It's saved with a
 * timestamp, so that it catches up on the real time that passed while the emulator wasn't running.
 */
final class RealTimeClock {

  static final int SECONDS = 0x08;
  static final int MINUTES = 0x09;
  static final int HOURS = 0x0a;
  static final int DAYS_LOW = 0x0b;
  static final int DAYS_HIGH = 0x0c;

  /** The size of a saved clock: 5 live and 5 latched registers, then a timestamp. */
  static final int SAVE_SIZE = 48;

  private static final int DOTS_PER_CRYSTAL_TICK = 128;
  private static final int CRYSTAL_TICKS_PER_SECOND = 32768;

  private final LongSupplier epochSeconds;
  private final int[] live = new int[5];
  private final int[] latched = new int[5];

  private int dots = 0;
  private int crystalTicks = 0;
  private boolean dirty = false;

  RealTimeClock() {
    this(() -> System.currentTimeMillis() / 1000);
  }

  /** {@code epochSeconds} gives the current time, for saving and catching up. */
  RealTimeClock(LongSupplier epochSeconds) {
    this.epochSeconds = epochSeconds;
  }

  /** Whether the clock's registers have been written since it was last saved. */
  boolean hasUnsavedChanges() {
    return dirty;
  }

  void markSaved() {
    dirty = false;
  }

  int read(int register) {
    return latched[register - SECONDS];
  }

  void write(int register, int value) {
    live[register - SECONDS] = value & mask(register);
    dirty = true;
    if (register == SECONDS) {
      crystalTicks = 0;
    }
  }

  void latch() {
    System.arraycopy(live, 0, latched, 0, live.length);
  }

  /**
   * Writes the clock in the format BGB and other emulators use: the live registers, then the
   * latched ones, each as a little-endian 32-bit value, then the time as 64-bit Unix seconds.
   */
  void save(ByteBuffer save) {
    for (int value : live) {
      save.putInt(value);
    }
    for (int value : latched) {
      save.putInt(value);
    }
    save.putLong(epochSeconds.getAsLong());
  }

  /**
   * Reads a clock written by {@link #save}, then advances it by the time since it was saved.
   * Saves with a 32-bit timestamp (44 bytes, as some emulators write) are also accepted.
   */
  void load(ByteBuffer save) {
    for (int i = 0; i < live.length; i++) {
      live[i] = save.getInt() & mask(SECONDS + i);
    }
    for (int i = 0; i < latched.length; i++) {
      latched[i] = save.getInt() & mask(SECONDS + i);
    }
    long savedAt = save.remaining() >= 8 ? save.getLong() : Integer.toUnsignedLong(save.getInt());
    advance(Math.max(0, epochSeconds.getAsLong() - savedAt));
  }

  /** Advances the clock (if it isn't halted) by a number of seconds, as if it had been running. */
  void advance(long seconds) {
    if (isHalted()) {
      return;
    }
    // Counters written past their limits don't carry normally, so step those one at a time.
    while (seconds > 0 && !isNormal()) {
      addSecond();
      seconds--;
    }
    if (seconds == 0) {
      return;
    }
    long time =
        live[SECONDS - SECONDS] + 60L * live[MINUTES - SECONDS] + 3600L * live[HOURS - SECONDS];
    time += seconds;
    long days = days() + time / 86400;
    time %= 86400;
    live[SECONDS - SECONDS] = (int) (time % 60);
    live[MINUTES - SECONDS] = (int) (time / 60 % 60);
    live[HOURS - SECONDS] = (int) (time / 3600);
    int high = live[DAYS_HIGH - SECONDS] & 0xc0;
    if (days > 0x1ff) {
      high |= 0x80;
      days &= 0x1ff;
    }
    live[DAYS_LOW - SECONDS] = (int) (days & 0xff);
    live[DAYS_HIGH - SECONDS] = high | (int) (days >>> 8);
  }

  private boolean isNormal() {
    return live[SECONDS - SECONDS] < 60
        && live[MINUTES - SECONDS] < 60
        && live[HOURS - SECONDS] < 24;
  }

  private int days() {
    return live[DAYS_LOW - SECONDS] | ((live[DAYS_HIGH - SECONDS] & 0x01) << 8);
  }

  /** Advances by one dot. */
  void tick() {
    if (++dots < DOTS_PER_CRYSTAL_TICK) {
      return;
    }
    dots = 0;
    if (isHalted() || ++crystalTicks < CRYSTAL_TICKS_PER_SECOND) {
      return;
    }
    crystalTicks = 0;
    addSecond();
  }

  private boolean isHalted() {
    return (live[DAYS_HIGH - SECONDS] & 0x40) != 0;
  }

  /**
   * Each counter only carries when it reaches its normal limit; a counter written past its limit
   * counts up to the top of its bits and wraps to 0 without carrying.
   */
  private void addSecond() {
    if (increment(SECONDS, 60) && increment(MINUTES, 60) && increment(HOURS, 24)) {
      int days = (days() + 1) & 0x1ff;
      int high = (live[DAYS_HIGH - SECONDS] & 0xc0) | (days >>> 8);
      if (days == 0) {
        high |= 0x80;
      }
      live[DAYS_LOW - SECONDS] = days & 0xff;
      live[DAYS_HIGH - SECONDS] = high;
    }
  }

  /** Increments a counter, returning whether it reached its limit (and so wrapped and carried). */
  private boolean increment(int register, int limit) {
    int i = register - SECONDS;
    int value = live[i] + 1;
    if (value == limit) {
      live[i] = 0;
      return true;
    }
    live[i] = value & mask(register);
    return false;
  }

  private static int mask(int register) {
    return switch (register) {
      case SECONDS, MINUTES -> 0x3f;
      case HOURS -> 0x1f;
      case DAYS_LOW -> 0xff;
      case DAYS_HIGH -> 0xc1;
      default -> throw new IllegalArgumentException("%02x".formatted(register));
    };
  }
}
