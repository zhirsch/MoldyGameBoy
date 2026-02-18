load("@rules_java//java:java_binary.bzl", "java_binary")
load("@rules_java//java:java_library.bzl", "java_library")
load("@rules_jvm_external//:defs.bzl", "artifact")

config_setting(
    name = "macos",
    constraint_values = [
        "@platforms//os:osx",
    ],
)

java_library(
    name = "emulator",
    srcs = glob(
        ["*.java"],
        exclude = ["MoldyGameBoyMain.java"],
    ),
    visibility = ["//src/test/java/com/zacharyhirsch/moldygameboy/emulator:__pkg__"],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/arch",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/bus",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/registers",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/memory",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/timer",
    ],
)

java_binary(
    name = "MoldyGameBoy",
    srcs = ["MoldyGameBoyMain.java"],
    jvm_flags =
        select({
            ":macos": ["-XstartOnFirstThread"],
            "//conditions:default": [],
        }) + [
            "--enable-native-access=ALL-UNNAMED",
        ],
    main_class = "com.zacharyhirsch.moldygameboy.emulator.MoldyGameBoyMain",
    resources = [
        "//src/main/resources:cgb.bin",
        "//src/main/resources:logback.xml",
    ],
    runtime_deps = [artifact("ch.qos.logback:logback-classic")],
    deps = [
        ":emulator",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/registers",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/memory",
        artifact("com.google.guava:guava"),
    ],
)
