load("@rules_java//java:java_import.bzl", "java_import")

java_import(
    name = "sdl3",
    jars = select({
        "//:macos": ["macos-arm64/sdl3.jar"],
        "//:windows": ["win32-x64/sdl3.jar"],
    }),
    srcjar = select({
        "//:macos": "macos-arm64/sdl3-sources.jar",
        "//:windows": "win32-x64/sdl3-sources.jar",
    }),
    visibility = ["//src/main/java/com/zacharyhirsch/moldygameboy/emulator/io/sdl:__pkg__"],
)
