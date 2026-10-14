#!/usr/bin/env python3
"""Build the debug app and record its navigation instrumentation test on an emulator."""

import os
from pathlib import Path
import subprocess
import time

PROJECT = Path(__file__).resolve().parents[1]
SDK = Path(os.environ.get("ANDROID_HOME", Path.home() / "Library/Android/sdk"))
ADB = str(SDK / "platform-tools/adb")
SERIAL = os.environ.get("ANDROID_SERIAL", "emulator-5554")
REMOTE_VIDEO = "/sdcard/NanitNavigationDemo.mp4"
OUTPUT = PROJECT / "outputs/NavigationDemo.mp4"


def adb(*arguments, **kwargs):
    return subprocess.run([ADB, "-s", SERIAL, *arguments], check=True, **kwargs)


def main():
    subprocess.run([str(PROJECT / "gradlew"), "assembleDebug", "assembleDebugAndroidTest"], cwd=PROJECT, check=True)
    adb("install", "-r", str(PROJECT / "app/build/outputs/apk/debug/app-debug.apk"))
    adb("install", "-r", str(PROJECT / "app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"))
    existing = subprocess.run([ADB, "-s", SERIAL, "shell", "pidof", "screenrecord"], capture_output=True, text=True)
    if existing.stdout.strip():
        raise RuntimeError("Another emulator recording is already running; stop it before recording this demo.")
    # Open the app before recording so unrelated launcher/sign-in screens are excluded.
    adb("shell", "am", "start", "-W", "-n", "com.andreavieira.nanithappybirthday/.MainActivity")
    time.sleep(1)
    OUTPUT.parent.mkdir(exist_ok=True)
    recorder = subprocess.Popen([ADB, "-s", SERIAL, "shell", "screenrecord", "--time-limit", "45", "--size", "720x1600", REMOTE_VIDEO])
    try:
        time.sleep(1)
        result = adb("shell", "am", "instrument", "-w", "-e", "recordDemo", "true", "-e", "class",
                     "com.andreavieira.nanithappybirthday.BirthdayNavigationTest",
                     "com.andreavieira.nanithappybirthday.test/androidx.test.runner.AndroidJUnitRunner",
                     capture_output=True, text=True)
        print(result.stdout)
        if "OK (1 test)" not in result.stdout:
            raise RuntimeError("Navigation verification failed; recording is not approved review evidence.")
    finally:
        pid = subprocess.run([ADB, "-s", SERIAL, "shell", "pidof", "screenrecord"], capture_output=True, text=True).stdout.strip()
        if pid:
            adb("shell", "kill", "-2", pid)
        recorder.wait(timeout=15)
        adb("pull", REMOTE_VIDEO, str(OUTPUT))
        adb("shell", "rm", REMOTE_VIDEO)
    adb("pull", "/sdcard/Android/data/com.andreavieira.nanithappybirthday/files/BirthdayNavigation.png",
        str(PROJECT / "outputs/BirthdayNavigation.png"))
    print(f"Recording: {OUTPUT}")


if __name__ == "__main__":
    main()
