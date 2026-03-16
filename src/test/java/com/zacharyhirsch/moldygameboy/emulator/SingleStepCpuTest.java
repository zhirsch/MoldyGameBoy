package com.zacharyhirsch.moldygameboy.emulator;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.zacharyhirsch.moldygameboy.emulator.cpu.Cpu;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

final class SingleStepCpuTest {

  static final class TestInputProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(
        ParameterDeclarations parameters, ExtensionContext context) {
      Gson gson =
          new GsonBuilder()
              .registerTypeAdapter(Memory.class, new MemoryDeserializer())
              .registerTypeAdapter(Cycle.class, new CycleDeserializer())
              .create();
      return Streams.stream(glob())
          .map(SingleStepCpuTest::readJson)
          .map(json -> gson.fromJson(json, new TypeToken<List<TestInput>>() {}))
          .sorted(Comparator.comparing(list -> list.getFirst().name()))
          .map(Arguments::of);
    }
  }

  private static DirectoryStream<Path> glob() {
    try {
      return Files.newDirectoryStream(Path.of("sm83/v1"), "*.json");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static String readJson(Path path) {
    try {
      return Files.readString(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  record Memory(short address, byte value) {}

  record State(
      short pc,
      short sp,
      byte a,
      byte b,
      byte c,
      byte d,
      byte e,
      byte f,
      byte h,
      byte l,
      byte ime,
      byte ie,
      Memory[] ram) {}

  public record Cycle(short address, byte value, String mode) {}

  record TestInput(
      String name, State initial, @SerializedName("final") State final_, Cycle[] cycles) {

    @Override
    public String toString() {
      return name();
    }
  }

  static final class MemoryDeserializer implements JsonDeserializer<Memory> {

    @Override
    public Memory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      JsonArray arr = json.getAsJsonArray();
      return new Memory(arr.get(0).getAsShort(), arr.get(1).getAsByte());
    }
  }

  static final class CycleDeserializer implements JsonDeserializer<Cycle> {

    @Override
    public Cycle deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      JsonArray arr = json.getAsJsonArray();
      return new Cycle(arr.get(0).getAsShort(), arr.get(1).getAsByte(), arr.get(2).getAsString());
    }
  }

  @ParameterizedTest
  @ArgumentsSource(TestInputProvider.class)
  void testInstructions(List<TestInput> inputs) {
    for (TestInput input : inputs) {
      ArrayList<Cycle> cycles = new ArrayList<>(Arrays.asList(input.cycles()));
      cycles.add(null);
      doTest(input.initial(), input.final_(), cycles);
    }
  }

  private static void doTest(State initial, State final_, final List<Cycle> cycles) {
    ByteBuffer ram = ByteBuffer.allocate(1 << 16);

    Registers registers = new Registers();
    registers.pc().set(initial.pc());
    registers.sp().set(initial.sp());
    registers.a().set(initial.a());
    registers.f().set(initial.f());
    registers.b().set(initial.b());
    registers.c().set(initial.c());
    registers.d().set(initial.d());
    registers.e().set(initial.e());
    registers.h().set(initial.h());
    registers.l().set(initial.l());
    for (Memory mem : initial.ram()) {
      ram.put(Short.toUnsignedInt(mem.address()), mem.value());
    }

    com.zacharyhirsch.moldygameboy.emulator.arch.Memory memory =
        new com.zacharyhirsch.moldygameboy.emulator.arch.Memory() {
          @Override
          public byte read(short address) {
            byte data = ram.get(Short.toUnsignedInt(address));
            Cycle cycle = cycles.removeFirst();
            if (cycle != null) {
              assertThat("r-m").isEqualTo(cycle.mode());
              assertThat(address).isEqualTo(cycle.address());
              assertThat(data).isEqualTo(cycle.value());
            }
            return data;
          }

          @Override
          public void write(short address, byte data) {
            ram.put(Short.toUnsignedInt(address), data);
            Cycle cycle = cycles.removeFirst();
            if (cycle != null) {
              assertThat("-wm").isEqualTo(cycle.mode());
              assertThat(address).isEqualTo(cycle.address());
              assertThat(data).isEqualTo(cycle.value());
            }
          }

          @Override
          public void none(short address) {
            cycles.removeFirst();
          }
        };

    Cpu cpu = new Cpu(registers, memory);
    while (!cycles.isEmpty()) {
      cpu.tick();
    }
    registers.pc().getAndDecrement();

    assertThat(registers.pc().get()).isEqualTo(final_.pc());
    assertThat(registers.sp().get()).isEqualTo(final_.sp());
    assertThat(registers.a().get()).isEqualTo(final_.a());
    assertThat(registers.f().get()).isEqualTo(final_.f());
    assertThat(registers.b().get()).isEqualTo(final_.b());
    assertThat(registers.c().get()).isEqualTo(final_.c());
    assertThat(registers.d().get()).isEqualTo(final_.d());
    assertThat(registers.e().get()).isEqualTo(final_.e());
    assertThat(registers.h().get()).isEqualTo(final_.h());
    assertThat(registers.l().get()).isEqualTo(final_.l());
    for (Memory mem : final_.ram()) {
      assertThat(ram.get(Short.toUnsignedInt(mem.address()))).isEqualTo(mem.value());
    }
  }
}
