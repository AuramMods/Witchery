# Witchery 1.7.10 Legacy Manifest (Breadth-First Port Baseline)

## Scope
This document inventories legacy registry surfaces for the port.

Primary source root:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery`

## Source Footprint
- Total files under `old-1.7.10`: `1597`
- Total Java files under `old-1.7.10`: `803`

## Package File Counts (top-level)
| Package | Java Files |
|---|---:|
| `(root)` | 7 |
| `blocks` | 85 |
| `brewing` | 132 |
| `client` | 177 |
| `common` | 10 |
| `crafting` | 12 |
| `dimension` | 6 |
| `entity` | 91 |
| `familiar` | 2 |
| `infusion` | 35 |
| `integration` | 25 |
| `item` | 61 |
| `network` | 20 |
| `predictions` | 12 |
| `ritual` | 55 |
| `util` | 49 |
| `worldgen` | 24 |

## Bootstrap + Lifecycle Map
Main bootstrap class:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/Witchery.java`

Lifecycle stages:
- `preInit`
  - config setup
  - world generator registration
  - village structure handlers
  - packet pipeline preInit
  - construct `WitcheryPotions`, `WitcheryFluids`, `WitcheryBlocks`, `WitcheryItems`, `WitcheryEntities`
  - recipe + potion preInit
- `init`
  - packet init
  - entity init
  - GUI handler registration
  - Dream/Torment/Mirror dimensions registration
  - grass seed entries
  - proxy handlers/renderers
  - mod hooks registration + init
  - potions init, recipes init, brew registry init
- `postInit`
  - recipes postInit
  - mod hooks postInit
  - external biome hook
  - proxy postInit + event registration
  - dispenser behavior bindings
  - chunkloading callback setup
- `serverLoad`
  - server command registration
  - `PowerSources.initiate()`
  - `BlockAreaMarker.AreaMarkerRegistry.serverStart()`
  - worldgen runtime init

## Core Legacy Registry Owners
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryBlocks.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryItems.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryFluids.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryEntities.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryRecipes.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/brewing/WitcheryBrewRegistry.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/brewing/potions/WitcheryPotions.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/infusion/infusions/symbols/EffectRegistry.java`

## Registry Counts (Legacy)
| Registry Surface | Count |
|---|---:|
| Blocks (`func_149663_c`) | 121 |
| Items (`func_77655_b`) | 102 |
| Fluids | 6 |
| Entity registrations | 55 |
| Potions/effects | 44 |
| `ItemGeneral` metadata variants | 163 |
| `RiteRegistry.addRecipe` | 96 |
| `PredictionManager.addPrediction` | 17 |
| `CreaturePower.Registry.add` | 25 |
| `KettleRecipes.addRecipe` | 39 |
| `DistilleryRecipes.addRecipe` | 9 |
| `SpinningRecipes.addRecipe` | 4 |
| `WitcheryBrewRegistry.register` | 158 |
| Symbol effects (`EffectRegistry`) | 31 |
| Packet messages | 19 |

## Registration Mechanics (Important)
- `BlockBase.func_149663_c` auto-registers blocks via `BlockUtil.registerBlock`.
- `BlockBaseContainer.func_149663_c` auto-registers blocks + tile entities.
- `ItemBase.func_77655_b` auto-registers items via `ItemUtil.registerItem`.
- Namespaced IDs are trimmed to path for legacy `GameRegistry` (`witchery:name` -> `name`).
- `witchwoodslab`, `iceslab`, `snowslab`, and their three legacy double IDs appear in both lists. The modern item IDs match their blocks. Both modern double states and the legacy double blocks drop two matching single slabs. Legacy witchwood color metadata remains a separate parity item.
- Legacy tree metadata `0/1/2` maps to `rowan/alder/hawthorn` on `witchsapling`, `witchlog`, `witchleaves`, and `witchwood`. Non-rowan item stacks preserve the variant in `BlockStateTag.wood_type`. Untagged stacks retain the rowan default. Sapling growth stage and leaves persistence use their modern vanilla block properties.

## Spawn Egg Model Coverage (2026-02-09)
- Runtime spawn egg item registration source:
  - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryItems.java` (`registerLegacySpawnEgg` from `WitcheryEntities.all()`).
