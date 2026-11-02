package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Cartridge RAM kept by a battery, which is saved. On MBC3 cartridges with a clock, the battery
 * keeps the clock too, so the save is the RAM followed by the clock (see {@link RealTimeClock}).
 */
final class BatteryBackedRam implements Ram {

  private final byte[] bytes;
  private final RealTimeClock clock;

  private boolean dirty = false;

  BatteryBackedRam(int size) {
    this(size, null);
  }

  /** {@code clock} is null for cartridges without one. */
  BatteryBackedRam(int size, RealTimeClock clock) {
    this.bytes = new byte[size];
    this.clock = clock;
  }

  @Override
  public int size() {
    return bytes.length;
  }

  @Override
  public byte read(int offset) {
    return bytes[offset];
  }

  @Override
  public void write(int offset, byte value) {
    bytes[offset] = value;
    dirty = true;
  }

  @Override
  public void save(SaveSink sink) throws IOException {
    if (!dirty && (clock == null || !clock.hasUnsavedChanges())) {
      return;
    }
    int clockSize = clock == null ? 0 : RealTimeClock.SAVE_SIZE;
    ByteBuffer save = ByteBuffer.allocate(bytes.length + clockSize).order(ByteOrder.LITTLE_ENDIAN);
    save.put(bytes);
    if (clock != null) {
      clock.save(save);
    }
    sink.write(save.array());
    dirty = false;
    if (clock != null) {
      clock.markSaved();
    }
  }

  @Override
  public void load(byte[] save) {
    if (save.length < bytes.length) {
      throw new IllegalArgumentException(
          "Save is %d bytes, expected at least %d".formatted(save.length, bytes.length));
    }
    System.arraycopy(save, 0, bytes, 0, bytes.length);
    if (clock != null && save.length > bytes.length) {
      clock.load(
          ByteBuffer.wrap(save, bytes.length, save.length - bytes.length)
              .order(ByteOrder.LITTLE_ENDIAN));
    }
  }
}
