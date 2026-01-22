package com.zacharyhirsch.moldygameboy.emulator;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.bus.AddressBus;
import com.zacharyhirsch.moldygameboy.emulator.bus.DataBus;
import com.zacharyhirsch.moldygameboy.emulator.cpu.Cpu;
import com.zacharyhirsch.moldygameboy.emulator.memory.CpuMemory;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

final class MoldyGameBoy {

  private static final String ROM_PREFIX =
      "/Users/zacharyhirsch/Documents/GitHub/MoldyGameBoy/roms/games/";
  private static final String BOOT_ROM_PATH = "cgb.bin";

  static void main(String[] args) throws IOException {
    ByteBuffer boot = ByteBuffer.wrap(Resources.toByteArray(Resources.getResource(BOOT_ROM_PATH)));
    ByteBuffer rom = readRom(args[0]);

    AddressBus addressBus = new AddressBus();
    DataBus dataBus = new DataBus();
    CpuMemory cpuMemory = new CpuMemory(addressBus, dataBus, boot, rom);
    Cpu cpu = new Cpu(addressBus, dataBus, cpuMemory);

    while (true) {
      cpu.tick();
    }
  }

  private static ByteBuffer readRom(String path) {
    try (FileInputStream input = new FileInputStream(path)) {
      return ByteBuffer.wrap(input.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
