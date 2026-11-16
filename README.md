# Unopened Indication

[日本語](#日本語) | [English](#english)

## 日本語
構造物などに生成されるルートテーブル持ちのチェスト・樽・MOD製コンテナのうち、**未開封のもの**だけにエンチャントのきらめきエフェクトを付与する Minecraft 1.20.1 (Forge) 用 MOD です。

### 主な特徴
- **幅広いコンテナ対応**: バニラのチェスト・樽・シュルカーボックスや、`RandomizableContainerBlockEntity` を継承する MOD 製チェスト・樽に対応。
- **低負荷＆NBT非乱用**:
  - 追加の NBT タグを一切保存せず、既存の `lootTable` フィールドのみを参照。
  - 毎 tick の全走査をせず、チャンク視聴時と開封・破壊時のイベント駆動同期のみを使用。
  - 未開封コンテナが存在しないチャンクではパケットを送信しません。
- **高速キャッシュと最適化描画**:
  - FastUtil プリミティブコレクションによる O(1) クライアントキャッシュ。
  - チェスト等の `BlockEntityRenderer` には再利用可能な `MultiBufferSource` ラッパー、樽等の静的ブロックにはバッチ描画を使用。

### 導入方法
1. Minecraft 1.20.1 + Forge 47.x を用意します。
2. `build/libs/unopenedindication-<version>.jar` を `mods` フォルダに入れます。
3. クライアント・サーバーの両方に導入してください。

---

## English
A lightweight Minecraft 1.20.1 (Forge) mod that highlights unopened loot containers (chests, barrels, and modded containers with loot tables) with an enchantment glint effect.

### Features
- **Broad Support**: Works with vanilla chests, barrels, shulker boxes, and modded containers extending `RandomizableContainerBlockEntity`.
- **Low Overhead & Zero Extra NBT**:
  - Writes zero extra NBT tags to save data by reading the existing `lootTable` field directly.
  - Event-driven synchronization on chunk watch and container open/break; sends no packets for empty chunks.
- **Optimized Client Rendering**:
  - O(1) FastUtil primitive cache on the client.
  - Reusable `MultiBufferSource` wrapper for `BlockEntityRenderer` containers and batched quad rendering for static blocks like barrels.

### Installation
1. Install Minecraft 1.20.1 with Forge 47.x.
2. Place `unopenedindication-<version>.jar` into the `mods` folder on both client and server.
