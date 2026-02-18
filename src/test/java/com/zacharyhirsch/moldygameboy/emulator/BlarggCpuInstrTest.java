package com.zacharyhirsch.moldygameboy.emulator;


import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.IORegisters;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

final class BlarggCpuInstrTest {

  enum BlarggTest {
    SPECIAL("cpu_instrs/individual/01-special.gb", 2_500_000),
    INTERRUPTS("cpu_instrs/individual/02-interrupts.gb", 2_500_000),
    OP_SL_HL("cpu_instrs/individual/03-op sp,hl.gb", 2_500_000),
    OP_R_IMM("cpu_instrs/individual/04-op r,imm.gb", 5_000_000),
    OP_RP("cpu_instrs/individual/05-op rp.gb", 5_000_000),
    LD_R_R("cpu_instrs/individual/06-ld r,r.gb", 2_500_000),
    JR_JP_CALL_RET_RST("cpu_instrs/individual/07-jr,jp,call,ret,rst.gb", 2_500_000),
    MISC_INSTRS("cpu_instrs/individual/08-misc instrs.gb", 2_500_000),
    OP_R_R("cpu_instrs/individual/09-op r,r.gb", 10_000_000),
    BIT_OPS("cpu_instrs/individual/10-bit ops.gb", 15_000_000),
    OP_A_HL("cpu_instrs/individual/11-op a,(hl).gb", 20_000_000),
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

    IORegisters ioRegisters = new IORegisters();

    Path root = Path.of(System.getenv("TEST_UNDECLARED_OUTPUTS_DIR"));
    Path path = root.resolve("%s.txt".formatted(test.name().toLowerCase()));
    try (Writer writer = new OutputStreamWriter(new FileOutputStream(path.toFile()))) {
      MoldyGameBoy.run(test.getCycles(), rom, rom, registers, ioRegisters, writer);
    }
  }
}
