package com.zacharyhirsch.moldygameboy.emulator;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.arch.Memory;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemoryMap;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

final class MoldyGameBoyMain {

  private static final String BOOT_ROM_PATH = "cgb.bin";

  static void main(String[] args) {
    Memory memory = new MemoryMap(readBootRom(), readRom(args[0]));
    MoldyGameBoy gb = new MoldyGameBoy(memory, new Registers());
    while (true) {
      gb.tick();
    }
  }

  private static ByteBuffer readBootRom() {
    try {
      return ByteBuffer.wrap(Resources.toByteArray(Resources.getResource(BOOT_ROM_PATH)));
    } catch (IOException e) {
      throw new RuntimeException(e);
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
