package com.zacharyhirsch.moldygameboy.emulator.memory;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class OamDmaRegister implements IoRegister {

  private Internal running = null;
  private Internal pending = null;

  public boolean isActive() {
    return running != null;
  }

  @Override
  public int get() {
    if (running != null) {
      return running.current();
    }
    return 0xff;
  }

  @Override
  public void set(int baseHi) {
    pending = new Internal((short) (baseHi << 8));
  }

  public void prepare(Function<Short, Byte> reader) {
    if (running != null) {
      running.tick(reader);
    }
  }

  public void commit(BiConsumer<Short, Byte> writer) {
    if (pending != null) {
      running = pending;
      pending = null;
      return;
    }
    if (running == null) {
      return;
    }
    if (!running.commit(writer)) {
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

    void tick(Function<Short, Byte> reader) {
      short address = (short) (base + offset);
      current = reader.apply(address);
    }

    boolean commit(BiConsumer<Short, Byte> writer) {
      short address = (short) (0xfe00 + offset++);
      writer.accept(address, current);
      return offset < 160;
    }

    @Override
    public String toString() {
      return "[base=%04x,offset=%02x]".formatted(base, offset);
    }
  }
}
