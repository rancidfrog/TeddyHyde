if [[ "$1" != "" ]]; then
    INQRY_PASSWORD=$1 STORE_FILE=~/android/inqry_keystore gradle assembleRelease $@
else
    echo "build.sh <password>"
fi
