package com.zacharyhirsch.moldygameboy.emulator.memory;

import com.zacharyhirsch.moldygameboy.emulator.arch.IoRegister;
import java.util.function.Consumer;

public final class IoRegisters {

  /* ff00 */ public final IoRegister joyp = IoRegister.create("joyp", (short) 0xff00);

  /* ff01 */ public final IoRegister sb = IoRegister.create("sb", (short) 0xff01);
  /* ff02 */ public final IoRegister sc = IoRegister.create("sc", (short) 0xff02);
  /* ff04 */ public final IoRegister div;
  /* ff05 */ public final IoRegister tima;
  /* ff06 */ public final IoRegister tma;
  /* ff07 */ public final IoRegister tac;
  /* ff0f */ public final IoRegister if_;

  /* ff10 */ public final IoRegister nr10 = IoRegister.create("nr10", (short) 0xff10);
  /* ff11 */ public final IoRegister nr11 = IoRegister.create("nr11", (short) 0xff11);
  /* ff12 */ public final IoRegister nr12 = IoRegister.create("nr12", (short) 0xff12);
  /* ff13 */ public final IoRegister nr13 = IoRegister.create("nr13", (short) 0xff13);
  /* ff14 */ public final IoRegister nr14 = IoRegister.create("nr14", (short) 0xff14);

  /* ff16 */ public final IoRegister nr21 = IoRegister.create("nr21", (short) 0xff16);
  /* ff17 */ public final IoRegister nr22 = IoRegister.create("nr22", (short) 0xff17);
  /* ff18 */ public final IoRegister nr23 = IoRegister.create("nr23", (short) 0xff18);
  /* ff19 */ public final IoRegister nr24 = IoRegister.create("nr24", (short) 0xff19);

  /* ff1a */ public final IoRegister nr30 = IoRegister.create("nr30", (short) 0xff1a);
  /* ff1b */ public final IoRegister nr31 = IoRegister.create("nr31", (short) 0xff1b);
  /* ff1c */ public final IoRegister nr32 = IoRegister.create("nr32", (short) 0xff1c);
  /* ff1d */ public final IoRegister nr33 = IoRegister.create("nr33", (short) 0xff1d);
  /* ff1e */ public final IoRegister nr34 = IoRegister.create("nr34", (short) 0xff1e);

  /* ff20 */ public final IoRegister nr41 = IoRegister.create("nr41", (short) 0xff20);
  /* ff21 */ public final IoRegister nr42 = IoRegister.create("nr42", (short) 0xff21);
  /* ff22 */ public final IoRegister nr43 = IoRegister.create("nr43", (short) 0xff22);
  /* ff23 */ public final IoRegister nr44 = IoRegister.create("nr44", (short) 0xff23);

  /* ff24 */ public final IoRegister nr50 = IoRegister.create("nr50", (short) 0xff24);
  /* ff25 */ public final IoRegister nr51 = IoRegister.create("nr51", (short) 0xff25);
  /* ff26 */ public final IoRegister nr52 = IoRegister.create("nr52", (short) 0xff26);

  /* ff30 */ public final IoRegister wave0 = IoRegister.create("wave0", (short) 0xff30);
  /* ff31 */ public final IoRegister wave1 = IoRegister.create("wave1", (short) 0xff31);
  /* ff32 */ public final IoRegister wave2 = IoRegister.create("wave2", (short) 0xff32);
  /* ff33 */ public final IoRegister wave3 = IoRegister.create("wave3", (short) 0xff33);
  /* ff34 */ public final IoRegister wave4 = IoRegister.create("wave4", (short) 0xff34);
  /* ff35 */ public final IoRegister wave5 = IoRegister.create("wave5", (short) 0xff35);
  /* ff36 */ public final IoRegister wave6 = IoRegister.create("wave6", (short) 0xff36);
  /* ff37 */ public final IoRegister wave7 = IoRegister.create("wave7", (short) 0xff37);
  /* ff38 */ public final IoRegister wave8 = IoRegister.create("wave8", (short) 0xff38);
  /* ff39 */ public final IoRegister wave9 = IoRegister.create("wave9", (short) 0xff39);
  /* ff3a */ public final IoRegister wavea = IoRegister.create("wavea", (short) 0xff3a);
  /* ff3b */ public final IoRegister waveb = IoRegister.create("waveb", (short) 0xff3b);
  /* ff3c */ public final IoRegister wavec = IoRegister.create("wavec", (short) 0xff3c);
  /* ff3d */ public final IoRegister waved = IoRegister.create("waved", (short) 0xff3d);
  /* ff3e */ public final IoRegister wavee = IoRegister.create("wavee", (short) 0xff3e);
  /* ff3f */ public final IoRegister wavef = IoRegister.create("wavef", (short) 0xff3f);

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

