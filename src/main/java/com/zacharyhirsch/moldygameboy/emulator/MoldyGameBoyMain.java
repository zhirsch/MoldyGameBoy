package com.zacharyhirsch.moldygameboy.emulator;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.IoFactory;
import com.zacharyhirsch.moldygameboy.emulator.io.Ticker;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

final class MoldyGameBoyMain {
  
  static void main(String[] args) throws Exception {
    try (Io io = IoFactory.sdl()) {
      MoldyGameBoy gb =
          new MoldyGameBoy(
              ByteBuffer.wrap(Resources.toByteArray(Resources.getResource(args[0]))),
              ByteBuffer.wrap(Files.readAllBytes(Path.of(args[1]))),
              io);
      io.eventLoop().run(new MoldyGameBoyTicker(gb));
    }
  }

  private record MoldyGameBoyTicker(MoldyGameBoy gb) implements Ticker {

    @Override
    public void tick() {
      gb.tick();
    }

    @Override
    public boolean isDone() {
      return false;
    }
  }
}
