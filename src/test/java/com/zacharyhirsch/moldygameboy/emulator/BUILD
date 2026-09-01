load("@contrib_rules_jvm//java:defs.bzl", "JUNIT5_DEPS", "JUNIT5_RUNTIME_DEPS", "java_junit5_test")
load("@rules_jvm_external//:defs.bzl", "artifact")

java_junit5_test(
    name = "SingleStepCpuTest",
    srcs = ["SingleStepCpuTest.java"],
    data = ["//:sm83-v1-files"],
    jvm_flags = ["-Djava.security.manager=disallow"],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/arch",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/registers",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/memory",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/timer",
        artifact("com.google.code.gson:gson"),
        artifact("com.google.guava:guava"),
        artifact("com.google.truth:truth"),
        artifact("org.junit.jupiter:junit-jupiter-api"),
        artifact("org.junit.jupiter:junit-jupiter-params"),
        artifact("org.mockito:mockito-core"),
        artifact("org.mockito:mockito-junit-jupiter"),
    ] + JUNIT5_DEPS,
)

java_junit5_test(
    name = "BlarggCpuInstrTest",
    srcs = ["BlarggCpuInstrTest.java"],
    jvm_flags = ["-Djava.security.manager=disallow"],
    resources = [
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
    jvm_flags = ["-Djava.security.manager=disallow"],
    resources = [
        "//:mooneye-test-suite/build/acceptance/bits/reg_f.gb",
        "//:mooneye-test-suite/build/acceptance/instr/daa.gb",
    ],
    deps = [
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
