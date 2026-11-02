package com.zacharyhirsch.moldygameboy.emulator.memory;

/** Cartridge RAM without a battery, which is lost when the power goes off (and isn't saved). */
final class VolatileRam implements Ram {

  private final byte[] bytes;

  VolatileRam(int size) {
    this.bytes = new byte[size];
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
  }

  @Override
  public void save(SaveSink sink) {}

  @Override
  public void load(byte[] save) {}
}
