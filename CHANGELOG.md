# Changelog / 変更履歴

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
