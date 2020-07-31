@echo off

color 2

cd C:\Users\Flo\Desktop\Java\Colored

set buildNum=unknown

for /F "tokens=1* delims==" %%A IN (dev/buildinfo.dat) DO (
    IF "%%A"=="name" set buildNum=%%B
)

echo Fetching build number...

SET BUILD_FOLDER=C:\Users\Flo\Desktop\Colored\builds\%buildNum%
SET WORLDS_FOLDER=C:/Users/Flo/Dropbox/Colored/Worlds/worlds
SET RES_FOLDER=C:/Users/Flo/Dropbox/Colored/Assets/res
SET DEV_FOLDER=C:/Users/Flo/Desktop/Java/Colored/dev
SET EXECUTABLE_FILE=C:\Users\Flo\Desktop\Java\Colored\desktop\build\libs\desktop-1.0.jar
SET DROPBOX_FOLDER=C:\Users\Flo\Dropbox\Colored\Programming

echo Creating build folder...

md "%BUILD_FOLDER%"

echo Building executable...

start /WAIT gradlew.bat desktop:dist

echo Adding executable...

copy /y NUL "%BUILD_FOLDER%/Colored.jar" >NUL

echo f | xcopy "%EXECUTABLE_FILE%" "%BUILD_FOLDER%/Colored.jar" /Y /Q 

echo Adding latest "worlds" files...

echo d | xcopy "%WORLDS_FOLDER%" "%BUILD_FOLDER%/worlds" /E /F /Y /Q

echo Adding latest "res" files...

echo d | xcopy "%RES_FOLDER%" "%BUILD_FOLDER%/res" /E /F /Y /Q

echo Adding latest "dev" files...

echo d | xcopy "%DEV_FOLDER%" "%BUILD_FOLDER%/dev" /E /F /Y /Q

echo Adding latest "launch.args" file...

echo f | xcopy "launch.args" "%BUILD_FOLDER%/launch.args" /Y /Q

echo Adding latest "licenses.args" file...

echo f | xcopy "licenses.txt" "%BUILD_FOLDER%/licenses.txt" /Y /Q

echo Adding latest "imagePackager.jar" file...

echo f | xcopy "imagePackager.jar" "%BUILD_FOLDER%/imagePackager.jar" /Y /Q

echo Creating archive file...

echo Adding packaged folders and meta files...

echo f | xcopy "dev.pkg" "%BUILD_FOLDER%/dev.pkg" /E /F /Y /Q
echo f | xcopy "worlds.pkg" "%BUILD_FOLDER%/worlds.pkg" /E /F /Y /Q
echo f | xcopy "res.pkg" "%BUILD_FOLDER%/res.pkg" /E /F /Y /Q
echo d | xcopy ".meta" "%BUILD_FOLDER%/.meta" /E /F /Y /Q

set prevPath=%CD%

cd %BUILD_FOLDER%

::"C:/Program Files/WinRAR/Rar.exe" a -r -idq Colored.zip Colored.jar .meta res.pkg dev.pkg worlds.pkg imagePackager.jar launch.args licenses.txt
"C:/Program Files/WinRAR/Rar.exe" a -r -idq Colored.zip Colored.jar res dev worlds imagePackager.jar launch.args licenses.txt

echo Uploading executable...

echo f | xcopy "Colored.jar" "%DROPBOX_FOLDER%/Colored.jar" /Y /Q

echo Uploading misc...

echo f | xcopy "launch.args" "%DROPBOX_FOLDER%/launch.args" /Y /Q
echo f | xcopy "licenses.txt" "%DROPBOX_FOLDER%/licenses.txt" /Y /Q
echo f | xcopy "imagePackager.jar" "%DROPBOX_FOLDER%/imagePackager.jar" /Y /Q
echo d | xcopy "dev" "%DROPBOX_FOLDER%/dev" /E /F /Y /Q

echo Uploading archive...

echo f | xcopy "Colored.zip" "%DROPBOX_FOLDER%/Colored.zip" /Y /Q

cd %prevPath%