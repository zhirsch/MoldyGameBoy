package com.zacharyhirsch.moldygameboy.emulator.memory;

import static com.google.common.truth.Truth.assertThat;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

final class MemoryTest {

  private final ByteBuffer vram = ByteBuffer.allocate(Memory.VRAM_SIZE);
  private final Memory memory =
      new Memory(Cartridge.create(ByteBuffer.allocate(0x8000)), vram, ByteBuffer.allocate(0xa0));

  @Test
  void vbkSelectsTheVramBank() {
    write(0x8123, 0x11);
    memory.vbk().set(1);
    write(0x8123, 0x22);

    assertThat(read(0x8123)).isEqualTo(0x22);
    assertThat(vram.get(0x2123)).isEqualTo((byte) 0x22);
    memory.vbk().set(0);
    assertThat(read(0x8123)).isEqualTo(0x11);
  }

  @Test
  void svbkSelectsTheWramBankAtD000() {
    memory.svbk().set(2);
    write(0xd456, 0x22);
    memory.svbk().set(3);
    write(0xd456, 0x33);

    memory.svbk().set(2);
    assertThat(read(0xd456)).isEqualTo(0x22);
    memory.svbk().set(3);
    assertThat(read(0xd456)).isEqualTo(0x33);
  }

  @Test
  void svbkZeroSelectsBankOne() {
    memory.svbk().set(1);
    write(0xd000, 0x44);

    memory.svbk().set(0);
    assertThat(read(0xd000)).isEqualTo(0x44);
  }

  @Test
  void svbkDoesNotAffectC000() {
    write(0xc789, 0x55);

    memory.svbk().set(5);
    assertThat(read(0xc789)).isEqualTo(0x55);
  }

  @Test
  void echoRamFollowsTheWramBank() {
    memory.svbk().set(6);
    write(0xd010, 0x66);

    assertThat(read(0xf010)).isEqualTo(0x66);
  }

  private void write(int address, int value) {
    memory.write((short) address, (byte) value);
  }

  private int read(int address) {
    return Byte.toUnsignedInt(memory.read((short) address));
  }
}
