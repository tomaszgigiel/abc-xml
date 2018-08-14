cd %~dp0/../..
rmdir /s /q target
start lein do clean, uberjar
pause
