#!/bin/bash
set -e

rm -rf ./cmake/build-w64
mkdir -p ./cmake/build-w64/sysroot
mkdir -p ./cmake/build-w64/sysroot/lib
cd ./cmake/build-w64

export PKG_CONFIG_PATH="$(readlink -f .)/sysroot/lib/pkgconfig"
export PKG_CONFIG_PATH_CUSTOM="$(readlink -f .)/sysroot/lib/pkgconfig"

TOOLCHAIN_FILE="$(readlink -f ../windows-x64-toolchain-mingw-x86_64.cmake)"
CROSS_FILE="$(readlink -f ../windows-x64-mingw-x86_64-cross_file.txt)"

cmake ../.. -G Ninja \
       	-DCMAKE_BUILD_TYPE=Release \
       	-DCMAKE_TOOLCHAIN_FILE=$TOOLCHAIN_FILE  \
       	-DMESON_CROSS_FILE=$CROSS_FILE \
        -DWEBVIEW_USE_COMPAT_MINGW=ON

cmake --build . -j $(nproc)

cp /usr/lib/gcc/x86_64-w64-mingw32/13-posix/libstdc++-6.dll ./sysroot/bin
cp /usr/lib/gcc/x86_64-w64-mingw32/13-posix/libgcc_s_seh-1.dll ./sysroot/bin
cp /usr/lib/gcc/x86_64-w64-mingw32/13-posix/libgomp-1.dll ./sysroot/bin
cp /usr/x86_64-w64-mingw32/lib/libwinpthread-1.dll ./sysroot/bin

for lib in sysroot/bin/*dll; do
  x86_64-w64-mingw32-strip $lib
done
