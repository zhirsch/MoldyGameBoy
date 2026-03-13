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
import com.zacharyhirsch.moldygameboy.emulator.arch.MemoryRange;
import com.zacharyhirsch.moldygameboy.emulator.cpu.registers.Registers;
import com.zacharyhirsch.moldygameboy.emulator.memory.IORegisters;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclaration;
import org.junit.jupiter.params.support.ParameterDeclarations;

@Execution(ExecutionMode.CONCURRENT)
final class SingleStepCpuTest {

  private static final Gson gson =
      new GsonBuilder()
          .registerTypeAdapter(Memory.class, new MemoryDeserializer())
          .registerTypeAdapter(Cycle.class, new CycleDeserializer())
          .create();

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.PARAMETER})
  public @interface TestDataGlob {
    String value();
  }

  static final class TestDataProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(
        ParameterDeclarations parameters, ExtensionContext context) {
      return parameters.getFirst().stream()
          .map(ParameterDeclaration::getAnnotatedElement)
          .filter(element -> element.isAnnotationPresent(TestDataGlob.class))
          .map(element -> element.getAnnotation(TestDataGlob.class))
          .flatMap(annotation -> loadTestData(annotation.value()))
          .map(Arguments::of);
    }
  }

  private static Stream<TestData> loadTestData(String glob) {
    return Streams.stream(glob(glob))
        .map(SingleStepCpuTest::readJson)
        .flatMap(json -> gson.fromJson(json, new TypeToken<List<TestData>>() {}).stream())
        .sorted(Comparator.comparing(TestData::name));
  }

  private static DirectoryStream<Path> glob(String value) {
    try {
      return Files.newDirectoryStream(Path.of("sm83/v1"), value);
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

  record TestData(
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

  @Test
  void testOne() {
    List<TestData> data = loadTestData("03.json").toList();
    doTest(data.get(0));
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void test0(@TestDataGlob("0?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void test1(@TestDataGlob("1?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void test2(@TestDataGlob("2?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void test3(@TestDataGlob("3?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void test4(@TestDataGlob("4?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void test5(@TestDataGlob("5?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void test6(@TestDataGlob("6?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void test7(@TestDataGlob("7?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void test8(@TestDataGlob("8?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void test9(@TestDataGlob("9?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testA(@TestDataGlob("a?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testB(@TestDataGlob("b?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testC(@TestDataGlob("c?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testD(@TestDataGlob("d?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testE(@TestDataGlob("e?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testF(@TestDataGlob("f?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testCb0(@TestDataGlob("cb 0?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testCb1(@TestDataGlob("cb 1?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testCb2(@TestDataGlob("cb 2?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testCb3(@TestDataGlob("cb 3?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testCb4(@TestDataGlob("cb 4?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testCb5(@TestDataGlob("cb 5?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testCb6(@TestDataGlob("cb 6?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testCb7(@TestDataGlob("cb 7?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testCb8(@TestDataGlob("cb 8?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testCb9(@TestDataGlob("cb 9?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testCbA(@TestDataGlob("cb a?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testCbB(@TestDataGlob("cb b?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testCbC(@TestDataGlob("cb c?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testCbD(@TestDataGlob("cb d?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testCbE(@TestDataGlob("cb e?.json") TestData data) {
    doTest(data);
  }

  @ParameterizedTest
  @ArgumentsSource(TestDataProvider.class)
  void testCbF(@TestDataGlob("cb f?.json") TestData data) {
    doTest(data);
  }

  private static void doTest(TestData data) {
    ByteBuffer ram = ByteBuffer.allocate(1 << 16);

    Registers registers = new Registers();
    registers.pc().set(data.initial().pc());
    registers.sp().set(data.initial().sp());
    registers.a().set(data.initial().a());
    registers.f().set(data.initial().f());
    registers.b().set(data.initial().b());
    registers.c().set(data.initial().c());
    registers.d().set(data.initial().d());
    registers.e().set(data.initial().e());
    registers.h().set(data.initial().h());
    registers.l().set(data.initial().l());
    for (Memory mem : data.initial().ram()) {
      ram.put(Short.toUnsignedInt(mem.address()), mem.value());
    }

    IORegisters ioRegisters = new IORegisters();

    List<Cycle> cycles = new ArrayList<>(Arrays.asList(data.cycles));
    cycles.add(null);
    MemoryRange memory =
        new MemoryRange() {
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
    MoldyGameBoy gb = new MoldyGameBoy(memory, registers, ioRegisters);
    for (long i = 0; i < (long) (data.cycles().length + 1); i++) {
      gb.tick();
    }
    registers.pc().getAndDecrement();

    assertThat(registers.pc().get()).isEqualTo(data.final_().pc());
    assertThat(registers.sp().get()).isEqualTo(data.final_().sp());
    assertThat(registers.a().get()).isEqualTo(data.final_().a());
    assertThat(registers.f().get()).isEqualTo(data.final_().f());
    assertThat(registers.b().get()).isEqualTo(data.final_().b());
    assertThat(registers.c().get()).isEqualTo(data.final_().c());
    assertThat(registers.d().get()).isEqualTo(data.final_().d());
    assertThat(registers.e().get()).isEqualTo(data.final_().e());
    assertThat(registers.h().get()).isEqualTo(data.final_().h());
    assertThat(registers.l().get()).isEqualTo(data.final_().l());
    for (Memory mem : data.final_().ram()) {
      assertThat(ram.get(Short.toUnsignedInt(mem.address()))).isEqualTo(mem.value());
    }
  }
}
