package com.zacharyhirsch.moldygameboy.emulator.memory;

import com.zacharyhirsch.moldygameboy.emulator.arch.IoRegister;

public final class OamDmaRegister implements IoRegister {

  private final Memory memory;

  private int value = 0xff;
  private Internal running = null;
  private Internal pending = null;
  private int pendingDelay = 0;

  public OamDmaRegister(Memory memory) {
    this.memory = memory;
  }

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
    pending = new Internal(memory, (short) (value << 8));
    pendingDelay = 1;
  }

  public void tickT1() {}

  public void tickT2() {}

  public void tickT3() {
    if (pending != null) {
      if (pendingDelay > 0) {
        pendingDelay--;
        if (running != null && !running.tick()) {
          running = null;
        }
        return;
      }
      // Startup: the new transfer takes over (and OAM is blocked) from the next M-cycle.
      running = pending;
      pending = null;
      return;
    }
    if (running != null && !running.tick()) {
      running = null;
    }
  }

  public void tickT4() {}

  @Override
  public String toString() {
    return "dma @ ff46 = %02x [running=%s,pending=%s]".formatted(get(), running, pending);
  }

  private static final class Internal {

    private final Memory memory;
    private final short base;

    private int offset = 0;

    Internal(Memory memory, short base) {
      this.memory = memory;
      this.base = base;
    }

    boolean tick() {
      short src = (short) (base + offset);
      short dst = (short) (0xfe00 + offset);
      byte value = memory.read(src);
      memory.write(dst, value);
      offset++;
      return offset < 160;
    }

    @Override
    public String toString() {
      return "[base=%04x,offset=%02x]".formatted(base, offset);
    }
  }
}
