package com.zacharyhirsch.moldygameboy.emulator.arch;

public final class Bus {

  private final AddressBus addressBus;
  private final DataBus dataBus;
  private final Signal rd;
  private final Signal wr;

  public Bus() {
    addressBus = new AddressBus();
    dataBus = new DataBus();
    rd = new Signal();
    wr = new Signal();
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
}
