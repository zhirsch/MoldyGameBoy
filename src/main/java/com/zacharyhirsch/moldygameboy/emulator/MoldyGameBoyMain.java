package com.zacharyhirsch.moldygameboy.emulator;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.IoFactory;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MoldyGameBoyMain {

  private static final Logger log = LoggerFactory.getLogger(MoldyGameBoyMain.class);

  static void main(String[] args) throws Exception {
    try (Io io = IoFactory.sdl()) {
      Memory memory = new Memory(readBootRom(args[0]), readRom(args[1]));
      MoldyGameBoy gb = new MoldyGameBoy(memory, new Registers(), io);
      try {
        io.eventLoop().run(gb::tick);
      } catch (Exception e) {
        log.error("Emulator crashed!", e);
        io.video().setError(e);
        io.eventLoop().run(() -> {});
        throw e;
      }
    }
  }

  private static ByteBuffer readBootRom(String bootRomPath) {
    try {
      return ByteBuffer.wrap(Resources.toByteArray(Resources.getResource(bootRomPath)));
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
