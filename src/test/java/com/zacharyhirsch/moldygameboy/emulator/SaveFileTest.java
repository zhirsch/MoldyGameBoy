package com.zacharyhirsch.moldygameboy.emulator;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class SaveFileTest {

  @TempDir Path dir;

  @Test
  void saveIsNextToTheRomWithSavAppended() {
    assertThat(SaveFile.forRom(Path.of("roms", "games", "zelda.gbc")).path())
        .isEqualTo(Path.of("roms", "games", "zelda.gbc.sav"));
  }

  @Test
  void writtenSaveCanBeRead() throws Exception {
    SaveFile saveFile = SaveFile.forRom(dir.resolve("game.gb"));
    assertThat(saveFile.read()).isEmpty();

    saveFile.write(new byte[] {1, 2, 3});
    saveFile.write(new byte[] {4, 5});

    assertThat(saveFile.read()).hasValue(new byte[] {4, 5});
    try (var files = Files.list(dir)) {
      assertThat(files.map(p -> p.getFileName().toString()).toList())
          .containsExactly("game.gb.sav");
    }
  }
}
