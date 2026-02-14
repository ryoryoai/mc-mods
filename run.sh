#!/bin/bash
# MC MOD 開発用ランチャー
# 使い方: ~/mc-mods/run.sh
# ワールドは ~/mc-mods/shared-run/saves/ に保存（再起動しても消えない）

export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"

SHARED_RUN=~/mc-mods/shared-run

# 1. 前の開発用 Minecraft を全部止める
echo "前のMinecraftを停止中..."
pkill -f "gradlew.*runClient" 2>/dev/null
pkill -f "net.fabricmc.devlaunchinjector" 2>/dev/null
sleep 1

# 2. 全MODをビルド
echo "MODをビルド中..."
FAILED=0
for mod_dir in ~/mc-mods/*/; do
    if [ -f "$mod_dir/build.gradle" ]; then
        MOD_NAME=$(basename "$mod_dir")
        echo "  ビルド: $MOD_NAME"
        (cd "$mod_dir" && ./gradlew build -q 2>&1) || { echo "  ビルド失敗: $MOD_NAME"; FAILED=1; }
    fi
done

if [ "$FAILED" = "1" ]; then
    echo "ビルドエラーがあります。修正してから再実行してください。"
    exit 1
fi

# 3. ランチャー用 mods フォルダも更新
MODS_DIR="$HOME/Library/Application Support/minecraft/mods"
echo "modsフォルダ更新中..."
for mod_dir in ~/mc-mods/*/; do
    if [ -d "$mod_dir/build/libs" ]; then
        JAR=$(ls "$mod_dir/build/libs/"*-*.jar 2>/dev/null | grep -v sources | head -1)
        if [ -n "$JAR" ]; then
            cp "$JAR" "$MODS_DIR/"
            echo "  ランチャー: $(basename "$JAR")"
        fi
    fi
done

# 4. 共有 run/mods/ に全MOD JARを配置
mkdir -p "$SHARED_RUN/mods"
rm -f "$SHARED_RUN/mods"/*.jar
FIRST_MOD_DIR=""
for mod_dir in ~/mc-mods/*/; do
    if [ -f "$mod_dir/build.gradle" ]; then
        [ -z "$FIRST_MOD_DIR" ] && FIRST_MOD_DIR="$mod_dir"
        JAR=$(ls "$mod_dir/build/libs/"*-*.jar 2>/dev/null | grep -v sources | head -1)
        if [ -n "$JAR" ]; then
            cp "$JAR" "$SHARED_RUN/mods/"
            echo "  追加: $(basename "$JAR")"
        fi
    fi
done

FIRST_MOD_NAME=$(basename "$FIRST_MOD_DIR")

echo ""
echo "Minecraft 起動中... (ベース: $FIRST_MOD_NAME)"
echo "ワールド保存先: $SHARED_RUN/saves/"
echo "読み込みMOD:"
ls "$SHARED_RUN/mods/"*.jar 2>/dev/null | while read f; do echo "  - $(basename "$f")"; done
echo ""
echo "閉じるには Minecraft のウィンドウを閉じてください"
echo ""
cd "$FIRST_MOD_DIR" && ./gradlew runClient
