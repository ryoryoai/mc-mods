# mc-mods

Minecraft Fabric 1.20.1 MOD モノレポ。Claude Code + skills で直接MODを開発する。

## プロジェクト構成

```
mc-mods/
├── dragonrider/    ← エンダードラゴン騎乗MOD（Mixin）
├── guns/           ← 銃MOD（マグナム、リボルバー、グレネードランチャー）
├── thunderaxe/     ← 雷の斧MOD
├── shared-run/     ← 共有実行環境（.gitignore）
└── run.sh
```

新しいMODは `mc-mods/MOD名/` に作成する。

## テクスチャ生成ルール

- 必ず Python (PIL/Pillow) で作成する
- nanobanana / AI 画像生成は使わない
- 16x16 RGBA PNG、透明背景、ドット打ちで直接描画

## Item Namespace 規約

- 武器: `gun:` namespace（magnum, revolver, grenade_launcher）
- 弾薬: `bullet:` namespace、`gun種類_弾種類` 命名
  - 例: `bullet:magnum_bullet`, `bullet:grenade_launcher_freeze_grenade`
- 乗り物系: MOD名をそのまま namespace にする（例: `dragonrider:`）

---

# ビルド・起動トラブル知見集

ここに蓄積された知見は、同じ失敗を繰り返さないための永続ルール。

## Java / Gradle

- **Java 17 必須**: `JAVA_HOME=/opt/homebrew/opt/openjdk@17`（macOS Homebrew の場合）
- **Gradle 8.x 必須**: fabric-loom 1.5.4 は Gradle 9.x 非対応
- ビルドコマンド: `JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew build`

## Fabric Mixin のハマりポイント

- **Mixin 作成前に `./gradlew genSources` でメソッド名を必ず検証**
  - Minecraft のメソッド名は難読化されていて、想像で書くと失敗する
- **"No refMap loaded" エラー** = ターゲットメソッドが存在しない
  - 原因: メソッド名の間違い。genSources で正しい名前を確認する
- **EnderDragonEntity 固有の注意点**:
  - `tick()` ではなく `tickMovement()` を使う
  - `canAddPassenger()` は Entity から継承、オーバーライドなし → `startRiding(entity, true)` で強制搭乗
  - モデルが通常エンティティと180度逆向き → `setYaw(yaw + 180)` が必要

## 起動失敗パターンと対処法

### 1. MODが読み込まれない（アイテムが「不明なアイテム」になる）

**原因**: ランチャーの mods フォルダに古いJARが残っている
- `./gradlew build` で作ったJARは `build/libs/` に出力される
- **ランチャーで遊ぶ場合**: `~/Library/Application Support/minecraft/mods/` にコピーが必要
- **gradlew runClient の場合**: `shared-run/mods/` にコピー

**対処**: ビルド後に必ず正しいフォルダにJARをコピーする
```bash
cp MOD名/build/libs/MOD名-1.0.0.jar ~/Library/Application\ Support/minecraft/mods/
```

### 2. Mixin クラッシュ（起動直後に落ちる）

**原因**: Mixin のターゲットメソッドが存在しないか、引数が違う
- **対処**: `./gradlew genSources` → IDE で正しいメソッドシグネチャを確認
- **Inject の at**: `@At("HEAD")` か `@At("TAIL")` で安全に開始

### 3. `./gradlew runClient` で他MODが読み込まれない

**原因**: gradlew runClient は自プロジェクトのMODしかロードしない
- **対処**: build.gradle に追加:
  ```gradle
  modRuntimeOnly files("../他MOD/build/libs/他MOD-1.0.0.jar")
  ```
- または `shared-run/mods/` にJARを置いて `loom.runConfigs` で共有する

### 4. テクスチャが表示されない（紫黒の市松模様）

**原因**: models/item JSON のパスとテクスチャファイルの配置が一致していない
- **確認**: `assets/NAMESPACE/models/item/ITEM.json` の `textures.layer0` が
  `NAMESPACE:item/ITEM` になっているか
- **確認**: `assets/NAMESPACE/textures/item/ITEM.png` が存在するか

### 5. レシピが機能しない

**原因**: `data/NAMESPACE/recipes/ITEM.json` の `type` や `key` のミス
- **確認**: `item` フィールドは `"NAMESPACE:ITEM_ID"` の形式か
- **確認**: クラフティングテーブルのパターンと key が一致しているか

## マルチMOD開発

- `loom.runConfigs.configureEach { runDir = "../shared-run" }` で共有ワールド
- shared-run/ はワールドデータ・ログが大きいため .gitignore 済み

## Key Patterns

- BaseGunItem 抽象クラスで全銃を統一（guns MOD）
- DragonWhistleItem: 右クリックでドラゴン召喚＋自動騎乗
- EnderDragonMixin: tickMovement Inject でライダー操縦を実装

## Skills

- `/create-weapon` - 武器MOD生成（剣、斧、弓、銃）
- 新しいMOD種別を作ったら、そのパターンを skill として追加する
