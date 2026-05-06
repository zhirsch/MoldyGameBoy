load("@rules_java//java:java_library.bzl", "java_library")

java_library(
    name = "registers",
    srcs = glob(["*.java"]),
    visibility = ["//src/main/java/com/zacharyhirsch/moldygameboy/emulator/memory:__pkg__"],
    deps = ["//src/main/java/com/zacharyhirsch/moldygameboy/emulator/arch"],
)
