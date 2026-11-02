package com.zacharyhirsch.moldygameboy.emulator.memory;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

final class CartridgeTest {

  @Test
  void onlyCartridgesWithABatteryAreSaved() throws IOException {
    assertThat(saveAfterWriting(0x1b, 0x03, 0x00)).isNotNull(); // MBC5+RAM+BATTERY
    assertThat(saveAfterWriting(0x1a, 0x03, 0x00)).isNull(); // MBC5+RAM
    assertThat(saveAfterWriting(0x06, 0x00, 0x00)).isNotNull(); // MBC2+BATTERY
    assertThat(saveAfterWriting(0x0f, 0x00, 0x08)).isNotNull(); // MBC3+TIMER+BATTERY's clock
    assertThat(saveAfterWriting(0x00, 0x00, 0x00)).isNull(); // ROM only
  }

  @Test
  void bankedRamSurvivesSaveAndLoad() throws IOException {
    Cartridge cartridge = Cartridge.create(rom(0x1b, 0x03));
    selectRamBank(cartridge.mapper(), 2);
    cartridge.mapper().mapRam().write((short) 0xa123, (byte) 0x42);

    Cartridge loaded = Cartridge.create(rom(0x1b, 0x03));
    loaded.load(save(cartridge));

    selectRamBank(loaded.mapper(), 2);
    assertThat(loaded.mapper().mapRam().read((short) 0xa123)).isEqualTo((byte) 0x42);
  }

  /** Writes to cartridge RAM (or a clock register) at $A000, then returns what saving writes. */
  private static byte[] saveAfterWriting(int type, int ramSize, int bank) throws IOException {
    Cartridge cartridge = Cartridge.create(rom(type, ramSize));
    selectRamBank(cartridge.mapper(), bank);
    cartridge.mapper().mapRam().write((short) 0xa000, (byte) 0x01);
    return save(cartridge);
  }

  private static void selectRamBank(Mapper mapper, int bank) {
    mapper.mapRom().write((short) 0x0000, (byte) 0x0a);
    mapper.mapRom().write((short) 0x4000, (byte) bank);
  }

  /** What saving writes, or null if it writes nothing. */
  private static byte[] save(Cartridge cartridge) throws IOException {
    byte[][] saved = {null};
    cartridge.save(save -> saved[0] = save);
    return saved[0];
  }

  private static ByteBuffer rom(int type, int ramSize) {
    ByteBuffer rom = ByteBuffer.allocate(0x8000);
    rom.put(0x0147, (byte) type);
    rom.put(0x0149, (byte) ramSize);
    return rom;
  }
}
