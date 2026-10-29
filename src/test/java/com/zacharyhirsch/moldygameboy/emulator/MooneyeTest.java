package com.zacharyhirsch.moldygameboy.emulator;

import static com.zacharyhirsch.moldygameboy.emulator.RegistersSubject.assertThat;

import com.google.common.io.Resources;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.io.Io;
import com.zacharyhirsch.moldygameboy.emulator.io.IoFactory;
import com.zacharyhirsch.moldygameboy.emulator.io.Ticker;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

final class MooneyeTest {

  private static ByteBuffer readRom(String path) throws IOException {
    return ByteBuffer.wrap(Resources.toByteArray(Resources.getResource(path)));
  }

  private static void doTest(String path) throws Exception {
    try (Io io = Boolean.getBoolean("moldy.gui") ? IoFactory.sdl() : IoFactory.none()) {
      MoldyGameBoy gb = new MoldyGameBoy(readRom("dmg.bin"), readRom(path), io);
      io.eventLoop().run(new BreakpointTicker(gb));
    }
  }

  private record BreakpointTicker(MoldyGameBoy gb) implements Ticker {

    @Override
    public void tick() {
      gb.tick();
    }

    @Override
    public boolean isDone() {
      if (isAtBreakpoint(gb.cpu().registers())) {
        assertThat(gb.cpu().registers()).isMooneyeSuccess();
        return true;
      }
      return false;
    }

    private static boolean isAtBreakpoint(Registers r) {
      if (r.ir().get() != (byte) 0x40) {
        return false;
      }
      byte b = r.b().get(), c = r.c().get(), d = r.d().get();
      byte e = r.e().get(), h = r.h().get(), l = r.l().get();
      boolean pass = b == 3 && c == 5 && d == 8 && e == 13 && h == 21 && l == 34;
      boolean failed = b == 0x42 && c == 0x42 && d == 0x42 && e == 0x42 && h == 0x42 && l == 0x42;
      return pass || failed;
    }
  }

