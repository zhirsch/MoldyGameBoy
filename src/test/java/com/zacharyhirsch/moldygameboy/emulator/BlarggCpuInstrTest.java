package com.zacharyhirsch.moldygameboy.emulator;


import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.cpu.Cpu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

final class BlarggCpuInstrTest {

  enum BlarggTest {
    SPECIAL("cpu_instrs/individual/01-special.gb", 1_256_633),
    INTERRUPTS("cpu_instrs/individual/02-interrupts.gb", 1_256_633),
    ;

    private final String path;
    private final int cycles;

    BlarggTest(String path, int cycles) {
      this.path = path;
      this.cycles = cycles;
    }

    public String getPath() {
      return path;
    }

    public int getCycles() {
      return cycles;
    }
  }

  @ParameterizedTest
  @EnumSource(BlarggTest.class)
  void testBlarggTest(BlarggTest test) throws Exception {
    URL romResource = Resources.getResource(test.getPath());
    ByteBuffer rom = ByteBuffer.wrap(Resources.toByteArray(romResource));
    Memory memory = new Memory(rom, rom);

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

    Path root = Path.of(System.getenv("TEST_UNDECLARED_OUTPUTS_DIR"));
    Path path = root.resolve("%s.txt".formatted(test.name().toLowerCase()));
    try (Writer writer = new OutputStreamWriter(new FileOutputStream(path.toFile()))) {
      Cpu cpu =
          new Cpu(
              registers,
              memory,
              mem -> {
                @SuppressWarnings("UnnecessaryLocalVariable")
                byte data = mem.execute(memory);
                // TODO: tick the other components
                return data;
              },
              writer);

      for (int i = 0; i < test.getCycles(); i++) {
        cpu.tick();
      }
    }
  }
}
