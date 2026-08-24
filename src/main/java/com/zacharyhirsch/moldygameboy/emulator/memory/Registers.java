package com.zacharyhirsch.moldygameboy.emulator.memory;

import com.zacharyhirsch.moldygameboy.emulator.arch.IORegister;
import com.zacharyhirsch.moldygameboy.emulator.memory.registers.*;

public record Registers(
    /* 01 */ Sb sb,
    /* 02 */ Sc sc,
    /* 04 */ Div div,
    /* 05 */ Tima tima,
    /* 06 */ Tma tma,
    /* 07 */ Tac tac,
    /* 0f */ If if_,
    /* 11 */ Nr11 nr11,
    /* 12 */ Nr12 nr12,
    /* 13 */ Nr13 nr13,
    /* 14 */ Nr14 nr14,
    /* 24 */ Nr50 nr50,
    /* 25 */ Nr51 nr51,
    /* 26 */ Nr52 nr52,
    /* 40 */ Lcdc lcdc,
    /* 41 */ Stat stat,
    /* 42 */ Scy scy,
    /* 43 */ Scx scx,
    /* 44 */ Ly ly,
    /* 45 */ Lyc lyc,
    /* 47 */ Bgp bgp,
    /* 48 */ Obp0 obp0,
    /* 49 */ Obp1 obp1,
    /* 4a */ Wy wy,
    /* 4b */ Wx wx,
    /* 50 */ Bank bank,
    /* 70 */ Svbk svbk) {

  Registers() {
    this(
        new Sb(),
        new Sc(),
        new Div(),
        new Tima(),
        new Tma(),
        new Tac(),
        new If(),
        new Nr11(),
        new Nr12(),
        new Nr13(),
        new Nr14(),
        new Nr50(),
        new Nr51(),
        new Nr52(),
        new Lcdc(),
        new Stat(),
        new Scy(),
        new Scx(),
        new Ly(),
        new Lyc(),
        new Bgp(),
        new Obp0(),
        new Obp1(),
        new Wy(),
        new Wx(),
        new Bank(),
        new Svbk());
  }

  IORegister at(int index) {
    return switch (index) {
      case 0x01 -> sb();
      case 0x02 -> sc();
      case 0x04 -> div();
      case 0x05 -> tima();
      case 0x06 -> tma();
      case 0x07 -> tac();
      case 0x0f -> if_();
      case 0x11 -> nr11();
      case 0x12 -> nr12();
      case 0x13 -> nr13();
      case 0x14 -> nr14();
      case 0x24 -> nr50();
      case 0x25 -> nr51();
      case 0x26 -> nr52();
      case 0x40 -> lcdc();
      case 0x41 -> stat();
      case 0x42 -> scy();
      case 0x43 -> scx();
      case 0x44 -> ly();
      case 0x45 -> lyc();
      case 0x47 -> bgp();
      case 0x48 -> obp0();
      case 0x49 -> obp1();
      case 0x4a -> wy();
      case 0x4b -> wx();
      case 0x50 -> bank();
      case 0x70 -> svbk();
      default -> throw new IllegalArgumentException("ff%02x".formatted(index));
    };
  }
}
