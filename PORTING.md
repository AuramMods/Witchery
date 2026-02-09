# Witchery 1.7.10 -> 1.20.1 Porting Plan

## Working Style
- Breadth first first, depth second.
- Prefer creating full vertical skeletons (all systems present, minimal behavior) before polish.
- Check `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING_MEMORY.md` before starting work each session.
- Check `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING_MANIFEST.md` before hunting for source details.

## Current Snapshot (2026-02-09)
- Legacy source inventory completed for blocks, items, fluids, entities, key registries, and bootstrap flow.
- Manifest + durable memory created.
- Phase 1 registry scaffolding is in place and compiling.
- Phase 2 asset skeleton is partially in place:
  - generated placeholder `blockstates`, `models/block`, and `models/item` for registered content.
  - copied legacy textures into modern resource path.
  - normalized resource-location usage to lowercase-safe paths to avoid model parse failures.
  - migrated model texture refs and assets to singular atlas paths (`block`/`item`) for 1.20.1 compatibility.
  - added temporary barrier-based placeholder textures for missing legacy sprite names to eliminate purple-black fallback during scaffolding.
  - completed lowercase filename normalization in `textures/block` + `textures/item` and validated clean model bake log.
  - started targeted 1.7 visual parity fixes for high-priority blocks (`wolftrap`, `icedoor`, `witchlog`, `mindrake`, `wallgen`).
  - updated `icedoor` to use 1.20.1 door model parent chain (`door_*`) and vanilla door-state rotation mapping.
  - replaced simplified `wolftrap` placeholder with geometry based on legacy `ModelBeartrap` dimensions.
  - converted legacy crop family to staged crop parity assets + crop block classes:
    - crop blockstates now use `age=*` variants instead of cube placeholders for `belladonna`, `mandrake`, `artichoke`, `snowbell`, `wormwood`, `wolfsbane`, `garlicplant`.
    - added stage model sets for those crops (`*_stageN`) and switched crop item models to flat generated icons using mature-stage textures.
    - mapped crop max-age behavior in block scaffolding (`4`, `5`, or `7` depending on legacy crop).
  - expanded `witchlog` parity to multi-wood variants via `wood_type` blockstate (`rowan`, `alder`, `hawthorn`) with dedicated alder/hawthorn model sets.
  - switched `wallgen` block model texture from hardcoded iron placeholder to `witchery:block/wallgen` for visual parity testing.
  - replaced major structural cube placeholders with 1.20.1 state-driven models + block classes:
    - stairs family (`stairswoodrowan`, `stairswoodalder`, `stairswoodhawthorn`, `icestairs`, `snowstairs`)
    - slab family (`witchwoodslab`, `iceslab`, `snowslab`)
    - fence/fence gate (`icefence`, `icefencegate`)
    - pressure plates (`icepressureplate`, `snowpressureplate`, `cwoodpressureplate`, `cstonepressureplate`, `csnowpressureplate`)
    - buttons (`cbuttonwood`, `cbuttonstone`)
  - completed broad non-cube model conversion pass for legacy placeholders:
    - plant-like models migrated to `cross`/flat forms (`bramble`, `somniancotton`, `spanishmoss`, `vine`, `web`, `witchsapling`, `lilypad`, `leapinglily`, `cactus`).
    - technical/utility blocks switched off full-cube rendering (`barrier`, `light`, `slurp`, `force`, `brewgas`) with explicit inventory item icons.
    - fluid-family placeholders converted from full cubes to short liquid approximations (`brew`, `brewliquid`, `disease`, `spiritflowing`, `hollowtears`).
    - portal-family placeholders converted to crossed thin-pane portal geometry (`spiritportal`, `tormentportal`).
    - decorative/functional placeholders converted to non-full approximations (`altar`, `kettle`, `spinningwheel`, `statueofworship`, `coffinblock`, `refillingchest`, `mirrorwall`, `infinityegg`, plus machine-family placeholders `clever`, `distillery*`, `fumefunnel*`, `witchesoven*`).
  - fixed broken double-slab item model placeholders:
    - `icedoubleslab`, `snowdoubleslab`, `witchwooddoubleslab` no longer point at `witchery:item/taglockkit`.
  - added horizontal-facing scaffold parity for key machine/decor blocks in code + blockstates:
    - blocks now place with facing state (`north/east/south/west`) instead of static default state.
    - covered IDs: `altar`, `kettle`, `spinningwheel`, `distilleryidle`, `distilleryburning`, `witchesovenidle`, `witchesovenburning`, `fumefunnel`, `filteredfumefunnel`, `coffinblock`, `statueofworship`, `refillingchest`, `leechchest`.
  - added legacy variant-state parity for core wood/bundle families:
    - `witchwood` now uses `wood_type=rowan|alder|hawthorn` with `planks_*` textures.
    - `witchleaves` now uses `wood_type=rowan|alder|hawthorn` with `leaves_*` textures.
    - `witchsapling` now uses `wood_type=rowan|alder|hawthorn` with `sapling_*` textures.
    - `wickerbundle` now uses `bundle_type=plain|bloodied` with `wicker_block_*` textures.
  - added `shadedglass` color-state parity:
    - `shadedglass` and `shadedglass_active` now use `color=...` with 16 legacy color values.
    - generated `shadedglass_<color>` and `shadedglass_active_<color>` model families from legacy `shadedglassoff_*` / `shadedglass_*` textures.
  - remaining `cube_all` block models reduced to 14 intentionally-full placeholders:
    - core placeholders: `bloodedwool`, `icedoubleslab`, `perpetualice`, `pitdirt`, `pitgrass`, `snowdoubleslab`, `tormentstone`, `wallgen`, `witchwooddoubleslab`.
    - intentional variant models (not generic placeholders): `witchwood_rowan`, `witchwood_alder`, `witchwood_hawthorn`, `shadedglass_*`, `shadedglass_active_*`.
  - fluid scaffold parity now includes fluid block + bucket wiring:
    - converted `spiritflowing`, `hollowtears`, `disease`, `brew`, `brewgas`, `brewliquid` from static placeholder blocks to `LiquidBlock` scaffolds backed by Forge fluid registrations.
    - wired legacy bucket items to Forge fluid buckets: `bucketspirit` -> `fluidspirit`, `buckethollowtears` -> `hollowtears`, `bucketbrew` -> `brew`.
    - added fluid-to-block/bucket mapping in `WitcheryFluids` so source/flowing fluids, blocks, and bucket items are linked for world interaction testing.
  - minimal server data-generation scaffold is now present:
    - added `GatherDataEvent` hook and providers for block tags, loot tables, and recipes.
    - current providers are breadth-first placeholders (`legacy_blocks` tag, generic block self-drops, empty recipe migration stub).
  - placeholder spawn-egg coverage added for entity registry breadth:
    - all legacy entity placeholders now get generated `*_spawn_egg` items (via `WitcheryItems` + `WitcheryEntities` maps).
    - spawn eggs are included in the creative-tab aggregate list for quick scaffolding QA.
  - network skeleton scaffolding now mirrors legacy packet surface:
    - added `WitcheryNetwork` `SimpleChannel` bootstrap and registration from mod init path.
    - added normalized packet-intent inventory for all 19 legacy pipeline messages with legacy ID + direction metadata.
    - added no-op intent packet handler scaffold with direction validation warnings (behavior implementation deferred to depth pass).
  - dimension migration skeleton now has stable resource-key anchors:
    - added `WitcheryDimensions` keys for `dream`, `torment`, and `mirror` across `Level`, `LevelStem`, and `DimensionType`.
    - datapack/worldgen wiring is still pending for actual dimension registration/teleport flow.
  - event-bus hook skeleton now has explicit Forge-side anchor points:
    - added `WitcheryEventHooks` no-op subscribers for entity capability attach, player login/clone, and level load.
    - preserves breadth visibility for upcoming `ExtendedPlayer` replacement and world runtime bootstrap migration.
  - capability/data-attachment scaffold now exists for player data migration:
    - added `WitcheryCapabilities` MOD-bus registration for `WitcheryPlayerData`.
    - added `WitcheryPlayerDataProvider` attachment + clone-copy wiring through `WitcheryEventHooks`.
    - added `WitcheryPlayerData` NBT-serializable payload shell (`initialized`, `syncRevision`) and login-time sync-intent send (`extended_player_sync`).
  - GUI/menu scaffold now has explicit legacy GUI-ID parity anchors:
    - migrated menu intent list from plain names to `(legacyGuiId, key)` metadata in `LegacyRegistryData`.
    - replaced generic `ChestMenu` placeholders with per-key `LegacyPlaceholderMenu` registrations.
    - added client-side `LegacyPlaceholderScreen` registration for every legacy menu type to keep all GUI IDs routable in 1.20.1.

