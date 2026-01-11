#!/bin/bash

# Define que o script deve sair em caso de erro
set -e

# --- Application Configuration ---
APP_NAME="MyApp"
APP_VERSION="1.2"
APP_VENDOR="YOUR NAME OR BUSINESS NAME"
APP_COPYRIGHT="Copyright 2025"
APP_DESCRIPTION="YOUR APP DESCRIPTION HERE"
APP_CATEGORY="Utility"
APP_MAIN_CLASS="my_app.App"
JAR_FILE="my_app-${APP_VERSION}-jar-with-dependencies.jar"
# Módulos essenciais + extras. Removi 'java.sql' se não for usado.
FX_MODULES="javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.web"
JAVAFX_SDK_VERSION="25.0.1"
FX_SDK_PATH="java_fx_modules/linux-${JAVAFX_SDK_VERSION}/lib"
APP_ICON="src/main/resources/logo_256.png" # Certifique-se de que este caminho está correto

# Pastas de trabalho
BUILD_DIR="build"
DIST_DIR="dist"
RUNTIME_DIR="${BUILD_DIR}/runtime"
INPUT_DIR="${BUILD_DIR}/input_app"

echo "### 📦 JPackage Build Script para Linux (JavaFX/JRE Embutido) ###"
echo

# --- 1. Requirements Check (Simplificado) ---
echo "1. Checando 'jpackage' e 'java'..."
if ! command -v jpackage &> /dev/null || ! command -v java &> /dev/null; then
    echo "🚨 ERRO: 'jpackage' ou 'java' não encontrados. Verifique a instalação do seu JDK e o PATH."
    exit 1
fi

# Não é estritamente necessário checar a versão ou JAVA_HOME se 'jpackage' estiver no PATH.
echo "Requisitos básicos atendidos."
echo

# --- 2. Cleanup and Preparation (REVISADO) ---
echo "2. Limpando diretórios temporários e de saída..."
rm -rf "$BUILD_DIR" "$DIST_DIR"

# Criação das pastas
mkdir -p "$INPUT_DIR" "$DIST_DIR"

# Cópia do JAR (Principal e Dependências)
echo "   Copiando JAR principal para o diretório de entrada..."
cp "target/${JAR_FILE}" "$INPUT_DIR"/

# NOVO: Copia as bibliotecas nativas do JavaFX SDK diretamente para o diretório de entrada
echo "   Copiando bibliotecas nativas do JavaFX para a entrada do JPackage..."
# Copiamos a pasta inteira para a estrutura de lib esperada
cp -r "$FX_SDK_PATH" build/input_app/lib

# --- 3. JLink: Create Runtime Image (JRE) ---
echo "3. Criando imagem de runtime customizada (JRE) com JLink..."
jlink \
    --module-path "$FX_SDK_PATH" \
    --add-modules $FX_MODULES \
    --output "$RUNTIME_DIR" \
    --strip-debug \
    --compress=2 \
    --no-header-files \
    --no-man-pages

echo "   Runtime image criada em: ${RUNTIME_DIR}"
echo

# --- 4. JPackage: Create Installer (Single Step) ---
echo "4. Criando instalador Linux (.deb) com o JRE customizado..."
jpackage \
    --input "$INPUT_DIR" \
    --dest "$DIST_DIR" \
    --main-jar "${JAR_FILE}" \
    --main-class "$APP_MAIN_CLASS" \
    --name "$APP_NAME" \
    --app-version "$APP_VERSION" \
    --vendor "$APP_VENDOR" \
    --copyright "$APP_COPYRIGHT" \
    --description "$APP_DESCRIPTION" \
    --type deb \
    --runtime-image "$RUNTIME_DIR" \
    --icon "$APP_ICON" \
    --linux-shortcut \
    --linux-app-category "$APP_CATEGORY" \
    --java-options "--enable-native-access=javafx.graphics" \
    --java-options "-Dprism.verbose=true" \
    --java-options "-Djava.library.path=\$APPDIR/lib"
#    --java-options "-Djava.library.path=\$APPDIR/lib/runtime/lib"

echo
echo "✅ Instalador criado com sucesso!"
echo "O arquivo do instalador está em: ${DIST_DIR}"
echo

# --- 5. Final Cleanup ---
echo "5. Limpando diretórios de build temporários..."
rm -rf "$BUILD_DIR"