package com.zacharyhirsch.moldygameboy.emulator;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.IoFactory;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MoldyGameBoyMain {

  private static final Logger log = LoggerFactory.getLogger(MoldyGameBoyMain.class);

  static void main(String[] args) throws Exception {
    try (Io io = IoFactory.sdl()) {
      MoldyGameBoy gb =
          new MoldyGameBoy(
              ByteBuffer.wrap(Resources.toByteArray(Resources.getResource(args[0]))),
              ByteBuffer.wrap(Files.readAllBytes(Path.of(args[1]))),
              io);
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
}
