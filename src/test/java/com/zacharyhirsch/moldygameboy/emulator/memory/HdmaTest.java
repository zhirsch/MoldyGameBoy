package com.zacharyhirsch.moldygameboy.emulator.memory;

import static com.google.common.truth.Truth.assertThat;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class HdmaTest {

  private final Memory memory =
      new Memory(
          Cartridge.create(ByteBuffer.allocate(0x8000)),
          ByteBuffer.allocate(Memory.VRAM_SIZE),
          ByteBuffer.allocate(0xa0));

  private boolean lcdOn = true;
  private final Hdma hdma = new Hdma(memory, () -> lcdOn);

  @BeforeEach
  void setUp() {
    for (int i = 0; i < 64; i++) {
      memory.write((short) (0xc000 + i), (byte) (i + 1));
    }
    // Copy from $C000 to $8100.
    hdma.hdma1().set(0xc0);
    hdma.hdma2().set(0x00);
    hdma.hdma3().set(0x81);
    hdma.hdma4().set(0x00);
  }

  @Test
  void generalPurposeTransferCopiesEveryBlockWhileTheCpuIsStopped() {
    hdma.hdma5().set(0x01);

    assertThat(hdma.isTransferring()).isTrue();
    tick(15);
    assertThat(hdma.isTransferring()).isTrue();
    tick(1);
    assertThat(hdma.isTransferring()).isFalse();
    assertThat(hdma.hdma5().get()).isEqualTo(0xff);
    assertCopied(32);
  }

  @Test
  void hblankTransferCopiesOneBlockPerHBlank() {
    hdma.hdma5().set(0x81);

    assertThat(hdma.isTransferring()).isFalse();
    assertThat(hdma.hdma5().get()).isEqualTo(0x01);
    hdma.onHBlank();
    tick(8);
    assertThat(hdma.isTransferring()).isFalse();
    assertThat(hdma.hdma5().get()).isEqualTo(0x00);
    assertCopied(16);

    hdma.onHBlank();
    tick(8);
    assertThat(hdma.hdma5().get()).isEqualTo(0xff);
    assertCopied(32);
  }

  @Test
  void hblankTransferStartsStraightAwayWithTheLcdOff() {
    lcdOn = false;
    hdma.hdma5().set(0x81);

    assertThat(hdma.isTransferring()).isTrue();
    tick(8);
    assertCopied(16);
  }

  @Test
  void writingBit7ClearStopsAnHBlankTransfer() {
    hdma.hdma5().set(0x83);
    hdma.onHBlank();
    tick(8);

    hdma.hdma5().set(0x00);

    assertThat(hdma.hdma5().get()).isEqualTo(0x82);
    hdma.onHBlank();
    assertThat(hdma.isTransferring()).isFalse();
  }

  @Test
  void transferWritesToTheSelectedVramBank() {
    memory.vbk().set(1);
    hdma.hdma5().set(0x00);
    tick(8);

    assertThat(memory.read((short) 0x8100)).isEqualTo((byte) 1);
    memory.vbk().set(0);
    assertThat(memory.read((short) 0x8100)).isEqualTo((byte) 0);
  }

  private void tick(int mcycles) {
    for (int i = 0; i < mcycles; i++) {
      hdma.tick();
    }
  }

  private void assertCopied(int bytes) {
    for (int i = 0; i < 64; i++) {
      byte expected = i < bytes ? (byte) (i + 1) : 0;
      assertThat(memory.read((short) (0x8100 + i))).isEqualTo(expected);
    }
  }
}