## Phase Checklist

### Phase 0 - Discovery + Inventory (complete)
- [x] Map legacy startup lifecycle (`preInit/init/postInit/serverLoad`).
- [x] Enumerate legacy block registrations.
- [x] Enumerate legacy item registrations.
- [x] Enumerate fluid registrations and fluid-container bindings.
- [x] Enumerate entity registrations.
- [x] Enumerate high-impact registries (rites, brews, potions, predictions, creature powers, packets, dimensions, integrations).
- [x] Capture findings in manifest + memory docs.

### Phase 1 - 1.20 Registration Skeleton (breadth)
- [x] Create `DeferredRegister` structure for blocks.
- [x] Create `DeferredRegister` structure for items.
- [x] Create `DeferredRegister` structure for fluids + fluid types.
- [x] Create `DeferredRegister` structure for block entities.
- [x] Create `DeferredRegister` structure for entity types.
- [x] Create `DeferredRegister` structure for menu types.
- [x] Create `DeferredRegister` structure for mob effects (potions/effects).
- [x] Create creative mode tab wiring.
- [x] Register placeholder instances for every legacy registry entry (names first, behavior later).

### Phase 2 - Data + Assets Skeleton (breadth)
- [x] Add placeholder blockstates/models/lang entries for all registered blocks/items.
- [x] Add minimal data-gen providers (loot, recipes, tags).
  - `WitcheryBlockTagsProvider` (`legacy_blocks`), `WitcheryLootTableProvider`/`WitcheryBlockLoot` (self-drop scaffolding), and `WitcheryRecipeProvider` stub are registered via `WitcheryDataGenerators`.
