# Changelog / 変更履歴

## [1.0.0-beta.2] - 2026-11-16

### 日本語
- **Jade 通常ツールチップ表示の確実化**:
  - `details_only` オプションによる Shift 待ち分岐を完全に撤去し、既存の Jade 設定ファイルが残っている環境でも Shift なしの通常ツールチップに開封・未開封状態が即座に表示されるよう修正しました。

### English
- **Always Show Jade Status on Standard Tooltip**:
  - Removed the `details_only` check so loot container status is always shown on the standard (no-Shift) Jade tooltip regardless of previously saved Jade plugin configs.

## [1.0.0-beta.1] - 2026-11-16

### 日本語
- **Beta リリース**:
  - Jade ツールチップの開封状態表示を、詳細表示時だけでなくコンテナに視線を合わせた際の通常ツールチップへデフォルトで表示するよう変更（`details_only` のデフォルトを `false` に変更）。

### English
- **Beta Release**:
  - Changed Jade loot status tooltip to display on the standard look-at tooltip by default instead of requiring details mode (`details_only` now defaults to `false`).

## [1.0.0-alpha.4] - 2026-11-16

### 日本語
- **視認性強化**:
  - グリントの輝度倍率（`BRIGHT_GLINT_TRANSPARENCY`: `1.35x`、`AMBIENT_GLINT_TRANSPARENCY`: `0.90x`）およびデフォルトの重ね掛け回数（`glintPasses`: `2` → `3`）を引き上げ、全体の輝きを約 50% 強化しました。
- **Jade オプション連携**:
  - Jade 導入時に、詳細表示ツールチップ（Shift 等）でコンテナの「未開封 / 開封済み」状態を確認できるプラグインを追加しました（Jade プラグイン設定から表示の有無および詳細表示時のみ表示の切り替えが可能）。

### English
- **Brightness Boost**:
  - Increased glint shader multipliers (`1.35x` bright / `0.90x` ambient) and raised default `glintPasses` from `2` to `3`, boosting overall glint brightness by ~50%.
- **Optional Jade Integration**:
  - Added an optional Jade plugin that displays container loot status (`Unopened` / `Opened / No Loot`) in Jade's details tooltip, with Jade plugin config toggles.

## [1.0.0-alpha.3] - 2026-11-16

### 日本語
- **樽・静的ブロックコンテナの描画修正**:
  - バニラの `entityGlintDirect()` が `GL_EQUAL` 深度テストを使用するため、`scale` で浮かせていた樽（`BarrelBlockEntity`）等が深度テストで破棄されていた問題を修正。静的ブロック専用の `GL_LEQUAL` 深度テスト＋`VIEW_OFFSET_Z_LAYERING` を備えた `GlintRenderTypes` を実装。
  - `ModelDataManager` から MOD ブロックの `ModelData` を取得するよう改善。
- **MOD製チェスト・コンテナの対応強化**:
  - `RandomizableContainerBlockEntity` を継承しない MOD 製コンテナ向けに `ClassValue`＋`MethodHandle` キャッシュによる低負荷 `lootTable` フィールド自動検出と、右クリック時/破壊時の解除同期を追加。
  - `DefaultVertexFormat.BLOCK` 形式の `BlockEntityRenderer` を持つ MOD 製チェストにも対応する `BlockToEntityGlintAdapter` を実装。
- **輝きの波の平準化・視認性強化**:
  - 90度回転・位相オフセット付き対向グリント（`BER_GLINT_COUNTER` / `STATIC_GLINT_COUNTER`）と常時底上げ発光レイヤー（`BER_GLINT_AMBIENT` / `STATIC_GLINT_AMBIENT`）を導入し、最も暗い谷間でも常に上位 1/4 相当の明るさを維持するよう改善。

### English
- **Barrel & Static Block Container Rendering Fix**:
  - Fixed barrels (`BarrelBlockEntity`) and other static block containers not rendering glint because vanilla `entityGlintDirect()` uses `GL_EQUAL` depth testing; added custom `GlintRenderTypes` with `GL_LEQUAL` and `VIEW_OFFSET_Z_LAYERING`.
  - Added `ModelData` lookup via `ModelDataManager` for modded static block containers.
- **Enhanced Modded Chest & Container Support**:
  - Added `ModContainerLootAccessor` (`ClassValue` + `MethodHandle` cache) to detect loot-table fields on modded `BlockEntity` classes that do not extend `RandomizableContainerBlockEntity`, with right-click and removal sync.
  - Added `BlockToEntityGlintAdapter` in `GlintMultiBufferSource` to support modded `BlockEntityRenderer`s using `DefaultVertexFormat.BLOCK`.
- **Consistent Glint Brightness**:
  - Added counter-phase rotating glint and steady ambient glint layers so brightness remains at or above the previous top-quarter intensity even during wave troughs.

## [1.0.0-alpha.2] - 2026-11-16

### 日本語
- **バグ修正**:
  - `unpackLootTable` 内で `LootTable.fill` が `setItem()` 等を通じて `unpackLootTable(null)` を再帰呼び出しした際に、未開封判定フラグが上書きされて開封後の解除通知が送られない不具合を修正（`lootTable` フィールドの `PUTFIELD` 直後を直接フック）。
- **視認性向上**:
  - エンチャントのきらめきエフェクトを一目で判別できるよう、デフォルトの重ね掛け描画回数（`glintPasses`）を `2` に設定し、静的ブロックコンテナ（樽等）の描画パスも `entityGlintDirect` に統一。
  - クライアントコンフィグに `rendering.glintPasses`（範囲: `1`～`4`）を追加。

### English
- **Bug Fixes**:
  - Fixed an issue where unopened state was not cleared after opening/interacting with a container due to recursive `unpackLootTable(null)` calls triggered by `LootTable.fill` overwriting the pre-unpack flag; now hooks directly after the `PUTFIELD lootTable` instruction.
- **Visual Improvements**:
  - Increased default additive glint passes (`glintPasses`) to `2` and unified static container rendering onto `entityGlintDirect` so unopened containers stand out immediately at a glance.
  - Added `rendering.glintPasses` (range `1`–`4`) to the client config.

## [1.0.0-alpha.1] - 2026-11-16

### 日本語
- 初回リリース (Alpha):
  - `RandomizableContainerBlockEntity` への Mixin によるイベント駆動・追加 NBT ゼロの未開封検知を実装。
  - チャンク視聴時の一括同期（空チャンクはスキップ）および開封・破壊時の 9 バイト差分同期を実装。
  - FastUtil ベースの O(1) クライアントキャッシュを実装。
  - `BlockEntityRenderer` 持ちコンテナ（チェスト等）と静的ブロックコンテナ（樽等）の両方に対する低負荷エンチャントグリント描画を実装。
  - クライアント／共通コンフィグおよび日英言語ファイル (`en_us.json`, `ja_jp.json`) を実装。

### English
- Initial Release (Alpha):
  - Event-driven, zero-extra-NBT unopened loot container detection via `RandomizableContainerBlockEntity` mixins.
  - Chunk-watch bulk sync (empty chunks skipped) and 9-byte incremental update packets on open/break.
  - FastUtil-backed O(1) client-side cache.
  - Low-overhead enchantment glint rendering for both `BlockEntityRenderer` containers and static block containers (barrels).
  - Client/common configuration and English/Japanese localization files.
