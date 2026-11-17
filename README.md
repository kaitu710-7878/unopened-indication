# Unopened Indication / 未開封コンテナ表示MOD

[English](#english) | [日本語](#日本語)

---

## English
A lightweight Minecraft 1.20.1 (Forge) mod that highlights unopened loot containers (chests, barrels, and modded containers with loot tables) with an enchantment glint effect.

### Features
- **Broad Support**: Works with vanilla chests, barrels, shulker boxes, and modded containers extending `RandomizableContainerBlockEntity` as well as custom modded containers via lightweight reflection caching.
- **Low Overhead & Zero Extra NBT**:
  - Writes zero extra NBT tags to save data by reading the existing `lootTable` field directly.
  - Event-driven synchronization on chunk watch and container open/break; sends no packets for empty chunks.
- **Optimized Client Rendering**:
  - O(1) FastUtil primitive cache on the client.
  - Reusable `MultiBufferSource` wrapper for `BlockEntityRenderer` containers and batched quad rendering for static blocks like barrels.
- **Optional Jade Integration**:
  - Displays loot container status (`Unopened` / `Opened / No Loot`) directly on Jade's standard tooltip when Jade is installed.

### Installation
1. Install Minecraft 1.20.1 with Forge 47.x.
2. Place `unopenedindication-<version>.jar` into the `mods` folder on both client and server.

### Notes & Out of Scope
- This mod detects and highlights containers that hold a **Loot Table** whose contents are generated only upon opening or breaking.
- Some structures generate containers with **pre-populated items already stored inside** rather than a Loot Table (containers whose contents are visible in Jade even before opening without this mod). Because they do not have a Loot Table, they are treated as `Opened / No Loot` and will not glow—this is outside the scope of this mod by design.

---

## 日本語
構造物などに生成されるルートテーブル持ちのチェスト・樽・MOD製コンテナのうち、**未開封のもの**だけにエンチャントのきらめきエフェクトを付与する Minecraft 1.20.1 (Forge) 用 MOD です。

### 主な特徴
- **幅広いコンテナ対応**: バニラのチェスト・樽・シュルカーボックスや、`RandomizableContainerBlockEntity` を継承する MOD 製チェスト・樽に加え、軽量キャッシュを用いた独自実装の MOD 製コンテナにも対応。
- **低負荷＆NBT非乱用**:
  - 追加の NBT タグを一切保存せず、既存の `lootTable` フィールドのみを参照。
  - 毎 tick の全走査をせず、チャンク視聴時と開封・破壊時のイベント駆動同期のみを使用。
  - 未開封コンテナが存在しないチャンクではパケットを送信しません。
- **高速キャッシュと最適化描画**:
  - FastUtil プリミティブコレクションによる O(1) クライアントキャッシュ。
  - チェスト等の `BlockEntityRenderer` には再利用可能な `MultiBufferSource` ラッパー、樽等の静的ブロックにはバッチ描画を使用。
- **Jade オプション連携**:
  - Jade 導入時は、コンテナに視線を合わせた際の通常ツールチップに開封・未開封状態を表示します。

### 導入方法
1. Minecraft 1.20.1 + Forge 47.x を用意します。
2. `build/libs/unopenedindication-<version>.jar` を `mods` フォルダに入れます。
3. クライアント・サーバーの両方に導入してください。

### 注意事項（仕様・対象外について）
- 本 MOD は、**ルートテーブル（`LootTable`）が設定されており、開封または破壊時に初めて中身が抽選・生成されるコンテナ** を「未開封」として判別・装飾します。
- 一部の構造物に生成されるコンテナには、ルートテーブルではなく**生成時から固定のアイテムが直接設定されているもの**（本 MOD を導入せずとも Jade 等で開封前から中身が確認できるコンテナ）があります。これらはルートテーブルを保持していないため、まだ一度も開けていなくても「開封済み / ルートなし」と表示され、きらめきエフェクトも付与されません（本 MOD の仕様上の管轄外となります）。
