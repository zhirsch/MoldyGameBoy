package com.zacharyhirsch.moldygameboy.emulator.memory;

public final class VideoMemoryLock {

  private boolean oamReadLocked = false;
  private boolean oamWriteLocked = false;
  private boolean vramReadLocked = false;
  private boolean vramWriteLocked = false;

  public boolean isOamReadLocked() {
    return oamReadLocked;
  }

  public boolean isOamWriteLocked() {
    return oamWriteLocked;
  }

  public boolean isVramReadLocked() {
    return vramReadLocked;
  }

  public boolean isVramWriteLocked() {
    return vramWriteLocked;
  }

  public void set(
      boolean oamReadLocked,
      boolean oamWriteLocked,
      boolean vramReadLocked,
      boolean vramWriteLocked) {
    this.oamReadLocked = oamReadLocked;
    this.oamWriteLocked = oamWriteLocked;
    this.vramReadLocked = vramReadLocked;
    this.vramWriteLocked = vramWriteLocked;
  }
}
