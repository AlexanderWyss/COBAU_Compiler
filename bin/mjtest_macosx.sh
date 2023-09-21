#!/bin/bash

mkdir -p build/tmp/compile

TMP_PATH="build/tmp/compile/test"
FILE_MJ="${TMP_PATH}.mj"
FILE_BIN="${TMP_PATH}"
FILE_IN="${TMP_PATH}.in"

rm -f ${FILE_MJ} ${FILE_BIN} ${FILE_IN}
awk '
{ if ($1 == "----INPUT----") { input = 1 } }
{ if (input != 1) print $0 > "build/tmp/compile/test.mj"}
{ if (input == 1 && $1 != "----INPUT----" ) print $0 > "build/tmp/compile/test.in"}
' <&0

touch ${FILE_IN}
bin/mjc_macosx.sh ${FILE_MJ} ${FILE_BIN} && ${FILE_BIN} < ${FILE_IN}