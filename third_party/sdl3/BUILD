config_setting(
    name = "macos",
    constraint_values = [
        "@platforms//os:osx",
    ],
)

config_setting(
    name = "windows",
    constraint_values = [
        "@platforms//os:windows",
    ],
)

java_import(
    name = "sdl3",
    jars = select({
        ":macos": ["macos-arm64/sdl3.jar"],
        ":windows": ["win32-x64/sdl3.jar"],
    }),
    srcjar = select({
        ":macos": "macos-arm64/sdl3-sources.jar",
        ":windows": "win32-x64/sdl3-sources.jar",
    }),
)
