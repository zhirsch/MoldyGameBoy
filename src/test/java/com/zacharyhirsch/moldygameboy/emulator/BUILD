load("@contrib_rules_jvm//java:defs.bzl", "java_test_suite")
load("@rules_jvm_external//:defs.bzl", "artifact")

java_test_suite(
    name = "AllTests",
    srcs = glob(["*.java"]),
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
    runner = "junit5",
    runtime_deps = [
        artifact("org.junit.jupiter:junit-jupiter-engine"),
        artifact("org.junit.platform:junit-platform-launcher"),
        artifact("org.junit.platform:junit-platform-reporting"),
    ],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/registers",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/memory",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/timer",
        artifact("com.google.guava:guava"),
        artifact("com.google.truth:truth"),
        artifact("org.junit.jupiter:junit-jupiter-api"),
        artifact("org.junit.jupiter:junit-jupiter-params"),
    ],
)
