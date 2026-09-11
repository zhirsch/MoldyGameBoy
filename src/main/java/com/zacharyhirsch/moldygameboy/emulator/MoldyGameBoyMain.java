package com.zacharyhirsch.moldygameboy.emulator;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.IoFactory;
import com.zacharyhirsch.moldygameboy.emulator.memory.BootRom;
import com.zacharyhirsch.moldygameboy.emulator.memory.Rom;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MoldyGameBoyMain {

  private static final Logger log = LoggerFactory.getLogger(MoldyGameBoyMain.class);

  static void main(String[] args) throws Exception {
    BootRom boot = readBootRom(args[0]);
    Rom rom = readRom(args[1]);
    try (Io io = IoFactory.sdl()) {
      MoldyGameBoy gb = new MoldyGameBoy(boot, rom, new Registers(), io);
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

  private static BootRom readBootRom(String path) throws IOException {
    byte[] bytes = Resources.toByteArray(Resources.getResource(path));
    if (bytes.length != 0x0100) {
      throw new IllegalArgumentException(
          "boot rom size is %04x, expected=0x0100".formatted(bytes.length));
    }
    return new BootRom(ByteBuffer.wrap(bytes));
  }

  private static Rom readRom(String path) throws IOException {
    try (RandomAccessFile file = new RandomAccessFile(Path.of(path).toFile(), "r");
        FileChannel channel = file.getChannel()) {

      if (channel.size() % 0x4000 != 0) {
        throw new IllegalArgumentException(
            "file size %d is not a multiple of bank size %d".formatted(channel.size(), 0x4000));
      }

      ArrayList<ByteBuffer> buffers = new ArrayList<>();
      for (long position = 0; position != channel.size(); position += 0x4000) {
        buffers.add(channel.map(FileChannel.MapMode.READ_ONLY, position, 0x4000));
      }
      return new Rom(buffers.toArray(ByteBuffer[]::new));
    }
  }
}
