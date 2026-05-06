load("@rules_java//java:java_library.bzl", "java_library")

java_library(
    name = "instructions",
    srcs = glob(["*.java"]),
    visibility = ["//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu:__pkg__"],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/arch",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/alu",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/registers",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/memory",
    ],
)