- [x] Add placeholder spawn egg / bucket items where needed.
  - bucket scaffolding is wired for legacy fluid buckets (`bucketspirit`, `buckethollowtears`, `bucketbrew`).
  - placeholder spawn eggs are generated from legacy entity IDs using the `*_spawn_egg` naming pattern.
- [~] Ensure game boots with full registry surface present.
  - All static resource checks pass (no missing model parent/texture references).
  - Fresh client run verified: no Witchery model/texture bake errors after latest `icedoor`/`wolftrap` asset fixes.

### Phase 3 - Systems Migration Skeleton (breadth)
- [x] Network channel + packet stubs (all legacy packet message intents represented).
- [~] GUI/menu stubs for all legacy GUI IDs.
  - menu intents now preserve legacy GUI ID mapping (`0..8`) plus key name in `LegacyRegistryData`.
  - all legacy menu keys now register typed `LegacyPlaceholderMenu` + `LegacyPlaceholderScreen` scaffolds.
  - opening behavior and real container slot logic are still TODO.
- [~] Dimension/key migration for Dream/Torment/Mirror.
  - resource keys are scaffolded (`Level`, `LevelStem`, `DimensionType`) for `dream`, `torment`, and `mirror`.
  - datapack level-stem + dimension-type content and runtime travel hooks are still TODO.
- [~] Event bus hooks migrated with no-op or minimal behavior.
  - placeholder Forge-bus hooks are in place for attach-capabilities, player login/clone, and level load events.
  - attach-capabilities/player-clone hooks now include capability provider wiring for player data scaffold.
  - real handler routing/feature logic is still TODO.
- [~] Capability/data attachment plan for replacing `ExtendedPlayer`/custom NBT patterns.
  - `WitcheryPlayerData` + provider + capability registration scaffolding is in place.
  - migration of legacy fields/logic from `ExtendedPlayer` remains TODO.

### Phase 4 - Feature Behavior Pass (depth)
- [ ] Blocks/tile entity behavior parity pass.
- [ ] Item behavior parity pass (including metadata-subitem conversions).
- [ ] Brewing/kettle/distillery/spinning/brazier parity.
- [ ] Ritual and circle magic parity.
- [ ] Infusion, prediction, creature power parity.
- [ ] Entity AI/combat/spawn tuning parity.

