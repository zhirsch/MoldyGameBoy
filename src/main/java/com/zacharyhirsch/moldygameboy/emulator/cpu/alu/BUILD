load("@rules_java//java:java_library.bzl", "java_library")
load("@rules_jvm_external//:defs.bzl", "artifact", "java_plugin_artifact")

java_library(
    name = "alu",
    srcs = glob(["*.java"]),
    plugins = [
        java_plugin_artifact("com.google.auto.value:auto-value", "com.google.auto.value.processor.AutoBuilderProcessor"),
    ],
    visibility = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/instructions:__pkg__",
        "//src/test/java/com/zacharyhirsch/moldygameboy/emulator/cpu/alu:__pkg__",
    ],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/arch",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/registers",
        artifact("com.google.auto.value:auto-value-annotations"),
    ],
)
