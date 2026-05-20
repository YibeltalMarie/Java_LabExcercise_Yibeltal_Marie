#!/bin/bash
# ============================================================
#  University RMI — Build & Run Helper
#  Usage:
#    ./run.sh compile          — compile all sources
#    ./run.sh server           — start the RMI server
#    ./run.sh client           — start the JavaFX client
# ============================================================

LIBS="java_libs/mariadb-java-client-3.3.0.jar"
SRC_DIR="src"
OUT_DIR="out"
JAVAFX_MODS="/usr/share/java/javafx-sdk-24/lib"

compile() {
    mkdir -p "$OUT_DIR"
    echo "Compiling..."
    javac \
        --module-path "$JAVAFX_MODS" \
        --add-modules javafx.controls \
        -cp "$LIBS" \
        -d "$OUT_DIR" \
        $(find "$SRC_DIR" -name "*.java")
    echo "Done."
}

server() {
    echo "Starting RMI Server..."
    java -cp "$OUT_DIR:$LIBS" university.server.ServerMain
}

client() {
    echo "Starting RMI Client..."
    java \
        --module-path "$JAVAFX_MODS" \
        --add-modules javafx.controls \
        -cp "$OUT_DIR:$LIBS" \
        university.client.UniversityClientApp
}

case "$1" in
    compile) compile ;;
    server)  server  ;;
    client)  client  ;;
    *)
        echo "Usage: ./run.sh [compile|server|client]"
        exit 1
        ;;
esac
