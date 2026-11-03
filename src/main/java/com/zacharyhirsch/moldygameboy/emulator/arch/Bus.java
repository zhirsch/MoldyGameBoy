package com.zacharyhirsch.moldygameboy.emulator.arch;

public final class Bus {

  private final AddressBus addressBus;
  private final DataBus dataBus;
  private final Signal rd;
  private final Signal wr;
  private final Signal idu;

  public Bus() {
    addressBus = new AddressBus();
    dataBus = new DataBus();
    rd = new Signal();
    wr = new Signal();
    idu = new Signal();
  }

  public AddressBus addressBus() {
    return addressBus;
  }

  public DataBus dataBus() {
    return dataBus;
  }

  public Signal rd() {
    return rd;
  }

  public Signal wr() {
    return wr;
  }

  /**
   * Asserted while the 16-bit increment/decrement unit drives the address bus: when INC rr, DEC rr,
   * LD A,(HL+/-), POP or PUSH increments or decrements a register pair, its value is put on the
   * address bus even without a read or write, which matters for the DMG's OAM corruption bug.
   */
  public Signal idu() {
    return idu;
  }
}
