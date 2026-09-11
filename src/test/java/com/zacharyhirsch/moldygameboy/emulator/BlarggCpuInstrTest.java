package com.zacharyhirsch.moldygameboy.emulator;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.IoFactory;
import com.zacharyhirsch.moldygameboy.emulator.memory.BootRom;
import com.zacharyhirsch.moldygameboy.emulator.memory.Rom;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
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
    ByteBuffer[] banks = read(test.getPath(), 0x4000);
    BootRom boot = new BootRom(banks[0].slice(0, 0x0100));
    Rom rom = new Rom(banks);

    Registers registers = new Registers();
    registers.ir().write(rom.read((short) 0x0100));
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

    try (Io io = IoFactory.none()) {
      MoldyGameBoy gb = new MoldyGameBoy(boot, rom, registers, io);
      for (long i = 0; i < (long) test.getCycles(); i++) {
        gb.tick();
      }
    }
  }

  public static ByteBuffer[] read(String path, int size) throws IOException {
    URL romResource = Resources.getResource(path);
    ByteBuffer bytes = ByteBuffer.wrap(Resources.toByteArray(romResource));

    if (bytes.limit() % size != 0) {
      throw new IllegalArgumentException(
          "file size %d is not a multiple of bank size %d".formatted(bytes.limit(), size));
    }

    ArrayList<ByteBuffer> buffers = new ArrayList<>();
    for (int position = 0; position != bytes.limit(); position += size) {
      buffers.add(bytes.slice(position, size));
    }
    return buffers.toArray(ByteBuffer[]::new);
  }
}
