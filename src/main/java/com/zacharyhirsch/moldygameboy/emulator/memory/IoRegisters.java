package com.zacharyhirsch.moldygameboy.emulator.memory;

import com.zacharyhirsch.moldygameboy.emulator.arch.IoRegister;
import com.zacharyhirsch.moldygameboy.emulator.arch.Model;

public final class IoRegisters {

  private final Model model;

  /* ff00 */ public final IoRegister joyp;

  /* ff01 */ public final IoRegister sb;
  /* ff02 */ public final IoRegister sc;
  /* ff04 */ public final IoRegister div;
  /* ff05 */ public final IoRegister tima;
  /* ff06 */ public final IoRegister tma;
  /* ff07 */ public final IoRegister tac;
  /* ff0f */ public final IoRegister if_;

  /* ff10 */ public final IoRegister nr10;
  /* ff11 */ public final IoRegister nr11;
  /* ff12 */ public final IoRegister nr12;
  /* ff13 */ public final IoRegister nr13;
  /* ff14 */ public final IoRegister nr14;

  /* ff16 */ public final IoRegister nr21;
  /* ff17 */ public final IoRegister nr22;
  /* ff18 */ public final IoRegister nr23;
  /* ff19 */ public final IoRegister nr24;

  /* ff1a */ public final IoRegister nr30;
  /* ff1b */ public final IoRegister nr31;
  /* ff1c */ public final IoRegister nr32;
  /* ff1d */ public final IoRegister nr33;
  /* ff1e */ public final IoRegister nr34;

  /* ff20 */ public final IoRegister nr41;
  /* ff21 */ public final IoRegister nr42;
  /* ff22 */ public final IoRegister nr43;
  /* ff23 */ public final IoRegister nr44;

  /* ff24 */ public final IoRegister nr50;
  /* ff25 */ public final IoRegister nr51;
  /* ff26 */ public final IoRegister nr52;

  /* ff30 */ public final IoRegister wave0;
  /* ff31 */ public final IoRegister wave1;
  /* ff32 */ public final IoRegister wave2;
  /* ff33 */ public final IoRegister wave3;
  /* ff34 */ public final IoRegister wave4;
  /* ff35 */ public final IoRegister wave5;
  /* ff36 */ public final IoRegister wave6;
  /* ff37 */ public final IoRegister wave7;
  /* ff38 */ public final IoRegister wave8;
  /* ff39 */ public final IoRegister wave9;
  /* ff3a */ public final IoRegister wavea;
  /* ff3b */ public final IoRegister waveb;
  /* ff3c */ public final IoRegister wavec;
  /* ff3d */ public final IoRegister waved;
  /* ff3e */ public final IoRegister wavee;
  /* ff3f */ public final IoRegister wavef;

  /* ff40 */ public final IoRegister lcdc = IoRegister.create("lcdc", (short) 0xff40);
  /* ff41 */ public final IoRegister stat = IoRegister.create("stat", (short) 0xff41);
  /* ff42 */ public final IoRegister scy = IoRegister.create("scy", (short) 0xff42);
  /* ff43 */ public final IoRegister scx = IoRegister.create("scx", (short) 0xff43);
  /* ff44 */ public final IoRegister ly = IoRegister.create("ly", (short) 0xff44);
  /* ff45 */ public final IoRegister lyc = IoRegister.create("lyc", (short) 0xff45);
  /* ff46 */ public final OamDmaRegister dma;
  /* ff47 */ public final IoRegister bgp = IoRegister.create("bgp", (short) 0xff47);
  /* ff48 */ public final IoRegister obp0 = IoRegister.create("obp0", (short) 0xff48);
  /* ff49 */ public final IoRegister obp1 = IoRegister.create("obp1", (short) 0xff49);
  /* ff4a */ public final IoRegister wy = IoRegister.create("wy", (short) 0xff4a);
  /* ff4b */ public final IoRegister wx = IoRegister.create("wx", (short) 0xff4b);

  // CGB only.
  /* ff4c */ public final IoRegister key0 = IoRegister.create("key0", (short) 0xff4c);
  /* ff4d */ public final IoRegister key1 = IoRegister.create("key1", (short) 0xff4d);
  /* ff4f */ public final IoRegister vbk;

  /* ff50 */ public final IoRegister bank = IoRegister.create("bank", (short) 0xff50);

