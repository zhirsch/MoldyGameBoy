load("@rules_java//java:java_binary.bzl", "java_binary")
load("@rules_jvm_external//:defs.bzl", "artifact")

config_setting(
    name = "macos",
    constraint_values = [
        "@platforms//os:osx",
    ],
)

java_binary(
    name = "MoldyGameBoy",
    srcs = ["MoldyGameBoy.java"],
    jvm_flags =
        select({
            ":macos": ["-XstartOnFirstThread"],
            "//conditions:default": [],
        }) + [
            "--enable-native-access=ALL-UNNAMED",
        ],
    resources = [
        "//src/main/resources:cgb.bin",
        "//src/main/resources:logback.xml",
    ],
    runtime_deps = [artifact("ch.qos.logback:logback-classic")],
    deps = [
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/arch",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/bus",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/cpu/registers",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/memory",
        "//src/main/java/com/zacharyhirsch/moldygameboy/emulator/timer",
        artifact("com.google.guava:guava"),
        artifact("org.slf4j:slf4j-api"),
    ],
)
