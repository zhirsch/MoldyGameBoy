package com.zacharyhirsch.moldygameboy.emulator;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/** A cartridge's battery-backed save, kept next to the ROM as {@code <rom>.sav}. */
final class SaveFile {

  private final Path path;

  SaveFile(Path path) {
    this.path = path;
  }

  /** The save file for a ROM: {@code games/zelda.gbc} saves to {@code games/zelda.gbc.sav}. */
  static SaveFile forRom(Path rom) {
    return new SaveFile(rom.resolveSibling(rom.getFileName() + ".sav"));
  }

  Path path() {
    return path;
  }

  Optional<byte[]> read() throws IOException {
    return Files.exists(path) ? Optional.of(Files.readAllBytes(path)) : Optional.empty();
  }

  /**
   * Writes the save to a temporary file and then moves it into place, so that a crash can't leave
   * a partly written save.
   */
  void write(byte[] save) throws IOException {
    Path temp = path.resolveSibling(path.getFileName() + ".tmp");
    Files.write(temp, save);
    try {
      Files.move(
          temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException e) {
      Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
