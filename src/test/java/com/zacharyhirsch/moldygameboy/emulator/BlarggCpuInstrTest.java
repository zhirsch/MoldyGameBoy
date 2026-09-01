package com.zacharyhirsch.moldygameboy.emulator;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Register8;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.IoFactory;
import com.zacharyhirsch.moldygameboy.emulator.memory.Memory;
import java.net.URL;
import java.nio.ByteBuffer;
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
    byte value = rom.get(0x0100);
    registers.ir().write(value);
    registers.pc().set((short) 0x0101);
    registers.sp().set((short) 0xfffe);
    Register8 register14 = registers.a();
    register14.write((byte) 0x01);
    registers.f().set((byte) 0xb0);
    Register8 register13 = registers.b();
    register13.write((byte) 0x00);
    Register8 register12 = registers.c();
    register12.write((byte) 0x13);
    Register8 register11 = registers.d();
    register11.write((byte) 0x00);
    Register8 register10 = registers.e();
    register10.write((byte) 0xd8);
    Register8 register9 = registers.h();
    register9.write((byte) 0x01);
    Register8 register8 = registers.l();
    register8.write((byte) 0x4d);

    try (Io io = IoFactory.none()) {
      Memory memory = new Memory(rom, rom);
      MoldyGameBoy gb = new MoldyGameBoy(memory, registers, io);
      for (long i = 0; i < (long) test.getCycles(); i++) {
        gb.tick();
      }
    }
  }
}
