sleep 5
adb -s 0222fdc60910aede shell screenrecord /sdcard/calabash.mp4 &
ADB_DEVICE_ARG=0222fdc60910aede calabash-android run ../build/apk/TeddyHyde-debug-unaligned.apk 
# osascript record.script &
