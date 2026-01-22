package com.zacharyhirsch.moldygameboy.emulator.cpu.alu;

import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8Input;
import com.zacharyhirsch.moldygameboy.emulator.arch.UInt8Output;

public interface AluOperation {

  <T extends UInt8Input & UInt8Output> void execute(T flags);
}
