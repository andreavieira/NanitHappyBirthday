#!/usr/bin/env python3
"""Build and run instrumentation tests without uninstalling the app or clearing its data."""

import argparse
import os
from pathlib import Path
import subprocess
import sys

PROJECT = Path(__file__).resolve().parents[1]
PACKAGE = "com.andreavieira.nanithappybirthday"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", help="ADB device serial (required when multiple devices are connected)")
    parser.add_argument("--test-class", help="Optional fully qualified instrumentation test class")
    args = parser.parse_args()
    sdk = Path(os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT") or Path.home() / "Library/Android/sdk")
    adb = str(sdk / "platform-tools/adb")
    devices = subprocess.check_output([adb, "devices"], text=True)
    available = [line.split()[0] for line in devices.splitlines()[1:] if line.endswith("\tdevice")]
    serial = args.serial
    if serial is None:
        if len(available) != 1:
            parser.error("Connect exactly one emulator/device or specify --serial")
        serial = available[0]
    if serial not in available:
        parser.error("Selected device is not connected and authorized")

    subprocess.run([str(PROJECT / "gradlew"), "--offline", "assembleDebug", "assembleDebugAndroidTest"], cwd=PROJECT, check=True)
    command = [adb, "-s", serial]
    # Replacement installs preserve private profile metadata and photos; never uninstall or clear data here.
    for apk in ("app/build/outputs/apk/debug/app-debug.apk", "app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"):
        subprocess.run(command + ["install", "-r", str(PROJECT / apk)], check=True)
    runner = command + ["shell", "am", "instrument", "-w"]
    if args.test_class:
        runner += ["-e", "class", args.test_class]
    result = subprocess.run(runner + [PACKAGE + ".test/androidx.test.runner.AndroidJUnitRunner"], text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    print(result.stdout, end="")
    # ADB can return zero even when the instrumentation reports failed tests.
    if result.returncode or "FAILURES!!!" in result.stdout or "INSTRUMENTATION_FAILED" in result.stdout or "OK (" not in result.stdout:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
