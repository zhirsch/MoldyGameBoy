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
    //    URL romResource = Resources.getResource("mooneye-test-suite/build/acceptance/instr/daa.gb");
    URL romResource = Resources.getResource("mooneye-test-suite/build/acceptance/bits/reg_f.gb");
    ByteBuffer rom = ByteBuffer.wrap(Resources.toByteArray(romResource));

    Registers registers = new Registers();
    registers.ir().set(rom.get(0x0100));
    registers.pc().set((short) 0x0101);
    registers.sp().set((short) 0xfffe);
    registers.a().set((byte) 0x01);
    registers.f().set((byte) 0xb0);
    registers.b().set((byte) 0x00);
    registers.c().set((byte) 0x13);
    registers.d().set((byte) 0x00);
    registers.e().set((byte) 0xd8);
    registers.h().set((byte) 0x01);
    registers.l().set((byte) 0x4d);

    int magicBreakpointCount = 0;
    try (Io io = IoFactory.none()) {
      Memory memory = new Memory(rom, rom);
      MoldyGameBoy gb = new MoldyGameBoy(memory, registers, io);
      while (magicBreakpointCount < 2) {
        if (registers.ir().get() == 0x40) {
          magicBreakpointCount++;
        }
        gb.tick();
      }
    }

    assertWithMessage("%s", registers).that(isSuccess(registers)).isTrue();
    assertWithMessage("%s", registers).that(isFailure(registers)).isFalse();
  }

  private boolean isSuccess(Registers registers) {
    return registers.b().get() == 3
        && registers.c().get() == 5
        && registers.d().get() == 8
        && registers.e().get() == 13
        && registers.h().get() == 21
        && registers.l().get() == 34;
  }

  private boolean isFailure(Registers registers) {
    return registers.b().get() == 0x42
        && registers.c().get() == 0x42
        && registers.d().get() == 0x42
        && registers.e().get() == 0x42
        && registers.h().get() == 0x42
        && registers.l().get() == 0x42;
  }
}
