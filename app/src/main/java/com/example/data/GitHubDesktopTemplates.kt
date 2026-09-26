package com.example.data

object GitHubDesktopTemplates {

    val GITHUB_WORKFLOW_YAML = """
name: DhvaniFlow Mobile (Android APK) & Desktop App CI/CD

on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

permissions:
  contents: write

jobs:
  build-android-apk:
    name: Build Android Mobile App (APK)
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '9.3.1'

      - name: Ensure .env exists from .env.example
        shell: bash
        run: |
          if [ ! -f .env ]; then
            cp .env.example .env
          fi

      - name: Generate Debug Keystore if missing
        shell: bash
        run: |
          if [ ! -f debug.keystore ]; then
            keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
          fi

      - name: Build Debug APK
        shell: bash
        run: gradle assembleDebug --stacktrace

      - name: Upload Android APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: DhvaniFlow-Android-Mobile-APK
          path: app/build/outputs/apk/debug/*.apk

  build-desktop-app:
    name: Build Desktop App (${'$'}{{ matrix.os }})
    runs-on: ${'$'}{{ matrix.os }}
    strategy:
      fail-fast: false
      matrix:
        os: [ubuntu-latest, windows-latest, macos-latest]
    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Set up Python 3.11
        uses: actions/setup-python@v5
        with:
          python-version: '3.11'

      - name: Install Desktop Dependencies & PyInstaller
        shell: bash
        run: |
          python -m pip install --upgrade pip
          python -m pip install pyinstaller SpeechRecognition pydub
          if [ -f desktop/requirements.txt ]; then
            python -m pip install -r desktop/requirements.txt
          fi

      - name: Package Standalone Desktop Executable
        shell: bash
        run: |
          python -m PyInstaller --noconfirm --onefile --windowed --name DhvaniFlowDesktop desktop/dhvaniflow_desktop.py

      - name: Upload Desktop Executable Artifact
        uses: actions/upload-artifact@v4
        with:
          name: DhvaniFlow-Desktop-${'$'}{{ matrix.os }}
          path: dist/*
    """.trimIndent()

    val DESKTOP_PYTHON_APP = """
#!/usr/bin/env python3
# DhvaniFlow Studio - Desktop App (Windows / macOS / Linux)
# 1. Menu 1: Strip Audio from Video in Seconds
# 2. Menu 2: Remove Old Audio & Merge New Voice into Video
# 3. Menu 3: Voice-Synced Bottom-to-Top Scrolling Text Video -> Saves to ~/Downloads

import subprocess, threading, pathlib
import tkinter as tk
from tkinter import ttk, filedialog, messagebox

def get_downloads_folder() -> pathlib.Path:
    d = pathlib.Path.home() / "Downloads"
    d.mkdir(parents=True, exist_ok=True)
    return d

class DhvaniFlowDesktop(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("ಧ್ವನಿಫ್ಲೋ ಸ್ಟುಡಿಯೋ • DhvaniFlow Desktop")
        self.geometry("900x620")
        self.configure(bg="#0F172A")
        self.video_path = ""
        self.audio_path = ""

        nb = ttk.Notebook(self)
        nb.pack(fill="both", expand=True, padx=16, pady=16)

        t1, t2, t3 = ttk.Frame(nb), ttk.Frame(nb), ttk.Frame(nb)
        nb.add(t1, text="1. ಧ್ವನಿ ತೆಗೆಯಿರಿ (Mute)")
        nb.add(t2, text="2. ಹೊಸ ಧ್ವನಿ ಸೇರಿಸಿ (Add Voice)")
        nb.add(t3, text="3. ಪಠ್ಯ ಸ್ವೈಪ್ ವಿಡಿಯೋ (Scroll Text)")

        ttk.Button(t1, text="ವಿಡಿಯೋ ಅಪ್ಲೋಡ್ ಮಾಡಿ ಮತ್ತು ಧ್ವನಿ ತೆಗೆಯಿರಿ", command=self.mute_video).pack(pady=40)
        ttk.Button(t2, text="ಹೊಸ ಧ್ವನಿ ಆಯ್ಕೆಮಾಡಿ ಮತ್ತು ಸೇರಿಸಿ", command=self.dub_video).pack(pady=40)
        ttk.Button(t3, text="ಕೆಳಗಿಂದ ಮೇಲಕ್ಕೆ ಸ್ವೈಪ್ ಪಠ್ಯದ ವಿಡಿಯೋ Downloads ಗೆ ಸೇವ್ ಮಾಡಿ", command=self.scroll_video).pack(pady=40)

    def mute_video(self):
        p = filedialog.askopenfilename(filetypes=[("Video", "*.mp4 *.mov *.mkv")])
        if not p: return
        out = get_downloads_folder() / f"DhvaniFlow_Muted_{pathlib.Path(p).stem}.mp4"
        subprocess.run(["ffmpeg", "-y", "-i", p, "-c:v", "copy", "-an", str(out)])
        self.video_path = str(out)
        messagebox.showinfo("Done", f"Saved to Downloads:\n{out}")

    def dub_video(self):
        if not self.video_path:
            self.video_path = filedialog.askopenfilename(filetypes=[("Video", "*.mp4")])
        a = filedialog.askopenfilename(filetypes=[("Audio", "*.m4a *.mp3 *.wav *.mp4")])
        if not self.video_path or not a: return
        out = get_downloads_folder() / f"DhvaniFlow_Dubbed_{pathlib.Path(self.video_path).stem}.mp4"
        subprocess.run(["ffmpeg", "-y", "-i", self.video_path, "-i", a, "-map", "0:v:0", "-map", "1:a:0", "-c:v", "copy", "-c:a", "aac", "-shortest", str(out)])
        self.video_path = str(out)
        messagebox.showinfo("Done", f"Saved to Downloads:\n{out}")

    def scroll_video(self):
        if not self.video_path:
            self.video_path = filedialog.askopenfilename(filetypes=[("Video", "*.mp4")])
        if not self.video_path: return
        out = get_downloads_folder() / f"DhvaniFlow_ScrollText_{pathlib.Path(self.video_path).stem}.mp4"
        vf = "drawtext=text='ನಮಸ್ಕಾರ ಗೆಳೆಯರೇ!\\nಧ್ವನಿಗೆ ತಕ್ಕಂತೆ ಪಠ್ಯ ಮೇಲಕ್ಕೆ ಸ್ವೈಪ್ ಆಗುತ್ತಿದೆ':fontcolor=white:fontsize=36:x=(w-text_w)/2:y=h-(t*95)"
        subprocess.run(["ffmpeg", "-y", "-i", self.video_path, "-vf", vf, "-c:a", "copy", str(out)])
        messagebox.showinfo("Saved to Downloads", f"Saved directly to Downloads:\n{out}")

if __name__ == "__main__":
    DhvaniFlowDesktop().mainloop()
    """.trimIndent()
}