  // CGB only.
  /* ff51 */ public final IoRegister hdma1;
  /* ff52 */ public final IoRegister hdma2;
  /* ff53 */ public final IoRegister hdma3;
  /* ff54 */ public final IoRegister hdma4;
  /* ff55 */ public final IoRegister hdma5;
  /* ff56 */ public final IoRegister rp = IoRegister.create("rp", (short) 0xff56);
  /* ff68 */ public final IoRegister bcps;
  /* ff69 */ public final IoRegister bcpd;
  /* ff6a */ public final IoRegister ocps;
  /* ff6b */ public final IoRegister ocpd;
  /* ff6c */ public final IoRegister opri = IoRegister.create("opri", (short) 0xff6c);
  /* ff70 */ public final IoRegister svbk;
  /* ff72 */ public final IoRegister ff72 = IoRegister.create("ff72", (short) 0xff72);
  /* ff73 */ public final IoRegister ff73 = IoRegister.create("ff73", (short) 0xff73);
  /* ff74 */ public final IoRegister ff74 = IoRegister.create("ff74", (short) 0xff74);
  /* ff75 */ public final IoRegister ff75 = IoRegister.create("ff75", (short) 0xff75);
  /* ff76 */ public final IoRegister pcm12;
  /* ff77 */ public final IoRegister pcm34;

  public IoRegisters(
      Model model,
      IoRegister joyp,
      IoRegister sb,
      IoRegister sc,
      IoRegister div,
      IoRegister tima,
      IoRegister tma,
      IoRegister tac,
      IoRegister if_,
      IoRegister nr10,
      IoRegister nr11,
      IoRegister nr12,
      IoRegister nr13,
      IoRegister nr14,
      IoRegister nr21,
      IoRegister nr22,
      IoRegister nr23,
      IoRegister nr24,
      IoRegister nr30,
      IoRegister nr31,
      IoRegister nr32,
      IoRegister nr33,
      IoRegister nr34,
      IoRegister nr41,
      IoRegister nr42,
      IoRegister nr43,
      IoRegister nr44,
      IoRegister nr50,
      IoRegister nr51,
      IoRegister nr52,
      IoRegister wave0,
      IoRegister wave1,
      IoRegister wave2,
      IoRegister wave3,
      IoRegister wave4,
      IoRegister wave5,
      IoRegister wave6,
      IoRegister wave7,
      IoRegister wave8,
      IoRegister wave9,
      IoRegister wavea,
      IoRegister waveb,
      IoRegister wavec,
      IoRegister waved,
      IoRegister wavee,
      IoRegister wavef,
      IoRegister vbk,
      IoRegister hdma1,
      IoRegister hdma2,
      IoRegister hdma3,
      IoRegister hdma4,
      IoRegister hdma5,
      IoRegister bcps,
      IoRegister bcpd,
      IoRegister ocps,
      IoRegister ocpd,
      IoRegister svbk,
      IoRegister pcm12,
      IoRegister pcm34,
      Memory memory) {
    this.model = model;
    this.joyp = joyp;
    this.sb = sb;
    this.sc = sc;
    this.div = div;
    this.tima = tima;
    this.tma = tma;
    this.tac = tac;
    this.if_ = if_;
    this.nr10 = nr10;
    this.nr11 = nr11;
    this.nr12 = nr12;
    this.nr13 = nr13;
    this.nr14 = nr14;
    this.nr21 = nr21;
    this.nr22 = nr22;
    this.nr23 = nr23;
    this.nr24 = nr24;
    this.nr30 = nr30;
    this.nr31 = nr31;
    this.nr32 = nr32;
    this.nr33 = nr33;
    this.nr34 = nr34;
    this.nr41 = nr41;
    this.nr42 = nr42;
    this.nr43 = nr43;
    this.nr44 = nr44;
    this.nr50 = nr50;
    this.nr51 = nr51;
    this.nr52 = nr52;
    this.wave0 = wave0;
    this.wave1 = wave1;
    this.wave2 = wave2;
    this.wave3 = wave3;
    this.wave4 = wave4;
    this.wave5 = wave5;
    this.wave6 = wave6;
    this.wave7 = wave7;
    this.wave8 = wave8;
    this.wave9 = wave9;
    this.wavea = wavea;
    this.waveb = waveb;
    this.wavec = wavec;
    this.waved = waved;
    this.wavee = wavee;
    this.wavef = wavef;
    this.vbk = vbk;
    this.hdma1 = hdma1;
    this.hdma2 = hdma2;
    this.hdma3 = hdma3;
    this.hdma4 = hdma4;
    this.hdma5 = hdma5;
    this.bcps = bcps;
    this.bcpd = bcpd;
    this.ocps = ocps;
    this.ocpd = ocpd;
    this.svbk = svbk;
    this.pcm12 = pcm12;
    this.pcm34 = pcm34;
    this.dma = new OamDmaRegister(memory);
  }

