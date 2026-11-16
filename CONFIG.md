# Configuration / コンフィグ設定

[日本語](#日本語) | [English](#english)

## 日本語
- `config/unopenedindication-client.toml`
  - `rendering.enabled` (Boolean, デフォルト: `true`): きらめき表示の有効/無効。
  - `rendering.maxRenderDistance` (Integer, デフォルト: `48`, 範囲: `8`～`256`): 描画する最大距離（ブロック）。
  - `rendering.renderForBlockEntityRenderers` (Boolean, デフォルト: `true`): チェスト等の `BlockEntityRenderer` を持つコンテナへの描画。
  - `rendering.renderForStaticBlocks` (Boolean, デフォルト: `true`): 樽等の静的ブロックコンテナへの描画。
- `config/unopenedindication-common.toml`
  - `filtering.blacklistedBlockEntities` (List<String>, デフォルト: `[]`): 除外するブロックエンティティID（例: `["minecraft:barrel"]`）。

## English
- `config/unopenedindication-client.toml`
  - `rendering.enabled` (Boolean, default: `true`): Enable or disable glint rendering.
  - `rendering.maxRenderDistance` (Integer, default: `48`, range: `8`–`256`): Maximum render distance in blocks.
  - `rendering.renderForBlockEntityRenderers` (Boolean, default: `true`): Render glint on containers with a `BlockEntityRenderer`.
  - `rendering.renderForStaticBlocks` (Boolean, default: `true`): Render glint on static block containers like barrels.
- `config/unopenedindication-common.toml`
  - `filtering.blacklistedBlockEntities` (List<String>, default: `[]`): Registry IDs of block entities to exclude.
