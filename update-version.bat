@echo off
setlocal enabledelayedexpansion

if "%~1"=="" (
  echo Usage: update-version.bat ^<version^>
  echo Example: update-version.bat 1.1.0
  exit /b 1
)

set "NEW_VERSION=%~1"

echo.
echo ===============================
echo Updating version to %NEW_VERSION%
echo ===============================

if not exist "pom.xml" (
  echo pom.xml not found in current directory.
  exit /b 1
)

:: 1) Update VERSION file (only this file)
> "VERSION" echo %NEW_VERSION%
echo VERSION updated.

:: 2) Update ONLY <project><version> in pom.xml using XML parsing
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$xml = [xml](Get-Content 'pom.xml');" ^
  "$ns = New-Object System.Xml.XmlNamespaceManager($xml.NameTable);" ^
  "$ns.AddNamespace('m','http://maven.apache.org/POM/4.0.0');" ^
  "$node = $xml.SelectSingleNode('/m:project/m:version', $ns);" ^
  "if ($null -eq $node) { Write-Error 'Could not find <project><version> in pom.xml'; exit 2 }" ^
  "$node.InnerText = '%NEW_VERSION%';" ^
  "$xml.Save('pom.xml');"

if errorlevel 1 (
  echo Failed to update pom.xml
  exit /b 1
)

echo pom.xml updated.

git rev-parse --is-inside-work-tree >nul 2>&1
if errorlevel 1 (
  echo Not a git repository, skipping commit.
  echo Done.
  exit /b 0
)

git add VERSION pom.xml
git commit -m "VERSION: %NEW_VERSION%"

if errorlevel 1 (
  echo Git commit failed.
  exit /b 1
)

echo Version committed.
echo.
echo =====================================
echo Version update complete.
echo Push to trigger CI/CD:
echo git push
echo =====================================