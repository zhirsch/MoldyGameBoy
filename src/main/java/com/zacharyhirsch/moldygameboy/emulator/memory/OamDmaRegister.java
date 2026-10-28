package com.zacharyhirsch.moldygameboy.emulator.memory;

import com.zacharyhirsch.moldygameboy.emulator.arch.IoRegister;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class OamDmaRegister implements IoRegister {

  private int value = 0xff;
  private Internal running = null;
  private Internal pending = null;
  private int pendingDelay = 0;

  public boolean isActive() {
    return running != null;
  }

  @Override
  public int get() {
    return value;
  }

  @Override
  public void set(int baseHi) {
    value = baseHi & 0xff;
    pending = new Internal((short) (value << 8));
    pendingDelay = 1;
  }

  public void tick(Function<Short, Byte> reader, BiConsumer<Short, Byte> writer) {
    if (pending != null) {
      if (pendingDelay > 0) {
        pendingDelay--;
        if (running != null && !running.tick(reader, writer)) {
          running = null;
        }
        return;
      }
      // Startup: the new transfer takes over (and OAM is blocked) from the next M-cycle.
      running = pending;
      pending = null;
      return;
    }
    if (running != null && !running.tick(reader, writer)) {
      running = null;
    }
  }

  @Override
  public String toString() {
    return "dma @ ff46 = %02x [running=%s,pending=%s]".formatted(get(), running, pending);
  }

  private static final class Internal {

    private final short base;

    private int offset = 0;
    private byte current = (byte) 0xff;

    Internal(short base) {
      this.base = base;
    }

    byte current() {
      return current;
    }

    boolean tick(Function<Short, Byte> reader, BiConsumer<Short, Byte> writer) {
      short src = (short) (base + offset);
      short dst = (short) (0xfe00 + offset);
      current = reader.apply(src);
      writer.accept(dst, current);
      offset++;
      return offset < 160;
    }

    @Override
    public String toString() {
      return "[base=%04x,offset=%02x]".formatted(base, offset);
    }
  }
}
