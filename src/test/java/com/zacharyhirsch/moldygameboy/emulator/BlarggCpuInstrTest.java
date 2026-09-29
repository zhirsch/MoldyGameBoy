package com.zacharyhirsch.moldygameboy.emulator;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.IoFactory;
import com.zacharyhirsch.moldygameboy.emulator.memory.BootRom;
import com.zacharyhirsch.moldygameboy.emulator.memory.Rom;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

final class BlarggCpuInstrTest {

  private static ByteBuffer read(String path) throws IOException {
    return ByteBuffer.wrap(Resources.toByteArray(Resources.getResource(path)));
  }

  @Test
  void testAll() throws Exception {
    BootRom boot = new BootRom(read("dmg.bin"));
    //    Rom rom = new Rom(read("cpu_instrs/cpu_instrs.gb"));
    //    Rom rom = new Rom(read("cpu_instrs/individual/01-special.gb"));
    Rom rom = new Rom(read("cpu_instrs/individual/02-interrupts.gb"));
    //    Rom rom = new Rom(read("cpu_instrs/individual/03-op sp,hl.gb"));
    //    Rom rom = new Rom(read("cpu_instrs/individual/04-op r,imm.gb"));
    //    Rom rom = new Rom(read("cpu_instrs/individual/05-op rp.gb"));
    //    Rom rom = new Rom(read("cpu_instrs/individual/06-ld r,r.gb"));
    //    Rom rom = new Rom(read("cpu_instrs/individual/07-jr,jp,call,ret,rst.gb"));
    //    Rom rom = new Rom(read("cpu_instrs/individual/08-misc instrs.gb"));
    //    Rom rom = new Rom(read("cpu_instrs/individual/09-op r,r.gb"));
    //    Rom rom = new Rom(read("cpu_instrs/individual/10-bit ops.gb"));
    //    Rom rom = new Rom(read("cpu_instrs/individual/11-op a,(hl).gb"));

    Registers registers = new Registers();

    try (Io io = IoFactory.sdl()) {
      MoldyGameBoy gb = new MoldyGameBoy(boot, rom, registers, io);
      try {
        io.eventLoop().run(gb::tick);
      } catch (Exception e) {
        io.video().setError(e);
        io.eventLoop().run(() -> {});
        throw e;
      }
    }
  }
}
