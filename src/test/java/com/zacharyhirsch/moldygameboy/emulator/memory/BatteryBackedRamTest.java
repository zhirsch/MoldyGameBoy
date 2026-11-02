package com.zacharyhirsch.moldygameboy.emulator.memory;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.junit.jupiter.api.Test;

final class BatteryBackedRamTest {

  @Test
  void contentsSurviveSaveAndLoad() throws IOException {
    Ram ram = new BatteryBackedRam(0x2000);
    ram.write(0x0123, (byte) 0x42);

    Ram loaded = new BatteryBackedRam(0x2000);
    loaded.load(save(ram));

    assertThat(loaded.read(0x0123)).isEqualTo((byte) 0x42);
  }

  @Test
  void savesOnlyWhenChangedSinceTheLastSave() throws IOException {
    Ram ram = new BatteryBackedRam(0x2000);
    assertThat(save(ram)).isNull();

    ram.write(0x0000, (byte) 0x01);
    assertThat(save(ram)).hasLength(0x2000);
    assertThat(save(ram)).isNull();
  }

  @Test
  void clockIsSavedAfterTheRam() throws IOException {
    RealTimeClock clock = new RealTimeClock(() -> 0);
    Ram ram = new BatteryBackedRam(0x2000, clock);
    clock.write(RealTimeClock.MINUTES, 5);

    assertThat(save(ram)).hasLength(0x2000 + RealTimeClock.SAVE_SIZE);
    assertThat(save(ram)).isNull();
  }

  @Test
  void loadingAShortSaveFails() {
    Ram ram = new BatteryBackedRam(0x2000);

    assertThrows(IllegalArgumentException.class, () -> ram.load(new byte[0x1000]));
  }

  @Test
  void volatileRamIsNeverSaved() throws IOException {
    Ram ram = new VolatileRam(0x2000);
    ram.write(0x0000, (byte) 0x01);

    assertThat(save(ram)).isNull();
  }

  /** What saving writes, or null if it writes nothing. */
  private static byte[] save(Ram ram) throws IOException {
    byte[][] saved = {null};
    ram.save(save -> saved[0] = save);
    return saved[0];
  }
}
