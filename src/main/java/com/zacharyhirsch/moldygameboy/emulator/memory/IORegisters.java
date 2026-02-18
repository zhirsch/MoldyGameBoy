package com.zacharyhirsch.moldygameboy.emulator.memory;

import com.zacharyhirsch.moldygameboy.emulator.arch.Register8;

public record IORegisters(
    /* ff01 */ Register8 sb,
    /* ff02 */ Register8 sc,
    /* ff04 */ Register8 div,
    /* ff05 */ Register8 tima,
    /* ff06 */ Register8 tma,
    /* ff07 */ Register8 tac,
    /* ff0f */ Register8 if_,
    /* ff11 */ Register8 nr11,
    /* ff12 */ Register8 nr12,
    /* ff24 */ Register8 nr50,
    /* ff25 */ Register8 nr51,
    /* ff26 */ Register8 nr52,
    /* ff40 */ Register8 lcdc,
    /* ff42 */ Register8 scy,
    /* ff43 */ Register8 scx,
    /* ff47 */ Register8 bgp,
    /* ff48 */ Register8 obp0,
    /* ff49 */ Register8 obp1,
    /* ff4d */ Register8 key1,
    /* ff4f */ Register8 vbk,
    /* ff50 */ Register8 bank,
    /* ff68 */ Register8 bcps,
    /* ff70 */ Register8 svbk,
    /* ffff */ Register8 ie) {

  public IORegisters() {
    this(
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8(),
        new Register8());
  }
}