### Phase 5 - Integration + Stabilization
- [ ] Optional mod compat strategy (modern equivalents where relevant).
- [ ] Save migration / missing mapping strategy.
- [ ] Balance and progression validation.
- [ ] Multiplayer sync and desync hardening.
- [ ] Test matrix + release checklist.

## Immediate Next Actions
- [x] Add placeholder bucket/block hookups for fluids (if needed for world interaction/testing).
- [ ] Add placeholder block items for non-itemized legacy blocks where useful for QA visibility.
- [x] Re-run client and validate `run/logs/latest.log` after the lowercase resource-location fix.
- [x] Re-run client and validate `run/logs/latest.log` after `icedoor` parent-model and `wolftrap` geometry parity fixes.
- [~] Continue replacing placeholder block models with old 1.7 equivalents (breadth pass before behavior depth).
  - completed crop-family stage model parity pass.
  - completed structural families (stairs/slabs/fence/fence-gate/pressure-plate/button) with 1.20.1 state models.
  - completed broad non-cube decorative/functional pass; remaining full-cube placeholders now narrowed to 14 likely-intentional cube blocks.
  - next breadth target: metadata/state parity for remaining multi-variant legacy cubes (`witchwood`, `witchleaves`, `wickerbundle`, `wallgen`) and orientation/state behavior where legacy used TESR/custom renderers.
- [ ] Keep this file updated as phases move from breadth to depth.
  - fixed plant-style solidity/culling mismatch (example: blood rose):
    - added non-occluding bounded block scaffolds for plant-like legacy blocks so they no longer behave as full cubes.
    - converted `bloodrose`, `glintweed`, `embermoss`, `crittersnare`, `plantmine` to `minecraft:block/cross` models.
    - moved `witchsapling` to a dedicated non-colliding variant block scaffold (`wood_type` still preserved).
  - added client render-layer registration (`cutout`/`cutoutMipped`/`translucent`) for transparent/cross assets to avoid opaque/full-block visual artifacts:
    - file: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/client/WitcheryClient.java`
  - completed broad non-opaque shape parity pass for legacy decorative/special blocks:
    - extended `WitcheryBlocks` mapping to non-occluding/custom-shape scaffolds for high-impact IDs that were still default full cubes (`demonheart`, `wolfhead`, `alluringskull`, `chalice`, `candelabra`, `brazier`, `crystalball`, `placeditem`, `garlicgarland`, `dreamcatcher`, `poppetshelf`, `silvervat`, `daylightcollector`, `bloodcrucible`, `mirrorblock*`, `circle*`, `beartrap`, `wolftrap`, plus fluid/portal placeholders).
    - added shape-aware horizontal scaffold support for machine/chest-style blocks so existing `facing` blockstates now use non-full bounding where legacy blocks were not full cubes.
    - mapped `stockade` and `icestockade` to fence behavior scaffold for breadth parity with legacy post/connection behavior.
  - expanded client render-layer coverage to include additional alpha-heavy decorative blocks and portal/fluid-style placeholders (cutout + translucent groups).
  - migrated legacy fluid-like block IDs to true fluid scaffolds:
    - `spiritflowing`, `hollowtears`, `disease`, `brew`, `brewgas`, `brewliquid` now use `LiquidBlock` tied to `WitcheryFluids` source fluids.
    - bucket items `bucketspirit`, `buckethollowtears`, `bucketbrew` now register as `BucketItem` instead of generic items.
  - upgraded GUI placeholder surface to legacy-ID aware menu/screen scaffolds:
    - `WitcheryMenus` now registers `LegacyPlaceholderMenu` entries keyed from `LegacyRegistryData.LegacyMenuIntent`.
    - `WitcheryClient` now registers `LegacyPlaceholderScreen` for all menu placeholders.
    - this keeps every legacy GUI ID wired while container/screen behavior is still breadth-level placeholder logic.
  - look-ahead queue for next operations:
    - dimension datapack scaffolding for `dream`/`torment`/`mirror` (`dimension_type` + `dimension` JSON, then travel hooks).
    - replace generic network intent packet with typed payload stubs per legacy packet class.
    - expand `WitcheryPlayerData` fields toward legacy `ExtendedPlayer` coverage (inventory/state/effect sync groups).
  - validation: `./gradlew compileJava` succeeds after this pass.
