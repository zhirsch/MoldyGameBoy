package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class OamDmaRegister implements IoRegister {

  private int delay = 0;
  private boolean active = false;

  private int offset = 0;
  private short base = 0;

  public boolean isActive() {
    return active;
  }

  @Override
  public int get() {
    // TODO: return the byte currently being transferred
    return 0xff;
  }

  @Override
  public void set(int baseHi) {
    delay = 1;
    offset = 0;
    base = (short) (baseHi << 8);
  }

  public void tick(Function<Short, Byte> reader, BiConsumer<Short, Byte> writer) {
    if (delay > 0) {
      delay--;
      active = true;
      return;
    }

    if (offset == 160) {
      active = false;
    }

    if (!active) {
      return;
    }

    short src = (short) (base + offset);
    byte value = reader.apply(src);

    writer.accept((short) (0xfe00 + offset), value);

    offset++;
  }

  @Override
  public String toString() {
    return "dma @ ff46 = %02x [active=%s,base=%04x,offset=%02x]"
        .formatted(get(), active, base, offset);
  }
}
