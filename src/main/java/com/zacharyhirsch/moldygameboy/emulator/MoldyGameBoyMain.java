package com.zacharyhirsch.moldygameboy.emulator;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.arch.MemoryRange;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.IORegisters;
import com.zacharyhirsch.moldygameboy.emulator.memory.MemoryMap;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

final class MoldyGameBoyMain {

  private static final String BOOT_ROM_PATH = "cgb.bin";

  static void main(String[] args) {
    IORegisters ioRegisters = new IORegisters();
    MemoryRange memory = new MemoryMap(readBootRom(), readRom(args[0]), ioRegisters);
    MoldyGameBoy gb = new MoldyGameBoy(memory, new Registers(), ioRegisters);
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
