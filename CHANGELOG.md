# Changelog / 変更履歴

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
