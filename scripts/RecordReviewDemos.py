#!/usr/bin/env python3
"""Record review flows in a separate demo APK, without touching the normal app profile.

Requires a connected emulator and --photo pointing to an image approved for review.
Controlled age/date demonstrations are labelled test fixtures, not device-clock changes.
"""
import argparse
import os
from pathlib import Path
import shutil
import subprocess

PROJECT = Path(__file__).resolve().parents[1]
DEMO_PACKAGE = "com.andreavieira.nanithappybirthday.demo"
TEST_CLASS = "com.andreavieira.nanithappybirthday.ReviewDemoTest"
MODES = ["persistence", "reload", "themes", "photos", "photoReload", "share", "ages", "edges"]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", default="emulator-5554")
    parser.add_argument("--photo", required=True, type=Path)
    parser.add_argument("--mode", choices=MODES, help="Repeat one flow using an already built demo APK")
    args = parser.parse_args()
    if not args.photo.is_file():
        parser.error("Photo file does not exist")
    sdk = Path(os.environ.get("ANDROID_HOME", Path.home() / "Library/Android/sdk"))
    adb = [str(sdk / "platform-tools/adb"), "-s", args.serial]
    build = PROJECT / "work/ReviewDemoBuild"
    output = PROJECT / "outputs/ReviewDemos"
    output.mkdir(parents=True, exist_ok=True)

    def run(*command, **kwargs):
        return subprocess.run(command, check=True, **kwargs)

    if not args.mode:
        # Copy only source/configuration; never mutate the real app's application ID or data.
        if build.exists():
            shutil.rmtree(build)
        shutil.copytree(PROJECT, build, ignore=shutil.ignore_patterns(".git", ".gradle", ".kotlin", ".idea", "build", "work", "outputs", ".agents", ".codex"))
        gradle = build / "app/build.gradle.kts"
        gradle.write_text(gradle.read_text().replace('applicationId = "com.andreavieira.nanithappybirthday"', f'applicationId = "{DEMO_PACKAGE}"'))
        fixture = build / "app/src/androidTest/java/com/andreavieira/nanithappybirthday/ReviewDemoTest.kt"
        shutil.copyfile(PROJECT / "scripts/review/ReviewDemoTest.kt", fixture)
        run(str(build / "gradlew"), "--offline", "assembleDebug", "assembleDebugAndroidTest", cwd=build)
        for apk in ("app/build/outputs/apk/debug/app-debug.apk", "app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"):
            run(*adb, "install", "-r", str(build / apk))
    remote = f"/sdcard/Android/data/{DEMO_PACKAGE}/files"
    run(*adb, "shell", "mkdir", "-p", remote)
    run(*adb, "push", str(args.photo), remote + "/ReviewBaby.jpeg")
    names = ["PersistenceSetup", "PersistenceReload", "RandomThemes", "PhotoSelection", "PhotoReload", "ShareFlow", "AgeOptions", "DateEdgeCases"]
    # Earlier single-file recordings must not overwrite locally joined clips during later pulls.
    run(*adb, "shell", "rm", "-f", *[remote + "/" + name + ".mp4" for name in names])
    for mode in [args.mode] if args.mode else MODES:
        # A real process stop between sessions demonstrates durable storage, not only ViewModel state.
        run(*adb, "shell", "am", "force-stop", DEMO_PACKAGE)
        result = run(*adb, "shell", "am", "instrument", "-w", "-e", "demo", mode, "-e", "class", TEST_CLASS,
                     DEMO_PACKAGE + ".test/androidx.test.runner.AndroidJUnitRunner", capture_output=True, text=True)
        (PROJECT / f"work/ReviewDemo{mode}.log").write_text(result.stdout + result.stderr)
        print(mode, result.stdout[-350:], flush=True)
        run(*adb, "pull", remote + "/.", str(output))
        if "OK (1 test)" not in result.stdout:
            raise RuntimeError(f"Demo {mode} failed; its recording is not approved evidence")
        name = {"persistence": "PersistenceSetup", "reload": "PersistenceReload", "themes": "RandomThemes", "photos": "PhotoSelection", "photoReload": "PhotoReload", "share": "ShareFlow", "ages": "AgeOptions", "edges": "DateEdgeCases"}[mode]
        parts = sorted(output.glob(name + "-*.mp4"))
        if parts:
            run("xcrun", "swift", str(PROJECT / "scripts/review/VideoTools.swift"), "join", str(output / (name + ".mp4")), *map(str, parts))
    if not args.mode:
        swift = PROJECT / "scripts/review/VideoTools.swift"
        for name, parts in {"Persistence": ["PersistenceSetup", "PersistenceReload"], "PhotoFlow": ["PhotoSelection", "PhotoReload"]}.items():
            run("xcrun", "swift", str(swift), "join", str(output / (name + ".mp4")), *[str(output / (p + ".mp4")) for p in parts])
        videos = [output / (name + ".mp4") for name in ["Persistence", "RandomThemes", "PhotoFlow", "ShareFlow", "AgeOptions", "DateEdgeCases"]]
        run("xcrun", "swift", str(swift), "inspect", str(PROJECT / "work/ReviewVideoFrames"), *map(str, videos))
    print(f"Review recordings: {output}")


if __name__ == "__main__":
    main()
