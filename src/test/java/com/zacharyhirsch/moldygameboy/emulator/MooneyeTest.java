package com.zacharyhirsch.moldygameboy.emulator;

import static com.google.common.truth.Truth.assertWithMessage;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.IoFactory;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import java.net.URL;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

final class MooneyeTest {

  @Test
  void mooneyeTest() throws Exception {
//        URL romResource = Resources.getResource("mooneye-test-suite/build/acceptance/instr/daa.gb");
//    URL romResource = Resources.getResource("mooneye-test-suite/build/acceptance/bits/reg_f.gb");
    URL romResource = Resources.getResource("mooneye-test-suite/build/acceptance/bits/unused_hwio-GS.gb");
    ByteBuffer rom = ByteBuffer.wrap(Resources.toByteArray(romResource));

    Registers registers = new Registers();
    registers.ir().write(rom.get(0x0100));
    registers.pc().write((short) 0x0101);
    registers.sp().write((short) 0xfffe);
    registers.a().write((byte) 0x01);
    registers.f().write((byte) 0xb0);
    registers.b().write((byte) 0x00);
    registers.c().write((byte) 0x13);
    registers.d().write((byte) 0x00);
    registers.e().write((byte) 0xd8);
    registers.h().write((byte) 0x01);
    registers.l().write((byte) 0x4d);

    int magicBreakpointCount = 0;
    try (Io io = IoFactory.none()) {
      Memory memory = new Memory(rom, rom);
      MoldyGameBoy gb = new MoldyGameBoy(memory, registers, io);
      while (magicBreakpointCount < 2) {
        if (registers.ir().read() == 0x40) {
          magicBreakpointCount++;
        }
        gb.tick();
      }
    }

    assertWithMessage("%s", registers).that(isSuccess(registers)).isTrue();
    assertWithMessage("%s", registers).that(isFailure(registers)).isFalse();
  }

  private boolean isSuccess(Registers registers) {
    return registers.b().read() == 3
        && registers.c().read() == 5
        && registers.d().read() == 8
        && registers.e().read() == 13
        && registers.h().read() == 21
        && registers.l().read() == 34;
  }

  private boolean isFailure(Registers registers) {
    return registers.b().read() == 0x42
        && registers.c().read() == 0x42
        && registers.d().read() == 0x42
        && registers.e().read() == 0x42
        && registers.h().read() == 0x42
        && registers.l().read() == 0x42;
  }
}