  @Test
  void test__acceptance__add_sp_e_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/add_sp_e_timing.gb");
  }

  @Test
  void test__acceptance__bits__mem_oam() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/bits/mem_oam.gb");
  }

  @Test
  void test__acceptance__bits__reg_f() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/bits/reg_f.gb");
  }

  @Test
  void test__acceptance__bits__unused_hwio_GS() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/bits/unused_hwio-GS.gb");
  }

  @Test
  @Disabled
  void test__acceptance__boot_div_dmg0() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/boot_div-dmg0.gb");
  }

  @Test
  void test__acceptance__boot_div_dmgABCmgb() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/boot_div-dmgABCmgb.gb");
  }

  @Test
  @Disabled
  void test__acceptance__boot_div_S() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/boot_div-S.gb");
  }

  @Test
  @Disabled
  void test__acceptance__boot_div2_S() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/boot_div2-S.gb");
  }

  @Test
  @Disabled
  void test__acceptance__boot_hwio_dmg0() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/boot_hwio-dmg0.gb");
  }

  @Test
  void test__acceptance__boot_hwio_dmgABCmgb() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/boot_hwio-dmgABCmgb.gb");
  }

  @Test
  @Disabled
  void test__acceptance__boot_hwio_S() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/boot_hwio-S.gb");
  }

  @Test
  @Disabled
  void test__acceptance__boot_regs_dmg0() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/boot_regs-dmg0.gb");
  }

  @Test
  void test__acceptance__boot_regs_dmgABC() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/boot_regs-dmgABC.gb");
  }

  @Test
  @Disabled
  void test__acceptance__boot_regs_mgb() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/boot_regs-mgb.gb");
  }

  @Test
  @Disabled
  void test__acceptance__boot_regs_sgb() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/boot_regs-sgb.gb");
  }

  @Test
  @Disabled
  void test__acceptance__boot_regs_sgb2() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/boot_regs-sgb2.gb");
  }

  @Test
  void test__acceptance__call_cc_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/call_cc_timing.gb");
  }

  @Test
  void test__acceptance__call_cc_timing2() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/call_cc_timing2.gb");
  }

  @Test
  void test__acceptance__call_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/call_timing.gb");
  }

  @Test
  void test__acceptance__call_timing2() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/call_timing2.gb");
  }

  @Test
  void test__acceptance__di_timing_GS() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/di_timing-GS.gb");
  }

  @Test
  void test__acceptance__div_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/div_timing.gb");
  }

  @Test
  void test__acceptance__ei_sequence() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/ei_sequence.gb");
  }

  @Test
  void test__acceptance__ei_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/ei_timing.gb");
  }

  @Test
  void test__acceptance__halt_ime0_ei() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/halt_ime0_ei.gb");
  }

  @Test
  void test__acceptance__halt_ime0_nointr_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/halt_ime0_nointr_timing.gb");
  }

  @Test
  void test__acceptance__halt_ime1_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/halt_ime1_timing.gb");
  }

  @Test
  void test__acceptance__halt_ime1_timing2_GS() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/halt_ime1_timing2-GS.gb");
  }

  @Test
  void test__acceptance__if_ie_registers() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/if_ie_registers.gb");
  }

  @Test
  void test__acceptance__instr__daa() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/instr/daa.gb");
  }

  @Test
  void test__acceptance__interrupts__ie_push() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/interrupts/ie_push.gb");
  }

  @Test
  void test__acceptance__intr_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/intr_timing.gb");
  }

  @Test
  void test__acceptance__jp_cc_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/jp_cc_timing.gb");
  }

  @Test
  void test__acceptance__jp_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/jp_timing.gb");
  }

  @Test
  void test__acceptance__ld_hl_sp_e_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/ld_hl_sp_e_timing.gb");
  }

  @Test
  void test_oam_dma() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/oam_dma_restart.gb");
    doTest("mooneye-test-suite/build/acceptance/oam_dma_start.gb");
    doTest("mooneye-test-suite/build/acceptance/oam_dma_timing.gb");
    doTest("mooneye-test-suite/build/acceptance/oam_dma/basic.gb");
    doTest("mooneye-test-suite/build/acceptance/oam_dma/reg_read.gb");
    doTest("mooneye-test-suite/build/acceptance/oam_dma/sources-GS.gb");
  }

  @Test
  void test__acceptance__pop_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/pop_timing.gb");
  }

  @Test
  void test__acceptance__ppu__hblank_ly_scx_timing_GS() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/ppu/hblank_ly_scx_timing-GS.gb");
  }

  @Test
  void test__acceptance__ppu__intr_1_2_timing_GS() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/ppu/intr_1_2_timing-GS.gb");
  }

  @Test
  void test__acceptance__ppu__intr_2_0_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/ppu/intr_2_0_timing.gb");
  }

  @Test
  void test__acceptance__ppu__intr_2_mode0_timing_sprites() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/ppu/intr_2_mode0_timing_sprites.gb");
  }

  @Test
  void test__acceptance__ppu__intr_2_mode0_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/ppu/intr_2_mode0_timing.gb");
  }

  @Test
  void test__acceptance__ppu__intr_2_mode3_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/ppu/intr_2_mode3_timing.gb");
  }

  @Test
  void test__acceptance__ppu__intr_2_oam_ok_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/ppu/intr_2_oam_ok_timing.gb");
  }

  @Test
  void test__acceptance__ppu__lcdon_timing_GS() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/ppu/lcdon_timing-GS.gb");
  }

  @Test
  void test__acceptance__ppu__lcdon_write_timing_GS() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/ppu/lcdon_write_timing-GS.gb");
  }

  @Test
  void test__acceptance__ppu__stat_irq_blocking() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/ppu/stat_irq_blocking.gb");
  }

  @Test
  void test__acceptance__ppu__stat_lyc_onoff() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/ppu/stat_lyc_onoff.gb");
  }

  @Test
  void test__acceptance__ppu__vblank_stat_intr_GS() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/ppu/vblank_stat_intr-GS.gb");
  }

  @Test
  void test__acceptance__push_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/push_timing.gb");
  }

  @Test
  void test__acceptance__rapid_di_ei() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/rapid_di_ei.gb");
  }

  @Test
  void test__acceptance__ret_cc_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/ret_cc_timing.gb");
  }

  @Test
  void test__acceptance__ret_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/ret_timing.gb");
  }

  @Test
  void test__acceptance__reti_intr_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/reti_intr_timing.gb");
  }

  @Test
  void test__acceptance__reti_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/reti_timing.gb");
  }

  @Test
  void test__acceptance__rst_timing() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/rst_timing.gb");
  }

  @Test
  void test__acceptance__serial__boot_sclk_align_dmgABCmgb() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/serial/boot_sclk_align-dmgABCmgb.gb");
  }

  @Test
  void test__acceptance__timer__div_write() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/timer/div_write.gb");
  }

  @Test
  void test__acceptance__timer__rapid_toggle() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/timer/rapid_toggle.gb");
  }

  @Test
  void test__acceptance__timer__tim00_div_trigger() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/timer/tim00_div_trigger.gb");
  }

  @Test
  void test__acceptance__timer__tim00() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/timer/tim00.gb");
  }

  @Test
  void test__acceptance__timer__tim01_div_trigger() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/timer/tim01_div_trigger.gb");
  }

  @Test
  void test__acceptance__timer__tim01() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/timer/tim01.gb");
  }

  @Test
  void test__acceptance__timer__tim10_div_trigger() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/timer/tim10_div_trigger.gb");
  }

  @Test
  void test__acceptance__timer__tim10() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/timer/tim10.gb");
  }

  @Test
  void test__acceptance__timer__tim11_div_trigger() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/timer/tim11_div_trigger.gb");
  }

  @Test
  void test__acceptance__timer__tim11() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/timer/tim11.gb");
  }

  @Test
  void test__acceptance__timer__tima_reload() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/timer/tima_reload.gb");
  }

  @Test
  void test__acceptance__timer__tima_write_reloading() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/timer/tima_write_reloading.gb");
  }

  @Test
  void test__acceptance__timer__tma_write_reloading() throws Exception {
    doTest("mooneye-test-suite/build/acceptance/timer/tma_write_reloading.gb");
  }

  @Test
  void test_mbc1() throws Exception {
    doTest("mooneye-test-suite/build/emulator-only/mbc1/bits_bank1.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc1/bits_bank2.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc1/bits_mode.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc1/bits_ramg.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc1/multicart_rom_8Mb.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc1/ram_256kb.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc1/ram_64kb.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc1/rom_16Mb.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc1/rom_1Mb.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc1/rom_2Mb.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc1/rom_4Mb.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc1/rom_512kb.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc1/rom_8Mb.gb");
  }

  @Test
  void test__emulator_only__mbc2__bits_ramg() throws Exception {
    doTest("mooneye-test-suite/build/emulator-only/mbc2/bits_ramg.gb");
  }

  @Test
  void test__emulator_only__mbc2__bits_romb() throws Exception {
    doTest("mooneye-test-suite/build/emulator-only/mbc2/bits_romb.gb");
  }

  @Test
  void test__emulator_only__mbc2__bits_unused() throws Exception {
    doTest("mooneye-test-suite/build/emulator-only/mbc2/bits_unused.gb");
  }

  @Test
  void test__emulator_only__mbc2__ram() throws Exception {
    doTest("mooneye-test-suite/build/emulator-only/mbc2/ram.gb");
  }

  @Test
  void test__emulator_only__mbc2__rom_1Mb() throws Exception {
    doTest("mooneye-test-suite/build/emulator-only/mbc2/rom_1Mb.gb");
  }

  @Test
  void test__emulator_only__mbc2__rom_2Mb() throws Exception {
    doTest("mooneye-test-suite/build/emulator-only/mbc2/rom_2Mb.gb");
  }

  @Test
  void test__emulator_only__mbc2__rom_512kb() throws Exception {
    doTest("mooneye-test-suite/build/emulator-only/mbc2/rom_512kb.gb");
  }

  @Test
  void test_mbc5() throws Exception {
    doTest("mooneye-test-suite/build/emulator-only/mbc5/rom_16Mb.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc5/rom_1Mb.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc5/rom_2Mb.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc5/rom_32Mb.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc5/rom_4Mb.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc5/rom_512kb.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc5/rom_64Mb.gb");
    doTest("mooneye-test-suite/build/emulator-only/mbc5/rom_8Mb.gb");
  }

  @Test
  @Disabled("too long")
  void test__madness__mgb_oam_dma_halt_sprites() throws Exception {
    doTest("mooneye-test-suite/build/madness/mgb_oam_dma_halt_sprites.gb");
  }

  @Test
  @Disabled("manual-only")
  void test__manual_only__sprite_priority() throws Exception {
    doTest("mooneye-test-suite/build/manual-only/sprite_priority.gb");
  }

  @Test
  void test__misc__bits__unused_hwio_C() throws Exception {
    doTest("mooneye-test-suite/build/misc/bits/unused_hwio-C.gb");
  }

  @Test
  void test__misc__boot_div_A() throws Exception {
    doTest("mooneye-test-suite/build/misc/boot_div-A.gb");
  }

  @Test
  void test__misc__boot_div_cgb0() throws Exception {
    doTest("mooneye-test-suite/build/misc/boot_div-cgb0.gb");
  }

  @Test
  void test__misc__boot_div_cgbABCDE() throws Exception {
    doTest("mooneye-test-suite/build/misc/boot_div-cgbABCDE.gb");
  }

  @Test
  void test__misc__boot_hwio_C() throws Exception {
    doTest("mooneye-test-suite/build/misc/boot_hwio-C.gb");
  }

  @Test
  void test__misc__boot_regs_A() throws Exception {
    doTest("mooneye-test-suite/build/misc/boot_regs-A.gb");
  }

  @Test
  void test__misc__boot_regs_cgb() throws Exception {
    doTest("mooneye-test-suite/build/misc/boot_regs-cgb.gb");
  }

  @Test
  void test__misc__ppu__vblank_stat_intr_C() throws Exception {
    doTest("mooneye-test-suite/build/misc/ppu/vblank_stat_intr-C.gb");
  }
}
