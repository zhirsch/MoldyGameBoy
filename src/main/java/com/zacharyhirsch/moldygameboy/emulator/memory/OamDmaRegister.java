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
    int base = value << 8;
    if (base >= 0xe000) {
      base -= 0x2000; // DMG: $E0xx-$FFxx sources read from WRAM
    }
    pending = new Internal(memory, (short) base);
    pendingDelay = 1;
  }

  public void tickT1() {}

  public void tickT2() {}

  public void tickT3() {
    // The previous M-cycle copied the last byte; OAM is accessible again from here.
    if (running != null && running.done()) {
      running = null;
    }
    if (pending != null && pendingDelay-- == 0) {
      running = pending;
      pending = null;
    }
    if (running != null) {
      running.tick();
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

    void tick() {
      short src = (short) (base + offset);
      short dst = (short) (0xfe00 + offset);
      memory.write(dst, memory.read(src));
      offset++;
    }

    public boolean done() {
      return offset >= 160;
    }

    @Override
    public String toString() {
      return "[base=%04x,offset=%02x]".formatted(base, offset);
    }
  }
}
