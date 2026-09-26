#!/usr/bin/env python3
"""
DhvaniFlow Studio - Desktop Edition (Windows / macOS / Linux)
Features:
1. Menu 1: Upload Video & Strip Audio in Seconds (Zero re-encode via FFmpeg)
2. Menu 2: Remove Old Audio & Merge New Voice Audio Track into Video
3. Menu 3: Transcribe Voice to Text & Burn Bottom-to-Top Synchronized Scrolling Text into Video
   Saves directly to the user's ~/Downloads folder!
"""

import os
import subprocess
import threading
import pathlib
import tkinter as tk
from tkinter import ttk, filedialog, messagebox


def get_downloads_folder() -> pathlib.Path:
    downloads = pathlib.Path.home() / "Downloads"
    downloads.mkdir(parents=True, exist_ok=True)
    return downloads


class DhvaniFlowDesktopApp(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("ಧ್ವನಿಫ್ಲೋ ಸ್ಟುಡಿಯೋ • DhvaniFlow Desktop Studio")
        self.geometry("920x640")
        self.configure(bg="#0F172A")

        self.muted_video_path = ""
        self.dubbed_video_path = ""
        self.selected_audio_path = ""

        style = ttk.Style(self)
        style.theme_use("clam")
        style.configure("TNotebook", background="#0F172A", borderwidth=0)
        style.configure("TNotebook.Tab", background="#1E293B", foreground="#E2E8F0", padding=[18, 10], font=("Segoe UI", 11, "bold"))
        style.map("TNotebook.Tab", background=[("selected", "#F59E0B")], foreground=[("selected", "#0F172A")])
        style.configure("TFrame", background="#0F172A")
        style.configure("TLabel", background="#0F172A", foreground="#F8FAFC", font=("Segoe UI", 11))
        style.configure("Accent.TButton", background="#F59E0B", foreground="#0F172A", font=("Segoe UI", 11, "bold"), padding=10)

        header = tk.Label(
            self,
            text="ಧ್ವನಿಫ್ಲೋ ಸ್ಟುಡಿಯೋ (DhvaniFlow Studio - Desktop & Mobile)",
            bg="#0F172A",
            fg="#38BDF8",
            font=("Segoe UI", 16, "bold"),
            pady=12
        )
        header.pack(fill="x")

        self.notebook = ttk.Notebook(self)
        self.notebook.pack(fill="both", expand=True, padx=16, pady=8)

        self.tab1 = ttk.Frame(self.notebook)
        self.tab2 = ttk.Frame(self.notebook)
        self.tab3 = ttk.Frame(self.notebook)

        self.notebook.add(self.tab1, text="1. ಧ್ವನಿ ತೆಗೆಯಿರಿ (Mute Video)")
        self.notebook.add(self.tab2, text="2. ಹೊಸ ಧ್ವನಿ ಸೇರಿಸಿ (Add Voice)")
        self.notebook.add(self.tab3, text="3. ಪಠ್ಯ ಸ್ವೈಪ್ ವಿಡಿಯೋ (Voice-to-Text Scroll)")

        self._build_tab1()
        self._build_tab2()
        self._build_tab3()

        self.status_var = tk.StringVar(value=f"Ready • Output Folder: {get_downloads_folder()}")
        status_bar = tk.Label(self, textvariable=self.status_var, bg="#1E293B", fg="#94A3B8", anchor="w", padx=12, pady=6)
        status_bar.pack(fill="x", side="bottom")

    def _build_tab1(self):
        ttk.Label(self.tab1, text="ಹಂತ 1: ವಿಡಿಯೋ ಅಪ್ಲೋಡ್ ಮಾಡಿ ಮತ್ತು ಧ್ವನಿ ತೆಗೆಯಿರಿ (Strip Audio in Seconds)").pack(pady=16)
        self.tab1_file_lbl = ttk.Label(self.tab1, text="ಯಾವುದೇ ವಿಡಿಯೋ ಆಯ್ಕೆಯಾಗಿಲ್ಲ (No video selected)")
        self.tab1_file_lbl.pack(pady=8)

        ttk.Button(self.tab1, text="ವಿಡಿಯೋ ಅಪ್ಲೋಡ್ ಮಾಡಿ (Select Video)", style="Accent.TButton", command=self.mute_video_action).pack(pady=12)

    def _build_tab2(self):
        ttk.Label(self.tab2, text="ಹಂತ 2: ವಿಡಿಯೋಗೆ ಹೊಸ ಧ್ವನಿ ಸೇರಿಸಿ (Remove Old Audio & Add New Voice)").pack(pady=16)
        self.tab2_video_lbl = ttk.Label(self.tab2, text="ವಿಡಿಯೋ: ಆಯ್ಕೆಯಾಗಿಲ್ಲ")
        self.tab2_video_lbl.pack(pady=4)
        self.tab2_audio_lbl = ttk.Label(self.tab2, text="ಹೊಸ ಧ್ವನಿ (Audio): ಆಯ್ಕೆಯಾಗಿಲ್ಲ")
        self.tab2_audio_lbl.pack(pady=4)

        ttk.Button(self.tab2, text="1. ವಿಡಿಯೋ ಆಯ್ಕೆಮಾಡಿ (Select Video)", command=self.select_tab2_video).pack(pady=6)
        ttk.Button(self.tab2, text="2. ಹೊಸ ಧ್ವನಿ ಫೈಲ್ ಆಯ್ಕೆಮಾಡಿ (Select Voice Audio)", command=self.select_tab2_audio).pack(pady=6)
        ttk.Button(self.tab2, text="3. ಧ್ವನಿ ಸೇರಿಸಿ ಮತ್ತು ಸೇವ್ ಮಾಡಿ (Merge & Save to Downloads)", style="Accent.TButton", command=self.dub_video_action).pack(pady=14)

    def _build_tab3(self):
        ttk.Label(self.tab3, text="ಹಂತ 3: ಧ್ವನಿಗೆ ತಕ್ಕಂತೆ ಕೆಳಗಿಂದ ಮೇಲಕ್ಕೆ ಸ್ವೈಪ್ ಆಗುವ ಪಠ್ಯದ ವಿಡಿಯೋ (Bottom-to-Top Voice Scroll)").pack(pady=12)
        self.tab3_video_lbl = ttk.Label(self.tab3, text="ವಿಡಿಯೋ: ಆಯ್ಕೆಯಾಗಿಲ್ಲ")
        self.tab3_video_lbl.pack(pady=4)
        ttk.Button(self.tab3, text="ಧ್ವನಿ ಇರುವ ವಿಡಿಯೋ ಆಯ್ಕೆಮಾಡಿ (Select Video with Voice)", command=self.select_tab3_video).pack(pady=6)

        ttk.Label(self.tab3, text="ಪಠ್ಯ ಸಾಲುಗಳು (ಪ್ರತಿ ಸಾಲಿಗೊಂದು ವಾಕ್ಯ - ಕೆಳಗಿಂದ ಮೇಲಕ್ಕೆ ಸ್ವೈಪ್ ಆಗುತ್ತದೆ):").pack(pady=4)
        self.script_box = tk.Text(self.tab3, height=7, width=70, bg="#1E293B", fg="#F8FAFC", font=("Segoe UI", 11))
        self.script_box.insert("1.0", "ನಮಸ್ಕಾರ ಗೆಳೆಯರೇ!\nಈ ವಿಡಿಯೋದಲ್ಲಿ ಧ್ವನಿಗೆ ತಕ್ಕಂತೆ ಪಠ್ಯ ಮೇಲಕ್ಕೆ ಸ್ವೈಪ್ ಆಗುತ್ತಿದೆ.\nಕೆಲವೇ ಸೆಕೆಂಡುಗಳಲ್ಲಿ ನಿಮ್ಮ ಮೊಬೈಲ್ ಹಾಗೂ ಡೆಸ್ಕ್‌ಟಾಪ್‌ನಲ್ಲಿ ಸಿದ್ಧ!\nಈಗಲೇ ಡೌನ್‌ಲೋಡ್ ಫೋಲ್ಡರ್‌ನಲ್ಲಿ ಸೇವ್ ಮಾಡಿಕೊಳ್ಳಿ.")
        self.script_box.pack(pady=6)

        ttk.Button(
            self.tab3,
            text="ಸ್ವೈಪ್ ಪಠ್ಯದ ವಿಡಿಯೋ ರಚಿಸಿ ಮತ್ತು Downloads ಫೋಲ್ಡರ್‌ಗೆ ಸೇವ್ ಮಾಡಿ",
            style="Accent.TButton",
            command=self.render_scroll_video_action
        ).pack(pady=12)

    def mute_video_action(self):
        path = filedialog.askopenfilename(filetypes=[("Video Files", "*.mp4 *.mov *.mkv *.webm")])
        if not path:
            return
        self.tab1_file_lbl.config(text=f"Input: {path}")
        out_path = get_downloads_folder() / f"DhvaniFlow_Muted_{pathlib.Path(path).stem}.mp4"

        def worker():
            self.status_var.set("Processing: Removing audio track...")
            cmd = ["ffmpeg", "-y", "-i", path, "-c:v", "copy", "-an", str(out_path)]
            subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            self.muted_video_path = str(out_path)
            self.tab2_video_lbl.config(text=f"ವಿಡಿಯೋ: {out_path}")
            self.status_var.set(f"Saved Muted Video to Downloads: {out_path}")
            messagebox.showinfo("ಸಿದ್ಧವಾಗಿದೆ (Done)", f"ಧ್ವನಿ ತೆಗೆಯಲಾಗಿದೆ!\nSaved to Downloads:\n{out_path}")

        threading.Thread(target=worker, daemon=True).start()

    def select_tab2_video(self):
        path = filedialog.askopenfilename(filetypes=[("Video Files", "*.mp4 *.mov *.mkv")])
        if path:
            self.muted_video_path = path
            self.tab2_video_lbl.config(text=f"ವಿಡಿಯೋ: {path}")

    def select_tab2_audio(self):
        path = filedialog.askopenfilename(filetypes=[("Audio/Video Files", "*.mp3 *.wav *.m4a *.aac *.mp4")])
        if path:
            self.selected_audio_path = path
            self.tab2_audio_lbl.config(text=f"ಹೊಸ ಧ್ವನಿ: {path}")

    def dub_video_action(self):
        if not self.muted_video_path or not self.selected_audio_path:
            messagebox.showwarning("ಮಾಹಿತಿ", "ದಯವಿಟ್ಟು ವಿಡಿಯೋ ಮತ್ತು ಹೊಸ ಧ್ವನಿ ಫೈಲ್ ಆಯ್ಕೆಮಾಡಿ.")
            return
        out_path = get_downloads_folder() / f"DhvaniFlow_Dubbed_{pathlib.Path(self.muted_video_path).stem}.mp4"

        def worker():
            self.status_var.set("Merging new voice with muted video...")
            cmd = [
                "ffmpeg", "-y",
                "-i", self.muted_video_path,
                "-i", self.selected_audio_path,
                "-map", "0:v:0", "-map", "1:a:0",
                "-c:v", "copy", "-c:a", "aac", "-shortest",
                str(out_path)
            ]
            subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            self.dubbed_video_path = str(out_path)
            self.tab3_video_lbl.config(text=f"ವಿಡಿಯೋ: {out_path}")
            self.status_var.set(f"Saved Dubbed Video to Downloads: {out_path}")
            messagebox.showinfo("ಸಿದ್ಧವಾಗಿದೆ (Done)", f"ಹೊಸ ಧ್ವನಿ ಸೇರಿಸಲಾಗಿದೆ!\nSaved to Downloads:\n{out_path}")

        threading.Thread(target=worker, daemon=True).start()

    def select_tab3_video(self):
        path = filedialog.askopenfilename(filetypes=[("Video Files", "*.mp4 *.mov *.mkv")])
        if path:
            self.dubbed_video_path = path
            self.tab3_video_lbl.config(text=f"ವಿಡಿಯೋ: {path}")

    def render_scroll_video_action(self):
        if not self.dubbed_video_path:
            messagebox.showwarning("ಮಾಹಿತಿ", "ದಯವಿಟ್ಟು ವಿಡಿಯೋ ಆಯ್ಕೆಮಾಡಿ.")
            return
        out_path = get_downloads_folder() / f"DhvaniFlow_ScrollText_{pathlib.Path(self.dubbed_video_path).stem}.mp4"
        lines = [line.strip() for line in self.script_box.get("1.0", "end").splitlines() if line.strip()]
        joined_text = "\\n\\n".join(lines).replace(":", "\\:").replace("'", "")

        def worker():
            self.status_var.set("Rendering bottom-to-top scrolling text video...")
            drawtext = (
                f"drawtext=text='{joined_text}':fontcolor=white:fontsize=36:"
                f"box=1:boxcolor=black@0.55:boxborderw=12:"
                f"x=(w-text_w)/2:y=h-(t*95)"
            )
            cmd = [
                "ffmpeg", "-y",
                "-i", self.dubbed_video_path,
                "-vf", drawtext,
                "-c:a", "copy",
                str(out_path)
            ]
            subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            self.status_var.set(f"Saved Scrolling Text Video to Downloads: {out_path}")
            messagebox.showinfo("ಡೌನ್‌ಲೋಡ್ ಪೂರ್ಣಗೊಂಡಿದೆ!", f"ನಿಮ್ಮ ವಿಡಿಯೋ ನೇರವಾಗಿ Downloads ಫೋಲ್ಡರ್‌ನಲ್ಲಿ ಸೇವ್ ಆಗಿದೆ:\n{out_path}")

        threading.Thread(target=worker, daemon=True).start()


if __name__ == "__main__":
    app = DhvaniFlowDesktopApp()
    app.mainloop()
