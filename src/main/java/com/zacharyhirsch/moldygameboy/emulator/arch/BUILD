load("@rules_java//java:java_library.bzl", "java_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

java_library(
    name = "arch",
    srcs = glob(["*.java"]),
    visibility = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator:__pkg__",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/bus:__pkg__",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu:__pkg__",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/alu:__pkg__",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/idu:__pkg__",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/mem:__pkg__",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/registers:__pkg__",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/memory:__pkg__",
        "//src/test/java/com/zacharyhirsch/moldygameboy/emulator:__pkg__",
    ],
    deps = [
        artifact("com.google.guava:guava"),
    ],
)
