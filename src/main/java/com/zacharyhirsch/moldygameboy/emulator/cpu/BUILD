load("@rules_java//java:java_library.bzl", "java_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

java_library(
    name = "cpu",
    srcs = glob(["*.java"]),
    visibility = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator:__pkg__",
        "//src/test/java/com/zacharyhirsch/moldygameboy/emulator:__pkg__",
    ],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/arch",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/bus",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/alu",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/registers",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/memory",
        artifact("org.jspecify:jspecify"),
        artifact("com.google.guava:guava"),
    ],
)