  /* ff50 */ public final IoRegister bank = IoRegister.create("bank", (short) 0xff50);

  private Consumer<Character> serialOutput = IO::print;

  public IoRegisters(
      IoRegister div,
      IoRegister tima,
      IoRegister tma,
      IoRegister tac,
      IoRegister if_,
      Memory memory) {
    this.div = div;
    this.tima = tima;
    this.tma = tma;
    this.tac = tac;
    this.if_ = if_;
    this.dma = new OamDmaRegister(memory);
  }

  /** Where bytes sent over the serial port go. Defaults to stdout. */
  public void setSerialOutput(Consumer<Character> serialOutput) {
    this.serialOutput = serialOutput;
  }

  public byte read(short address) {
    return switch (Short.toUnsignedInt(address)) {
      case 0xff00 -> (byte) (joyp.get() | 0xcf);
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
      case 0xff14 -> (byte) (nr14.get() | 0x38);
      case 0xff16 -> (byte) (nr21.get() | 0x3f);
      case 0xff17 -> (byte) nr22.get();
      case 0xff18 -> (byte) (nr23.get() | 0xff);
      case 0xff19 -> (byte) (nr24.get() | 0x38);
      case 0xff1a -> (byte) (nr30.get() | 0x7f);
      case 0xff1b -> (byte) (nr31.get() | 0xff);
      case 0xff1c -> (byte) (nr32.get() | 0x9f);
      case 0xff1d -> (byte) (nr33.get() | 0xff);
      case 0xff1e -> (byte) (nr34.get() | 0x38);
      case 0xff20 -> (byte) (nr41.get() | 0xff);
      case 0xff21 -> (byte) nr42.get();
      case 0xff22 -> (byte) nr43.get();
      case 0xff23 -> (byte) (nr44.get() | 0x3f);
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
      case 0xff50 -> (byte) (bank.get() | 0xff);
      default -> (byte) 0xff;
    };
  }

  public void write(short address, byte value) {
    switch (Short.toUnsignedInt(address)) {
      case 0xff00 -> joyp.set((byte) (value & 0x30));
      case 0xff01 -> sb.set((byte) (value & 0xff));
      case 0xff02 -> {
        sc.set((byte) (value & 0x81));
        serialOutput.accept((char) sb.get());
      }
      case 0xff04 -> div.set((byte) 0);
      case 0xff05 -> tima.set((byte) (value & 0xff));
      case 0xff06 -> tma.set((byte) (value & 0xff));
      case 0xff07 -> tac.set((byte) (value & 0x07));
      case 0xff0f -> if_.set((byte) (value & 0x1f));
      case 0xff10 -> nr10.set((byte) (value & 0x7f));
      case 0xff11 -> nr11.set((byte) (value & 0xff));
      case 0xff12 -> nr12.set((byte) (value & 0xff));
      case 0xff13 -> nr13.set((byte) (value & 0xff));
      case 0xff14 -> nr14.set((byte) (value & 0x87));
      case 0xff16 -> nr21.set((byte) (value & 0xff));
      case 0xff17 -> nr22.set((byte) (value & 0xff));
      case 0xff18 -> nr23.set((byte) (value & 0xff));
      case 0xff19 -> nr24.set((byte) (value & 0x87));
      case 0xff1a -> nr30.set((byte) (value & 0x80));
      case 0xff1b -> nr31.set((byte) (value & 0xff));
      case 0xff1c -> nr32.set((byte) (value & 0x60));
      case 0xff1d -> nr33.set((byte) (value & 0xff));
      case 0xff1e -> nr34.set((byte) (value & 0x87));
      case 0xff20 -> nr41.set((byte) (value & 0x3f));
      case 0xff21 -> nr42.set((byte) (value & 0xff));
      case 0xff22 -> nr43.set((byte) (value & 0xff));
      case 0xff23 -> nr44.set((byte) (value & 0x80));
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
      case 0xff50 -> bank.set((byte) (value & 0x01));
      default -> {}
    }
  }
}
