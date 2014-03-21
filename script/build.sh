echo "Make sure to run under gradle 1.11 (something like: PATH=~/bin/gradle-1.11/bin:\$PATH ./script/build.sh password)"

if [[ "$1" != "" ]]; then
    INQRY_PASSWORD=$1 STORE_FILE=~/android/inqry_keystore gradle assembleRelease 
else
    echo "build.sh <password>"
fi
