@echo off
if "%~1" == "" goto show_usage
if "%~2" == "" goto show_usage

:check
if exist "build\tmp\compile" goto compile
mkdir build\tmp\compile

:compile
java -jar build\libs\MiniJCompiler.jar < %1 > build\tmp\compile\file.asm
if %errorlevel% neq 0 exit /B %errorlevel%

:assemble
bin\nasm_windows.exe -f win64 -o build\tmp\compile\file.obj -dWINDOWS_X64 build\tmp\compile\file.asm
if %errorlevel% neq 0 exit /B %errorlevel%

:link
bin\golink.exe /ni /console /fo %2 /entry:_start kernel32.dll user32.dll build\tmp\compile\file.obj build\objects\runtime\iosyscalls.obj build\objects\runtime\built-in.obj
if %errorlevel% neq 0 exit /B %errorlevel%
goto end

:show_usage
echo "usage: mjc_windows.bat <input> <output>"

:end
