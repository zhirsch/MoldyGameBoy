package com.zacharyhirsch.moldygameboy.emulator;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.IORegisters;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

final class MoldyGameBoyMain {

  private static final String BOOT_ROM_PATH = "cgb.bin";

  static void main(String[] args) {
    MoldyGameBoy.run(
        Long.MAX_VALUE,
        readBootRom(),
        readRom(args[0]),
        new Registers(),
        new IORegisters(),
        null);
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
