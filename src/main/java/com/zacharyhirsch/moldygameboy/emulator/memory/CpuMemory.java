package com.zacharyhirsch.moldygameboy.emulator.memory;

import com.zacharyhirsch.moldygameboy.emulator.bus.AddressBus;
import com.zacharyhirsch.moldygameboy.emulator.bus.DataBus;
import java.nio.ByteBuffer;

public final class CpuMemory {

  private final AddressBus addressBus;
  private final DataBus dataBus;
  private final ByteBuffer boot;
  private final ByteBuffer rom;
  private final ByteBuffer hram;

  private boolean bank;

  public CpuMemory(AddressBus addressBus, DataBus dataBus, ByteBuffer boot, ByteBuffer rom) {
    this.boot = boot;
    this.rom = rom;
    this.addressBus = addressBus;
    this.dataBus = dataBus;
    this.hram = ByteBuffer.allocate(0x7f);
    this.bank = true;
  }

  public void read() {
    int address = Short.toUnsignedInt(addressBus.get());
    assert 0x0000 <= address && address <= 0xffff;
    if (bank) {
      if (0x0000 <= address && address <= 0x00ff) {
        dataBus.set(boot.get(address));
        return;
      }
      if (0x0100 <= address && address <= 0x01ff) {
        dataBus.set(rom.get(address));
        return;
      }
      if (0x0200 <= address && address <= 0x08ff) {
        dataBus.set(boot.get(address));
        return;
      }
    } else {
      if (0x0000 <= address && address <= 0x00ff) {
        dataBus.set(rom.get(address));
        return;
      }
      if (0x0100 <= address && address <= 0x01ff) {
        dataBus.set(rom.get(address));
        return;
      }
      if (0x0200 <= address && address <= 0x08ff) {
        dataBus.set(rom.get(address));
        return;
      }
    }
    if (0x0900 <= address && address <= 0xff7f) {
      throw new IllegalStateException("%04x".formatted(addressBus.get()));
    }
    if (0xff80 <= address && address <= 0xfffe) {
      dataBus.set(hram.get(address - 0xff80));
      return;
    }
    if (address == 0xffff) {
      throw new IllegalStateException("%04x".formatted(addressBus.get()));
    }
    throw new IllegalStateException("%04x".formatted(addressBus.get()));
  }

  public void write() {}
}
