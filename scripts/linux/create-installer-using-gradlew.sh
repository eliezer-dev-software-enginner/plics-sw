#!/bin/bash

# Define que o script deve sair em caso de erro
set -e

# --- Application Configuration ---
APP_NAME="plics-sw"
APP_VERSION="1.0.0"
APP_VENDOR="Eliezer Dev Software Enginner"
APP_COPYRIGHT="Copyright 2026"
APP_DESCRIPTION="Sistema de gestão para pequenos negócios. Controle vendas, compras, estoque e financeiro."
APP_CATEGORY="Utility"
APP_MAIN_CLASS="my_app.Launcher"
JAR_FILE="${APP_NAME}-${APP_VERSION}.jar"


# Módulos JavaFX e JDK
FX_MODULES="javafx.controls,javafx.graphics"
JDK_MODULES="java.base,java.desktop,java.sql,jdk.unsupported,jdk.charsets"
JAVAFX_SDK_VERSION="25.0.1"
FX_SDK_PATH="java_fx_modules/linux-${JAVAFX_SDK_VERSION}/lib"
APP_ICON="src/main/resources/assets/app_ico.png"

# Tenta encontrar o diretório jmods do JDK
if [ -z "$JAVA_HOME" ]; then
    JAVA_PATH=$(readlink -f $(which java))
    JAVA_HOME=$(dirname $(dirname $JAVA_PATH))
fi
JMODS_PATH="$JAVA_HOME/jmods"

# Pastas de trabalho
BUILD_DIR="build"
DIST_DIR="dist"
RUNTIME_DIR="${BUILD_DIR}/runtime"
INPUT_DIR="${BUILD_DIR}/input_app"
IMAGE_DIR="${BUILD_DIR}/app-image"

echo "### 📦 JPackage Build Script para Linux (JavaFX/JRE Embutido) ###"
echo "JAVA_HOME: $JAVA_HOME"
echo "JMODS: $JMODS_PATH"

# --- 1. Requirements Check ---
echo "1. Checando requisitos..."
if [ ! -d "$JMODS_PATH" ]; then
    echo "🚨 ERRO: Diretório jmods não encontrado em $JMODS_PATH. Verifique seu JDK."
    exit 1
fi
if [ ! -d "$FX_SDK_PATH" ]; then
    echo "🚨 ERRO: JavaFX SDK não encontrado em $FX_SDK_PATH"
    exit 1
fi

# --- 2. Cleanup and Preparation ---
echo "2. Preparando diretórios..."
rm -rf "$DIST_DIR" "$INPUT_DIR" "$RUNTIME_DIR" "$IMAGE_DIR"
mkdir -p "$INPUT_DIR" "$DIST_DIR"

echo "   Copiando JAR principal e dependências..."
if [ -f "build/libs/${JAR_FILE}" ]; then
    cp "build/libs/${JAR_FILE}" "$INPUT_DIR/"
else
    # Busca qualquer jar que comece com o nome da app caso a versão mude
    JAR_FOUND=$(ls build/libs/${APP_NAME}*.jar | head -n 1)
    if [ -n "$JAR_FOUND" ]; then
        cp "$JAR_FOUND" "$INPUT_DIR/"
        JAR_FILE=$(basename "$JAR_FOUND")
    else
        echo "🚨 ERRO: JAR não encontrado em build/libs/"
        exit 1
    fi
fi

cp build/dependencies/*.jar "$INPUT_DIR/"

# --- 3. JLink: Create Runtime Image (JRE) ---
echo "3. Criando imagem de runtime customizada (JRE) com JLink..."
# Importante: Incluir os módulos do JDK E do JavaFX
jlink \
    --module-path "$JMODS_PATH:$FX_SDK_PATH" \
    --add-modules ${JDK_MODULES},${FX_MODULES} \
    --output "$RUNTIME_DIR" \
    --strip-debug \
    --compress=2 \
    --no-header-files \
    --no-man-pages

echo "   Copiando bibliotecas nativas do JavaFX para o JRE..."
cp "$FX_SDK_PATH"/*.so "$RUNTIME_DIR/lib/"
cp "$FX_SDK_PATH/javafx.properties" "$RUNTIME_DIR/lib/" || true

echo "   Runtime image criada em: ${RUNTIME_DIR}"
echo

# --- 4. JPackage: Create App Image (Para testes rápidos) ---
echo "4. Criando imagem da aplicação (app-image) para teste..."
jpackage \
    --type app-image \
    --input "$INPUT_DIR" \
    --dest "$IMAGE_DIR" \
    --main-jar "${JAR_FILE}" \
    --main-class "$APP_MAIN_CLASS" \
    --name "$APP_NAME" \
    --runtime-image "$RUNTIME_DIR" \
    --java-options "--enable-native-access=javafx.graphics" \
    --java-options "-Dprism.verbose=true"

echo "✅ App-image criada em: ${IMAGE_DIR}/${APP_NAME}"
echo "🚀 PARA TESTAR SEM INSTALAR: ${IMAGE_DIR}/${APP_NAME}/bin/${APP_NAME}"
echo

# --- 5. JPackage: Create Installer (.deb) ---
echo "5. Criando instalador Linux (.deb)..."
jpackage \
    --type deb \
    --runtime-image "$RUNTIME_DIR" \
    --input "$INPUT_DIR" \
    --dest "$DIST_DIR" \
    --main-jar "${JAR_FILE}" \
    --main-class "$APP_MAIN_CLASS" \
    --name "$APP_NAME" \
    --app-version "$APP_VERSION" \
    --vendor "$APP_VENDOR" \
    --copyright "$APP_COPYRIGHT" \
    --description "$APP_DESCRIPTION" \
    --icon "$APP_ICON" \
    --linux-menu-group "Utility;Utilities;Tool;Tools" \
    --linux-shortcut \
    --linux-app-category "$APP_CATEGORY" \
    --linux-deb-maintainer "eliezer@dev.com" \
    --java-options "--enable-native-access=javafx.graphics" \
    --java-options "-Dprism.verbose=true" \
    --java-options "--enable-native-access=ALL-UNNAMED" \
    --java-options "-Dprism.verbose=true" \
    --java-options "-Djavafx.embed.singleThread=true"

echo
echo "✅ Instalador criado com sucesso!"
echo "O arquivo do instalador está em: ${DIST_DIR}"
echo

# --- 6. Final Cleanup ---
# Comentado para permitir inspeção em caso de erro
# echo "6. Limpando diretórios de build temporários..."
# rm -rf "$INPUT_DIR" "$RUNTIME_DIR"
