package com.zacharyhirsch.moldygameboy.emulator;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.cpu.Cpu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import com.zacharyhirsch.moldygameboy.emulator.timer.Divider;
import com.zacharyhirsch.moldygameboy.emulator.timer.Timer;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

final class MoldyGameBoy {

  private static final String BOOT_ROM_PATH = "cgb.bin";

  static void main(String[] args) {
    ByteBuffer boot = readBootRom();
    ByteBuffer rom = readRom(args[0]);

    Memory memory = new Memory(boot, rom);
    Divider divider = new Divider(memory);
    Timer timer = new Timer(memory);
    Cpu cpu =
        new Cpu(
            new Registers(),
            memory,
            mem -> {
              byte data = mem.execute(memory);
              divider.tick();
              timer.tick();
              return data;
            },
            null);

    while (true) {
      cpu.tick();
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