- Asset model source:
  - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/assets/witchery/models/item/*_spawn_egg.json`.
- Coverage rule:
  - exactly one model file per normalized legacy entity path from `LegacyRegistryData.ENTITIES` (`55` total).
  - parent model for each file is `minecraft:item/template_spawn_egg`.

## Block Model Parity Anchors (current focus)
- `witchery:wolfaltar`
  - Uses the original Wolf Altar geometry and texture through its block entity renderer and inventory renderer. The facing determines the front of the statue and the reward position.
  - The survival recipe accepts either Wolf Head variant in each trophy slot. The original offerings and trials advance from rank one through rank ten. The gold reward creates `witchery:mooncharm`, which retains its legacy wear value.
- `witchery:beartrap` and `witchery:wolftrap`
  - Legacy class: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/blocks/BlockBeartrap.java` (`new BlockBeartrap(true)`).
  - Legacy render model: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/client/model/ModelBeartrap.java`.
  - Legacy texture: `assets/witchery/textures/blocks/beartrap.png`.
  - Both variants use the original plate, hinges, jaws and teeth in a shared block entity renderer. The silvered variant uses the full-moon sheep lure and only springs on its recorded Wolfman.
- 2026-02-09 placeholder texture remap set (reported visual breakage triage):
  - `witchery:tormentstone` -> `minecraft:block/mycelium_top`.
  - `witchery:refillingchest` -> `witchery:block/leechchest`.
  - `witchery:brewliquid` -> `witchery:block/brew_still` + `witchery:block/brew_flow`.
  - `witchery:mirrorblock` -> `witchery:block/mirror`.
  - `witchery:mirrorblock2` -> `witchery:block/mirror2`.
- Fetish renderer/model anchors:
  - Legacy renderer: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/client/renderer/RenderFetish.java`.
  - Legacy models: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/client/model/ModelFetishTrent.java`, `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/client/model/ModelFetishScarecrow.java`.
  - `FetishRenderer` uses the legacy `64x64` Trent and Scarecrow textures, and the crossed Witch's Ladder texture. It renders both the items and placed models, with facing, dye and spectral opacity.
  - Rites 84–86 bind the five ordinary effects to the existing `scarecrow`, `trent` and `witchsladder` IDs. The hidden sixth result summons Death or a player equipped with the three Death armor pieces and Death's Hand.
- `witchery:brazier` keeps its original block and block entity IDs. Slots 0–2 hold the ingredients and slot 3 persists the ignition ash. The state exposes `lit` for client flames. The eight recipes live in `BrazierRecipe`.
- `witchery:spectre`, `witchery:banshee` and `witchery:poltergeist` use distinct entity types backed by `SpectralUndead`. The Spectral Stone's legacy metadata maps to `SpectralContents`, with the original species values 1 through 3 and matching item overrides. `witchery:wormy_apple` and `witchery:graveyard_dust` map legacy ingredient values 91 and 128.
- `witchery:icedoor`
  - Legacy class: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/blocks/BlockPerpetualIceDoor.java`.
  - Legacy textures: `assets/witchery/textures/blocks/icedoor_lower.png`, `icedoor_upper.png`.
  - 1.20.1 model parent requirement: `minecraft:block/door_*` parents, not `template_door_*`.
- `witchery:witchlog`
  - Legacy class: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/blocks/BlockWitchLog.java`.
  - Legacy rowan textures: `assets/witchery/textures/blocks/log_rowan.png`, `log_rowan_top.png`.
  - Current 1.20.1 scaffold uses `wood_type=rowan|alder|hawthorn` plus `axis` to mirror legacy multi-wood log variants.
- `witchery:mindrake`
  - Legacy class: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/blocks/BlockWitchCrop.java`.
  - Legacy stage textures: `assets/witchery/textures/blocks/mindrake_stage_0..4.png`.
- `witchery:wallgen`
  - Legacy class: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/worldgen/WorldHandlerVillageDistrict.java` (nested `BlockVillageWallGen`).
  - Internal worldgen helper block; legacy registration texture fallback was `iron_block`.
- Legacy crop family (`BlockWitchCrop`)
  - Legacy class: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/blocks/BlockWitchCrop.java`.
  - Legacy stage textures live under `assets/witchery/textures/blocks/*_stage_N.png`.
  - Current 1.20.1 staged parity coverage:
    - `witchery:belladonna` -> stage `0..4` (`age=5..7` mapped to stage `4`).
    - `witchery:mandrake` -> stage `0..4` (`age=5..7` mapped to stage `4`).
    - `witchery:artichoke` -> stage `0..4` (`age=5..7` mapped to stage `4`).
    - `witchery:snowbell` -> stage `0..4` (`age=5..7` mapped to stage `4`).
    - `witchery:wormwood` -> stage `0..4` (`age=5..7` mapped to stage `4`).
    - `witchery:mindrake` -> stage `0..4` (`age=5..7` mapped to stage `4`).
    - `witchery:wolfsbane` -> stage `0..7`.
    - `witchery:garlicplant` -> stage `0..5` (`age=6..7` mapped to stage `5`).
- Structural model/state families (2026-02-08 breadth pass)
  - Stairs IDs:
    - `witchery:stairswoodrowan`, `witchery:stairswoodalder`, `witchery:stairswoodhawthorn`, `witchery:icestairs`, `witchery:snowstairs`
    - blockstate shape source pattern: vanilla 1.20.1 `oak_stairs` variant matrix.
  - Slab IDs:
    - `witchery:witchwoodslab`, `witchery:iceslab`, `witchery:snowslab`
    - blockstate includes `type=bottom/top/double`; `double` points at legacy double slab cube models (`witchwooddoubleslab`, `icedoubleslab`, `snowdoubleslab`).
  - Fence/FenceGate IDs:
    - `witchery:icefence`, `witchery:icefencegate`
    - fence uses multipart `north/east/south/west`; gate uses `facing/open/in_wall`.
  - Pressure plate IDs:
    - `witchery:icepressureplate`, `witchery:snowpressureplate`, `witchery:cwoodpressureplate`, `witchery:cstonepressureplate`, `witchery:csnowpressureplate`
    - blockstate property: `powered=true/false`.
  - Button IDs:
    - `witchery:cbuttonwood`, `witchery:cbuttonstone`
    - blockstate properties: `face`, `facing`, `powered`.
- Broad non-cube placeholder pass snapshot (2026-02-08)
  - converted from full-cube placeholders to non-full approximations:
    - plants/flat: `bramble`, `somniancotton`, `spanishmoss`, `vine`, `web`, `witchsapling`, `lilypad`, `leapinglily`, `cactus`
    - technical/invisible-like: `barrier`, `light`, `slurp`, `force`, `brewgas`
    - fluid-like visuals: `brew`, `brewliquid`, `disease`, `spiritflowing`, `hollowtears`
    - portal geometry: `spiritportal`, `tormentportal`
    - decorative/machine approximations: `altar`, `kettle`, `spinningwheel`, `statueofworship`, `coffinblock`, `refillingchest`, `mirrorwall`, `infinityegg`, `clever`, `distilleryidle`, `distilleryburning`, `fumefunnel`, `filteredfumefunnel`, `witchesovenidle`, `witchesovenburning`
  - fixed slab item model placeholders:
    - `icedoubleslab`, `snowdoubleslab`, `witchwooddoubleslab` now parent block models instead of placeholder `taglockkit`.
  - remaining `cube_all` set narrowed to:
    - `bloodedwool`, `icedoubleslab`, `perpetualice`, `pitdirt`, `pitgrass`, `shadedglass`, `shadedglass_active`, `snowdoubleslab`, `tormentstone`, `wallgen`, `wickerbundle`, `witchleaves`, `witchwood`, `witchwooddoubleslab`.
- Horizontal-facing scaffold parity (2026-02-08)
  - code scaffold: `LegacyHorizontalFacingBlock` in `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryBlocks.java`
  - IDs now using placement-facing state + rotated blockstate variants:
    - `altar`
    - `kettle`
    - `spinningwheel`
    - `distilleryidle`
    - `distilleryburning`
    - `witchesovenidle`
    - `witchesovenburning`
    - `fumefunnel`
    - `filteredfumefunnel`
    - `coffinblock`
    - `statueofworship`
    - `refillingchest`
    - `leechchest`
- Wood/bundle variant-state parity (2026-02-08)
  - code scaffolds in `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryBlocks.java`:
    - `LegacyWitchWoodVariantBlock` (`wood_type` enum property)
    - `LegacyWickerBundleBlock` (`bundle_type` enum property)
  - blockstate coverage:
    - `witchwood` -> `wood_type=rowan|alder|hawthorn`
    - `witchleaves` -> `wood_type=rowan|alder|hawthorn`
    - `witchsapling` -> `wood_type=rowan|alder|hawthorn`
    - `wickerbundle` -> `bundle_type=plain|bloodied`
  - model coverage:
    - `witchwood_rowan|alder|hawthorn` using `planks_rowan|alder|hawthorn`
    - `witchleaves_rowan|alder|hawthorn` using `leaves_rowan|alder|hawthorn`
    - `witchsapling_rowan|alder|hawthorn` using `sapling_rowan|alder|hawthorn`
    - `wickerbundle_plain|bloodied` using `wicker_block_*` side/top textures
- Shaded glass color-state parity (2026-02-08)
  - code scaffold: `LegacyShadedGlassBlock` with enum property `color` in `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryBlocks.java`
  - IDs covered:
    - `shadedglass`
    - `shadedglass_active`
  - 16 legacy colors mapped:
    - `white`, `orange`, `magenta`, `light_blue`, `yellow`, `lime`, `pink`, `gray`, `silver`, `cyan`, `purple`, `blue`, `brown`, `green`, `red`, `black`
  - model families:
    - `shadedglass_<color>` -> texture `shadedglassoff_<color>`
    - `shadedglass_active_<color>` -> texture `shadedglass_<color>`
- Plant solidity/culling parity (2026-02-08)
  - code scaffold added in `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryBlocks.java`:
    - `LegacyNonSolidShapeBlock` (custom shape + collision control + no occlusion)
    - `LegacyWitchSaplingVariantBlock` (`wood_type` + no collision + no occlusion)
  - IDs remapped from default full-solid placeholder behavior:
    - `bloodrose`, `plantmine`, `glintweed`, `crittersnare`, `bramble`, `somniancotton`, `spanishmoss`, `vine`, `web`, `embermoss`, `lilypad`, `leapinglily`, `grassper`, `witchsapling`
  - model parity update to cross renderer:
    - `bloodrose`, `glintweed`, `embermoss`, `crittersnare`, `plantmine`
  - launch hotfix (2026-02-09):
    - fixed Forge block-state cache validation crash for `witchery:plantmine` (`offset type + collision shape`) by marking collision-enabled `LegacyNonSolidShapeBlock` variants as `dynamicShape`.
- Client transparency/cutout layer registration (2026-02-08)
  - file: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/client/WitcheryClient.java`
  - render-layer groups:
    - `cutout`: crops/flowers/vines/web/sapling/lily pads/doors (legacy plant-like translucent-alpha assets)
    - `cutoutMipped`: `witchleaves`
    - `translucent`: `shadedglass`, `shadedglass_active`, `perpetualice`

## Block Registry List (`WitcheryBlocks`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryBlocks.java`

| Field | Legacy Name |
|---|---|
| `CROP_BELLADONNA` | `witchery:belladonna` |
| `CROP_MANDRAKE` | `witchery:mandrake` |
| `CROP_ARTICHOKE` | `witchery:artichoke` |
| `CROP_SNOWBELL` | `witchery:snowbell` |
| `CROP_WORMWOOD` | `witchery:wormwood` |
| `CROP_MINDRAKE` | `witchery:mindrake` |
| `CROP_WOLFSBANE` | `witchery:wolfsbane` |
| `CROP_GARLIC` | `witchery:garlicplant` |
| `SAPLING` | `witchery:witchsapling` |
| `LOG` | `witchery:witchlog` |
| `LEAVES` | `witchery:witchleaves` |
| `VOID_BRAMBLE` | `witchery:voidbramble` |
| `BRAMBLE` | `witchery:bramble` |
| `GLINT_WEED` | `witchery:glintweed` |
| `SPANISH_MOSS` | `witchery:spanishmoss` |
| `LEAPING_LILY` | `witchery:leapinglily` |
| `TRAPPED_PLANT` | `witchery:plantmine` (twelve crafting, placed, and Boline-recoverable variants) |
| `EMBER_MOSS` | `witchery:embermoss` |
| `CRITTER_SNARE` | `witchery:crittersnare` |
| `GRASSPER` | `witchery:grassper` |
| `BLOOD_ROSE` | `witchery:bloodrose` |
| `WISPY_COTTON` | `witchery:somniancotton` |
| `WEB` | `witchery:web` |
| `VINE` | `witchery:vine` |
| `CACTUS` | `cactus` |
| `LILY` | `lilypad` |
| `DEMON_HEART` | `witchery:demonheart` |
| `WOLFHEAD` | `witchery:wolfhead` |
| `PLANKS` | `witchery:witchwood` |
| `STAIRS_ROWAN` | `witchery:stairswoodrowan` |
| `STAIRS_ALDER` | `witchery:stairswoodalder` |
| `STAIRS_HAWTHORN` | `witchery:stairswoodhawthorn` |
| `WOOD_SLAB_SINGLE` | `witchery:witchwoodslab` |
| `WOOD_SLAB_DOUBLE` | `witchery:witchwooddoubleslab` |
| `DOOR_ROWAN` | `witchery:rowanwooddoor` |
| `DOOR_ALDER` | `witchery:alderwooddoor` |
| `CHALICE` | `witchery:chalice` |
| `CANDELABRA` | `witchery:candelabra` |
| `PLACED_ITEMSTACK` | `witchery:placeditem` |
| `ALLURING_SKULL` | `witchery:alluringskull` |
| `WICKER_BUNDLE` | `witchery:wickerbundle` |
| `GLOW_GLOBE` | `witchery:glowglobe` |
| `STATUE_GODDESS` | `witchery:statuegoddess` |
| `STOCKADE` | `witchery:stockade` |
| `STOCKADE_ICE` | `witchery:icestockade` |
| `PERPETUAL_ICE` | `witchery:perpetualice` |
| `PERPETUAL_ICE_DOOR` | `witchery:icedoor` |
| `PERPETUAL_ICE_STAIRS` | `witchery:icestairs` |
| `PERPETUAL_ICE_SLAB_SINGLE` | `witchery:iceslab` |
| `PERPETUAL_ICE_SLAB_DOUBLE` | `witchery:icedoubleslab` |
| `PERPETUAL_ICE_FENCE` | `witchery:icefence` |
| `PERPETUAL_ICE_FENCE_GATE` | `witchery:icefencegate` |
| `PERPETUAL_ICE_PRESSURE_PLATE` | `witchery:icepressureplate` |
| `SNOW_PRESSURE_PLATE` | `witchery:snowpressureplate` |
| `SNOW_STAIRS` | `witchery:snowstairs` |
| `SNOW_SLAB_SINGLE` | `witchery:snowslab` |
| `SNOW_SLAB_DOUBLE` | `witchery:snowdoubleslab` |
| `INFINITY_EGG` | `witchery:infinityegg` |
| `SLURP` | `witchery:slurp` |
| `PIT_DIRT` | `witchery:pitdirt` |
| `PIT_GRASS` | `witchery:pitgrass` |
| `GARLIC_GARLAND` | `witchery:garlicgarland` |
| `BLOODED_WOOL` | `witchery:bloodedwool` |
| `SHADED_GLASS` | `witchery:shadedglass` |
| `SHADED_GLASS_ON` | `witchery:shadedglass_active` |
| `MIRROR_WALL` | `witchery:mirrorwall` |
| `REFILLING_CHEST` | `witchery:refillingchest` |
| `FORCE` | `witchery:force` |
| `TORMENT_STONE` | `witchery:tormentstone` |
| `BARRIER` | `witchery:barrier` |
| `LEECH_CHEST` | `witchery:leechchest` |
| `ALTAR` | `witchery:altar` |
| `KETTLE` | `witchery:kettle` |
| `POPPET_SHELF` | `witchery:poppetshelf` |
| `DREAM_CATCHER` | `witchery:dreamcatcher` |
| `CRYSTAL_BALL` | `witchery:crystalball` |
| `SPIRIT_PORTAL` | `witchery:spiritportal` |
| `TORMENT_PORTAL` | `witchery:tormentportal` |
| `SPINNING_WHEEL` | `witchery:spinningwheel` |
| `BRAZIER` | `witchery:brazier` |
| `OVEN_IDLE` | `witchery:witchesovenidle` |
| `OVEN_BURNING` | `witchery:witchesovenburning` |
| `OVEN_FUMEFUNNEL` | `witchery:fumefunnel` |
| `OVEN_FUMEFUNNEL_FILTERED` | `witchery:filteredfumefunnel` |
| `DISTILLERY_IDLE` | `witchery:distilleryidle` |
| `DISTILLERY_BURNING` | `witchery:distilleryburning` |
| `FETISH_SCARECROW` | `witchery:scarecrow` |
| `FETISH_TREANT_IDOL` | `witchery:trent` |
| `FETISH_WITCHS_LADDER` | `witchery:witchsladder` |
| `DECURSE_TELEPORT` | `witchery:decurseteleport` |
| `DECURSE_DIRECTED` | `witchery:decursedirected` |
| `STATUE_OF_WORSHIP` | `witchery:statueofworship` |
| `CAULDRON` | `witchery:cauldron` |
| `WOLF_ALTAR` | `witchery:wolfaltar` |
| `SILVER_VAT` | `witchery:silvervat` |
| `BEARTRAP` | `witchery:beartrap` |
| `WOLFTRAP` | `witchery:wolftrap` |
| `WALLGEN` | `witchery:wallgen` |
| `COFFIN` | `witchery:coffinblock` |
| `LIGHT` | `witchery:light` (invisible level-15 light, with a temporary state for moving sources) |
| `DAYLIGHT_COLLECTOR` | `witchery:daylightcollector` (`charge` 0–15 in block state) |
| `BLOOD_CRUCIBLE` | `witchery:bloodcrucible` |
| `MIRROR` | `witchery:mirrorblock` |
| `MIRROR_UNBREAKABLE` | `witchery:mirrorblock2` |
| `CIRCLE` | `witchery:circle` |
| `GLYPH_RITUAL` | `witchery:circleglyphritual` |
| `GLYPH_OTHERWHERE` | `witchery:circleglyphotherwhere` |
| `GLYPH_INFERNAL` | `witchery:circleglyphinfernal` |
| `FLOWING_SPIRIT` | `witchery:spiritflowing` |
| `HOLLOW_TEARS` | `witchery:hollowtears` |
| `DISEASE` | `witchery:disease` |
| `BREW` | `witchery:brew` |
| `BREW_GAS` | `witchery:brewgas` |
| `BREW_LIQUID` | `witchery:brewliquid` |
| `CURSED_LEVER` | `witchery:clever` |
| `CURSED_WOODEN_DOOR` | `witchery:cwoodendoor` |
| `CURSED_WOODEN_PRESSURE_PLATE` | `witchery:cwoodpressureplate` |
| `CURSED_STONE_PRESSURE_PLATE` | `witchery:cstonepressureplate` |
| `CURSED_SNOW_PRESSURE_PLATE` | `witchery:csnowpressureplate` |
| `CURSED_BUTTON_WOOD` | `witchery:cbuttonwood` |
| `CURSED_BUTTON_STONE` | `witchery:cbuttonstone` |

## Item Registry List (`WitcheryItems`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryItems.java`

The original `witchhat`, `witchrobe`, `necromancerrobe`, and `babashat` IDs equip the modeled clothing. The corresponding ingredient mapping uses `impregnated_leather` for 72 and `brew_of_raising` for 65. `necromantic_stone` issues commands to owned undead.

| Field | Legacy Name |
|---|---|
| `GENERIC` | `witchery:ingredient` |
| `TAGLOCK_KIT` | `witchery:taglockkit` |
| `POPPET` | `witchery:poppet` |
| `POTIONS` | `witchery:potion` |
| `SLAB_SINGLE` | `witchery:witchwoodslab` |
| `SLAB_DOUBLE` | `witchery:witchwooddoubleslab` |
| `PERPERTUAL_ICE_SLAB_SINGLE` | `witchery:iceslab` |
| `PERPERTUAL_ICE_SLAB_DOUBLE` | `witchery:icedoubleslab` |
| `SNOW_SLAB_SINGLE` | `witchery:snowslab` |
| `SNOW_SLAB_DOUBLE` | `witchery:snowdoubleslab` |
| `COFFIN` | `witchery:coffin` |
| `STEW` | `witchery:stew` |
| `STEW_RAW` | `witchery:stewraw` |
| `MIRROR` | `witchery:mirror` |
| `CHALK_RITUAL` | `witchery:chalkritual` |
| `CHALK_OTHERWHERE` | `witchery:chalkotherwhere` |
| `CHALK_INFERNAL` | `witchery:chalkinfernal` |
| `CHALK_GOLDEN` | `witchery:chalkheart` |
| `BOLINE` | `witchery:boline` |
| `ARTHANA` | `witchery:arthana` |
| `HUNTSMANS_SPEAR` | `witchery:huntsmanspear` |
| `CROSSBOW_PISTOL` | `witchery:handbow` |
| `DEATH_HAND` | `witchery:deathshand` |
| `SILVER_SWORD` | `witchery:silversword` |
| `CANE_SWORD` | `witchery:canesword` |
| `WITCH_HAND` | `witchery:witchhand` |
| `CIRCLE_TALISMAN` | `witchery:circletalisman` |
| `MYSTIC_BRANCH` | `witchery:mysticbranch` |
| `DIVINER_WATER` | `witchery:divinerwater` |
| `DIVINER_LAVA` | `witchery:divinerlava` |
| `POLYNESIA_CHARM` | `witchery:polynesiacharm` |
| `DEVILS_TONGUE_CHARM` | `witchery:devilstonguecharm` |
| `MUTATING_SPRIG` | `witchery:mutator` |
| `PARASYTIC_LOUSE` | `witchery:louse` |
| `BREW_BAG` | `witchery:brewbag` |
| `SPECTRAL_STONE` | `witchery:spectralstone` |
| `SHELF_COMPASS` | `witchery:shelfcompass` |
| `KOBOLDITE_PICKAXE` | `witchery:kobolditepickaxe` |
| `DUP_STAFF` | `witchery:dupstaff` |
| `BREW` | `witchery:brewbottle` |
| `BREW_FUEL` | `witchery:brew.fuel` |
| `BREW_ENDLESS_WATER` | `witchery:brew.water` |
| `BIOME_BOOK` | `witchery:bookbiomes2` |
| `BIOME_NOTE` | `witchery:biomenote` |
| `CAULDRON_BOOK` | `witchery:cauldronbook` |
| `LEONARDS_URN` | `witchery:leonardsurn` (legacy tiers 0–3, stored splash brews and Mystic Branch slots) |
| `PLAYER_COMPASS` | `witchery:playercompass` |
| `MOON_CHARM` | `witchery:mooncharm` |
| `HORN_OF_THE_HUNT` | `witchery:hornofthehunt` |
| `CREATIVE_WOLF_TOKEN` | `witchery:wolftoken` |
| `BLOOD_GOBLET` | `witchery:glassgoblet` |
| `SUN_GRENADE` | `witchery:sungrenade` (from the loaded Daylight Collector) |
| `VAMPIRE_BOOK` | `witchery:vampirebook` (`Damage` 0–9 becomes `VampireBookPages`; retains `pageStack` bookmarks) |
| `DUP_GRENADE` | `witchery:dupgrenade` (Mirror-charged sphere with `Owner` tag) |
| `WITCH_HAT` | `witchery:witchhat` |
| `BABAS_HAT` | `witchery:babashat` |
| `WITCH_ROBES` | `witchery:witchrobe` |
| `NECROMANCERS_ROBES` | `witchery:necromancerrobe` |
| `BITING_BELT` | `witchery:bitingbelt` |
| `BARK_BELT` | `witchery:barkbelt` |
| `ICY_SLIPPERS` | `witchery:iceslippers` |
| `RUBY_SLIPPERS` | `witchery:rubyslippers` |
| `SEEPING_SHOES` | `witchery:seepingshoes` |
| `HUNTER_HAT` | `witchery:hunterhat` |
| `HUNTER_COAT` | `witchery:huntercoat` |
| `HUNTER_LEGS` | `witchery:hunterlegs` |
| `HUNTER_BOOTS` | `witchery:hunterboots` |
| `HUNTER_HAT_SILVERED` | `witchery:hunterhatsilvered` |
| `HUNTER_COAT_SILVERED` | `witchery:huntercoatsilvered` |
| `HUNTER_LEGS_SILVERED` | `witchery:hunterlegssilvered` |
| `HUNTER_BOOTS_SILVERED` | `witchery:hunterbootssilvered` |
| `HUNTER_HAT_GARLICKED` | `witchery:hunterhatgarlicked` |
| `HUNTER_COAT_GARLICKED` | `witchery:huntercoatgarlicked` |
| `HUNTER_LEGS_GARLICKED` | `witchery:hunterlegsgarlicked` |
| `HUNTER_BOOTS_GARLICKED` | `witchery:hunterbootsgarlicked` |
| `DEATH_HOOD` | `witchery:deathscowl` |
| `DEATH_ROBE` | `witchery:deathsrobe` |
| `DEATH_FEET` | `witchery:deathsfeet` |
| `MOGS_QUIVER` | `witchery:quiverofmog` |
| `GULGS_GURDLE` | `witchery:gurdleofgulg` |
| `KOBOLDITE_HELM` | `witchery:kobolditehelm` |
| `EARMUFFS` | `witchery:earmuffs` |
| `VAMPIRE_HAT` | `witchery:vampirehat` |
| `VAMPIRE_HELMET` | `witchery:vampirehelmet` |
| `VAMPIRE_COAT` | `witchery:vampirecoat` |
| `VAMPIRE_COAT_FEMALE` | `witchery:vampirecoat_female` |
| `VAMPIRE_COAT_CHAIN` | `witchery:vampirechaincoat` |
| `VAMPIRE_COAT_FEMALE_CHAIN` | `witchery:vampirechaincoat_female` |
| `VAMPIRE_LEGS` | `witchery:vampirelegs` |
| `VAMPIRE_LEGS_KILT` | `witchery:vampirelegs_kilt` |
| `VAMPIRE_BOOTS` | `witchery:vampireboots` |
| `SEEDS_BELLADONNA` | `witchery:seedsbelladonna` |
| `SEEDS_MANDRAKE` | `witchery:seedsmandrake` |
| `SEEDS_ARTICHOKE` | `witchery:seedsartichoke` |
| `SEEDS_SNOWBELL` | `witchery:seedssnowbell` |
| `SEEDS_WORMWOOD` | `witchery:seedswormwood` |
| `SEEDS_MINDRAKE` | `witchery:seedsmindrake` |
| `SEEDS_WOLFSBANE` | `witchery:seedswolfsbane` |
| `SEEDS_GARLIC` | `witchery:garlic` |
| `BUCKET_FLOWINGSPIRIT` | `witchery:bucketspirit` |
| `BUCKET_HOLLOWTEARS` | `witchery:buckethollowtears` |
| `BUCKET_BREW` | `witchery:bucketbrew` |

## ItemGeneral Metadata Variants (`Witchery.Items.GENERIC`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/item/ItemGeneral.java`

This is one registered item (`witchery:ingredient`) with many metadata subtypes.

The first migrated subtypes have individual item IDs. Preserve `witchery:ingredient` for existing unconverted stacks. The mapping below is for the Witchery portion of a legacy stack upgrade.

| Legacy metadata | 1.20.1 item ID |
|---:|---|
| 0 | `witchery:candelabra` |
| 1 | `witchery:chalice` |
| 2 | `witchery:filled_chalice` |
| 10 | `witchery:attuned_stone` |
| 11 | `witchery:charged_attuned_stone` |
| 14 | `witchery:mutandis` |
| 15 | `witchery:mutandis_extremis` |
| 16 | `witchery:quicklime` |
| 17 | `witchery:gypsum` |
| 18 | `witchery:wood_ash` |
| 21 | `witchery:belladonna_flower` |
| 22 | `witchery:mandrake_root` |
| 24 | `witchery:bat_wool` |
| 25 | `witchery:dog_tongue` |
| 26 | `witchery:soft_clay_jar` |
| 27 | `witchery:clay_jar` |
| 28 | `witchery:foul_fume` |
| 29 | `witchery:diamond_vapour` |
| 30 | `witchery:oil_of_vitriol` |
| 31 | `witchery:exhale_of_the_horned_one` |
| 32 | `witchery:breath_of_the_goddess` |
| 33 | `witchery:hint_of_rebirth` |
| 34 | `witchery:whiff_of_magic` |
| 35 | `witchery:reek_of_misfortune` |
| 36 | `witchery:odour_of_purity` |
| 37 | `witchery:tear_of_the_goddess` |
| 38 | `witchery:refined_evil` |
| 39 | `witchery:drop_of_luck` |
| 40 | `witchery:redstone_soup` |
| 60 | `witchery:brew_of_ink` |
| 63 | `witchery:rowan_berries` |
| 66 | `witchery:spectral_dust` |
| 67 | `witchery:ender_dew` |
| 69 | `witchery:globe_artichoke` |
| 73 | `witchery:fume_filter` |
| 74 | `witchery:creeper_heart` |
| 75 | `witchery:brew_of_love` |
| 77 | `witchery:brew_of_the_depths` |
| 78 | `witchery:icy_needle` |
| 79 | `witchery:frozen_heart` |
| 99 | `witchery:disturbed_cotton` |
| 100 | `witchery:fanciful_thread` |
| 101 | `witchery:tormented_twine` |
| 102 | `witchery:golden_thread` |
| 105 | `witchery:purified_milk` |
| 111 | `witchery:wormwood_leaf` |
| 153 | `witchery:anointing_paste` |
| 156 | `witchery:wolfsbane_flower` |
| 160 | `witchery:torn_vampire_page` |
| 161 | `witchery:dark_cloth` |

Variant 156 consumes one flower when the tester targets a living entity within two blocks. Cursed targets emit flames and the Wolfman howl, including cursed humans and unshifted werevillagers. The `witchery:potion.wolfsbane` effect is a separate use of the same ingredient through general cauldron brewing.

| Meta | Unlocalized Key |
|---:|---|
| 0 | `candelabra` |
| 1 | `chalice` |
| 2 | `chaliceFull` |
| 3 | `weaveMoveFast` |
| 4 | `weaveDigFast` |
| 5 | `weaveSaturation` |
| 6 | `weaveNightmares` |
| 7 | `boneNeedle` |
| 8 | `broom` → `witchery:broom` |
| 9 | `broomEnchanted` → `witchery:enchanted_broom` |
| 10 | `attunedStone` |
| 11 | `attunedStoneCharged` |
| 12 | `waystone` → `witchery:waystone` |
| 13 | `waystoneBound` → `witchery:bound_waystone` |
| 14 | `mutandis` |
| 15 | `mutandisExtremis` |
| 16 | `quicklime` |
| 17 | `gypsum` |
| 18 | `ashWood` |
| 21 | `belladonna` |
| 22 | `mandrakeRoot` |
| 23 | `demonHeart` |
| 24 | `batWool` |
| 25 | `dogTongue` |
| 26 | `clayJarSoft` |
| 27 | `clayJar` |
| 28 | `foulFume` |
| 29 | `diamondVapour` |
| 30 | `oilOfVitriol` |
| 31 | `exhaleOfTheHornedOne` |
| 32 | `breathOfTheGoddess` |
| 33 | `hintOfRebirth` |
| 34 | `whiffOfMagic` |
| 35 | `reekOfMisfortune` |
| 36 | `odourOfPurity` |
| 37 | `tearOfTheGoddess` |
| 38 | `refinedEvil` |
| 39 | `dropOfLuck` |
| 40 | `redstoneSoup` |
| 41 | `flyingOintment` → `witchery:flying_ointment` |
| 42 | `ghostOfTheLight` → `witchery:ghost_of_the_light` |
| 43 | `soulOfTheWorld` → `witchery:soul_of_the_world` |
| 44 | `spiritOfOtherwhere` → `witchery:spirit_of_otherwhere` |
| 45 | `infernalAnimus` → `witchery:infernal_animus` |
| 46 | `bookOven` |
| 47 | `bookDistilling` |
| 48 | `bookCircleMagic` |
| 49 | `bookInfusions` |
| 50 | `oddPorkchopRaw` |
| 51 | `oddPorkchopCooked` |
| 52 | `doorRowan` |
| 53 | `doorAlder` |
| 54 | `doorKey` |
| 55 | `rock` → `witchery:rock` |
| 56 | `web` → `witchery:thrown_web` |
| 57 | `brewVines` |
| 58 | `brewWeb` |
| 59 | `brewThorns` |
| 60 | `brewInk` |
| 61 | `brewSprouting` |
| 62 | `brewErosion` |
| 63 | `berriesRowan` |
| 64 | `necroStone` |
| 65 | `brewRaising` |
| 66 | `spectralDust` |
| 67 | `enderDew` |
| 69 | `artichoke` |
| 70 | `seedsTreefyd` |
| 71 | `brewGrotesque` |
| 72 | `impregnatedLeather` |
| 73 | `fumeFilter` |
| 74 | `creeperHeart` |
| 75 | `brewLove` |
| 76 | `brewIce` |
| 77 | `brewDepths` |
| 78 | `icyNeedle` |
| 79 | `frozenHeart` |
| 80 | `infernalBlood` |
| 81 | `bookHerbology` |
| 82 | `entbranch` |
| 83 | `mysticunguent` → `witchery:mystic_unguent` |
| 84 | `doorKeyring` |
| 85 | `brewFrogsTongue` |
| 86 | `brewCursedLeaping` |
| 87 | `brewHitchcock` |
| 88 | `brewInfection` |
| 89 | `owletsWing` |
| 90 | `toeOfFrog` |
| 91 | `appleWormy` |
| 92 | `quartzSphere` → `witchery:quartz_sphere` |
| 93 | `happenstanceOil` → `witchery:happenstance_oil` |
| 94 | `seerStone` → `witchery:seer_stone` |
| 95 | `brewSleep` |
| 96 | `brewFlowingSpirit` |
| 97 | `brewWasting` |
| 98 | `sleepingApple` |
| 99 | `disturbedCotton` |
| 100 | `fancifulThread` |
| 101 | `tormentedTwine` |
| 102 | `goldenThread` |
| 103 | `mellifluousHunger` |
| 104 | `weaveIntensity` |
| 105 | `purifiedMilk` |
| 106 | `bookBiomes` |
| 107 | `bookWands` |
| 108 | `batBall` |
| 109 | `brewBats` |
| 110 | `charmDisruptedDreams` |
| 111 | `wormwood` |
| 112 | `subduedSpirit` → `witchery:subdued_spirit` |
| 113 | `focusedWill` |
| 114 | `condensedFear` |
| 115 | `brewHollowTears` |
| 116 | `brewSolidStone` |
| 117 | `brewSolidDirt` |
| 118 | `brewSolidSand` |
| 119 | `brewSolidSandstone` |
| 120 | `brewSolidErosion` |
| 121 | `infusionBase` → `witchery:infused_brew_base` |
| 122 | `brewSoaring` → `witchery:brew_of_soaring` |
| 123 | `brewGrave` |
| 124 | `brewRevealing` → `witchery:brew_of_revealing` (Kettle splash) |
| 125 | `brewSubstitution` |
| 126 | `brewCongealedSpirit` → `witchery:congealed_spirit` |
| 127 | `bookBurning` |
| 128 | `graveyardDust` |
| 129 | `binkyhead` → `witchery:binky_head` (places Binky) |
| 130 | `nullcatalyst` |
| 131 | `nullifiedleather` |
| 132 | `boltStake` |
| 133 | `boltAntiMagic` |
| 134 | `boltHoly` |
| 135 | `boltSplitting` |
| 136 | `brewSoulHunger` |
| 137 | `brewSoulAnguish` |
| 138 | `brewSoulFear` |
| 139 | `brewSoulTorment` |
| 140 | `contract` → `witchery:contract_of_ownership` |
| 141 | `contractTorment` → `witchery:contract_of_torment` |
| 142 | `contractBlaze` |
| 143 | `contractResistFire` |
| 144 | `contractEvaporate` |
| 145 | `contractFieryTouch` |
| 146 | `contractSmelting` |
| 147 | `waystoneCreatureBound` → `witchery:creature_bound_waystone` |
| 148 | `kobolditedust` → `witchery:koboldite_dust` |
| 149 | `kobolditenugget` → `witchery:koboldite_nugget` |
| 150 | `kobolditeingot` → `witchery:koboldite_ingot` |
| 151 | `pentacle` |
| 152 | `doorIce` |
| 153 | `annointingPaste` |
| 154 | `subduedSpiritVillage` → `witchery:subdued_village_spirit` |
| 155 | `boltSilver` |
| 156 | `wolfsbane` |
| 157 | `silverdust` → `witchery:silver_dust` |
| 158 | `muttonraw` |
| 159 | `muttoncooked` |
| 160 | `vbookPage` |
| 161 | `darkCloth` |
| 162 | `stake` |
| 163 | `warmBlood` |
| 164 | `lilithsBlood` |
| 165 | `heartofgold` → `witchery:heart_of_gold` (Fairest drop and Animal Attraction or Repulsion brew action) |

## Poppet Variants (`Witchery.Items.POPPET`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/item/ItemPoppet.java`

| Meta | Key | Display Name |
|---:|---|---|
| 0 | `poppet` | Poppet |
| 1 | `protectEarth` | Earth Protection Poppet → `witchery:earth_protection_poppet` |
| 2 | `protectWater` | Water Protection Poppet → `witchery:water_protection_poppet` |
| 3 | `protectFire` | Fire Protection Poppet → `witchery:fire_protection_poppet` |
| 4 | `protectStarvation` | Hunger Protection Poppet → `witchery:hunger_protection_poppet` |
| 5 | `protectTool` | Tool Protection Poppet → `witchery:tool_protection_poppet` |
| 6 | `protectDeath` | Death Protection Poppet → `witchery:death_protection_poppet` |
| 7 | `protectVoodoo` | Voodoo Protection Poppet → `witchery:voodoo_protection_poppet` |
| 8 | `voodoo` | Voodoo Poppet → `witchery:voodoo_poppet` |
| 9 | `vampiric` | Vampiric Poppet → `witchery:vampiric_poppet` |
| 10 | `protectPoppet` | Poppet Protection → `witchery:poppet_protection` |
| 11 | `protectArmor` | Armor Protection Poppet → `witchery:armor_protection_poppet` |

## Fluids
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryFluids.java`

| Field | Fluid Name | Class |
|---|---|---|
| `FLOWING_SPIRIT` | `witchery:fluidspirit` | `Fluid` |
| `HOLLOW_TEARS` | `witchery:hollowtears` | `Fluid` |
| `BREW` | `witchery:brew` | `FluidBrew` |
| `BREW_GAS` | `witchery:brewgas` | `Fluid` |
| `BREW_LIQUID` | `witchery:brewliquid` | `Fluid` |
| `DISEASE` | `witchery:fluiddisease` | `Fluid` |

### Bucket / Container Bindings
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryItems.java`

| Item Field | Item Name | Fluid Field |
|---|---|---|
| `BUCKET_FLOWINGSPIRIT` | `witchery:bucketspirit` | `FLOWING_SPIRIT` |
| `BUCKET_HOLLOWTEARS` | `witchery:buckethollowtears` | `HOLLOW_TEARS` |
| `BUCKET_BREW` | `witchery:bucketbrew` | `BREW` |

### 1.20.1 Fluid Scaffold Mapping (current)
Implementation anchors:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryFluids.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryBlocks.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryItems.java`

| Legacy Fluid ID | 1.20.1 Source/Flowing IDs | Linked Block ID | Linked Bucket ID |
|---|---|---|---|
| `fluidspirit` | `witchery:fluidspirit`, `witchery:flowing_fluidspirit` | `witchery:spiritflowing` (`LiquidBlock`) | `witchery:bucketspirit` (`BucketItem`) |
| `hollowtears` | `witchery:hollowtears`, `witchery:flowing_hollowtears` | `witchery:hollowtears` (`LiquidBlock`) | `witchery:buckethollowtears` (`BucketItem`) |
| `brew` | `witchery:brew`, `witchery:flowing_brew` | `witchery:brew` (`LiquidBlock`) | `witchery:bucketbrew` (`BucketItem`) |
| `brewgas` | `witchery:brewgas`, `witchery:flowing_brewgas` | `witchery:brewgas` (`BrewDispersalBlock`) | _(none)_ |
| `brewliquid` | `witchery:brewliquid`, `witchery:flowing_brewliquid` | `witchery:brewliquid` (`BrewDispersalBlock`) | _(none)_ |
| `fluiddisease` | `witchery:fluiddisease`, `witchery:flowing_fluiddisease` | `witchery:disease` (`LiquidBlock`) | _(none)_ |

## 1.20.1 Data Generation Scaffolding (current)
Implementation anchors:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/data/WitcheryDataGenerators.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/data/WitcheryBlockTagsProvider.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/data/WitcheryLootTableProvider.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/data/WitcheryBlockLoot.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/data/WitcheryRecipeProvider.java`

Current provider coverage:
- `GatherDataEvent` registration: server-side data providers only (breadth scaffold).
- Tags: writes `witchery:legacy_blocks` containing all entries in `WitcheryBlocks.LEGACY_BLOCKS`.
- Loot: generates self-drop loot tables for blocks that currently resolve to an item (`block.asItem() != AIR`).
- Recipes: stub provider currently emits no recipes (planned depth-phase migration).

## 1.20.1 Spawn Egg Scaffolding (current)
Implementation anchor:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryItems.java`

Behavior:
- generates one placeholder `ForgeSpawnEggItem` per legacy entity placeholder entry in `LegacyRegistryData.ENTITIES` (`55` total).
- registry name pattern: `<normalized_entity_name>_spawn_egg`.
- spawn eggs are included in the creative-tab aggregation (`WitcheryItems.allForCreativeTab`).

Examples:
| Legacy Entity ID | Spawn Egg Item ID |
|---|---|
| `demon` | `witchery:demon_spawn_egg` |
| `hornedHuntsman` | `witchery:hornedhuntsman_spawn_egg` |
| `illusionZombie` | `witchery:illusionzombie_spawn_egg` |
| `wingedmonkey` | `witchery:wingedmonkey_spawn_egg` |

## Entity Registry (`WitcheryEntities`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryEntities.java`

| ID | Field | Class | Legacy Name |
|---:|---|---|---|
| 92 | `DEMON` | `EntityDemon` | `demon` |
| 93 | `BROOM` | `EntityBroom` | `broom` (rideable vehicle, hue, damage and recovery) |
| 94 | `BREW` | `EntityWitchProjectile` | `brew` |
| 95 | `SPECTRAL_FAMILIAR` | `EntityFamiliar` | `familiar` |
| 96 | `MANDRAKE` | `EntityMandrake` | `mandrake` |
| 97 | `TREEFYD` | `EntityTreefyd` | `treefyd` |
| 98 | `HUNTSMAN` | `EntityHornedHuntsman` | `hornedHuntsman` → `witchery:hornedhuntsman` (Horn of the Hunt, entrance, boss combat and rank-five trial) |
| 99 | `SPELL` | `EntitySpellEffect` | `spellEffect` → `witchery:spelleffect` |
| 100 | `ENT` | `EntityEnt` | `ent` (Witchwood harvest awakening, legacy `Owner` field retained) |
| 101 | `ILLUSION_CREEPER` | `EntityIllusionCreeper` | `illusionCreeper` |
| 102 | `ILLUSION_SPIDER` | `EntityIllusionSpider` | `illusionSpider` |
| 103 | `ILLUSION_ZOMBIE` | `EntityIllusionZombie` | `illusionZombie` |
| 104 | `OWL` | `EntityOwl` | `owl` (Mutating Sprig, taming, feathers, Waystone delivery, Infernal power 24) |
| 105 | `TOAD` | `EntityToad` | `toad` (Mutating Sprig, taming, amphibious behavior, Infernal power 25) |
| 106 | `CAT_FAMILIAR` | `EntityWitchCat` | `cat` |
| 107 | `LOUSE` | `EntityParasyticLouse` | `louse` |
| 108 | `EYE` | `EntityEye` | `eye` |
| 109 | `BABA_YAGA` | `EntityBabaYaga` | `babayaga` |
| 110 | `COVEN_WITCH` | `EntityCovenWitch` | `covenwitch` (spawn, appearance, recruitment, quests and membership restored) |
| 111 | `PLAYER_CORPSE` | `EntityCorpse` | `corpse` |
| 112 | `NIGHTMARE` | `EntityNightmare` | `nightmare` |
| 113 | `SPECTRE` | `EntitySpectre` | `spectre` |
| 114 | `POLTERGEIST` | `EntityPoltergeist` | `poltergeist` |
| 115 | `BANSHEE` | `EntityBanshee` | `banshee` |
| 116 | `SPIRIT` | `EntitySpirit` | `spirit` (Dream spawn, focus attraction, loot, structure guide) |
| 117 | `DEATH` | `EntityDeath` | `death` (hidden fetish rite, boss combat and reward drops) |
| 118 | `CROSSBOW_BOLT` | `EntityBolt` | `bolt` |
| 119 | `WITCH_HUNTER` | `EntityWitchHunter` | `witchhunter` |
| 120 | `BINKY_HORSE` | `EntityDeathsHorse` | `deathhorse` (persistent tame mount, saddle and reusable skull) |
| 121 | `LORD_OF_TORMENT` | `EntityLordOfTorment` | `lordoftorment` |
| 122 | `SOULFIRE` | `EntitySoulfire` | `soulfire` |
| 123 | `IMP` | `EntityImp` | `imp` (rite, ownership, gift progression, and power restored) |
| 124 | `DARK_MARK` | `EntityDarkMark` | `darkmark` |
| 125 | `MINDRAKE` | `EntityMindrake` | `mindrake` |
| 126 | `GOBLIN` | `EntityGoblin` | `goblin` |
| 127 | `GOBLIN_MOG` | `EntityGoblinMog` | `goblinmog` |
| 128 | `GOBLIN_GULG` | `EntityGoblinGulg` | `goblingulg` |
| 129 | `BREW2` | `EntityBrew` | `brew2` |
| 130 | `ITEM_WAYSTONE` | `EntityItemWaystone` | `item` (legacy dropped Waystone, item subtype and age) |
| 131 | `DROPLET` | `EntityDroplet` | `droplet` |
| 132 | `SPLATTER` | `EntitySplatter` | `splatter` |
| 133 | `LEONARD` | `EntityLeonard` | `leonard` (cauldron summon, spells, shield and Urn loot) |
| 134 | `LOST_SOUL` | `EntityLostSoul` | `lostsoul` (three vulnerabilities, legacy color and expiry) |
| 135 | `WOLFMAN` | `EntityWolfman` | `wolfman` |
| 136 | `HELLHOUND` | `EntityHellhound` | `hellhound` |
| 137 | `WERE_VILLAGER` | `EntityVillagerWere` | `werevillager` |
| 138 | `VILLAGE_GUARD` | `EntityVillageGuard` | `villageguard` |
| 139 | `VAMPIRE_VILLAGER` | `EntityVampire` | `vampire` |
| 140 | `GRENADE` | `EntityGrenade` | `grenade` (Sun and Duplicate Grenades, saved `Mode`, `Impacted`, and `Remaining`) |
| 141 | `LILITH` | `EntityLilith` | `lilith` |
| 142 | `FOLLOWER` | `EntityFollower` | `follower` (Elle, four named Fairest variants, and type-five duplicate with saved `Skin` and `TicksToLive`) |
| 143 | `WINGED_MONKEY` | `EntityWingedMonkey` | `wingedmonkey` |
| 144 | `ATTACK_BAT` | `EntityAttackBat` | `attackbat` |
| 145 | `MIRROR_FACE` | `EntityMirrorFace` | `mirrorface` (inhabited Mirror conversation and saved lifetime) |
| 146 | `REFLECTION` | `EntityReflection` | `reflection` (copies armor, weapons, effects, disguise, and Wolfman form) |

## Potions / Effects (`WitcheryPotions`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/brewing/potions/WitcheryPotions.java`

| Field | Legacy Key | Class |
|---|---|---|
| `PARALYSED` | `witchery:potion.paralysed` | `PotionParalysis` |
| `WRAPPED_IN_VINE` | `witchery:potion.wrappedinvine` | `PotionWrappedInVine` |
| `SPIKED` | `witchery:potion.spiked` | `PotionSpiked` |
| `SPROUTING` | `witchery:potion.sprouting` | `PotionSprouting` |
| `GROTESQUE` | `witchery:potion.grotesque` | `PotionGrotesque` |
| `LOVE` | `witchery:potion.love` | `PotionLove` |
| `SUN_ALLERGY` | `witchery:potion.allergysun` | `PotionSunAllergy` |
| `CHILLED` | `witchery:potion.chilled` | `PotionChilled` |
| `SNOW_TRAIL` | `witchery:potion.snowtrail` | `PotionSnowTrail` |
| `FLOATING` | `witchery:potion.floating` | `PotionFloating` |
| `NETHER_BOUND` | `witchery:potion.hellishaura` | `PotionHellishAura` |
| `BREWING_EXPERT` | `witchery:potion.brewingexpertise` | `PotionBrewingExpertise` |
| `DOUBLE_JUMP` | `witchery:potion.doublejump` | `PotionBase` |
| `FEATHER_FALL` | `witchery:potion.featherfall` | `PotionFeatherFall` |
| `DARKNESS_ALLERGY` | `witchery:potion.allergydark` | `PotionDarknessAllergy` |
| `REINCARNATE` | `witchery:potion.reincarnate` | `PotionReincarnate` |
| `INSANITY` | `witchery:potion.insane` | `PotionInsanity` |
| `KEEP_INVENTORY` | `witchery:potion.keepinventory` | `PotionKeepInventory` |
| `SINKING` | `witchery:potion.sinking` | `PotionSinking` |
| `OVERHEATING` | `witchery:potion.overheating` | `PotionOverheating` |
| `WAKING_NIGHTMARE` | `witchery:potion.wakingnightmare` | `PotionWakingNightmare` |
| `QUEASY` | `witchery:potion.queasy` | `PotionQueasy` |
| `SWIMMING` | `witchery:potion.swimming` | `PotionSwimming` |
| `RESIZING` | `witchery:potion.resizing` | `PotionResizing` |
| `COLORFUL` | `witchery:potion.colorful` | `PotionColorful` |
| `ENDER_INHIBITION` | `witchery:potion.enderinhibition` | `PotionEnderInhibition` |
| `ILL_FITTING` | `witchery:potion.illfitting` | `PotionIllFitting` |
| `VOLATILITY` | `witchery:potion.volatility` | `PotionVolatility` |
| `ENSLAVED` | `witchery:potion.enslaved` | `PotionEnslaved` |
| `MORTAL_COIL` | `witchery:potion.mortalcoil` | `PotionMortalCoil` |
| `ABSORB_MAGIC` | `witchery:potion.absorbmagic` | `PotionAbsorbMagic` |
| `POISON_WEAPONS` | `witchery:potion.poisonweapons` | `PotionPoisonWeapons` |
| `REFLECT_PROJECTILES` | `witchery:potion.reflectprojectiles` | `PotionReflectProjectiles` |
| `REFLECT_DAMAGE` | `witchery:potion.reflectdamage` | `PotionReflectDamage` |
| `ATTRACT_PROJECTILES` | `witchery:potion.attractprojectiles` | `PotionAttractProjectiles` |
| `REPELL_ATTACKER` | `witchery:potion.repellattacker` | `PotionRepellAttacker` |
| `STOUT_BELLY` | `witchery:potion.stoutbelly` | `PotionStoutBelly` |
| `FEEL_NO_PAIN` | `witchery:potion.feelnopain` | `PotionFeelNoPain` |
| `GAS_MASK` | `witchery:potion.gasmask` | `PotionGasMask` |
| `DISEASED` | `witchery:potion.diseased` | `PotionDiseased` |
| `FORTUNE` | `witchery:potion.fortune` | `PotionFortune` |
| `WORSHIP` | `witchery:potion.worship` | `PotionWorship` |
| `KEEP_EFFECTS` | `witchery:potion.keepeffects` | `PotionKeepEffectsOnDeath` |
| `WOLFSBANE` | `witchery:potion.wolfsbane` | `PotionBase` |

## Spell Symbol Effect Registry (`EffectRegistry`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/infusion/infusions/symbols/EffectRegistry.java`

| ID | Key |
|---:|---|
| 1 | `witchery.pott.accio` |
| 2 | `witchery.pott.aguamenti` |
| 3 | `witchery.pott.alohomora` |
| 4 | `witchery.pott.avadakedavra` |
| 5 | `witchery.pott.caveinimicum` |
| 6 | `witchery.pott.colloportus` |
| 8 | `witchery.pott.confundus` |
| 9 | `witchery.pott.crucio` |
| 10 | `witchery.pott.defodio` |
| 12 | `witchery.pott.ennervate` |
| 13 | `witchery.pott.episkey` |
| 15 | `witchery.pott.expelliarmus` |
| 16 | `witchery.pott.flagrate` |
| 17 | `witchery.pott.flipendo` |
| 19 | `witchery.pott.impedimenta` |
| 20 | `witchery.pott.imperio` |
| 21 | `witchery.pott.incendio` |
| 22 | `witchery.pott.lumos` |
| 23 | `witchery.pott.meteolojinxrecanto` |
| 26 | `witchery.pott.nox` |
| 31 | `witchery.pott.protego` |
| 36 | `witchery.pott.stupefy` |
| 39 | `witchery.pott.ignianima` |
| 40 | `witchery.pott.carnosadiem` |
| 41 | `witchery.pott.morsmordre` |
| 42 | `witchery.pott.tormentum` |
| 43 | `witchery.pott.leonard1` |
| 44 | `witchery.pott.leonard2` |
| 45 | `witchery.pott.leonard3` |
| 46 | `witchery.pott.leonard4` |
| 47 | `witchery.pott.attraho` |

## Ritual Registry (`RiteRegistry`)

The modern `witchery:item_rite` recipe type covers item-output rites at the Heart Glyph. Legacy rite 3, the Rite of Charging, is implemented as `witchery:rite_of_charging`. It validates the original 16- and 28-glyph white rings, consumes its five offerings in steps, and draws 2,000 power from the nearest altar.
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryRecipes.java`

| Rite ID | Book Entry | Unlocalized Name |
|---:|---:|---|
| 1 | 0 | `witchery.rite.bindcircle` |
| 2 | 1 | `witchery.rite.bindwaystone` |
| 3 | 3 | `witchery.rite.chargestone` |
| 4 | 4 | `witchery.rite.infusionrecharge` |
| 5 | 5 | `witchery.rite.teleporttowaystone` |
| 6 | 6 | `witchery.rite.teleportentity` |
| 7 | 7 | `witchery.rite.teleportironore` |
| 8 | 8 | `witchery.rite.protection` |
| 9 | 9 | `witchery.rite.imprisonment` |
| 10 | 10 | `witchery.rite.barrier` |
| 11 | 11 | `witchery.rite.barrierlarge` |
| 12 | 12 | `witchery.rite.barrierportable` |
| 13 | 13 | `witchery.rite.volcano` |
| 14 | 14 | `witchery.rite.storm` |
| 15 | 15 | `witchery.rite.stormlarge` |
| 16 | 16 | `witchery.rite.stormportable` |
| 17 | 17 | `witchery.rite.eclipse` |
| 18 | 18 | `witchery.rite.eclipseportable` |
| 19 | 19 | `witchery.rite.partearth` |
| 20 | 20 | `witchery.rite.raiseearth` |
| 21 | 23 | `witchery.rite.banishdemonportable` |
| 22 | 24 | `witchery.rite.banishdemon` |
| 23 | 25 | `witchery.rite.summondemon` |
| 24 | 26 | `witchery.rite.summondemonexpensive` |
| 25 | 27 | `witchery.rite.summonwither` |
| 26 | 28 | `witchery.rite.summonwitherexpensive` |
| 27 | 31 | `witchery.rite.infusionlight` |
| 28 | 32 | `witchery.rite.infusionearth` |
| 29 | 33 | `witchery.rite.infusionender` |
| 30 | 34 | `witchery.rite.infusionhell` |
| 31 | 35 | `witchery.rite.infusionsky` |
| 32 | 36 | `witchery.rite.necrostone` |
| 33 | 30 | `witchery.rite.summonfamiliar` |
| 34 | 2 | `witchery.rite.bindwaystonecopy` |
| 35 | 21 | `witchery.rite.fertility` |
| 36 | 22 | `witchery.rite.fertilityportable` |
| 37 | 37 | `witchery.rite.curseblight` |
| 38 | 38 | `witchery.rite.curseblindness` |
| 39 | 39 | `witchery.rite.hellonearth` |
| 40 | 29 | `witchery.rite.summonwitch` |
| 41 | 1 | `witchery.rite.bindwaystoneportable` |
| 42 | 2 | `witchery.rite.bindwaystonecopyportable` |
| 43 | 22 | `witchery.rite.naturespower` |
| 44 | 36 | `witchery.rite.priorincarnation` |
| 45 | 0 | `witchery.rite.bindcircleportable` |
| 46 | 20 | `witchery.rite.raiseearth` |
| 47 | 20 | `witchery.rite.raiseearth` |
| 48 | 48 | `witchery.rite.cursecreature1` |
| 49 | 49 | `witchery.rite.removecurse1` |
| 50 | 35 | `witchery.rite.infusiontree` |
| 51 | 20 | `witchery.rite.cookfood` |
| 52 | 48 | `witchery.rite.curseinsanity1` |
| 53 | 49 | `witchery.rite.removeinsanity1` |
| 54 | 1 | `witchery.rite.bindfamiliar` |
| 55 | 30 | `witchery.rite.callfamiliar` |
| 56 | 50 | `witchery.rite.corruptvoodooprotection` |
| 57 | 35 | `witchery.rite.infusionfuture` |
| 58 | 20 | `witchery.rite.cookfood` |
| 59 | 48 | `witchery.rite.cursesinking1` |
| 60 | 49 | `witchery.rite.removesinking1` |
| 61 | 35 | `witchery.rite.infusionseerstone` |
| 62 | 48 | `witchery.rite.curseoverheating` |
| 63 | 49 | `witchery.rite.cureoverheating` |
| 64 | 22 | `witchery.rite.climatechange` |
| 65 | 12 | `witchery.rite.iceshell` |
| 66 | 38 | `witchery.rite.rainoffrogs` |
| 67 | 4 | `witchery.rite.glyphictransform` |
| 68 | 7 | `witchery.rite.callbeasts` |
| 69 | 7 | `witchery.rite.manifest` |
| 70 | 22 | `witchery.rite.forestation` |
| 71 | 22 | `witchery.rite.forestation` |
| 72 | 22 | `witchery.rite.forestation` |
| 73 | 22 | `witchery.rite.forestation` |
| 74 | 22 | `witchery.rite.forestation` |
| 75 | 22 | `witchery.rite.forestation` |
| 76 | 22 | `witchery.rite.forestation` |
| 77 | 13 | `witchery.rite.volcano` |
| 78 | 48 | `witchery.rite.cursenightmare` |
| 79 | 49 | `witchery.rite.curenightmare` |
| 80 | 35 | `witchery.rite.infusebrewsoaring` |
| 81 | 35 | `witchery.rite.infusebrewgrave` |
| 82 | 36 | `witchery.rite.spectralstone` |
| 83 | 1 | `witchery.rite.bindspectral` |
| 84 | 1 | `witchery.rite.bindfetish` |
| 85 | 1 | `witchery.rite.bindfetish` |
| 86 | 1 | `witchery.rite.bindfetish` |
| 87 | 26 | `witchery.rite.summonimp` |
| 88 | 1 | `witchery.rite.bindwaystonetoplayer` |
| 89 | 1 | `witchery.rite.bindwaystonetoplayer` |
| 90 | 1 | `witchery.rite.bindstatuetoplayer` |
| 91 | 5 | `witchery.rite.teleporttowaystone` |
| 92 | 48 | `witchery.rite.wolfcurse.book` |
| 93 | 49 | `witchery.rite.wolfcure.book` |
| 94 | 49 | `witchery.rite.vampirecure.book` |
| 95 | 35 | `witchery.rite.infusionmirror` |
| 96 | 28 | `witchery.rite.summonreflection` |

- Rites 6 and 7 are `witchery:rite_of_transposition_creature` and `witchery:rite_of_transposition_ores`. They use medium and large Otherwhere circles, respectively. The protocol 55 item-rite actions retain the required sacrifices and altar power. An active ore rite migrates legacy `Rituals`, `RitualStages` and `RitualCovens` and saves the completed layer as `OreDepth`.
- The Scarecrow, Trent Effigy and Witch's Ladder retain their block entity IDs. The legacy `EffectTypeID` and item `WITCSpiritEffect` identify the active spirit enhancement. Effect 1 applies to carried Voodoo Protection against a forced summon and Death Protection against powered bolts within 16 blocks. Effects 2 through 5 drive Sentinel, Screamer, Twister and Ghost Walker using the alarm's Taglock list. Effect 6 is the hidden Death encounter, checked last. It consumes five of each spectral undead species and creates no bound fetish.
- Rite 82 uses `witchery:rite_of_necromancy_spectral_stone`. It consumes the Necromantic Stone and produces one blank `witchery:spectralstone`. Protocol 56 carries the explicit `consume_necromantic_stone` field and the Spirit's synchronized color.
- Rite 67 uses `witchery:rite_of_glyphic_transformation`. It consumes Gypsum, an Arthana, and 1,000 altar power. The first color of fresh chalk among nearby drops selects the target color. The number of sticks of that color selects the ring size and one chalk is consumed. Protocol 62 carries the new action.
- Rite 64 uses `witchery:rite_of_climate_change`, the large white circle, and seven offerings without an altar charge. At least four coven witches are required. Four, five, or six witches give a radius of 16, 32, or 48 blocks. The fifth pulse consumes nearby stacks of recognized foci and Glowstone Dust. The last recognized focus selects the family. Dust selects the one-indexed biome variant and clamps to the available choices.
- The climate foci are Oak Sapling, Short Grass, Obsidian, Stone, Slimeball, Water Bucket, Cactus, Icy Needle, Jungle Sapling, Netherrack, Sand, Red Mushroom, and Skeleton Skull. Vanilla variants and current biome tags substitute for the old Forge BiomeDictionary. The magical family falls back to Dark Forest if the tag is empty. Cactus removes surface snow in range and clears rain and thunder. Icy Needle starts precipitation when the weather is clear.
- Climate Change rewrites saved four-block-wide biome columns across all heights and sends Minecraft's biome update to clients tracking the affected chunks. The Heart Glyph imports legacy rite 64 upkeep and keeps the glyph as the center. Protocol 65 adds the action. The server's `allowBiomeModificationRituals` setting defaults to true and can disable the rite.
- Rite 65 uses `witchery:rite_of_ice_shell`. It checks for two coven members and grows a Perpetual Ice shell to radius 8, 12, or 16 according to coven size. The Heart Glyph accepts its legacy upkeep stage. Protocol 64 adds the action. Frozen Heart keeps legacy ingredient subtype 79.
- Rites 70–76 use `witchery:forestation/<species>`. They accept oak, spruce, birch, jungle, rowan, alder, and hawthorn in legacy order. Rite 77 uses `witchery:rite_of_raising_a_volcano`. It scans to modern bedrock or the build floor and accepts a lava reservoir that meets the original neighboring-source test.
- Both landscape actions use the optional legacy location-bound Waystone. `Landscape` saves the current stage, target, and forest species when present. The Heart Glyph imports a supported legacy upkeep entry using `Rituals`, `RitualStages`, `RitualCovens`, and `Locations`. Protocol 63 adds the two actions.
- The two Wicker Bundle variants keep `bundle_type` and have `axis=x|y|z` for placement. Flint and Steel on a Bloodied Wicker Man burns its original pattern and summons a Huntsman at one quarter of full health.
- Rite 96 uses `witchery:rite_of_summoning_a_reflection`, the large Infernal circle, and 5,000 altar power. It accepts the original Mirror or the earlier modern `witchery:mirrorblock` item. The spawned Reflection saves `FreeSpawn` and can live outside the Mirror. Protocol 61 adds the rite action and the Reflection's synchronized skin and Wolfman state.

## Prediction Registry (`PredictionManager`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryRecipes.java`

| ID | Class | Weight | Key |
|---:|---|---:|---|
| 1 | `PredictionFight` | 13 | `witchery.prediction.zombie` |
| 2 | `PredictionArrow` | 13 | `witchery.prediction.arrowhit` |
| 3 | `PredictionFight` | 3 | `witchery.prediction.ent` |
| 4 | `PredictionFall` | 13 | `witchery.prediction.fall` |
| 5 | `PredictionMultiMine` | 8 | `witchery.prediction.iron` |
| 6 | `PredictionMultiMine` | 3 | `witchery.prediction.diamond` |
| 7 | `PredictionMultiMine` | 3 | `witchery.prediction.emerald` |
| 8 | `PredictionBuriedTreasure` | 2 | `witchery.prediction.treasure` |
| 9 | `PredictionFallInLove` | 2 | `witchery.prediction.love` |
| 10 | `PredictionFight` | 2 | `witchery.prediction.bababad` |
| 11 | `PredictionFight` | 2 | `witchery.prediction.babagood` |
| 12 | `PredictionFight` | 3 | `witchery.prediction.friend` |
| 13 | `PredictionRescue` | 13 | `witchery.prediction.rescued` |
| 14 | `PredictionRescue` | 13 | `witchery.prediction.rescued` |
| 15 | `PredictionWet` | 13 | `witchery.prediction.wet` |
| 16 | `PredictionNetherTrip` | 3 | `witchery.prediction.tothenether` |
| 17 | `PredictionMultiMine` | 13 | `witchery.prediction.coal` |

`Predictions` registers all 17 IDs with their legacy weights, one active outcome, and `WITCPredict.WITCPreList` entries with `WITCPreID` and `WITCPreTime`. `CrystalBallBlockEntity` retains `LastUsedTime` and the training rite grants `WITCFTeller`. The two Baba Yaga readings use the original `witchery:babayaga` entity and legacy `Owner` name. New allied visits also retain the UUID and visit age.

Mining predictions extend Forge block loot after the harvest. Deepslate and its matching ores extend the legacy stone rules. The treasure prediction places a chest with `minecraft:chests/abandoned_mineshaft`, the modern counterpart of `mineshaftCorridor`.

## Creature Power Registry
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryRecipes.java`

| ID | Power Class |
|---:|---|
| 1 | `CreaturePowerSpider` |
| 2 | `CreaturePowerSpider` |
| 3 | `CreaturePowerCreeper` |
| 4 | `CreaturePowerBat` |
| 5 | `CreaturePowerSquid` |
| 6 | `CreaturePowerGhast` |
| 7 | `CreaturePowerBlaze` |
| 8 | `CreaturePowerPigMan` |
| 9 | `CreaturePowerZombie` |
| 10 | `CreaturePowerSkeleton` |
| 11 | `CreaturePowerJump` |
| 12 | `CreaturePowerJump` |
| 13 | `CreaturePowerSpeed` |
| 14 | `CreaturePowerSpeed` |
| 15 | `CreaturePowerSpeed` |
| 16 | `CreaturePowerSpeed` |
| 17 | `CreaturePowerEnderman` |
| 18 | `CreaturePowerHeal` |
| 19 | `CreaturePowerHeal` |
| 20 | `CreaturePowerHeal` |
| 21 | `CreaturePowerHeal` |
| 22 | `CreaturePowerHeal` |
| 23 | `CreaturePowerHeal` |
| 24 | `CreaturePowerBat` |
| 25 | `CreaturePowerJump` |

## Infusion Registry (`Infusion.Registry`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryRecipes.java`

| ID | Class |
|---:|---|
| 1 | `InfusionLight` |
| 2 | `InfusionOverworld` |
| 3 | `InfusionOtherwhere` |
| 4 | `InfusionInfernal` |

All four infusion recipes, their personal charge and the HUD are implemented. The Witch's Hand uses `witchery:witchhand` and restores Otherwhere movement, Light's cloak and barriers, Overworld's terrain and metal powers, and Infernal minion control and creature powers. Its recall point survives serialization in `WITCRecallD`, `WITCRecallX`, `WITCRecallY` and `WITCRecallZ`. Its right-click cooldown keeps the `WITCCooldown` field. The `witchery:barrier` block retains `remainingTicks`, `blocksPlayers` and `owner` in its block entity, with a UUID for new placements. Infernal minions read `WITCEnslaverName` and save the owner UUID for new bindings. Creature powers retain `WITCBeastPower` and `WITCBeastPowerCharges`. Their projectiles use the migrated Rock and Thrown Web ingredients.

`witchery:poppetshelf` reads legacy `Items` and its nine `Slot` indices. It shares taglock matching and `WITCDamage` with handheld wards. A Forge block ticket replaces the old `poppetX`, `poppetY` and `poppetZ` ticket fields and keeps the owning block position in the dimension's `chunks.dat`.

The Vampiric Poppet keeps both legacy Taglock indices. The first tracks the protected player, and the second tracks the damage recipient. Player bindings read `WITCPlayer1` and `WITCPlayer2` with optional `WITCPlayerUUID1` and `WITCPlayerUUID2`. Creature bindings retain the `WITCEntityIDm`, `WITCEntityIDl` and `WITCDisplayName` fields for each index.

`witchery:grassper` reads its single legacy `Items` entry at `Slot:0b`. The rite heart retains legacy `Sacrifices` item entries and writes `GrassperPos` for offerings it takes from a Grassper. Critter Snare item damage 1 through 4 becomes the `critter` block state. The older bat subtype 9 also sets `stocked`, which restores `WitcheryShopStock` when that bat is released.

The Owl reads `FeatherColor` and the Toad reads `SkinColor`. Both retain `Familiar` and `SuicideIn` and use vanilla tamed ownership. The Toad also reads `Poisonous`. The Owl keeps its carried item in its main hand equipment.

The Broom uses `BrushColor` on the vehicle and enchanted item. The vehicle reads legacy plain-text `CustomName` and saves the modern component name. An older port's `witchery:broom_spawn_egg` can place the vehicle. The Infused Brew of Soaring uses `WITCInfusedBrewType:1` and `WITCInfusedBrewStart` in the player record.

Bound Waystones retain `PosX`, `PosY`, `PosZ`, `PosD` and `NameD`. New bindings add the namespaced `Dimension` field. Old Waystones resolve the three vanilla dimensions and Witchery's default legacy IDs `-37` for Dream, `-38` for Torment and `-39` for Mirror. Creature-Bound Waystones share the first Taglock signature fields.

Owls retain `WITCWaypoint` and their launch coordinates in `HomeX`, `HomeY` and `HomeZ` while delivering. `ReturningFromDelivery` preserves the return leg on modern saves.

## Recipe System Registries

The modern datapack contains the altar's shaped recipe. Rowan logs match untagged stacks and stacks with explicit rowan state. Other Witchery wood variants and non-water potions are excluded.
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryRecipes.java`

- `RecipeSorter.register`: 7 custom recipes
- `KettleRecipes.instance().addRecipe`: 39
- `DistilleryRecipes.instance().addRecipe`: 9
- `SpinningRecipes.instance().addRecipe`: 4

### Custom RecipeSorter Keys
- `witchery:bindpoppet`
- `witchery:addpotion`
- `witchery:repair`
- `witchery:addcolor`
- `witchery:addkeys`
- `witchery:attachtaglock`
- `witchery:biomecopy`

## Brew Registry
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/brewing/WitcheryBrewRegistry.java`

- `WitcheryBrewRegistry.INSTANCE` has `158` `register(...)` entries.
- `witchery:brewbottle` uses an ordered `Items` list for drinkable and instant splash effects. Ingredient stacks inside `Items` support the named legacy `witchery:ingredient` plus `Damage` format. The `Splash` flag can be derived from known dispersal ingredients when absent. Native numeric 1.7.10 item IDs still require the save conversion workflow.
- `PotionBottling` retains the original player-data key. Successful bottling can grant skill according to the batch's used capacity. The Seer Stone reports it. Successful withdrawal charges the closest altar once per portion.
- Bat Wool and Wormwood select gas or liquid dispersal. Both original block entity IDs resolve to brew fluid cells and can read legacy effect, tint, duration, expansion and thrower data. Current saves also retain their age.
- Zombie Head selects triggered dispersal. The original stone and oak buttons, lever, oak, stone and snow plates, and oak door have cursed counterparts with the original block entity IDs. Cursed controls persist the ordered mixture, tint, charge count, duration, expansion and thrower. Named `minecraft:skull` metadata 2 decodes to the Zombie Head ingredient.
- Tongue of Dog adds 250 base power and starts the item curse with a full triggered brew. The cauldron retains `RitualTriggered` and `RitualTicks` during the warmup. A charged item stores its ordered recipe and remaining charges under the original `WITCCurse` keys `Curse`, `Count` and `Delay`. A named 1.7.10 cauldron's root `FluidName`, `Amount`, `TicksHeated` and nested fluid `Tag` map to the 1.20.1 tank and ingredient data.
- Tongue of Dog also casts instant, gas and liquid dispersals. A Bound Waystone adds 100 base power and supplies the first destination. Coal adds the one-level Extinguish action. Brew rain spawns `witchery:droplet` with saved `Brew` data. The 1.20.1 port keeps the earlier projectile spawn-egg item ID for save compatibility.
- The 26 built-in fixed recipes use `witchery:cauldron_ritual`. The legacy rules retain subset matching, firing priority, and ingredient power. Brew Fuel retains the original item ID and maps metadata 0–3 to `FuelTier`. Endless Water retains its item ID and maps named legacy top-level `Damage` to modern durability. The Player Compass keeps the original Taglock keys and the supplied 33-frame sprite set.
- A `witchery:taglockkit` costs 1,000 base power to cast a brew on the bound creature or player. The cauldron saves the ordered Taglock and warmup in `Ingredients`, `RitualTriggered`, and `RitualTicks`. Both `decursedirected` and `decurseteleport` keep their original block and block entity IDs and save `WITCPlacer` and `Killers`. The new horizontal block states use network protocol 54.
- Legacy terrain brew subtypes 57, 58, 59, and 61 use named `brew_of_*` items. The Kettle recipes are under `data/witchery/recipes/kettle/` and their projectiles dispatch to `BrewTerrain`.
- Plant Mine metadata 0 through 11 uses `BlockStateTag.variant` on the shared item and `variant` in the placed state. It encodes poppy, dandelion, or shrub in groups of four, ordered Webs, Ink, Thorns, and Sprouting. Legacy stack conversion retains other item tags.
- Kettle `bottling_familiar` accepts `cat`, `toad` and `owl`. Finished batches save `FinishedRecipe` alongside the result. The original 85, 86, 87, 97, 108 and 109 ingredient subtypes map to Frog's Tongue, Cursed Leaping, Hitchcock, Wasting, Bat Ball and Bats.
- Revealing uses `witchery:kettle/brew_of_revealing` for the fixed splash. Named vanilla potion damage 8198 imports as regular Night Vision. Odour of Purity adds Revealing to cauldron mixtures. Heart of Gold adds Animal Attraction and Fermented Spider Eye inverts it to Animal Repulsion. The ordered `Items` list retains the action order and inversion.
- Legacy infused brew subtype 123 maps to `witchery:brew_of_the_grave`. `InfusedBrews` uses the original type, start and Grave cooldown keys.
- Icy Needle subtype 78 adds Cold for two effect levels and 200 altar power. It applies three minutes of base Chilled and freezes connected water at a block impact. The range starts at two blocks and increases by two for each strength boost.
- Frozen Heart subtype 79 adds Ice Shell for five effect levels and 500 altar power. It applies ten seconds of base Chilled to susceptible entities. A direct hit or supported block impact forms Perpetual Ice. A glancing splash applies only the shorter Chilled effect. The shell uses the rite's original outline geometry and clears water, brew gas, and brew liquid inside its radius. The `witchery:ice_shell_resistant` entity tag includes blazes, golems, demons, ents, and boss types. Resistant targets receive water at their feet when the block is replaceable and the dimension permits water.
- `witchery:potion.chilled` keeps the original movement modifier, reduction to incoming fire damage, and periodic magic damage for blazes or at level III and above. Both cold brews keep the strength, duration, and particle suppression modifiers that preceded the ingredient.

## Network Packets (`PacketPipeline`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/network/PacketPipeline.java`

| ID | Packet Class | Side |
|---:|---|---|
| 1 | `PacketBrewPrepared` | `SERVER` |
| 2 | `PacketParticles` | `CLIENT` |
| 3 | `PacketCamPos` | `CLIENT` |
| 4 | `PacketItemUpdate` | `SERVER` |
| 5 | `PacketPlayerStyle` | `CLIENT` |
| 6 | `PacketPlayerSync` | `CLIENT` |
| 7 | `PacketPushTarget` | `CLIENT` |
| 8 | `PacketSound` | `CLIENT` |
| 9 | `PacketSpellPrepared` | `SERVER` |
| 10 | `PacketClearFallDamage` | `SERVER` |
| 11 | `PacketSyncEntitySize` | `CLIENT` |
| 12 | `PacketSyncMarkupBook` | `SERVER` |
| 13 | `PacketExtendedPlayerSync` | `CLIENT` |
| 14 | `PacketHowl` | `SERVER` |
| 15 | `PacketExtendedVillagerSync` | `CLIENT` |
| 16 | `PacketSelectPlayerAbility` | `SERVER` |
| 17 | `PacketExtendedEntityRequestSyncToClient` | `SERVER` |
| 18 | `PacketPartialExtendedPlayerSync` | `CLIENT` |
| 19 | `PacketSetClientPlayerFacing` | `CLIENT` |

### 1.20.1 Network Skeleton (current)
Implementation anchors:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/network/WitcheryNetwork.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/LegacyRegistryData.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/Witchery.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/client/packet/WitcheryClientPacketHandlers.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/client/WitcheryClientCameraState.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/client/event/WitcheryClientCameraHooks.java`

Current scaffold behavior:
- Uses one `SimpleChannel` (`witchery:main`) with protocol version `"40"`. The extended player payload carries vampire rank, blood power, blood reserve, quest progress, vision, ultimate choice and charges, the transient selected power and the saved creature form. `VampireQuestChunks` preserves the original packed `Location` values and records a modern dimension ID. Player style sync updates the other clients that track a transformed player. Vampire packet intents 15 through 17 select or activate a power and query or display a nearby target's blood. Markup book intent 12 saves an unlocked chapter from the held journal and raises the reader's saved cap. The infusion status includes the active infused brew and remaining duration, and the Kettle recipe codec carries the optional bottling familiar.
- Crystal Ball viewing sends the existing `cam_pos` message for the transient `witchery:eye` or the living subject of a Taglock Kit. Server chunk and entity tracking follow the viewed entity, and the client centers terrain rendering on it. The observing player stays beside the Crystal Ball.
- The Mystic Branch recognizes gestures from server-side player rotations. Protocol 12 retires the prepared-spell packet while recording the old intent in `LegacyRegistryData.PACKETS`.
- Validates packet receive direction against legacy flow metadata and logs mismatches.
- Represents all legacy packet intents in `LegacyRegistryData.PACKETS` as migration metadata.
- Adds codec-backed payload + handler scaffolding for high-priority sync packets:
  - `player_sync` -> `(UUID playerId, int syncRevision, CompoundTag playerData)`
  - `extended_player_sync` -> `(UUID playerId, boolean initialized, int syncRevision, CompoundTag playerData)`
  - `partial_extended_player_sync` -> `(UUID playerId, int syncRevision, CompoundTag playerData)`
- Adds codec-backed payload + handler scaffolding for additional interaction packets:
  - `item_update` -> `(int slotIndex, int damageValue, int pageIndex)` (serverbound, aligned to legacy packet shape)
  - `sync_entity_size` -> `(int entityId, float width, float height)` (clientbound)
  - `set_client_player_facing` -> `(float yaw, float pitch)` (clientbound)
  - `cam_pos` -> `(boolean active, boolean updatePosition, int entityId)` (clientbound; aligned with legacy packet shape)
  - `push_target` -> `(int entityId, double velocityX, double velocityY, double velocityZ)` (clientbound)
  - `sound` -> `(String soundId, double x, double y, double z, float volume, float pitch)` (clientbound)
- Adds codec-backed payload + handler scaffolding for utility packets:
  - `particles` -> `(int particleEffectId, int soundEffectId, double x, double y, double z, double width, double height, int color)` (clientbound)
  - `player_style` -> `(String username, int grotesqueTicks, int nightmare, boolean ghost, int creatureType, int blood, String playerSkin)` (clientbound)
  - `sync_markup_book` -> `(int slot, String chapter)` (serverbound)
- Keeps explicit no-payload codec records + handlers for:
  - `clear_fall_damage` (serverbound)
  - `howl` (serverbound)
- Adds first functional clientbound behavior hooks:
  - `particles` now dispatches to client packet bridge with legacy `ParticleEffect`/`SoundEffect` id mapping, effect-count scaling, and colored spell particle support.
  - `player_sync`, `extended_player_sync`, and `partial_extended_player_sync` now dispatch to client packet bridge and hydrate staged capability sync fields from serialized `WitcheryPlayerData` snapshots, including stale-revision guards + persistent-tag mirrors.
  - `player_style` now dispatches to client packet bridge to apply staged style sync to target players by username.
  - `sound` now dispatches to client packet bridge to resolve legacy/modern ids and play local scaffold sound.
  - `push_target` now dispatches to client packet bridge to apply motion vectors to client entities.
  - `set_client_player_facing` now dispatches to client packet bridge to apply local player rotation.
  - `sync_entity_size` now dispatches to client packet bridge and stages width/height sync metadata on target entities.
  - `cam_pos` now dispatches to client packet bridge with legacy semantics (`active`, `updatePosition`, `entityId`) and stages camera-active/target data in `WitcheryClientCameraState`.
  - `WitcheryClientCameraHooks` now consumes staged camera state each client tick and applies scaffold camera-entity override/reset behavior (`setCameraEntity(target)` while active, fallback to local player when inactive/invalid target).
- Adds first functional serverbound behavior hooks:
  - `clear_fall_damage` now clears sender fall distance.
  - `item_update` now mutates inventory stack NBT `CurrentPage` with legacy slot/damage validation.
  - `sync_markup_book` saves the unlocked chapter to the held journal's `pageStack` and syncs the learned rank cap.
  - `item_update` and `howl` still stage data into `WitcheryPlayerData`, bump sync revision, and emit `player_sync` with serialized capability snapshot payload.
  - `howl` also mirrors minimal state into sender persistent tags.

Intent mapping used by scaffold:
| Legacy ID | Intent Key | Flow |
|---:|---|---|
| 1 | `brew_prepared` | `SERVERBOUND` |
| 2 | `particles` | `CLIENTBOUND` |
| 3 | `cam_pos` | `CLIENTBOUND` |
| 4 | `item_update` | `SERVERBOUND` |
| 5 | `player_style` | `CLIENTBOUND` |
| 6 | `player_sync` | `CLIENTBOUND` |
| 7 | `push_target` | `CLIENTBOUND` |
| 8 | `sound` | `CLIENTBOUND` |
| 9 | `spell_prepared` | `SERVERBOUND` |
| 10 | `clear_fall_damage` | `SERVERBOUND` |
| 11 | `sync_entity_size` | `CLIENTBOUND` |
| 12 | `sync_markup_book` | `SERVERBOUND` |
| 13 | `extended_player_sync` | `CLIENTBOUND` |
| 14 | `howl` | `SERVERBOUND` |
| 15 | `extended_villager_sync` | `CLIENTBOUND` |
| 16 | `select_player_ability` | `SERVERBOUND` |
| 17 | `extended_entity_request_sync_to_client` | `SERVERBOUND` |
| 18 | `partial_extended_player_sync` | `CLIENTBOUND` |
| 19 | `set_client_player_facing` | `CLIENTBOUND` |

## GUI IDs
Sources:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/common/CommonProxy.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/client/ClientProxy.java`

| ID | Server | Client |
|---:|---|---|
| 0 | `null` | `BlockAltarGUI` |
| 1 | `null` | `GuiScreenWitchcraftBook` |
| 2 | `ContainerWitchesOven` | `BlockWitchesOvenGUI` |
| 3 | `ContainerDistillery` | `BlockDistilleryGUI` |
| 4 | `ContainerSpinningWheel` | `BlockSpinningWheelGUI` |
| 5 | `ContainerBrewBag` | `ItemBrewBagGUI` |
| 6 | `null` | `GuiScreenBiomeBook` |
| 7 | `null` | `GuiScreenMarkupBook` |
| 8 | `ContainerLeonardsUrn` | `ItemLeonardsUrnGUI` |

### 1.20.1 GUI/Menu Scaffold (current)
Implementation anchors:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/LegacyRegistryData.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryMenus.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/menu/LegacyPlaceholderMenu.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/client/screen/LegacyPlaceholderScreen.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/client/WitcheryClient.java`

Current scaffold behavior:
- menu metadata now tracks both legacy GUI ID and normalized menu key (`LegacyMenuIntent`).
- every legacy GUI entry (`0..8`) registers a dedicated menu type keyed to its legacy menu name.
- GUI ID 2 opens `WitchesOvenMenu` with five slots and synchronized burn and cook progress. Client setup binds `WitchesOvenScreen` for this menu and uses `LegacyPlaceholderScreen` for the other eight.

ID mapping used by scaffold:
| Legacy GUI ID | Key | Menu Type ID |
|---:|---|---|
| 0 | `altar` | `witchery:altar` |
| 1 | `witchcraft_book` | `witchery:witchcraft_book` |
| 2 | `witches_oven` | `witchery:witches_oven` |
| 3 | `distillery` | `witchery:distillery` |
| 4 | `spinning_wheel` | `witchery:spinning_wheel` |
| 5 | `brew_bag` | `witchery:brew_bag` |
| 6 | `biome_book` | `witchery:biome_book` |
| 7 | `markup_book` | `witchery:markup_book` |
| 8 | `leonards_urn` | `witchery:leonards_urn` (original layout backed by item inventory) |

Status:
- The oven has a live menu and screen implementation. Graphical client validation is pending.
- The other eight menus still need container and screen behavior.

## Dimensions
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/Witchery.java`

- Dream: `WorldProviderDreamWorld`
- Torment: `WorldProviderTorment`
- Mirror: `WorldProviderMirror`

### 1.20.1 Dimension Key Scaffold (current)
Implementation anchor:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/world/WitcheryDimensions.java`

| Legacy Dimension | Level Key | LevelStem Key | DimensionType Key |
|---|---|---|---|
| Dream | `witchery:dream` | `witchery:dream` | `witchery:dream` |
| Torment | `witchery:torment` | `witchery:torment` | `witchery:torment` |
| Mirror | `witchery:mirror` | `witchery:mirror` | `witchery:mirror` |

### 1.20.1 Dimension Datapack Scaffold (current)
Implementation anchors:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension_type/dream.json`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension_type/torment.json`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension_type/mirror.json`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension/dream.json`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension/torment.json`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/resources/data/witchery/dimension/mirror.json`

### 1.20.1 Dimension Travel
Implementation anchors:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/world/WitcheryDimensionTravelHooks.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/event/WitcheryEventHooks.java`

Current travel behavior:
- `TormentTravel` processes collision requests and chooses one of six generated entrances or the Overworld respawn point. It retains the numeric legacy `PlayerPersisted.WITCForceTorment` and `WITCForceTormentLevel` fields. `witchery_torment_maze` saves which footprint a world uses.
- Forge event wiring observes both travel attempts and completed dimension transitions for Witchery dimensions.
- `DreamTravel` handles Brew of Sleeping and waking in the server player tick. It records both inventories, health, hunger, the Overworld body coordinates and the nightmare flag under `PlayerPersisted.WITCSpiritWorld`, and retains `witchery:corpse` for the sleeping body.
- `witchery:spiritportal` uses a snow frame in the Dream and manifests only a spirit walker with remaining rite time. `WITCManifested` and `WITCManifestDuration` persist in the Forge player tag. The portal retains its block ID and adds a horizontal `axis` property.
- `witchery:tormentportal` uses `TormentPortalBlock`. It retains the block ID and adds `axis=x` and `axis=z` orientations.
- Brew of Flowing Spirit uses legacy ingredient damage 96. The Dream-only brewing restriction uses the optional `dimension` field on the datapack recipe and synchronizes it to clients. Focused Will, Condensed Fear, and Brew of Hollow Tears use damage values 113–115.
- `MirrorTravel` opens a linked chamber when the player walks into a correctly mounted `witchery:mirrorblock`. A generated `witchery:mirrorblock2` links back to the exterior. The room is allocated in saved data and the two mirrors store their connection and Reflection defeat state.

Status:
- resource keys and breadth datapack scaffold are now present for Dream/Torment/Mirror.
- Dream and Nightmare cotton drops are available from naturally generated Somnian Cotton. The ordinary Nightmare now uses the original `witchery:nightmare` key with a dedicated entity and renderer. Dreamweaves use `witchery:dreamcatcher` and preserve the five original `WITCWeaveID` values. Demonic nightmares add fireballs overhead and can summon `witchery:demon`. The Waking Nightmare curse and status effect use the same entity outside the Dream. The Imp's contract and Torment's boss encounter are pending.
- `witchery:demon` can also be summoned by two large Infernal rites in the Overworld. The entity persists bargains, remaining stock and whether it came from a rite. Demon Heart and Infernal Blood use legacy ingredient damage values 23 and 80. The heart block retains `witchery:demonheart` and drops the canonical `witchery:demon_heart`. The old port's heart item ID still works in both Infernal Blood recipes.
- Bone Needle and Brew of Grotesque map legacy ingredient damage values 7 and 71. Taglock Kit uses the original `witchery:taglockkit` ID and shows the filled icon when bound. `WITCPlayer1` and the creature UUID halves in `WITCEntityIDm1` and `WITCEntityIDl1` work with the bound-taglock recipe ingredient. The new player UUID is authoritative when present. The Curse of Waking Nightmare persists under `PlayerPersisted.witcheryWakingNightmare`.
- The `witchery:dream` dimension type references its registered client effects for fog, sky and lighting. The server chooses the shared mode from the active dreamers, independently of player-style packets.
- The Mirror uses the `witchery:mirror` chunk generator and biome. New chunks contain the original stacked chambers. The dimension uses the same modern build bounds as the previous port, including for chunks saved before the generator change. The opt-in `shrinkMirrorWorld` server setting restores eight tiers and caps wall generation at Y 127 after a restart. Room allocation retains the `NextCell` saved index and honors the shorter span for new links.
- The Rite of the Mirror creates `witchery:mirror`. The previous port's `witchery:mirrorblock` item also mounts two halves. The block entities preserve `DimCoords`, `Connected`, and `DemonSlain` and use a UUID to verify both ends. `PlayerPersisted.MirrorWorldEntryPoint` retains the chamber entry. `witchery:reflection` saves its pose, equipment, health, and identity. The encounter waits for saved entities to load before checking whether the room has a Reflection.
- Inhabited exterior Mirrors keep `Favorite`, `Fairest`, `PlayersSeen`, and `LastFairestSpawnTime` for conversations. `witchery:mirrorface` retains its age in `WITCTicksAlive`. The `newFairestOfThemAllSpawnChance` server setting controls the original 1% chance after the two-minute cooldown.
- `witchery:reflection` retains the legacy `FreeSpawn`, `LivingTicks`, `Owner`, `Model`, and `Vampire` fields. It saves the contemporary owner's UUID and the copied skin separately. A freely spawned Reflection leaves the linked chamber's defeat record alone.
- Inhabited exterior Mirrors use a Taglock Kit to apply creature form 4 for 4,000 power. `LastPlayerSkin` imports into `OtherPlayerSkin`. The `allowChatMasquerading` and `allowNameplateMasquerading` server settings control the display names. Quartz Spheres charge into named Duplicate Grenades for 2,000 power.
- Torment uses `witchery:torment` as its maze generator and biome. The original `witchery:force`, `witchery:tormentstone`, `witchery:tormentportal`, and `witchery:refillingchest` IDs appear in six stacked mazes. Refilling Chest now uses a chest block entity and preserves `Items` and `WITCLifeTicks`. The old Torment build bounds and saves remain readable.
- `witchery:lordoftorment` and `witchery:soulfire` retain their legacy IDs. The Lord preserves its attacker names under `WITCAttackers`. `PendingBossFloors` in `witchery_torment_maze` holds the queued second phase until the destination entities load. The Brew of Soul Torment uses ingredient damage 139 and writes `PlayerPersisted.WITCSpellBook.tormentum` on drinking.
- `witchery:cat` is the familiar subtype of a vanilla cat. The cat, Owl and Toad carry the legacy `Familiar` flag. `FamiliarBinding` reads and writes `PlayerPersisted.WITCFamiliar`, including the original `UUIDMost`, `UUIDLeast`, `FamiliarType`, `FamiliarColor`, `FamiliarName` and `FamiliarSummoned` fields. Species IDs remain `1` for cat, `2` for Toad and `3` for Owl. Both familiar rites use the medium white circle. The calling rite recalled a loaded cat from Torment without changing its UUID. Dismissing an Owl drops any item it is carrying.
- The Leech Chest and Blood Rose keep the original `witchery:leechchest` and `witchery:bloodrose` block and item IDs. Their block entities retain `WITCPlayers` with `Player` entries. The chest item keeps that list after harvesting and preserves inventory contents as separate drops. The Blood Rose uses `full` for its visible sample state and the Boline drops an empty plant.
- The placeable `witchery:coffin` uses two halves of `witchery:coffinblock` and the original `Opened` block entity field. `part`, `facing` and `occupied` follow modern bed states. `paired` distinguishes a placed pair from the previously saved decorative block. The shared bed Taglock path resolves the head from either half.
- `witchery:glassgoblet` keeps the original `WITCBloodUUID` and `WITCBloodName` tags. `witchery:follower` retains `FollowerType`, `Skin` and `TicksToLive` and saves the lava destination and transformation time. `witchery:lilith` keeps `Invul`, `Lifetime` and `Friendly`. The player capability accepts the rank and blood keys from the legacy `WitcheryExtendedPlayer` compound.
- The rank-ten offering turns an eligible villager or Village Guard into `witchery:vampire` and clears both blood tags from the goblet. The entity keeps `BaseX`, `BaseY` and `BaseZ` as its home and saves `Hunting` and `HuntBites`. Older port entities without the home tags adopt their saved position.
- `witchery:bloodcrucible` saves its original `BloodLevel` from 0 to 20. The player's `VampireUltimate` preserves legacy IDs 0 for none, 1 for Thunderstorm, 2 for Bat Swarm and 3 for travel. `VampireUltimateCharges` retains the remaining uses.
- `witchery:attackbat` saves its caster under the original `Owner` GameProfile tag and its remaining life under `SwarmTicks`. Summoned attack bats carry `WITCNoDrops` in their entity data.
- A bone selects ultimate ID 3. Coffin and village travel uses the existing modern respawn fields and the village information available in loaded chunks. A dimension change refreshes the replacement client's Witchery player data.
- The nine vampire garments retain their original item IDs. Armor uses the vanilla `Damage` and `display.color` item tags. Vampire mobs retain their equipped garments in `ArmorItems`.
- `witchery:vampire_fire` and `witchery:sunlight` identify the two legacy vampire damage sources. The server's `vampireDeathItemKeepAliveMins` ranges from five to thirty minutes and defaults to twelve. Dropped items save their configured lifespan through Forge's `Lifespan` tag.
- `witchery:canesword` retains the per-item `WITCBladeDeployed` flag. The player's `BloodReserve` retains the original tag and a maximum of 250.
- `witchery:silvervat` stores dust in the original `Items` slot zero and remembers the four adjacent gold output counts in `ObservedGold`. The optional Silver Sword recipe uses `forge:ingots/silver` when another mod supplies that tag.
- Legacy rite 94 maps to `witchery:remove_vampirism`. The normal circle save stores the active recipe, caster, coven and offerings. The cure resets the existing vampire capability fields and retains the learned rank cap.
- `witchery:garlicgarland` keeps its block and item ID. The block state saves a horizontal `facing` and defaults to north for existing 1.20.1 saves without that property.
- `witchery:villageguard` reads and writes the legacy `GuardType` byte and `BloodLevel` value. It stores equipment in `HandItems` and `ArmorItems`. Earlier scaffold saves without a blood value initialize at 500.
- `witchery:vampire_bite` preserves the attacking player's attribution and distinguishes feeding from a direct Cane Sword kill. Protocol 41 adds guard targets to the existing vampire blood display. Guard feeding uses the same saved `BloodLevel` as Cane Sword collection and the drained-target requirement for conversion.
- General village defense uses the villagers' existing `Gossips` save data and the same territory used for recruitment. The server remembers attributed attacks on residents in memory for 300 ticks. No extra village save file is required for those short-lived attacks.
- `witchery:handbow` reads and writes legacy `WITCBoltTypeCurrent` and `WITCBoltTypePreferred` IDs 1 through 5 for wooden, anti-magic, holy, splitting and silver ammunition. Ingredient subtypes 130 and 132–135 and 155 map to Null Catalyst and the five bolts. The dedicated `witchery:bolt` projectile keeps the original `boltType` IDs 0 through 4 and uses modern projectile owner and collision state. The older scaffold's `witchery:bolt_spawn_egg` stack remains readable. Protocol 42 includes the ammunition, sounds and projectile data.
- Hunter hats, coats, trousers and boots retain their twelve original item IDs. Ingredient subtype 131 maps to `witchery:nullified_leather`. `witchery:hunter_clothing_upgrade` copies the input armor's NBT into the crafted variant, including `Damage` and `display.color`. Protocol 43 includes those recipes and the restored clothing items.
- `witchery:witchhunter` keeps `HunterType` and `HunterTarget`, and adds `HunterTargetUUID` for stable player targeting. Legacy name-only targets still load. The original `PlayerPersisted.WITCHunterTrigger` uses Overworld game time. The server setting `vampireHunterSpawnChance` defaults to 0.01 per interval. Protocol 44 includes the restored entity and its arrival sound.
- Hellhound entity ID 136 maps to `witchery:hellhound` and retains `ConversionTime`. The Wolf Head block keeps `witchery:wolfhead` and stores the trophy's `hellhound`, `facing` and `rotation` properties. Ordinary Wolf Heads retain their item ID. Legacy item metadata 1 maps to `witchery:hellhound_head` in the Witchery stack loader. Protocol 45 includes the new trophy item and block states.
- Rite IDs 92 and 93 map to `witchery:curse_of_the_wolf` and `witchery:cure_lycanthropy`. Player saves preserve `WerewolfLevel` and the original wolf ordinal 1. Legacy nested data also reads `CreatureType`. Server setting `allowVampireWerewolfHybrids` defaults to true. Protocol 46 includes the two rites and original howl.
- Entity IDs 135 and 137 map to `witchery:wolfman` and `witchery:werevillager`. `Infectious` remains a legacy save field. `FormerVillager` stores the modern villager snapshot and reads the old numeric `FormerProfession` when available. `WITCWereVillagerUUID` preserves the pre-curse Taglock identity through each form and cure. Protocol 47 includes the creatures and their sounds.

## 1.20.1 Event Hook Scaffold (current)
Implementation anchor:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/event/WitcheryEventHooks.java`

Placeholder Forge-bus hook points:
- `AttachCapabilitiesEvent<Entity>`: capability attachment migration anchor (`ExtendedPlayer` replacement path).
- `PlayerEvent.PlayerLoggedInEvent`: player bootstrap/sync migration anchor.
- `PlayerEvent.Clone`: persistent player data copy/migration anchor.
- `TickEvent.PlayerTickEvent`: scaffold portal collision routing anchor.
- `PlayerInteractEvent.RightClickBlock`: scaffold mirror-rite trigger routing anchor.
- `EntityTravelToDimensionEvent`: cross-dimension route observation/migration anchor.
- `PlayerEvent.PlayerChangedDimensionEvent`: post-travel sync/bootstrap anchor for dimension transitions.
- `LevelEvent.Load`: world/runtime registry bootstrap migration anchor.

Status:
- handlers now contain minimal breadth scaffolding for capability attach/copy, typed player-sync routing, scaffold trigger routing, and dimension-travel observation.
- feature-specific behavior is deferred to depth migration passes.

## 1.20.1 Capability Scaffold (current)
Implementation anchors:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/capability/WitcheryCapabilities.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/capability/WitcheryPlayerData.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/capability/WitcheryPlayerDataProvider.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/event/WitcheryEventHooks.java`

Current scaffold behavior:
- Registers `WitcheryPlayerData` capability type on MOD bus (`RegisterCapabilitiesEvent`).
- Attaches player capability provider on `AttachCapabilitiesEvent<Entity>` when entity is a `Player`.
- Copies capability data during `PlayerEvent.Clone`.
- Sends typed scaffold sync packets from event hooks:
  - `PlayerLoggedInEvent` -> `extended_player_sync` + `player_sync`
  - `PlayerEvent.Clone` -> `partial_extended_player_sync`

Current placeholder data surface (`WitcheryPlayerData`):
| Field | Type | Purpose |
|---|---|---|
| `initialized` | `boolean` | breadth session-init marker |
| `syncRevision` | `int` | incremental sync marker for placeholder packet routing |
| `creatureTypeOrdinal` | `int` | staged legacy creature type sync slot |
| `humanBlood` | `int` | staged legacy blood-state sync slot |
| `grotesqueTicks` | `int` | staged player-style grotesque timer |
| `nightmareLevel` | `int` | staged dream/nightmare state marker |
| `ghost` | `boolean` | staged ghost-state marker |
| `otherPlayerSkin` | `String` | staged alternate skin/name reference |
| `preparedSpellEffectId` | `int` | staged `spell_prepared` effect id |
| `preparedSpellLevel` | `int` | staged `spell_prepared` level |
| `lastItemUpdateSlot` | `int` | staged last `item_update` slot |
| `lastItemUpdateDamage` | `int` | staged last `item_update` damage value |
| `lastItemUpdatePage` | `int` | staged last `item_update` page index |
| `lastMarkupBookSlot` | `int` | staged last `sync_markup_book` slot |
| `lastMarkupBookPages` | `List<String>` | staged last `sync_markup_book` page payload |
| `lastHowlGameTime` | `long` | shared cooldown for the wolf pack and Wolfman howls |

Active lycanthropy saves retain `WerewolfLevel`, `WolfmanQuestState`, `WolfmanQuestCounter` and each signed `Location` in `WolfmanQuestChunks`. `LastHowlGameTime` tracks the wolf pack cooldown.
The wolf's dirt loot modifier records its one-minute cooldown as `LastBoneFindGameTime`.

Status:
- structural replacement path for legacy `ExtendedPlayer` is now wired.
- actual migration of legacy fields/behaviors is still TODO.

## Mod Hook Integrations (`modHooks.register`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/Witchery.java`
- `ModHookArsMagica2`
- `ModHookBloodMagic`
- `ModHookForestry`
- `ModHookMineFactoryReloaded`
- `ModHookMystCraft`
- `ModHookThaumcraft4`
- `ModHookTinkersConstruct`
- `ModHookTreecapitator`
- `ModHookWaila`
- `ModHookMorph`
- `ModHookBaubles`

## Worldgen Registry Touchpoints
- World generator root: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/worldgen/WitcheryWorldGenerator.java`
- Overworld structure handlers used by generator:
  - `WorldHandlerWickerMan`
  - `WorldHandlerCoven`
  - `WorldHandlerShack`
  - `WorldHandlerClonedStructure(ComponentGoblinHut)`
- `witchery:surface_structures` is a Forge placed feature with one candidate chunk in each region of `8 + worldGenFrequency` chunks. The default frequency is 12 and the salt is 10387312. The seeded choice allocates one quarter of candidates to each original structure. An enabled structure can take over when another choice is disabled or its terrain is unsuitable. The earlier `witchery:wicker_man` feature remains available to datapacks. Surface placement evaluates the original biome exclusions with current tags.
- The feature builds the legacy hay effigy, grass and poppies or sand and dead bushes, and a vanilla spawner under one foot. The active chunk generator supplies the sea level for foundation height. `strawmanSpawnerRules` accepts modern entity IDs and old vanilla names. Repeated entries retain their weight and missing choices fall back to zombies.
- `generateWickerMen` and `worldGenInTwilightForest` default to true. Generation checks the actual dimension, including when a custom dimension reuses Overworld biomes. Twilight Forest biomes are included through its optional tag. [Twilight Forest 1.20.1 biome tag](https://github.com/TeamTwilight/twilightforest/blob/1bc3a4c21213bc443967e92125613cb9ef47891e/src/generated/resources/data/twilightforest/tags/worldgen/biome/in_twilight_forest.json)
- `generateCovens` and `generateShacks` default to true. The 11-block coven circle has two vanilla witch spawners, a water basin and an east-facing dispenser with the modern mineshaft loot table. Its sandstone variant follows the old desert palette. The 7-block shack keeps its rotated door, spruce roof, connected windows and `witchery:chests/witch_shack` loot table. The table uses the original eight entries and one to three rolls.
- The coven circle and shack can generate one persistent coven witch with their original 90% and 75% chances. Their generation checks whether the site is dry at the four corners.
- `generateHobgoblinHuts` defaults to true. The fourth regional choice uses the original 7-by-7-by-6 schematic and spawns one persistent `witchery:goblin` with a profession from zero through three. Wild Hobgoblins may use all five professions. Network protocol 66 carries the restored entity's synced profession and mining animation.
- Village handlers bootstrapped in `Witchery.preInit`:
  - `WorldHandlerVillageApothecary`
  - `WorldHandlerVillageWitchHut`
  - `WorldHandlerVillageBookShop`
  - `WorldHandlerVillageDistrict`

## Tile Entity Registration Surfaces
- Auto-registration path: `BlockBaseContainer.func_149663_c` -> `GameRegistry.registerTileEntity(clazzTile, blockName)`.
- Manual registration found:
  - `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/blocks/BlockRefillingChest.java`

### Block classes with direct `TileEntity*.class` constructor binding
| Class | Tile Entity Class |
|---|---|
| `BlockAlluringSkull` | `BlockAlluringSkull.TileEntityAlluringSkull` |
| `BlockAltar` | `BlockAltar.TileEntityAltar` |
| `BlockBarrier` | `BlockBarrier.TileEntityBarrier` |
| `BlockBeartrap` | `BlockBeartrap.TileEntityBeartrap` |
| `BlockBloodCrucible` | `BlockBloodCrucible.TileEntityBloodCrucible` |
| `BlockBloodRose` | `BlockBloodRose.TileEntityBloodRose` |
| `BlockBrazier` | `BlockBrazier.TileEntityBrazier` |
| `BlockBrewGas` | `TileEntityBrewFluid` |
| `BlockBrewLiquidEffect` | `TileEntityBrewFluid` |
| `BlockButtonBase` | `TileEntityCursedBlock` |
| `BlockCandelabra` | `BlockCandelabra.TileEntityCandelabra` |
| `BlockCauldron` | `TileEntityCauldron` |
| `BlockChalice` | `BlockChalice.TileEntityChalice` |
| `BlockCircle` | `BlockCircle.TileEntityCircle` |
| `BlockCoffin` | `BlockCoffin.TileEntityCoffin` |
| `BlockCrystalBall` | `BlockCrystalBall.TileEntityCrystalBall` |
| `BlockDemonHeart` | `BlockDemonHeart.TileEntityDemonHeart` |
| `BlockDistillery` | `BlockDistillery.TileEntityDistillery` |
| `BlockDoorBase` | `TileEntityCursedBlock` |
| `BlockDreamCatcher` | `BlockDreamCatcher.TileEntityDreamCatcher` |
| `BlockFetish` | `BlockFetish.TileEntityFetish` |
| `BlockFumeFunnel` | `BlockFumeFunnel.TileEntityFumeFunnel` |
| `BlockGarlicGarland` | `BlockGarlicGarland.TileEntityGarlicGarland` |
| `BlockGrassper` | `BlockGrassper.TileEntityGrassper` |
| `BlockKettle` | `BlockKettle.TileEntityKettle` |
| `BlockLeechChest` | `BlockLeechChest.TileEntityLeechChest` |
| `BlockLeverBase` | `TileEntityCursedBlock` |
| `BlockMirror` | `BlockMirror.TileEntityMirror` |
| `BlockPlacedItem` | `BlockPlacedItem.TileEntityPlacedItem` |
| `BlockPoppetShelf` | `BlockPoppetShelf.TileEntityPoppetShelf` |
| `BlockPressurePlateBase` | `TileEntityCursedBlock` |
| `BlockSilverVat` | `BlockSilverVat.TileEntitySilverVat` |
| `BlockSlurp` | `BlockSlurp.TileEntitySlurp` |
| `BlockSpinningWheel` | `BlockSpinningWheel.TileEntitySpinningWheel` |
| `BlockStatueGoddess` | `BlockStatueGoddess.TileEntityStatueGoddess` |
| `BlockStatueOfWorship` | `BlockStatueOfWorship.TileEntityStatueOfWorship` |
| `BlockStatueWerewolf` | `BlockStatueWerewolf.TileEntityStatueWerewolf` |
| `BlockVoidBramble` | `BlockVoidBramble.TileEntityVoidBramble` |
| `BlockWitchesOven` | `BlockWitchesOven.TileEntityWitchesOven` |
| `BlockWolfHead` | `BlockWolfHead.TileEntityWolfHead` |
| `WorldHandlerVillageDistrict.Wall.BlockVillageWallGen` | `WorldHandlerVillageDistrict.Wall.BlockVillageWallGen.TileEntityVillageWallGen` |

### Special-case dynamic tile bindings
- `BlockAreaMarker` constructor accepts tile class and is instantiated twice in `WitcheryBlocks`:
  - `TileEntityAreaTeleportPullProtect` (`DECURSE_TELEPORT`)
  - `TileEntityAreaCurseProtect` (`DECURSE_DIRECTED`)

## Runtime Singleton Registries (non-Forge)
- `RiteRegistry.instance()`
- `PredictionManager.instance()`
- `Infusion.Registry.instance()`
- `CreaturePower.Registry.instance()`
- `KettleRecipes.instance()`
- `DistilleryRecipes.instance()`
- `SpinningRecipes.instance()`
- `WitcheryBrewRegistry.INSTANCE`
- `EffectRegistry.instance()`
- `PowerSources.instance()` (client/server split instance)
- `BlockAreaMarker.AreaMarkerRegistry.instance()` (client/server split instance)

## Notes For 1.20 Port Setup
- Legacy metadata-heavy content (`ItemGeneral`, `ItemPoppet`, slabs, brew variants) needs explicit 1.20 representation strategy.
- Preserve legacy names as migration aliases where practical.
- Expect dimension and worldgen systems to be redesigns, not direct ports.
