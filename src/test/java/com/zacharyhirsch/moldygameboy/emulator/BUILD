load("@contrib_rules_jvm//java:defs.bzl", "JUNIT5_DEPS", "JUNIT5_RUNTIME_DEPS", "java_junit5_test")
load("@rules_java//java:java_library.bzl", "java_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

java_library(
    name = "emulator",
    testonly = True,
    srcs = ["RegistersSubject.java"],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/registers",
        artifact("com.google.truth:truth"),
    ],
)

java_junit5_test(
    name = "BlarggCpuInstrTest",
    srcs = ["BlarggCpuInstrTest.java"],
    jvm_flags =
        select({
            "//:macos": ["-XstartOnFirstThread"],
            "//conditions:default": [],
        }) + [
            "--enable-native-access=ALL-UNNAMED",
        ],
    resources = [
        "//src/main/resources:dmg.bin",
        "//src/main/resources:logback.xml",
        "//src/test/resources/cpu_instrs:cpu_instrs.gb",
        "//src/test/resources/cpu_instrs:individual/01-special.gb",
        "//src/test/resources/cpu_instrs:individual/02-interrupts.gb",
        "//src/test/resources/cpu_instrs:individual/03-op sp,hl.gb",
        "//src/test/resources/cpu_instrs:individual/04-op r,imm.gb",
        "//src/test/resources/cpu_instrs:individual/05-op rp.gb",
        "//src/test/resources/cpu_instrs:individual/06-ld r,r.gb",
        "//src/test/resources/cpu_instrs:individual/07-jr,jp,call,ret,rst.gb",
        "//src/test/resources/cpu_instrs:individual/08-misc instrs.gb",
        "//src/test/resources/cpu_instrs:individual/09-op r,r.gb",
        "//src/test/resources/cpu_instrs:individual/10-bit ops.gb",
        "//src/test/resources/cpu_instrs:individual/11-op a,(hl).gb",
    ],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/arch",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/registers",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io:factory",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/memory",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/timer",
        artifact("com.google.guava:guava"),
        artifact("com.google.truth:truth"),
        artifact("org.junit.jupiter:junit-jupiter-api"),
        artifact("org.junit.jupiter:junit-jupiter-params"),
    ] + JUNIT5_DEPS,
)

java_junit5_test(
    name = "MooneyeTest",
    srcs = ["MooneyeTest.java"],
    jvm_flags =
        select({
            "//:macos": ["-XstartOnFirstThread"],
            "//conditions:default": [],
        }) + [
            "--enable-native-access=ALL-UNNAMED",
        ],
    resources = [
        "//:mooneye-test-suite",
        "//src/main/resources:dmg.bin",
        "//src/main/resources:logback.xml",
    ],
    runtime_deps = [artifact("ch.qos.logback:logback-classic")],
    deps = [
        ":emulator",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/arch",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/registers",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io:factory",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/memory",
        artifact("com.google.guava:guava"),
        artifact("com.google.truth:truth"),
        artifact("org.junit.jupiter:junit-jupiter-api"),
        artifact("org.junit.jupiter:junit-jupiter-params"),
    ] + JUNIT5_DEPS,
)
