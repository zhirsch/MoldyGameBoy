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
    resources = ["//src/main/resources:logback.xml"],
    runtime_deps = [artifact("ch.qos.logback:logback-classic")],
    deps = [artifact("org.slf4j:slf4j-api")],
)
