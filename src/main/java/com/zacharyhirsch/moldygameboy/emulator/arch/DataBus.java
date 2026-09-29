package com.zacharyhirsch.moldygameboy.emulator.arch;

public final class DataBus {

  private byte data;

  public byte get() {
    return data;
  }

  public void set(byte data) {
    this.data = data;
  }
}
