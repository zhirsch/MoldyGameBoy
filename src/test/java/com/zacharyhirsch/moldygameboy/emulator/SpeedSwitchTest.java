package com.zacharyhirsch.moldygameboy.emulator;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Range;
import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.IoFactory;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

/**
 * Switches to double speed with KEY1 and STOP, and checks that the CPU side (here, DIV) then runs
 * twice as fast as the PPU. The program runs from cgb-acid2's cartridge, for its CGB header.
 */
final class SpeedSwitchTest {

  // Counts DIV ticks while LY goes from $10 to $1A (10 lines, 4560 dots) into D, switches speed,
  // reads KEY1 into B, counts again into C, then stops at LD B,B.
  private static final int[] PROGRAM = {
    0xf3, // di
    0xf0, 0x44, 0xfe, 0x10, 0x20, 0xfa, // wait for LY == $10
    0xaf, 0xe0, 0x04, // reset DIV
    0xf0, 0x44, 0xfe, 0x1a, 0x20, 0xfa, // wait for LY == $1A
    0xf0, 0x04, 0x57, // ld d, DIV
    0x3e, 0x01, 0xe0, 0x4d, // arm the speed switch
    0x10, 0x00, // stop
    0xf0, 0x4d, 0x47, // ld b, KEY1
    0xf0, 0x44, 0xfe, 0x10, 0x20, 0xfa, // wait for LY == $10
    0xaf, 0xe0, 0x04, // reset DIV
    0xf0, 0x44, 0xfe, 0x1a, 0x20, 0xfa, // wait for LY == $1A
    0xf0, 0x04, 0x4f, // ld c, DIV
    0x40, // ld b, b
    0x18, 0xfe, // jr -2
  };

  @Test
  void doubleSpeedRunsTheCpuSideTwiceAsFast() throws Exception {
    try (Io io = IoFactory.none()) {
      MoldyGameBoy gb = new MoldyGameBoy(readRom("cgb.bin"), program(), io);
      Registers registers = run(gb);

      int singleSpeedDivTicks = Byte.toUnsignedInt(registers.d().get());
      int doubleSpeedDivTicks = Byte.toUnsignedInt(registers.c().get());
      assertThat(registers.b().get()).isEqualTo((byte) 0xfe);
      // 4560 dots is 17.8 DIV ticks at single speed, and 35.6 at double speed.
      assertThat(singleSpeedDivTicks).isIn(Range.closed(17, 18));
      assertThat(doubleSpeedDivTicks).isIn(Range.closed(35, 36));
    }
  }

  private static Registers run(MoldyGameBoy gb) {
    Registers registers = gb.cpu().registers();
    boolean started = false;
    for (long dots = 0; dots < 100_000_000L; dots++) {
      gb.tick();
      started |= registers.pc().get() == 0x0150;
      if (started && registers.ir().get() == (byte) 0x40) {
        return registers;
      }
    }
    throw new AssertionError("Didn't reach the breakpoint: " + registers);
  }

  private static ByteBuffer program() throws IOException {
    byte[] rom = Resources.toByteArray(Resources.getResource("roms/cgb-acid2.gbc"));
    byte[] entry = {0x00, (byte) 0xc3, 0x50, 0x01}; // nop; jp $0150
    System.arraycopy(entry, 0, rom, 0x100, entry.length);
    for (int i = 0; i < PROGRAM.length; i++) {
      rom[0x150 + i] = (byte) PROGRAM[i];
    }
    return ByteBuffer.wrap(rom);
  }

  private static ByteBuffer readRom(String path) throws IOException {
    return ByteBuffer.wrap(Resources.toByteArray(Resources.getResource(path)));
  }
}
