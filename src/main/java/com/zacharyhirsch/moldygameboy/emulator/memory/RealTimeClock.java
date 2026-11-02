package com.zacharyhirsch.moldygameboy.emulator.memory;

/**
 * MBC3's real-time clock: seconds, minutes, hours and a 9-bit day counter, counted by a 32768 Hz
 * crystal. DH holds bit 8 of the day counter (bit 0), the halt flag (bit 6, which stops the clock)
 * and the day counter's carry (bit 7, which stays set once the days overflow). Reads give the
 * values copied by the last latch; writes go to the clock itself.
 *
 * <p>The clock runs on emulated time, at one crystal tick per 128 dots.
 */
final class RealTimeClock {

  static final int SECONDS = 0x08;
  static final int MINUTES = 0x09;
  static final int HOURS = 0x0a;
  static final int DAYS_LOW = 0x0b;
  static final int DAYS_HIGH = 0x0c;

  private static final int DOTS_PER_CRYSTAL_TICK = 128;
  private static final int CRYSTAL_TICKS_PER_SECOND = 32768;

  private final int[] live = new int[5];
  private final int[] latched = new int[5];

  private int dots = 0;
  private int crystalTicks = 0;

  int read(int register) {
    return latched[register - SECONDS];
  }

  void write(int register, int value) {
    live[register - SECONDS] = value & mask(register);
    if (register == SECONDS) {
      crystalTicks = 0;
    }
  }

  void latch() {
    System.arraycopy(live, 0, latched, 0, live.length);
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
      int days = live[DAYS_LOW - SECONDS] | ((live[DAYS_HIGH - SECONDS] & 0x01) << 8);
      days = (days + 1) & 0x1ff;
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
