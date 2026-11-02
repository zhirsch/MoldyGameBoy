package com.zacharyhirsch.moldygameboy.emulator;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.IoFactory;
import com.zacharyhirsch.moldygameboy.emulator.io.Ticker;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

final class MoldyGameBoyMain {

  /**
   * Runs a ROM: {@code MoldyGameBoyMain [boot ROM resource] <ROM path>}. Without a boot ROM, the
   * CGB's is used for cartridges that support the CGB, and the DMG's for the rest. A cartridge
   * with a battery is saved next to the ROM as {@code <rom>.sav}. Set the {@code moldy.record}
   * system property to a path to also record an MP4 there.
   */
  static void main(String[] args) throws Exception {
    String record = System.getProperty("moldy.record");
    Path romPath = Path.of(args[args.length - 1]);
    ByteBuffer rom = ByteBuffer.wrap(Files.readAllBytes(romPath));
    String boot = args.length > 1 ? args[0] : BootRoms.forCartridge(rom);
    SaveFile saveFile = SaveFile.forRom(romPath);
    Io sdl = IoFactory.sdl();
    try (Io io = record == null ? sdl : IoFactory.recording(sdl, Path.of(record))) {
      MoldyGameBoy gb =
          new MoldyGameBoy(
              ByteBuffer.wrap(Resources.toByteArray(Resources.getResource(boot))), rom, io);
      saveFile.read().ifPresent(gb::load);
      try {
        io.eventLoop().run(new MoldyGameBoyTicker(gb, saveFile));
      } finally {
        gb.save(saveFile::write);
      }
    }
  }

  /** Runs the emulator, saving about once a second while there are unsaved changes. */
  private static final class MoldyGameBoyTicker implements Ticker {

    private static final int SAVE_INTERVAL_DOTS = 1 << 22;

    private final MoldyGameBoy gb;
    private final SaveFile saveFile;

    private int dots = 0;

    MoldyGameBoyTicker(MoldyGameBoy gb, SaveFile saveFile) {
      this.gb = gb;
      this.saveFile = saveFile;
    }

    @Override
    public void tick() {
      gb.tick();
      if (++dots == SAVE_INTERVAL_DOTS) {
        dots = 0;
        try {
          gb.save(saveFile::write);
        } catch (IOException e) {
          throw new UncheckedIOException("Unable to write " + saveFile.path(), e);
        }
      }
    }

    @Override
    public boolean isDone() {
      return false;
    }
  }
}
