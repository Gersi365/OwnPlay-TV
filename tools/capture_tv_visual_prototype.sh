#!/usr/bin/env bash
set -euo pipefail

: "${QA_APK:?QA_APK must point to the authorized tvDebug APK}"

mkdir -p prototype-screenshots
adb install -r "${QA_APK}"
adb shell wm size 1920x1080
adb shell wm density 240

capture() {
  local index="$1"
  local screen="$2"

  adb shell am force-stop app.ownplay.tv
  adb shell am start -W \
    -n app.ownplay.tv/app.ownplay.player.prototype.TvVisualPrototypeActivity \
    --es screen "${screen}"
  sleep 2
  adb exec-out screencap -p > "prototype-screenshots/${index}_${screen}.png"
}

capture 01 live_categories
capture 02 live_channels_preview
capture 03 live_full
capture 04 live_full_epg
capture 05 movies
capture 06 movie_details
capture 07 movie_playback
capture 08 series
capture 09 series_details
capture 10 episodes
capture 11 series_playback
capture 12 settings
capture 13 playlists
capture 14 live_management
capture 15 backup_restore
capture 16 about