  /**
   * Whether this is a CGB running in CGB mode, rather than a DMG or a CGB in DMG compatibility mode
   * (which the CGB boot ROM selects for DMG games by writing bit 2 of KEY0).
   */
  public boolean isCgbMode() {
    return model == Model.CGB && (key0.get() & 0x04) == 0;
  }

  private boolean isCgb() {
    return model == Model.CGB;
  }

  private boolean isBootRomMapped() {
    return bank.get() == 0;
  }

  /**
   * Whether the CGB-mode registers can be accessed: in CGB mode, or while the boot ROM is still
   * running (it sets some of them up before selecting compatibility mode).
   */
  private boolean isCgbModeOrBooting() {
    return isCgbMode() || (isCgb() && isBootRomMapped());
  }

  public byte read(short address) {
    return switch (Short.toUnsignedInt(address)) {
      case 0xff00 -> (byte) (joyp.get() | 0xc0);
      case 0xff01 -> (byte) sb.get();
      case 0xff02 -> (byte) (sc.get() | 0x7e);
      case 0xff04 -> (byte) div.get();
      case 0xff05 -> (byte) tima.get();
      case 0xff06 -> (byte) tma.get();
      case 0xff07 -> (byte) (tac.get() | 0xf8);
      case 0xff0f -> (byte) (if_.get() | 0xe0);
      case 0xff10 -> (byte) (nr10.get() | 0x80);
      case 0xff11 -> (byte) (nr11.get() | 0x3f);
      case 0xff12 -> (byte) nr12.get();
      case 0xff13 -> (byte) (nr13.get() | 0xff);
      case 0xff14 -> (byte) (nr14.get() | 0xbf);
      case 0xff16 -> (byte) (nr21.get() | 0x3f);
      case 0xff17 -> (byte) nr22.get();
      case 0xff18 -> (byte) (nr23.get() | 0xff);
      case 0xff19 -> (byte) (nr24.get() | 0xbf);
      case 0xff1a -> (byte) (nr30.get() | 0x7f);
      case 0xff1b -> (byte) (nr31.get() | 0xff);
      case 0xff1c -> (byte) (nr32.get() | 0x9f);
      case 0xff1d -> (byte) (nr33.get() | 0xff);
      case 0xff1e -> (byte) (nr34.get() | 0xbf);
      case 0xff20 -> (byte) (nr41.get() | 0xff);
      case 0xff21 -> (byte) nr42.get();
      case 0xff22 -> (byte) nr43.get();
      case 0xff23 -> (byte) (nr44.get() | 0xbf);
      case 0xff24 -> (byte) nr50.get();
      case 0xff25 -> (byte) nr51.get();
      case 0xff26 -> (byte) (nr52.get() | 0x70);
      case 0xff30 -> (byte) wave0.get();
      case 0xff31 -> (byte) wave1.get();
      case 0xff32 -> (byte) wave2.get();
      case 0xff33 -> (byte) wave3.get();
      case 0xff34 -> (byte) wave4.get();
      case 0xff35 -> (byte) wave5.get();
      case 0xff36 -> (byte) wave6.get();
      case 0xff37 -> (byte) wave7.get();
      case 0xff38 -> (byte) wave8.get();
      case 0xff39 -> (byte) wave9.get();
      case 0xff3a -> (byte) wavea.get();
      case 0xff3b -> (byte) waveb.get();
      case 0xff3c -> (byte) wavec.get();
      case 0xff3d -> (byte) waved.get();
      case 0xff3e -> (byte) wavee.get();
      case 0xff3f -> (byte) wavef.get();
      case 0xff40 -> (byte) lcdc.get();
      case 0xff41 -> (byte) (stat.get() | 0x80);
      case 0xff42 -> (byte) scy.get();
      case 0xff43 -> (byte) scx.get();
      case 0xff44 -> (byte) ly.get();
      case 0xff45 -> (byte) lyc.get();
      case 0xff46 -> (byte) dma.get();
      case 0xff47 -> (byte) bgp.get();
      case 0xff48 -> (byte) obp0.get();
      case 0xff49 -> (byte) obp1.get();
      case 0xff4a -> (byte) wy.get();
      case 0xff4b -> (byte) wx.get();
      case 0xff4c -> isCgb() && isBootRomMapped() ? (byte) key0.get() : (byte) 0xff;
      case 0xff4d -> isCgbModeOrBooting() ? (byte) (key1.get() | 0x7e) : (byte) 0xff;
      case 0xff4f -> isCgb() ? (byte) (vbk.get() | 0xfe) : (byte) 0xff;
      case 0xff50 -> (byte) (bank.get() | 0xff);
      case 0xff55 -> isCgbModeOrBooting() ? (byte) hdma5.get() : (byte) 0xff;
      case 0xff56 -> isCgbModeOrBooting() ? (byte) (rp.get() | 0x3e) : (byte) 0xff;
      case 0xff68 -> isCgb() ? (byte) (bcps.get() | 0x40) : (byte) 0xff;
      case 0xff69 -> isCgbModeOrBooting() ? (byte) bcpd.get() : (byte) 0xff;
      case 0xff6a -> isCgb() ? (byte) (ocps.get() | 0x40) : (byte) 0xff;
      case 0xff6b -> isCgbModeOrBooting() ? (byte) ocpd.get() : (byte) 0xff;
      case 0xff6c -> isCgbModeOrBooting() ? (byte) (opri.get() | 0xfe) : (byte) 0xff;
      case 0xff70 -> isCgbModeOrBooting() ? (byte) (svbk.get() | 0xf8) : (byte) 0xff;
      case 0xff72 -> isCgb() ? (byte) ff72.get() : (byte) 0xff;
      case 0xff73 -> isCgb() ? (byte) ff73.get() : (byte) 0xff;
      case 0xff74 -> isCgbModeOrBooting() ? (byte) ff74.get() : (byte) 0xff;
      case 0xff75 -> isCgb() ? (byte) (ff75.get() | 0x8f) : (byte) 0xff;
      case 0xff76 -> isCgb() ? (byte) pcm12.get() : (byte) 0xff;
      case 0xff77 -> isCgb() ? (byte) pcm34.get() : (byte) 0xff;
      default -> (byte) 0xff;
    };
  }

