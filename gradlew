#!/bin/sh
# Gradle wrapper - download if needed
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
GRADLE_VERSION="8.4"
GRADLE_HOME="$SCRIPT_DIR/.gradle/wrapper/dists/gradle-$GRADLE_VERSION-bin"

if [ -f "$GRADLE_HOME/gradle-$GRADLE_VERSION/bin/gradle" ]; then
    exec "$GRADLE_HOME/gradle-$GRADLE_VERSION/bin/gradle" "$@"
else
    echo "Downloading Gradle $GRADLE_VERSION..."
    mkdir -p "$GRADLE_HOME"
    curl -sL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o /tmp/gradle.zip
    python3 -c "import zipfile; zipfile.ZipFile('/tmp/gradle.zip').extractall('$GRADLE_HOME')"
    exec "$GRADLE_HOME/gradle-$GRADLE_VERSION/bin/gradle" "$@"
fi
