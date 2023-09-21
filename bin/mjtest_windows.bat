@echo off

if not exist build\tmp\compile mkdir build\tmp\compile

set FILE=build\tmp\compile\test
set FILE_MJ=%FILE%.mj
set FILE_BIN=%FILE%.exe
set FILE_IN=%FILE%.in

if exist %FILE_MJ% del %FILE_MJ%
if exist %FILE_BIN% del %FILE_BIN%
if exist %FILE_IN% del %FILE_IN%

rem the same path as above, but with slashes suitable for awk...
set FILE2=build/tmp/compile/test
set FILE_MJ2=%FILE2%.mj
set FILE_IN2=%FILE2%.in
bin\gawk "{ if ($1 == \"----INPUT----\") { input = 1 }} { if (input != 1) print $0 > \"%FILE_MJ2%\"} { if (input == 1 && $0 != \"----INPUT----\" ) print $0 > \"%FILE_IN2%\"}"
if %errorlevel% neq 0 exit /B %errorlevel%

call bin\mjc_windows.bat %FILE_MJ% %FILE_BIN%
if %errorlevel% neq 0 exit /B %errorlevel%

if not exist %FILE_IN% %FILE_BIN%
if exist %FILE_IN% %FILE_BIN% < %FILE_IN%
if %errorlevel% neq 0 exit /B %errorlevel%
