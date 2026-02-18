load("@rules_java//java:java_library.bzl", "java_library")

java_library(
    name = "memory",
    srcs = glob(["*.java"]),
    visibility = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator:__pkg__",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu:__pkg__",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/instructions:__pkg__",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/timer:__pkg__",
        "//src/test/java/com/zacharyhirsch/moldygameboy/emulator:__pkg__",
    ],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/arch",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/bus",
    ],
)
