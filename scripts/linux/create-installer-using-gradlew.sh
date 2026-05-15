#!/bin/bash
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
FX_MODULES="javafx.controls,javafx.graphics,javafx.base"
JDK_MODULES="java.base,java.desktop,java.sql,jdk.unsupported,jdk.charsets"
JAVAFX_SDK_VERSION="25.0.1"
FX_SDK_PATH="java_fx_modules/linux-${JAVAFX_SDK_VERSION}/lib"
APP_ICON="src/main/resources/logo_256x256.png"

# Pastas
BUILD_DIR="build"
DIST_DIR="dist"
RUNTIME_DIR="${BUILD_DIR}/runtime"
INPUT_DIR="${BUILD_DIR}/input_app"
IMAGE_DIR="${BUILD_DIR}/app-image"
RESOURCE_DIR="${BUILD_DIR}/jpackage-resources"

echo "### 📦 JPackage Build Script para Linux (JavaFX/JRE + Ícone Automático) ###"

# 1. Requirements
echo "1. Checando requisitos..."
if [ ! -d "$JAVA_HOME/jmods" ]; then echo "🚨 ERRO: jmods não encontrado"; exit 1; fi
if [ ! -d "$FX_SDK_PATH" ]; then echo "🚨 ERRO: JavaFX SDK não encontrado"; exit 1; fi
if [ ! -f "$APP_ICON" ]; then echo "🚨 ERRO: Ícone 256x256 não encontrado em $APP_ICON"; exit 1; fi

# 2. Cleanup
echo "2. Preparando diretórios..."
rm -rf "$DIST_DIR" "$INPUT_DIR" "$RUNTIME_DIR" "$IMAGE_DIR" "$RESOURCE_DIR"
mkdir -p "$INPUT_DIR" "$DIST_DIR" "$RESOURCE_DIR"

# Copia JARs
echo "   Copiando JAR principal e dependências..."
if [ -f "build/libs/${JAR_FILE}" ]; then
    cp "build/libs/${JAR_FILE}" "$INPUT_DIR/"
else
    JAR_FOUND=$(ls build/libs/${APP_NAME}*.jar | head -n 1)
    cp "$JAR_FOUND" "$INPUT_DIR/"
    JAR_FILE=$(basename "$JAR_FOUND")
fi
cp build/dependencies/*.jar "$INPUT_DIR/"

# 3. JLink
echo "3. Criando JRE customizado..."
jlink \
    --module-path "$JAVA_HOME/jmods:$FX_SDK_PATH" \
    --add-modules ${JDK_MODULES},${FX_MODULES} \
    --output "$RUNTIME_DIR" \
    --strip-debug \
    --compress=2 \
    --no-header-files \
    --no-man-pages

cp "$FX_SDK_PATH"/*.so "$RUNTIME_DIR/lib/" 2>/dev/null || true
cp "$FX_SDK_PATH/javafx.properties" "$RUNTIME_DIR/lib/" || true

# 4. Resource-dir (ícone + .desktop automático)
echo "   Criando resource-dir para ícone e .desktop..."
cp "$APP_ICON" "$RESOURCE_DIR/${APP_NAME}.png"

cat << EOF > "$RESOURCE_DIR/${APP_NAME}.desktop"
[Desktop Entry]
Version=1.0
Type=Application
Name=Plics SW
GenericName=Sistema de Gestão para Pequenos Negócios
Comment=${APP_DESCRIPTION}
Exec=/opt/${APP_NAME}/bin/${APP_NAME}
Icon=${APP_NAME}
Terminal=false
Categories=Utility;Office;Finance;Business;
StartupWMClass=${APP_NAME}
StartupNotify=true
EOF

# 5. App-image
echo "4. Criando app-image..."
jpackage \
    --type app-image \
    --input "$INPUT_DIR" \
    --dest "$IMAGE_DIR" \
    --main-jar "${JAR_FILE}" \
    --main-class "$APP_MAIN_CLASS" \
    --name "$APP_NAME" \
    --runtime-image "$RUNTIME_DIR" \
    --icon "$APP_ICON" \
    --resource-dir "$RESOURCE_DIR" \
    --java-options "--enable-native-access=javafx.graphics" \
    --java-options "-Dprism.verbose=true"

# 6. .deb (com resource-dir → ícone e .desktop instalados automaticamente!)
echo "5. Criando instalador .deb..."
jpackage \
    --type deb \
    --app-image "${IMAGE_DIR}/${APP_NAME}" \
    --dest "$DIST_DIR" \
    --app-version "$APP_VERSION" \
    --vendor "$APP_VENDOR" \
    --copyright "$APP_COPYRIGHT" \
    --description "$APP_DESCRIPTION" \
    --linux-menu-group "Utility;Utilities;Tool;Tools" \
    --linux-shortcut \
    --linux-app-category "$APP_CATEGORY" \
    --linux-deb-maintainer "eliezer@dev.com" \
    --resource-dir "$RESOURCE_DIR"

echo "✅ Instalador criado em: ${DIST_DIR}"
echo "📦 Ícone final gerado:"
ls -l "${IMAGE_DIR}/${APP_NAME}/lib/${APP_NAME}.png" 2>/dev/null || echo "não encontrado (verifique)"

echo "🚀 Teste sem instalar: ${IMAGE_DIR}/${APP_NAME}/bin/${APP_NAME}"