  public void write(short address, byte value) {
    switch (Short.toUnsignedInt(address)) {
      case 0xff00 -> joyp.set((byte) (value & 0x30));
      case 0xff01 -> sb.set((byte) (value & 0xff));
      case 0xff02 -> sc.set((byte) (value & 0x81));
      case 0xff04 -> div.set((byte) 0);
      case 0xff05 -> tima.set((byte) (value & 0xff));
      case 0xff06 -> tma.set((byte) (value & 0xff));
      case 0xff07 -> tac.set((byte) (value & 0x07));
      case 0xff0f -> if_.set((byte) (value & 0x1f));
      case 0xff10 -> nr10.set((byte) (value & 0x7f));
      case 0xff11 -> nr11.set((byte) (value & 0xff));
      case 0xff12 -> nr12.set((byte) (value & 0xff));
      case 0xff13 -> nr13.set((byte) (value & 0xff));
      case 0xff14 -> nr14.set((byte) (value & 0xc7));
      case 0xff16 -> nr21.set((byte) (value & 0xff));
      case 0xff17 -> nr22.set((byte) (value & 0xff));
      case 0xff18 -> nr23.set((byte) (value & 0xff));
      case 0xff19 -> nr24.set((byte) (value & 0xc7));
      case 0xff1a -> nr30.set((byte) (value & 0x80));
      case 0xff1b -> nr31.set((byte) (value & 0xff));
      case 0xff1c -> nr32.set((byte) (value & 0x60));
      case 0xff1d -> nr33.set((byte) (value & 0xff));
      case 0xff1e -> nr34.set((byte) (value & 0xc7));
      case 0xff20 -> nr41.set((byte) (value & 0x3f));
      case 0xff21 -> nr42.set((byte) (value & 0xff));
      case 0xff22 -> nr43.set((byte) (value & 0xff));
      case 0xff23 -> nr44.set((byte) (value & 0xc0));
      case 0xff24 -> nr50.set((byte) (value & 0xff));
      case 0xff25 -> nr51.set((byte) (value & 0xff));
      case 0xff26 -> nr52.set((byte) (value & 0x80));
      case 0xff30 -> wave0.set((byte) (value & 0xff));
      case 0xff31 -> wave1.set((byte) (value & 0xff));
      case 0xff32 -> wave2.set((byte) (value & 0xff));
      case 0xff33 -> wave3.set((byte) (value & 0xff));
      case 0xff34 -> wave4.set((byte) (value & 0xff));
      case 0xff35 -> wave5.set((byte) (value & 0xff));
      case 0xff36 -> wave6.set((byte) (value & 0xff));
      case 0xff37 -> wave7.set((byte) (value & 0xff));
      case 0xff38 -> wave8.set((byte) (value & 0xff));
      case 0xff39 -> wave9.set((byte) (value & 0xff));
      case 0xff3a -> wavea.set((byte) (value & 0xff));
      case 0xff3b -> waveb.set((byte) (value & 0xff));
      case 0xff3c -> wavec.set((byte) (value & 0xff));
      case 0xff3d -> waved.set((byte) (value & 0xff));
      case 0xff3e -> wavee.set((byte) (value & 0xff));
      case 0xff3f -> wavef.set((byte) (value & 0xff));
      case 0xff40 -> lcdc.set((byte) (value & 0xff));
      case 0xff41 -> stat.set((byte) (value & 0x78));
      case 0xff42 -> scy.set((byte) (value & 0xff));
      case 0xff43 -> scx.set((byte) (value & 0xff));
      case 0xff45 -> lyc.set((byte) (value & 0xff));
      case 0xff46 -> dma.set((byte) (value & 0xff));
      case 0xff47 -> bgp.set((byte) (value & 0xff));
      case 0xff48 -> obp0.set((byte) (value & 0xff));
      case 0xff49 -> obp1.set((byte) (value & 0xff));
      case 0xff4a -> wy.set((byte) (value & 0xff));
      case 0xff4b -> wx.set((byte) (value & 0xff));
      case 0xff4c -> {
        // KEY0 can only be written by the boot ROM.
        if (isCgb() && isBootRomMapped()) {
          key0.set((byte) (value & 0xff));
        }
      }
      case 0xff4d -> {
        if (isCgbModeOrBooting()) {
          key1.set((byte) (value & 0x01));
        }
      }
      case 0xff4f -> {
        if (isCgbModeOrBooting()) {
          vbk.set((byte) (value & 0x01));
        }
      }
      case 0xff50 -> bank.set((byte) (value & 0x01));
      case 0xff51 -> {
        if (isCgbModeOrBooting()) {
          hdma1.set((byte) (value & 0xff));
        }
      }
      case 0xff52 -> {
        if (isCgbModeOrBooting()) {
          hdma2.set((byte) (value & 0xff));
        }
      }
      case 0xff53 -> {
        if (isCgbModeOrBooting()) {
          hdma3.set((byte) (value & 0xff));
        }
      }
      case 0xff54 -> {
        if (isCgbModeOrBooting()) {
          hdma4.set((byte) (value & 0xff));
        }
      }
      case 0xff55 -> {
        if (isCgbModeOrBooting()) {
          hdma5.set((byte) (value & 0xff));
        }
      }
      case 0xff56 -> {
        if (isCgbModeOrBooting()) {
          rp.set((byte) (value & 0xc1));
        }
      }
      case 0xff68 -> {
        if (isCgb()) {
          bcps.set((byte) (value & 0xbf));
        }
      }
      case 0xff69 -> {
        if (isCgbModeOrBooting()) {
          bcpd.set((byte) (value & 0xff));
        }
      }
      case 0xff6a -> {
        if (isCgb()) {
          ocps.set((byte) (value & 0xbf));
        }
      }
      case 0xff6b -> {
        if (isCgbModeOrBooting()) {
          ocpd.set((byte) (value & 0xff));
        }
      }
      case 0xff6c -> {
        if (isCgbModeOrBooting()) {
          opri.set((byte) (value & 0x01));
        }
      }
      case 0xff70 -> {
        if (isCgbModeOrBooting()) {
          svbk.set((byte) (value & 0x07));
        }
      }
      case 0xff72 -> {
        if (isCgb()) {
          ff72.set((byte) (value & 0xff));
        }
      }
      case 0xff73 -> {
        if (isCgb()) {
          ff73.set((byte) (value & 0xff));
        }
      }
      case 0xff74 -> {
        if (isCgbModeOrBooting()) {
          ff74.set((byte) (value & 0xff));
        }
      }
      case 0xff75 -> {
        if (isCgb()) {
          ff75.set((byte) (value & 0x70));
        }
      }
      default -> {}
    }
  }
}
