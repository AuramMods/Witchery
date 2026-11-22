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
- Once a legacy wood stack has its namespaced ID, the shared Witchery inventory loader reads its legacy top-level `Damage` and writes `BlockStateTag.wood_type`. Existing explicit wood state and unrelated tags take precedence. Saved cauldron mixtures use the same loader for Witchery saplings.
- The opt-in world importer reads each save's top-level `FML.ItemData` before modernization. The first byte of `K` distinguishes blocks (`0x01`) from items (`0x02`), and `V` carries that world's ID. Chunk conversion reads the low byte in `Blocks`, the high nibble in `Add`, and the metadata nibble in `Data`. It restores the supported blocks in vanilla's flattened palette. Chunk inventories and both kinds of player inventory resolve through the item table and the shared loader. Witchery block entities use an explicit vanilla item fix for mixed inventories. See the Legacy Save Fixtures in `PORTING.md` for verified coverage.
- Forge 1.7.10 stores Witchery's chunk entity IDs with a dot and the original registration's capitalization. The importer converts those registered IDs to the modern namespaced form. It uses vanilla's entity data fixes for UUIDs, names, animals, equipment and passenger trees, and fixes mixed Hobgoblin trades using the world item table.
- The importer records the three optional `DimensionID` settings from the original `witchery.cfg`. Numeric folders move into namespaced storage before the chunk pass. Dream uses its own legacy Overworld-style structure data. Torment and Mirror use fixed modern biome palettes. Player tags, Waystones, Door Keys, key rings, and Mirror exits use the saved world's numeric mapping.
- The three witchwood stair IDs use their corresponding plank textures and original four-stair recipes. Recipes read the plank stack's wood state and require all six inputs to match. Hawthorn stairs retain the original lower fire spread and ignition values.
- The Water and Lava Diviners retain `witchery:divinerwater` and `witchery:divinerlava`. Their legacy top-level item damage converts to modern durability. The wear values and unrelated stack data were verified with numeric IDs from an original Forge save and again after a normal dedicated restart.
- The original `witchery:stewraw` and `witchery:stew` retain their IDs, count, and item tags. The original raw mutton ingredient (158) already maps to `minecraft:mutton`, which fits the raw stew recipe.
- The Poppet Shelf Compass retains `witchery:shelfcompass`. An original stack's old metadata recorded the displayed icon. Its count and custom tags survive import, and the six modern models select their icon from the current world.
- The Creative Bat/Wolf Token retains `witchery:wolftoken`. A stack imported from an original numeric ID was used through both rank cycles, retained its custom tag, and survived a packaged dedicated restart with both ranks saved.
- The Staff of Duplication retains `witchery:dupstaff`, the original marker coordinates, rotation, and nested schematic. A real twelve-cell capture survived numeric ID import into a chest and placed after an ordinary dedicated restart. Modern captures hold a palette of block states in the same coordinate order as the original list.

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
  - `LegacyDyeColors` shares the original sixteen colors across the fetishes, brooms, and Witchery pets. Spectral Scarecrow fabric renders at 0.7 opacity and its plain parts at 0.6. Spectral Trent wood renders at 0.6 and its face stays opaque. The Witch's Ladder uses the cutout texture in both forms.
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
| `VOID_BRAMBLE` | `witchery:voidbramble` (owner in tile `WITCPlacer`, 32-block circle rite suppression) |
| `BRAMBLE` | `witchery:bramble` |
| `GLINT_WEED` | `witchery:glintweed` (light 14, local spreading limit) |
| `SPANISH_MOSS` | `witchery:spanishmoss` (wall bits south 1, west 2, north 4, east 8, ceiling 0) |
| `LEAPING_LILY` | `witchery:leapinglily` |
| `TRAPPED_PLANT` | `witchery:plantmine` (twelve crafting, placed, and Boline-recoverable variants) |
| `EMBER_MOSS` | `witchery:embermoss` (light six, contact ignition, shear harvest) |
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
| `DOOR_ROWAN` | `witchery:rowanwooddoor` (location-bound lock, key from item placement) |
| `DOOR_ALDER` | `witchery:alderwooddoor` (emits redstone when open) |
| `CHALICE` | `witchery:chalice` |
| `CANDELABRA` | `witchery:candelabra` |
| `PLACED_ITEMSTACK` | `witchery:placeditem` |
| `ALLURING_SKULL` | `witchery:alluringskull` (mount in block metadata, active state in tile `SkullType`, floor rotation in `Rot`) |
| `WICKER_BUNDLE` | `witchery:wickerbundle` |
| `GLOW_GLOBE` | `witchery:glowglobe` |
| `STATUE_GODDESS` | `witchery:statuegoddess` (facing in metadata, owner in tile `WITCPlacer`) |
| `STOCKADE` | `witchery:stockade` (wood subtypes 0–8, contact damage) |
| `STOCKADE_ICE` | `witchery:icestockade` (Ice World conversion, contact damage) |
| `PERPETUAL_ICE` | `witchery:perpetualice` |
| `PERPETUAL_ICE_DOOR` | `witchery:icedoor` (hand and redstone operation, ingredient subtype 152) |
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
Witch clothing models hide on invisible wearers and remain visible on armor stands. Vanilla armor visibility and crouching scale the legacy mob search for nearby invisible players. The existing `VampireLevel` data also controls whether zombies target a visible player at rank ten.

| Field | Legacy Name |
|---|---|
| `GENERIC` | `witchery:ingredient` |
| `TAGLOCK_KIT` | `witchery:taglockkit` |
| `POPPET` | `witchery:poppet` |
| `POTIONS` | `witchery:potion` (Universal Antidote, subtype zero, removes Poison and Wither) |
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
| `BIOME_BOOK` | `witchery:bookbiomes2` (stable `WitcheryBiome` ID and legacy `CurrentPage` import) |
| `BIOME_NOTE` | `witchery:biomenote` (legacy atlas `Damage` index imports to `WitcheryBiome`) |
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
| `BITING_BELT` | `witchery:bitingbelt` (dyeable leggings, two charges in `WITCPotion` and `WITCPotion2`) |
| `BARK_BELT` | `witchery:barkbelt` (dyeable leggings, renewable `WITCBarkPieces` ward) |
| `ICY_SLIPPERS` | `witchery:iceslippers` (boots that freeze water, solidify lava, and activate perpetual ice plates) |
| `RUBY_SLIPPERS` | `witchery:rubyslippers` (boots that cast a return-home or Waystone chant using infusion charge) |
| `SEEPING_SHOES` | `witchery:seepingshoes` (boots that turn Poison and Wither into nearby plant growth) |
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

The `mutandisAdditionalBlocks` server setting ports `MutandisAdditionalBlocks`. It defaults to Glintweed and Fern, the flattened equivalent of `tallgrass,2`. Each entry can be both an input and an outcome for ordinary Mutandis and Extremis. Repeated entries add weight. The setting accepts modern IDs and states and uses the legacy block-state converter for old IDs and metadata. The base plants remain data driven through the Mutandis block tags.

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
| 46 | `bookOven` → `witchery:book_collecting_fumes` |
| 47 | `bookDistilling` → `witchery:book_distilling` |
| 48 | `bookCircleMagic` → `witchery:book_circle_magic` |
| 49 | `bookInfusions` → `witchery:book_brews_and_infusions` |
| 50 | `oddPorkchopRaw` |
| 51 | `oddPorkchopCooked` |
| 52 | `doorRowan` → `witchery:rowanwooddoor` |
| 53 | `doorAlder` → `witchery:alderwooddoor` |
| 54 | `doorKey` → `witchery:door_key` |
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
| 69 | `artichoke` maps to `witchery:globe_artichoke`. Eating applies Hunger III based on food restored. |
| 70 | `seedsTreefyd` → `witchery:treefyd_seeds` |
| 71 | `brewGrotesque` |
| 72 | `impregnatedLeather` |
| 73 | `fumeFilter` |
| 74 | `creeperHeart` maps to `witchery:creeper_heart`. Eating gives brief Fire Resistance and a configurable blast. |
| 75 | `brewLove` |
| 76 | `brewIce` |
| 77 | `brewDepths` |
| 78 | `icyNeedle` |
| 79 | `frozenHeart` maps to `witchery:frozen_heart`. Eating gives brief Fire Resistance and clears the three Imp effects. |
| 80 | `infernalBlood` |
| 81 | `bookHerbology` → `witchery:book_herbology` |
| 82 | `entbranch` |
| 83 | `mysticunguent` → `witchery:mystic_unguent` |
| 84 | `doorKeyring` → `witchery:door_keyring` |
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
| 98 | `sleepingApple` maps to `witchery:sleeping_apple`. Eating it starts an ordinary nightmare in the Dream. |
| 99 | `disturbedCotton` |
| 100 | `fancifulThread` |
| 101 | `tormentedTwine` |
| 102 | `goldenThread` |
| 103 | `mellifluousHunger` |
| 104 | `weaveIntensity` |
| 105 | `purifiedMilk` |
| 106 | `bookBiomes` → `witchery:book_of_biomes` |
| 107 | `bookWands` → `witchery:book_symbology` |
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
| 127 | `bookBurning` → `witchery:book_conjuration_and_fetishes` |
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
| 142 | `contractBlaze` → `witchery:contract_living_flame` |
| 143 | `contractResistFire` → `witchery:contract_fiery_tolerance` |
| 144 | `contractEvaporate` → `witchery:contract_evaporation` |
| 145 | `contractFieryTouch` → `witchery:contract_fiery_touch` |
| 146 | `contractSmelting` → `witchery:contract_melting_touch` |
| 147 | `waystoneCreatureBound` → `witchery:creature_bound_waystone` |
| 148 | `kobolditedust` → `witchery:koboldite_dust` |
| 149 | `kobolditenugget` → `witchery:koboldite_nugget` |
| 150 | `kobolditeingot` → `witchery:koboldite_ingot` |
| 151 | `pentacle` → `witchery:koboldite_pentacle` |
| 152 | `doorIce` → `witchery:icedoor` |
| 153 | `annointingPaste` |
| 154 | `subduedSpiritVillage` → `witchery:subdued_village_spirit` |
| 155 | `boltSilver` |
| 156 | `wolfsbane` |
| 157 | `silverdust` → `witchery:silver_dust` |
| 158 | `muttonraw` |
| 159 | `muttoncooked` |
| 160 | `vbookPage` |
| 161 | `darkCloth` |
| 162 | `stake` (`witchery:stake`, eight garlic and a stick, `allowStakingVampires`) |
| 163 | `warmBlood` maps to `witchery:warm_blood`. Humans get Hunger, and vampires replenish blood. |
| 164 | `lilithsBlood` maps to `witchery:liliths_blood`. The drink grants a rank or replenishes blood at rank ten. |
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
| `brew` | `witchery:brew`, `witchery:flowing_brew` | `witchery:brew` (`LiquidBlock`) | `witchery:bucketbrew` (`BrewBucketItem`, ordered mixture and color in its tag) |
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
| 94 | `BREW` | `EntityWitchProjectile` | `brew` → `witchery:thrown_brew` (fixed brew or projectile selected by `damageValue`) |
| 95 | `SPECTRAL_FAMILIAR` | `EntityFamiliar` | `familiar` (tamed spectral pig, ore search, `ItemToFind`, `Searches`) |
| 96 | `MANDRAKE` | `EntityMandrake` | `mandrake` |
| 97 | `TREEFYD` | `EntityTreefyd` | `treefyd` (planted guardian, Taglock training, Boline patrol, heart upgrades) |
| 98 | `HUNTSMAN` | `EntityHornedHuntsman` | `hornedHuntsman` → `witchery:hornedhuntsman` (Horn of the Hunt, entrance, boss combat and rank-five trial) |
| 99 | `SPELL` | `EntitySpellEffect` | `spellEffect` → `witchery:spelleffect` |
| 100 | `ENT` | `EntityEnt` | `ent` (Witchwood harvest awakening, legacy `Owner` field retained) |
| 101 | `ILLUSION_CREEPER` | `EntityIllusionCreeper` | `illusionCreeper` |
| 102 | `ILLUSION_SPIDER` | `EntityIllusionSpider` | `illusionSpider` |
| 103 | `ILLUSION_ZOMBIE` | `EntityIllusionZombie` | `illusionZombie` |
| 104 | `OWL` | `EntityOwl` | `owl` (Mutating Sprig, taming, feathers, Waystone delivery, Infernal power 24) |
| 105 | `TOAD` | `EntityToad` | `toad` (Mutating Sprig, taming, amphibious behavior, Infernal power 25) |
| 106 | `CAT_FAMILIAR` | `EntityWitchCat` | `cat` |
| 107 | `LOUSE` | `EntityParasyticLouse` | `louse` (original arthropod model and mutation, saved `BitePotionEffect`) |
| 108 | `EYE` | `EntityEye` | `eye` |
| 109 | `BABA_YAGA` | `EntityBabaYaga` | `babayaga` |
| 110 | `COVEN_WITCH` | `EntityCovenWitch` | `covenwitch` (spawn, appearance, recruitment, quests, defensive potions, visits, ritual seats, and member death) |
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
| 123 | `IMP` | `EntityImp` | `imp` (rite, ownership, gift progression, power, and five signed spell contracts restored) |
| 124 | `DARK_MARK` | `EntityDarkMark` | `darkmark` |
| 125 | `MINDRAKE` | `EntityMindrake` | `mindrake` (Sprig mutation, three-second bulbs, owner-directed attacks and bursts) |
| 126 | `GOBLIN` | `EntityGoblin` | `goblin` |
| 127 | `GOBLIN_MOG` | `EntityGoblinMog` | `goblinmog` (ranged patron boss, `Invul` and `WitcheryTarget` saved) |
| 128 | `GOBLIN_GULG` | `EntityGoblinGulg` | `goblingulg` (melee patron boss, `Invul` and `WitcheryTarget` saved) |
| 129 | `BREW2` | `EntityBrew` | `brew2` → `witchery:thrown_brew` (saved `Brew` stack and `Spell` flight mode) |
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
| 143 | `WINGED_MONKEY` | `EntityWingedMonkey` | `wingedmonkey` (cocoa mutation, taming, Taglock pickup and Waystone transport) |
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

- IDs 2, 21, 22, 23, and 26 are active in `SymbolSpell`. The three projectile spells retain `EffectID` and `effectLevel` on saved `witchery:spelleffect` entities. Aguamenti and Incendio accept all original enhanced gestures. Nox and Meteolojinx Recanto take effect immediately when the gesture is released. Lumos uses the original `witchery:glowglobe` block ID and retains the earlier port's item ID for saved inventories.
- IDs 1, 3, 5, 6, and 10 restore Accio, Alohomora, Cave Inimicum, Colloportus, and Defodio. Door keys keep `doorX`, `doorY`, `doorZ`, `doorD`, and `doorDN`, with `doorDimension` for modern dimension IDs. Rings keep `doorKeys`. Protocol 70 includes the `witchery:door_keyring` crafting serializer.
- IDs 4, 9, 12, 13, 16, 20, 31, and 36 finish the legacy spell registry. All 31 IDs and their gesture sets match `EffectRegistry`. IDs 4 and 16 are hidden from Symbology. IDs 4, 9, and 20 require the Infernal Infusion in survival. Imperio keeps `WITCEnslaverName` and uses `WITCEnslaverUUID` for modern ownership.

## Ritual Registry (`RiteRegistry`)

The modern `witchery:item_rite` recipe type covers item-output rites at the Heart Glyph. Legacy rite 3, the Rite of Charging, is implemented as `witchery:rite_of_charging`. It validates the original 16- and 28-glyph white rings, consumes its five offerings in steps, and draws 2,000 power from the nearest altar.
Rite IDs 10, 11, 14, 15, 20, 39, and 70 through 77 accept an optional Bound Waystone before the altar charge. The active Heart Glyph saves the consumed stack and its destination under `Sacrifices` and `WaystoneTarget`.
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

- The Heart Glyph uses the registration IDs to match and queue recipes from distinct offering sources. It saves the pending recipe IDs in `QueuedRites` and tracks the order of independent upkeep in `ProtectionRites` and `UpkeepRites`. IDs 5 and 91 select the location-bound and creature-bound travel recipes independently.
- The Circle Magic guide lists 88 rites in the original order. Rites 71–76 share the Forestation chapter with rite 70, and rites 85–86 share the Binding chapter with rite 84. Each chapter lists the valid first offerings from its live recipes. Network protocol 88 carries the rite ID when saving the book's `WitcheryRite` bookmark.
- Rites 34, 41, and 42 restore powered location copying, portable binding, and portable copying. Protocol 73 carries the copy action. `OfferingStep` tracks progress separately from `Sacrifices`, and `OfferingPos` stores each retained offering's origin. The Arthana, Boline, reusable Necromantic Stone, and discharged Attuned Stone drop as their steps finish. Rite 82 still consumes the Necromantic Stone.
- Rites 1 and 45 bind the existing `witchery:circletalisman` item. `CirclePattern` preserves the original three-bit field per ring size, with white, Otherwhere, and Infernal encoded as 1, 2, and 3. Protocol 74 carries the binding action. Right-click placement submits one batch to Forge and returns a blank only when the placement succeeds.
- Rites 8–12 restore `witchery:rite_of_protection`, `witchery:rite_of_imprisonment`, and the three `witchery:rite_of_the_barrier` variants. The Heart Glyph saves active wards in `ProtectionRites` with kind, target and elapsed upkeep ticks. The legacy stage array stores seconds for the portable variant and zero for the powered wards. Protocol 72 carries the five recipe actions.
- Rites 14–16 restore the focused, greater and portable variants of `witchery:rite_of_the_skys_wrath`. The powered variants accept an optional location-bound Waystone. The Heart Glyph's `Storm` tag saves the target, variant and phase, and the fourth phase starts matching rain and thunder timers. Rites 17–18 restore the daytime powered and portable variants of `witchery:rite_of_total_eclipse`. Protocol 71 carries the four new recipe actions.
- Rite 19 uses `witchery:rite_of_broken_earth`. The offering position selects one of eight headings. The `Landscape` record stores `FissureSeed`, `FissureHeading`, and the completed stage. The older `RitualStages` array migrates with a northward path because 1.7.10 did not save the offering position or path.
- Rites 20, 46, and 47 are the small, medium, and large Rites of Moving Earth. Only the small ring accepts an optional location-bound Waystone. The glyph retains the target, coven size, stage and moved block entity data. Protocol 78 carries the four earth rite actions.
- Rites 21 and 22 restore portable and powered banishment. The strict nine-block sphere includes the original Demon, Death, Lord of Torment, Imp, and Reflection entity types. A banished creature disappears without death loot.
- Rites 25 and 26 summon a vanilla Wither from the two Infernal rings. The powered rite follows the original order of sacrificing a villager and paying the altar before checking the chamber clearance. Rites 33 and 40 summon `witchery:familiar` and `minecraft:witch`, respectively. The familiar imports `OwnerUUID`, `ItemToFind`, and `Searches`, with modern ore tags covering stone and deepslate. Protocol 79 carries the four action types for the six recipes.
- Rites 35 and 36 use `witchery:rite_of_fertility` and `witchery:rite_of_fertility_portable`. Their shared `FERTILITY` upkeep saves the expanding stage and imports the corresponding entry from `Initiators` as `InitiatorName`. New casts keep the existing UUID `Initiator`. Vanilla cure preserves modern villager data and invokes Forge's conversion events.
- Rites 37 and 38 use `witchery:curse_of_blight` and `witchery:curse_of_blindness`. Their wave shares Fertility's midpoint-circle surface scan and spherical entity range. It expands to 80 blocks plus two per coven member. Forge conversion events protect villagers and cows during Blight, and conversion to a Zombie Villager preserves the data needed for curing.
- Rite 39 uses `witchery:rite_of_hell_on_earth`. The source altar pays 5,000 within 16 blocks of the Heart Glyph. The target altar pays 200 within 16 blocks of the destination after the wave expands to 20 blocks plus two per coven member. The map-editing path respects Forge protections and the `allowHellOnEarthFires` setting enables the cat familiar's fires by default.
- Rite 43 uses `witchery:rite_of_natures_power`. The `witchery:natures_power_replaceable` block tag includes stone, sand, gravel and mycelium by default. It replaces the legacy `NaturesPowerReplaceableBlocks` configuration list. The portable recipe requires a Rowan, Alder and Hawthorn Sapling individually, along with oak, spruce and birch.
- Rite 44 uses `witchery:rite_of_prior_incarnation`. Player data retains `WITCPriIncInv` and the three `WITCPriIncLoc` coordinate fields. Drop stacks accept the original `WITCPriIncUsr` owner name. New stacks also carry `WITCPriIncOwner` and `WITCPriIncDeath` to keep player identity and track the originating death. Protocol 84 carries the recipe action.
- Rite 68 uses `witchery:rite_of_calling_the_beasts`. The legacy raw Fish offering with metadata zero is Cod. The target pool includes domestic cats and ocelots to cover both forms of the old ocelot entity. Arrivals check for block collision. The `NATURES_POWER` and `CALL_BEASTS` upkeep read unsigned values from the old `RitualStages` array. Protocol 80 carries these two actions and `FERTILITY`.
- Rite pairs 48–49, 52–53, 59–60, 62–63 and 78–79 use `curse_of_<curse>` and `cure_of_<curse>` recipes for Misfortune, Insanity, Sinking, Overheating and Waking Nightmare, respectively. Targets keep the original `witcheryCursed`, `witcheryInsanity`, `witcherySinking`, `witcheryOverheating` and `witcheryWakingNightmare` keys in entity Forge data or the player's `PlayerPersisted` compound. Protocol 81 sends the eight additional recipe actions and adds Sinking's strength to the infusion status packet.
- Rites 6 and 7 are `witchery:rite_of_transposition_creature` and `witchery:rite_of_transposition_ores`. They use medium and large Otherwhere circles, respectively. The protocol 55 item-rite actions retain the required sacrifices and altar power. An active ore rite migrates legacy `Rituals`, `RitualStages` and `RitualCovens` and saves the completed layer as `OreDepth`.
- Rite 90 is `witchery:rite_of_binding_a_worship_statue`. It uses the small white circle and 4,000 altar power. The bound item keeps `WITCBoundPlayer` and adds the player UUID. The placed `witchery:statueofworship` block entity accepts the original `Owner` string and carries it back onto drops. Protocol 67 adds the recipe action and the Hobgoblin worship pose.
- The Scarecrow, Trent Effigy and Witch's Ladder retain their block entity IDs. The legacy `EffectTypeID` and item `WITCSpiritEffect` identify the active spirit enhancement. Effect 1 applies to carried Voodoo Protection against a forced summon and Death Protection against powered bolts within 16 blocks. Effects 2 through 5 drive Sentinel, Screamer, Twister and Ghost Walker using the alarm's Taglock list. Effect 6 is the hidden Death encounter, checked last. It consumes five of each spectral undead species and creates no bound fetish.
- Rite 82 uses `witchery:rite_of_necromancy_spectral_stone`. It consumes the Necromantic Stone and produces one blank `witchery:spectralstone`. Protocol 56 carries the explicit `consume_necromantic_stone` field and the Spirit's synchronized color.
- Rite 67 uses `witchery:rite_of_glyphic_transformation`. It consumes Gypsum, an Arthana, and 1,000 altar power. The first color of fresh chalk among nearby drops selects the target color. The number of sticks of that color selects the ring size and one chalk is consumed. Protocol 62 carries the new action.
- Rite 64 uses `witchery:rite_of_climate_change`, the large white circle, and seven offerings without an altar charge. At least four coven witches are required. Four, five, or six witches give a radius of 16, 32, or 48 blocks. The fifth pulse consumes nearby stacks of recognized foci and Glowstone Dust. The last recognized focus selects the family. Dust selects the one-indexed biome variant and clamps to the available choices.
- The climate foci are Oak Sapling, Short Grass, Obsidian, Stone, Slimeball, Water Bucket, Cactus, Icy Needle, Jungle Sapling, Netherrack, Sand, Red Mushroom, and Skeleton Skull. Vanilla variants and current biome tags substitute for the old Forge BiomeDictionary. The magical family falls back to Dark Forest if the tag is empty. Cactus removes surface snow in range and clears rain and thunder. Icy Needle starts precipitation when the weather is clear.
- Climate Change rewrites saved four-block-wide biome columns across all heights and sends Minecraft's biome update to clients tracking the affected chunks. The Heart Glyph imports legacy rite 64 upkeep and keeps the glyph as the center. Protocol 65 adds the action. The server's `allowBiomeModificationRituals` setting defaults to true and can disable the rite.
- Rite 65 uses `witchery:rite_of_ice_shell`. It checks for two coven members and grows a Perpetual Ice shell to radius 8, 12, or 16 according to coven size. The Heart Glyph accepts its legacy upkeep stage. Protocol 64 adds the action. Frozen Heart keeps legacy ingredient subtype 79.
- Rite 66 uses `witchery:rite_of_rain_of_toads`. The Heart Glyph saves the foreground prelude in `ToadRainStage` and the stage-five-to-199 upkeep in `Landscape`. The rite sends four lightning bolts before the first shower. Temporary toads expire after 30 ticks and poison nearby creatures. The original `SuicideIn` and `Poisonous` fields persist. Protocol 83 carries the four new recipe actions.
- Rites 70–76 use `witchery:forestation/<species>`. They accept oak, spruce, birch, jungle, rowan, alder, and hawthorn in legacy order. Rite 77 uses `witchery:rite_of_raising_a_volcano`. It scans to modern bedrock or the build floor and accepts a lava reservoir that meets the original neighboring-source test.
- Forestation and the powered volcano use the optional legacy location-bound Waystone. `Landscape` saves the current stage, target, and forest species when present. The Heart Glyph imports a supported legacy upkeep entry using `Rituals`, `RitualStages`, `RitualCovens`, and `Locations`. Protocol 63 added the two actions.
- Rite 13 uses `witchery:rite_of_raising_a_volcano_portable` and saves `RAISE_PORTABLE_VOLCANO` as the action at the glyph's own location. Rites 51 and 58 use `witchery:rite_of_cooking` and `witchery:rite_of_cooking_portable`. They use the active `minecraft:smelting` recipes and turn individual burn rolls into Charcoal. Protocol 82 carries the three new actions.
- The two Wicker Bundle variants keep `bundle_type` and `axis=x|y|z`. The legacy chunk importer reads the type bit and direction bits separately. Flint and Steel resolves the figure along one horizontal axis, burns the original pattern, and summons a Huntsman at one quarter health. The Wicker entrance has no explosion. The Horn of the Hunt enables its own entrance explosion.
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

Both charm IDs use the same 50-durability item for animal shops. Stock is stored on the mob at `ForgeData.WitcheryShopStock.Recipes`. The importer normalizes numeric Witchery and vanilla item stacks in the original `buy`, `buyB` and `sell` fields and preserves `uses` and `maxUses`. An existing empty compound marks a spent shop. Modern horse variants use the original Horse pool, and Cats use the Ocelot pool. Mooshrooms use the Cow pool's wheat currency, matching the original branch order.

One customer can access an animal's stock at a time. The modern menu closes if the creature disappears or the customer moves more than eight blocks away. Its shift-click path uses the animal's own voice because the menu wraps an ordinary mob. Demon bargains retain their separate merchant and Devil's Tongue discount.

The Owl reads `FeatherColor` and the Toad reads `SkinColor`. Both retain `Familiar` and `SuicideIn` and use vanilla tamed ownership. The Toad also reads `Poisonous`. The Owl keeps its carried item in its main hand equipment.

The Broom uses `BrushColor` on the vehicle and enchanted item. The vehicle reads legacy plain-text `CustomName` and saves the modern component name. An older port's `witchery:broom_spawn_egg` can place the vehicle. The Infused Brew of Soaring uses `WITCInfusedBrewType:1` and `WITCInfusedBrewStart` in the player record.

Bound Waystones retain `PosX`, `PosY`, `PosZ`, `PosD` and `NameD`. New bindings add the namespaced `Dimension` field. Old Waystones resolve the three vanilla dimensions and Witchery's default legacy IDs `-37` for Dream, `-38` for Torment and `-39` for Mirror. Creature-Bound Waystones share the first Taglock signature fields.

Owls retain `WITCWaypoint` and their launch coordinates in `HomeX`, `HomeY` and `HomeZ` while delivering. `ReturningFromDelivery` preserves the return leg on modern saves.

The Winged Monkey retains `FeatherColor`, `SuicideIn`, `WITCWaypoint` and the three `Home` coordinates. `MonkeyFlightReturning` identifies the return leg in new saves. An older active Waystone flight stores its destination in the `Home` coordinates. The modern goal uses Forge's flying navigation and only loads routes within the monkey's dimension. The creature identified by a Taglock rides as a passenger without taking control of the monkey.

Treefyd keeps `Owner`, `SentinalPlant`, `KnownPlayers`, `KnownCreatureTypes`, and `KnownCreatures`. New bindings also save player UUIDs and the namespaced keys of groupable animal species. Legacy individual permissions use the original `CreatureMost` and `CreatureLeast` fields. Planting creates short grass and an owned Treefyd in one interaction. A Forge cancellation keeps the original block and seed.

## Recipe System Registries

The modern datapack contains the altar's shaped recipe. Rowan logs match untagged stacks and stacks with explicit rowan state. Other Witchery wood variants and non-water potions are excluded.
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryRecipes.java`

The `witchery:wood_ash` smelting recipe uses `minecraft:saplings` when `addSmeltingForAllSaplingsToWoodAsh` is true in `config/witchery-common.toml`. The false branch accepts the original six vanilla species and Rowan. Both the vanilla furnace and Witch's Oven load the selected recipe through Forge. The oven saves pending experience under `RecipesUsed` and pays it out when a player takes the main result.

- `RecipeSorter.register`: 7 custom recipes
- `KettleRecipes.instance().addRecipe`: 39. The port has 39 Kettle recipe IDs. The original Spirit of Otherwhere and Infernal Animus IDs accept their regular potions and appear in Brews & Infusions. The two separate splash-potion IDs follow the original hidden-page rule and brew the same outputs.
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

- Forge 1.7.10 reports 184 registered brew keys across 88 base items. Newly mixed brews use the captured base item translation keys, including one shared hash across each legacy metadata family. Modern Dead Bush also represents the original Tall Grass metadata-zero variant and uses the original Dead Bush hash. An explicit saved color survives import with all 32 bits, and rendering takes its low 24 bits.
- `witchery:brewbottle` uses an ordered `Items` list for drinkable and instant splash effects. Ingredient stacks inside `Items` support the named legacy `witchery:ingredient` plus `Damage` format. The `Splash` flag can be derived from known dispersal ingredients when absent. Native numeric 1.7.10 item IDs still require the save conversion workflow.
- Legacy `minecraft:speckled_melon` loads as `minecraft:glistering_melon_slice` in saved mixtures. It retains the original Healing effect or Harming when inverted, with the saved ingredient order and brew color.
- `witchery:brewbag` preserves `WITCColor` and eight numbered slots in `Inventory.Items`. Sneak-use opens `witchery:brew_bag`. Normal use shows the eight-direction selector. The server derives selection from player rotation and throws the matching brew on release. The menu and dye recipe use protocol 75.
- `PotionBottling` retains the original player-data key. Successful player bottling can grant skill according to the batch's used capacity and charges the closest altar once per portion. The Seer Stone reports the skill.
- An empty glass bottle in a dispenser can draw a full boiling Witch's Cauldron from any of the five sides other than the outlet. The original ordering and power requirement apply. Successful automatic bottling checks the altar and leaves its stored power intact. Fixed throwable and splash-format brews launch the same projectile as player throws, with original dispenser velocity and spread.
- Bat Wool and Wormwood select gas or liquid dispersal. Both original block entity IDs resolve to brew fluid cells and can read legacy effect, tint, duration, expansion and thrower data. Current saves also retain their age.
- Gunpowder, Globe Artichoke, Bat Wool, Wormwood and Zombie Head remove the earlier dispersal and all earlier Wheat actions when added. This frees the Wheat action's one capacity and removes its Moonshine effect. Later Wheat remains eligible as an ordinary brew ingredient.
- Zombie Head selects triggered dispersal. The original stone and oak buttons, lever, oak, stone and snow plates, and oak door have cursed counterparts with the original block entity IDs. Cursed controls persist the ordered mixture, tint, charge count, duration, expansion and thrower. Named `minecraft:skull` metadata 2 decodes to the Zombie Head ingredient.
- Tongue of Dog adds 250 base power and starts the item curse with a full triggered brew. The cauldron retains `RitualTriggered` and `RitualTicks` during the warmup. A charged item stores its ordered recipe and remaining charges under the original `WITCCurse` keys `Curse`, `Count` and `Delay`. A named 1.7.10 cauldron's root `FluidName`, `Amount`, `TicksHeated` and nested fluid `Tag` map to the 1.20.1 tank and ingredient data.
- Tongue of Dog also casts instant, gas and liquid dispersals. A Bound Waystone adds 100 base power and supplies the first destination. Coal adds the one-level Extinguish action. Brew rain spawns `witchery:droplet` with saved `Brew` data. The 1.20.1 port keeps the earlier projectile spawn-egg item ID for save compatibility.
- The legacy chunk importer converts numeric vanilla item IDs inside a falling droplet's `Brew.Items` list. The original entity type, ingredient order, and tint survive the normal save upgrade.
- Both original thrown brew types import as `witchery:thrown_brew`. Fixed projectiles resolve their original ingredient subtype. Custom projectiles keep the ordered bottle ingredients and color, and spells retain their gravity-free flight. A saved thrower's name stays with the projectile until that player is available in the same level, then saves as an owner UUID.
- The 26 built-in fixed recipes use `witchery:cauldron_ritual`. The legacy rules retain subset matching, firing priority, and ingredient power. Active fixed casts save their trigger stack in `RitualTrigger`. A dispenser can withdraw a boiling and powered legacy cast with the trigger at the end of `Items`, keeping `RitualTriggered` and the recipe ID in `FixedRitual`. The bottle applies the valid brew effects in order. Brew Fuel retains the original item ID and maps metadata 0–3 to `FuelTier`. Endless Water retains its item ID and maps named legacy top-level `Damage` to modern durability. The Player Compass keeps the original Taglock keys and the supplied 33-frame sprite set.
- A `witchery:taglockkit` costs 1,000 base power to cast a brew on the bound creature or player. The cauldron saves the ordered Taglock and warmup in `Ingredients`, `RitualTriggered`, and `RitualTicks`. Both `decursedirected` and `decurseteleport` keep their original block and block entity IDs and save `WITCPlacer` and `Killers`. The new horizontal block states use network protocol 54.
- The Statue of Broken Curses renders the original wolf model, and the Statue of Occluded Summons uses the stationary Mandrake model. Each uses its original statue atlas, with placed rotation from the saved facing and separate item display transforms.
- Legacy terrain brew subtypes 57, 58, 59, and 61 use named `brew_of_*` items. The Kettle recipes are under `data/witchery/recipes/kettle/` and their projectiles dispatch to `BrewTerrain`.
- Legacy subtypes 62 and 76 decode to `witchery:brew_of_erosion` and `witchery:brew_of_ice`. Both use the standard thrown brew projectile and Brew Bag slots. The original Ice resistance list lives in the `witchery:brew_of_ice_resistant` entity tag.
- Legacy subtype 88 decodes to `witchery:brew_of_infection`. It uses the original Kettle ingredients, three servings, and no bottling bonus. Loose Quicklime retains subtype 16 and uses the shared projectile on right-click.
- Dropped Quicklime reacts with prepared cauldron brews if a player is nearby. The nearest player is excluded from the block-safe explosion. Stored brewing actions and the active recipe trigger contribute to blast power. Dropped Gypsum subtype 17 clears any filled cauldron and uses its entire item stack.
- The cauldron resolves an original `RitualTriggered` cast using the saved final `Items` entry and the matching fixed recipe. Each saved warmup pulse becomes ten ticks. A partially drained cast waits for matching brew, and imported tagged Brew Buckets keep the trigger when recombined.
- Powered fixed recipes use the original misfortune roll after completion. White and Infernal circles adjust altar cost and risk. The cauldron counts Leonard before producing the cast result and uses the original lightning roll sequence.
- Legacy subtypes 116–120 decode to the five `brew_of_solid_*` items. In order, they turn Hollow Tears into stone, dirt, sand, sandstone, or air with erosion beneath. The models use the original solidifier overlay. Subtype 125 decodes to `witchery:brew_of_substitution`.
- Plant Mine metadata 0 through 11 uses `BlockStateTag.variant` on the shared item and `variant` in the placed state. It encodes poppy, dandelion, or shrub in groups of four, ordered Webs, Ink, Thorns, and Sprouting. Legacy stack conversion retains other item tags.
- Kettle `bottling_familiar` accepts `cat`, `toad` and `owl`. Finished batches save `FinishedRecipe` alongside the result. The original 85, 86, 87, 97, 108 and 109 ingredient subtypes map to Frog's Tongue, Cursed Leaping, Hitchcock, Wasting, Bat Ball and Bats.
- Revealing uses `witchery:kettle/brew_of_revealing` for the fixed splash. Named vanilla potion damage 8198 imports as regular Night Vision. Odour of Purity adds Revealing to cauldron mixtures. Heart of Gold adds Animal Attraction and Fermented Spider Eye inverts it to Animal Repulsion. The ordered `Items` list retains the action order and inversion.
- Legacy infused brew subtype 123 maps to `witchery:brew_of_the_grave`. `InfusedBrews` uses the original type, start and Grave cooldown keys.
- Icy Needle subtype 78 adds Cold for two effect levels and 200 altar power. It applies three minutes of base Chilled and freezes connected water at a block impact. The range starts at two blocks and increases by two for each strength boost.
- Frozen Heart subtype 79 adds Ice Shell for five effect levels and 500 altar power. It applies ten seconds of base Chilled to susceptible entities. A direct hit or supported block impact forms Perpetual Ice. A glancing splash applies only the shorter Chilled effect. The shell uses the rite's original outline geometry and clears water, brew gas, and brew liquid inside its radius. The `witchery:ice_shell_resistant` entity tag includes blazes, golems, demons, ents, and boss types. Resistant targets receive water at their feet when the block is replaceable and the dimension permits water.
- `witchery:potion.chilled` keeps the original movement modifier, reduction to incoming fire damage, and periodic magic damage for blazes or at level III and above. Both cold brews keep the strength, duration, and particle suppression modifiers that preceded the ingredient.
- Focused Will subtype 113, Redstone Soup subtype 40 and Hint of Rebirth subtype 33 add `potion.keepinventory`, `potion.keepeffects` and `potion.reincarnate`. Wild Bramble metadata 1 adds `potion.illfitting`. These four recipes use the ordered Cauldron ingredient list and the existing modifiers and dispersals.
- `WITCPoSpawn` in `PlayerPersisted` stores pending effect instances using Forge names. Migration accepts the original vanilla and default Witchery effect IDs. Keeping Inventory preserves the real vanilla inventory until the death clone takes its slots. Modern Vanishing and experience loss run at the normal points.
- Whiff of Magic subtype 34, Spectral Dust subtype 66, Ender Bramble metadata zero, Red Mushroom, Foul Fume subtype 28 and Wheat add `potion.absorbmagic`, `potion.reflectdamage`, `potion.repellattacker`, `potion.poisonweapons`, `potion.stoutbelly` and `potion.feelnopain`. The same Bramble item retains both variants in `Items` and the Cauldron guide. Their status icons use the corresponding ingredient sprites in the modern effect atlas.
- `witchery:absorbable_magic` maps the old magic damage flag to modern damage types. Moonshine keeps its legacy brew name and uses 1.20.1 protection and hunger data. Both food effects have an empty milk cure list. All six recipes use the existing ordered ingredient format and recipe modifiers.
- Sugar Cane, Feather, Cod, and Toe of Frog subtype 90 add `potion.floating`, `potion.featherfall`, `potion.swimming` and `potion.doublejump`. Cod is raw fish metadata zero. Named `minecraft:reeds` and `minecraft:web` import as Sugar Cane and Cobweb. Cobweb adds `potion.reflectprojectiles` or, if inverted, `potion.attractprojectiles`. All six continue to save the original ordered bottle ingredients.
- Protocol 85 uses legacy `clear_fall_damage` for the air-jump request and sends a clientbound approval after checking the player's active Double Jump effect and ground takeoff. Durations and amplifiers use the existing saved effect format and no new player NBT keys.
- Drop of Luck subtype 39 adds `potion.brewingexpertise` or the inverted `potion.insane`. Disturbed Cotton subtype 99, Embermoss, and Mellifluous Hunger subtype 103 add `potion.sinking`, `potion.overheating`, and `potion.wakingnightmare`. A Taglock cast reconstructs the original long duration and hidden particles from the ordered recipe and applies its coven strength limit.
- Soul Sand adds `potion.allergydark` and Salmon adds `potion.allergysun`. Named legacy `minecraft:fish` metadata 1 resolves to Salmon. `minecraft:golden_apple` metadata 1 resolves to the Enchanted Golden Apple and preserves the separate Health Boost recipe. The ordinary Golden Apple produces Absorption.
- Snowball adds the terrain snow action and the entity effect `potion.snowtrail`. Brick stops later creature effects, Nether Brick stops later terrain actions, and Tropical Fish lifts the individual effect strength ceiling. The original ordered ingredient format carries these modifiers without extra NBT fields. Legacy `minecraft:netherbrick` and `minecraft:fish` metadata 2 load as the corresponding modern items.
- Pumpkin, Blue Orchid, and Silver Dust subtype 157 restore the species-targeted damage actions. Named `minecraft:red_flower` metadata 1 resolves to Blue Orchid. `witchery:silver_brew` keeps the original silver classification and armor reduction for shifted players, Wolfmen, and Wolfman reflections. Silver Dust also starts rain or thunder on a block-targeted ritual. These recipes retain the same ordered ingredient format.
- Garlic uses `witchery:garlic` and applies Weakness to vampires, including vampire reflections. A vampire player also loses the original strength-scaled amount of blood. The modern capability clamps the value and synchronizes it to the client.
- Purified Milk subtype 105 and Reek of Misfortune subtype 35 restore Remove Debuffs and Remove Buffs. Taglock purification uses the original removal and worsening rolls. A boosted ordinary purification can cure the Diseased effect and remove nearby disease fluid.
- Subdued Spirit subtype 112 adds Wasting or, after Fermented Spider Eye, Fullness. Condensed Fear subtype 114 lowers Witchery infusion charge according to capacity, strength, and exposure. Creeper Heart subtype 74 extends active buffs and adds the original Queasy penalty. The standalone Brew of Wasting retains its own projectile behavior.
- Skeleton Skull and Creeper Head add Steal Buffs and Spread Debuffs. Named legacy `minecraft:skull` metadata 0 and 4 restore these recipes inside saved mixtures. The actions preserve donor and recipient selection, amplifier limits, remaining durations, and the exposure-scaled number of effects.
- Rotten Flesh adds the `witchery:potion.diseased` curse. That effect uses the original attack-damage modifier UUID and overlay texture. Tracking clients receive updates through vanilla effect packets. Disease fluid and the ordinary Rotten Flesh brew respect the same creature immunities. An inverted Rotten Flesh brew bypasses the immunity check as in 1.7.10. `diseaseBlockRemovalChance` sets the fluid's scheduled removal roll.
- All sixteen wools set the original RGB without spending power, consuming capacity, or entering the ordered `Items` list. The cauldron persists the separate `Color` field and reads the legacy nested fluid `Tag.Color`. Bottle and dispersal tags retain their saved color.
- The twelve original coloring dyes add `witchery:potion.colorful` for 1,800 ticks and one effect level at zero power. The amplifier is the original dye ordinal. The color label and tint use that ordinal. Named `minecraft:dye` and `minecraft:wool` metadata resolve to their modern items. Ink Sac, Cocoa Beans, Lapis Lazuli, and Bone Meal retain their original ingredient identities.
- Breath of the Goddess subtype 32, Slimeball, and Stick restore Air Hike, Frog Tongue, and Knockback. The current delivery path carries the impact position separately from the caster, including for gas cells, ritual rain, and triggered brews.
- Bat Ball subtype 108 and Owlet's Wing subtype 89 restore Bat Burst and Bodega. Bats use the original `WITCNoDrops` key. Bodega uses active Owl familiar mastery and the existing temporary Owl entity. Sunflower adds Poison Toad with the original `SuicideIn` and `Poisonous` fields. Named `minecraft:double_plant` metadata zero imports as Sunflower.
- Bone adds Raising. The raised creatures retain the original weighted Zombie, Skeleton, and Zombified Piglin selection. Normal throws use the existing enslaver name and UUID. Ritual minions use `witchery:potion.mortalcoil` for the strength-scaled lifetime. Spawns require room for the creature in the modern world.
- Poppy, Dead Bush, Clay Ball, and Emerald add Love, Volatility, Fortune, and Resizing. Named legacy Red Flower metadata zero maps to Poppy. Named Tall Grass metadata zero and `minecraft:deadbush` map to Dead Bush. Love uses separate courtship from the fixed Kettle recipe. Volatility's block damage follows the server option. Fortune adjusts the block's loot tool before evaluating the actual loot table. Resizing uses the original scale levels and saves active dimensions independently of the ordered recipe.
- Dandelion, Bone Meal, Apple, Dirt, and Wheat Seeds restore Flowers, Fertilization, Harvesting, Tilling, and Planting. They use the original overlapping horizontal scanlines, individual vertical ranges, and strength-dependent footprint. Planting uses nearby dropped seeds and ordinary Forge placement. Named legacy Yellow Flower and Bone Meal map into the ordered recipe. Dirt metadata one and two stay separate from the ordinary Dirt recipe.
- Brown Mushroom, String, and Flint restore Pruning, Felling, and Pulverisation. Pruning and Felling use their original separate sphere sizes. Pulverisation visits its open sphere once and advances each eligible block by a single conversion. All eight recipes use one effect level.
- Stone adds Dissipate for one level and zero power. It clears Witchery gas within its vertical disk and deals five magic damage per strength tier to spectral undead. At strength one it also recognizes other gaseous fluids through Forge's lighter-than-air property. Sand restores Tide Hold for one level and zero power. Cobblestone restores Lava Hold for two levels and 100 power. The two holds replace only the matching vanilla fluid, with an open sphere of splash radius plus two. `witchery:slurp` preserves the exact fluid level and remaining loaded ticks, including original `Timeout`, `blockName` and `blockMeta` saves. Legacy Sand metadata one maps to Red Sand and stays a separate item.
- Terrain and creature impacts accumulate duration boosts independently. A potion effect resets the creature boost and leaves the terrain boost available to a later terrain action. Tide Hold and Lava Hold use the terrain multiplier and reset it for the next action.
- Vanilla Lily Pad adds Lilify for one level and 200 power. Named legacy `minecraft:waterlily` imports as `minecraft:lily_pad`. The brew scans up from the adjacent face or target creature and places `witchery:lilypad` on the first free water surface. Its `growth` state retains the original two-bit range and schedules spread to nearby source water where space and protections permit it.
- Oil of Vitriol and Netherrack add Erosion and Levelling for two levels and 200 power each. The original Erosion bytecode reuses the impact Y on each pass, so the outer disk defines the cleared layer. It returns only Obsidian and wears the legacy five equipment slots according to exposure. Levelling clears raised terrain and fills the open cells in original material order. Its ritual reduces the working Y by one and adds `(strength + 1) * 3` to the radius. Both use the existing ordered item format and leave the standalone Kettle recipes intact.
- Thrown Web, Vine, and Cactus add Webs, Vines, and Thorns for two levels. They restore `witchery:web`, `witchery:vine` and `witchery:cactus` as their distinct original blocks. Webs expand by strength from the center to faces and then edges. Vines store a horizontal facing and become climbable without the natural vine growth rules. Thorns grow three plus strength blocks high and extend existing Witchery cactus. Vines and Thorns also restore their 90-second targeted effects and reuse the original overlay textures.
- Ent Branch subtype 82 adds Sprouting for two levels and 350 power. Terrain casts grow oriented logs of the struck species with side twigs. Creature casts use the 15-second `witchery:potion.sprouting` effect and periodically grow and lift from beneath the target.
- Oak, Spruce, Birch, Jungle, Acacia, Dark Oak, Rowan, Alder, and Hawthorn Saplings each add a separate tree-growth brew for two levels and 200 power. Forge configured features provide the modern tree forms and respect growth and placement events. Oak and Spruce change at effective strength two. Jungle changes at raw block strength three. Named legacy Sapling and Witchery Sapling stacks normalize their metadata to the modern item or `BlockStateTag.wood_type`.
- Poisonous Potato and Nether Quartz add Blight and Raise Land for four levels and 2,000 power each. Blight uses the original single impact layer, withering and soil conversions, villager and cow conversions, and chance of magic damage to animals. Raise Land moves one column for an ordinary splash or creature cast and uses the original strict disk for a block-targeted ritual. It retains the vertical block states and rolls back movement on a Forge veto.
- Ender Pearl adds Transpose for four levels and 1,000 power. Normal creature casts use three attempts to find footing around the impact. Taglock rituals use the first Waystone and fixed inhibition threshold three. Block rituals use the first and second Waystones in ingredient order, require the original 24-glyph Otherwhere circle at the first site, and move the original three-layer footprint.
- Iron Ingot adds Transpose Ore for four levels and 2,000 power. Strength selects the original Iron, Gold, Lapis, and Emerald order, including each modern Deepslate form. A Waystone ritual expands the footprint with the coven and resumes the four-block descent using the saved cauldron pulse.
- Glint Weed adds Inferno for three levels and 750 power. The legacy `witchery:splatter` projectile launches from the impact, sets supported terrain or struck creatures on fire, and splits at higher strengths.
- Refined Evil adds Hell Gate for five levels and 3,000 power. Creature casts apply the original Hellish Aura with synchronized green fire. An instant Dog Tongue ritual summons a Demon at a valid medium Infernal circle.
- Gold Ingot adds Blast for five levels and 500 power. Brew strength controls the radius of the ordinary destructive explosion. Block casts use the adjacent face and creature casts use the original exposure-scaled chance.
- A `witchery:biomenote` adds Changing Seasons for eight effect levels and 5,000 base altar power. Its `WitcheryBiome` persists in the ordered `Items` list. The block-targeted action forces instant dispersal and supports a Tongue of Dog ritual at a Waystone. The biome writer updates and resends every intersecting quart column, and dry biomes clear snow layers within the original block footprint.

### Optional legacy brew integrations

- Drain Magic and powered Anti-Magic Bolts debit the optional Blood Magic Soul Network using the original Life Essence tiers. Thaumaturge wands use the original whole-Vis debit loop and the current wand's consumption modifier.
- With Ars Magica Legacy installed, both attacks drain a fraction of the target's maximum mana, with the original minimum debit of one. The original in-wall damage exception applies to small Resizing players and players in wolf or bat form when their feet are in a block that does not render as solid.

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
- Uses one `SimpleChannel` (`witchery:main`) with protocol version `"88"`. The extended player payload carries vampire rank, blood power, blood reserve, quest progress, vision, ultimate choice and charges, the transient selected power and the saved creature form. `VampireQuestChunks` preserves the original packed `Location` values and records a modern dimension ID. Player style sync updates the other clients that track a transformed player. Vampire packet intents 15 through 17 select or activate a power and query or display a nearby target's blood. Markup book intent 12 saves the trail through the held journal's unlocked chapter and raises the reader's saved cap. The infusion status includes the active infused brew and remaining duration, and the Kettle recipe codec carries the optional bottling familiar.
- Crystal Ball viewing sends the existing `cam_pos` message for the transient `witchery:eye` or the living subject of a Taglock Kit. Server chunk and entity tracking follow the viewed entity, and the client centers terrain rendering on it. The observing player stays beside the Crystal Ball.
- The Mystic Branch recognizes gestures from server-side player rotations. Protocol 12 retires the prepared-spell packet while recording the old intent in `LegacyRegistryData.PACKETS`. The client previews the shared stroke rules from local rotations and displays the recognized spell. `branchGlyphSet` selects the original texture bank with value 1 and defaults to bank 0. Cancelling the use clears the drawing.
- The `render3dGlintEffect` client option applies to the held Mystic Branch, Huntsman Spear, and enchanted Cane Sword models. It defaults to true and preserves the inventory icons' standard foil. The Huntsman and goblin model glint use their existing independent setting.
- Validates packet receive direction against legacy flow metadata and logs mismatches.
- Represents all legacy packet intents in `LegacyRegistryData.PACKETS` as migration metadata.
- Adds codec-backed payload + handler scaffolding for high-priority sync packets:
  - `player_sync` -> `(UUID playerId, int syncRevision, CompoundTag playerData)`
  - `extended_player_sync` -> `(UUID playerId, boolean initialized, int syncRevision, CompoundTag playerData)`
  - `partial_extended_player_sync` -> `(UUID playerId, int syncRevision, CompoundTag playerData)`
- Adds codec-backed payload + handler scaffolding for additional interaction packets:
  - `item_update` -> `(int slotIndex, Volume volume, int pageIndex, ResourceLocation entryId)` (serverbound, optional biome ID for the atlas or rite ID for Circle Magic)
  - `sync_entity_size` -> `(int entityId, float scale)` (clientbound, protocol 86)
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
  - `sync_entity_size` restores the active Resizing scale on the client. The client uses its local pose and creature form to compute physical dimensions from that scale.
  - `cam_pos` now dispatches to client packet bridge with legacy semantics (`active`, `updatePosition`, `entityId`) and stages camera-active/target data in `WitcheryClientCameraState`.
  - `WitcheryClientCameraHooks` now consumes staged camera state each client tick and applies scaffold camera-entity override/reset behavior (`setCameraEntity(target)` while active, fallback to local player when inactive/invalid target).
- Adds first functional serverbound behavior hooks:
  - `clear_fall_damage` now clears sender fall distance.
  - `item_update` validates the held slot and guide volume. Atlas requests resolve the supplied biome ID against the server's current list and save both `CurrentPage` and `WitcheryBiome`. Network protocol 87 carries the atlas ID.
  - `sync_markup_book` saves the full original `pageStack` trail through the requested unlocked chapter and syncs the learned rank cap.
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

### 1.20.1 Forge Menus and Book Screens
Implementation anchors:
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/LegacyRegistryData.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/registry/WitcheryMenus.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/menu/LegacyPlaceholderMenu.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/client/screen/LegacyPlaceholderScreen.java`
- `/Users/cyberpwn/development/workspace/AuramMods/Witchery/src/main/java/art/arcane/witchery/client/WitcheryClient.java`

Current scaffold behavior:
- menu metadata now tracks both legacy GUI ID and normalized menu key (`LegacyMenuIntent`).
- every legacy GUI entry (`0..8`) registers a dedicated menu type keyed to its legacy menu name.
- The altar, oven, distillery, spinning wheel, Brew Bag, and Leonard's Urn have working Forge menus and screens. Guide volumes and the atlas open the guide screen through the held book packet. Atlas Contents includes the original family order. The held vampire journal opens its reader with sequential navigation and a return to the cover.

ID mapping used by scaffold:
| Legacy GUI ID | Key | Menu Type ID |
|---:|---|---|
| 0 | `altar` | `witchery:altar` |
| 1 | `witchcraft_book` | `witchery:witchcraft_book` |
| 2 | `witches_oven` | `witchery:witches_oven` |
| 3 | `distillery` | `witchery:distillery` |
| 4 | `spinning_wheel` | `witchery:spinning_wheel` |
| 5 | `brew_bag` | `witchery:brew_bag` (eight slots backed by the held bag) |
| 6 | `biome_book` | `witchery:biome_book` |
| 7 | `markup_book` | `witchery:markup_book` |
| 8 | `leonards_urn` | `witchery:leonards_urn` (original layout backed by item inventory) |

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
- Dream and Nightmare cotton drops are available from naturally generated Somnian Cotton. The ordinary Nightmare now uses the original `witchery:nightmare` key with a dedicated entity and renderer. Dreamweaves use `witchery:dreamcatcher` and preserve the five original `WITCWeaveID` values. Demonic nightmares add fireballs overhead and can summon `witchery:demon`. The Waking Nightmare curse and status effect use the same entity outside the Dream.
- `witchery:demon` can also be summoned by two large Infernal rites in the Overworld. The entity persists bargains, remaining stock and whether it came from a rite. Demon Heart and Infernal Blood use legacy ingredient damage values 23 and 80. The heart block retains `witchery:demonheart` and drops the canonical `witchery:demon_heart`. The old port's heart item ID still works in both Infernal Blood recipes.
- Bone Needle and Brew of Grotesque map legacy ingredient damage values 7 and 71. Taglock Kit uses the original `witchery:taglockkit` ID and shows the filled icon when bound. `WITCPlayer1` and the creature UUID halves in `WITCEntityIDm1` and `WITCEntityIDl1` work with the bound-taglock recipe ingredient. The new player UUID is authoritative when present. The Curse of Waking Nightmare persists under `PlayerPersisted.witcheryWakingNightmare`.
- The `witchery:dream` dimension type references its registered client effects for fog and sky. The server chooses the shared mode from the active dreamers, independently of player-style packets. The same mode gives the server and client a day angle of zero in normal Dream and midnight in both nightmare modes, including actual sky darkening.
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
- The `hellhoundSpawnWeight` server setting feeds the Forge Nether biome modifier when the world loads. It defaults to 25, clamps to at least one, and preserves the original groups of one to three.
- Rite IDs 92 and 93 map to `witchery:curse_of_the_wolf` and `witchery:cure_lycanthropy`. Player saves preserve `WerewolfLevel` and the original wolf ordinal 1. Legacy nested data also reads `CreatureType`. Server setting `allowVampireWerewolfHybrids` defaults to true. Protocol 46 includes the two rites and original howl.
- Entity IDs 135 and 137 map to `witchery:wolfman` and `witchery:werevillager`. `Infectious` remains a legacy save field. `FormerVillager` stores the modern villager snapshot and reads the old numeric `FormerProfession` when available. `WITCWereVillagerUUID` preserves the pre-curse Taglock identity through each form and cure. Protocol 47 includes the creatures and their sounds.

## 1.20.1 Event Hook Scaffold (current)

- `allowInterModBlockBreakEvents` defaults to true. Forge block-break cancellation is honored when Witchery terrain effects, spells, or rites would remove or replace blocks. The toggle applies to events posted by Witchery's scripted changes.
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
- `ModHookArsMagica2` maps its mana debit and suffocation exception to Ars Magica Legacy 1.5.0, the build in Auram. The modern API provides mana access for living entities. The inspected 1.5.0 build contains no identified equivalents for the Reconstructor or its Soulbound enchantment hook.
- `ModHookBloodMagic`
- `ModHookForestry` had empty initialization and magic-drain methods in 0.24.1.
- `ModHookMineFactoryReloaded` maps to Industrial Foregoing. The Sower preserves the three saved sapling variants. Forge's crop and bonemeal interfaces and the log and leaf tags cover the eight crops and three trees. The Crusher, Imprisonment Tool, and Duplicator use their respective entity type tags for the 29, 25, and 30 original exclusions. Witchery makes the Crusher apply its full exclusion tag before selecting a target. [Industrial Foregoing 3.5.21](https://www.curseforge.com/minecraft/mc-mods/industrial-foregoing/files/7068937)
- `ModHookMystCraft` excludes Flowing Spirit, Hollow Tears, Brew, liquid brew and brew gas from automatic sea symbols in Auram's Mystcraft 2 Legacy 0.8.0. Mystcraft still reads server-authored symbol definitions and registers the other fluids.
- `ModHookThaumcraft4` maps to Thaumaturge 1.1.3 in Auram. The integration drains carried wands and supplies the base item and entity aspects through Thaumaturge's data maps. The modern Harvest Seal recognizes Witchery crops through `CropBlock.isMaxAge`. Dream and Torment preserve the original dimension exclusions for features, structure starts, initial aura, and natural spawning. Thaumaturge's item aspect lookup reads the current stack variant for saplings, Wicker Bundles, mines, Brambles, and Circle Talismans.
- `ModHookTinkersConstruct` offset the Bark Belt icons for its legacy replacement health display. Auram's Tinkers' Construct 3.11.2.166 has a hotbar overlay for equipment. The port places Bark Belt charges above the current health and armor rows using Forge's `leftHeight` counter.
- `ModHookTreecapitator` maps to the optional Enhanced Falling Trees 0.6.0. Log and leaf tags expose the three species to the felling mod. Witchery's wood state distinguishes adjacent species, and harvesting each log can still awaken an Ent. [Enhanced Falling Trees 0.6.0](https://www.curseforge.com/minecraft/mc-mods/enhanced-falling-trees/files/5899084)
- `ModHookWaila` maps to the optional Jade plugin on Forge 1.20.1. It presents the original cover identities for trapped plants, Alder Doors, both pit blocks, the three illusions, and werevillagers. Rowan Doors use Jade's normal item lookup.
- `ModHookMorph`
- `ModHookBaubles` uses the optional Curios inventory on Forge 1.20.1. The `witchery:vampire_mortality_accessories` item tag contains the three surviving Botania accessories. Botania removed the Ring of the Aesir. [Botania changelog](https://botaniamod.net/changelog.html)

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
- `witchery:village/bookshop` is a 10-by-8-by-9 template with the modern `minecraft:building_entrance` connector. The village pool hook appends one weight-15 entry to each of the five ordinary house pools. Jigsaw assembly limits each village to one shop. This preserves the ordinary legacy limit. The old custom world-size exception for a second shop has no equivalent in standard 1.20.1 village generation.
- `generateBookShops` controls new village assembly. `townBookshopAllowedBooks` accepts extra registered item IDs and the old `id,metadata` form for recognized Witchery subtypes. The processor fills the original chest and four frames during placement. The chest rolls five to ten entries and guarantees `witchery:vampirebook`. The original three Torn Page entries have weights three, two, and one. Frames draw uniformly from the catalogue entries.
- `witchery:village/apothecary` is a 9-by-9-by-8 template with the original sign, furnishings, and chest beneath the filled cauldron. Its pool weight is 15 in all five ordinary village styles. `generateApothecaries` defaults to true, and the one-per-village limit is independent of the bookshop. Current villages use the ordinary legacy cap of one.
- The registered `witchery:apothecary` profession uses the birch-styled `witchery:apothecary_shelf` at the original shelf position. The generated resident spawns with all eight legacy trades. Each starts with seven uses and can restock at the shelf. The chest uses `witchery:chests/village_apothecary`, with the original two to five rolls, weights, and stack sizes.
- `witchery:village/witch_hut` is a 4-by-6-by-6 template. The legacy 4-by-5 footprint gains a front approach step for its road connector. The furnishings are a water cauldron, Rowan log, and potted spruce. The template spawns one persistent `witchery:covenwitch`. The seeded village limit is zero or one, and the hut has weight 10 when eligible. `generateWitchHuts` defaults to true.
- Coven Witches and unleashed Hobgoblins use occupied village POIs to find settled areas. Sheltering examines the covered side of actual wooden doors and uses the usual door-opening pathfinder, so a hut with no bed can provide shelter. The daytime door restriction applies to both species.
- `witchery:village/keep` uses a 16-by-27-by-16 template that includes the legacy keep's top clearance. `townKeepMode` defaults to one, which gives each start a seeded zero-or-one limit at weight 100. Mode two permits one keep wherever the village geometry fits it. Mode zero disables the building. Its ten-entry chest rolls three to eight times. Six guards spawn at the courtyard and east rampart posts.
- `witchery:village/watchtower` uses the 8-by-23-by-8 legacy layout. The `townGuardTowerCluster` settings default to four entries of weight 20 and independently roll a limit of zero or one for each. Each entry stops competing once its own quota is filled. Its eighteen-entry chest rolls two to five times, and four armed guards spawn on the upper lookout.
- The village buildings share the `witchery:village_foundation` processor. Each processor list sets the original support footprint. The keep and watchtower use cobblestone, sandstone, or snow block foundations according to the village style. Desert pieces convert their stonework to sandstone and plankwork to birch. Snowy pieces restore snow construction, with packed ice supports and perpetual ice fence.
- `townWallMode` and `townWallWeight` restore the wall's defaults of seeded eligibility and weight 100. The village roads assemble with a 2-by-7-by-2 anchor. The start replaces it with one saved `witchery:village_wall` piece containing the exterior contour, gate columns, elevations, material palette, and guard posts. Each generating chunk places its part of the same plan. The old `wallgen` ID remains registered for existing data.
- The district footprint extends 20 blocks sideways and seven blocks past street ends. Modern building bounds are included with a small margin. The wall closes short gaps and forms three-wide openings aligned with outward roads. Stone bricks, cut sandstone, and snow blocks match their corresponding legacy palettes. Stair parapets and gate trim follow the relevant wall material, and snowy gate railings use perpetual ice fences.
- The `witchery:village_biomes` Forge structure modifier uses the existing Overworld and village tags. Sandy and badlands additions use desert villages. Snowy, savanna, and taiga additions use their respective styles, with plains as the general land style. Each `townBiome…Allowed` server setting applies to additions. Jungle defaults to false and supports an explicit opt-in. Existing vanilla and datapack structure memberships continue to supply their original eligibility.
- Vanilla building groups use the legacy `town…ClusterGroups`, `Weight`, `Min`, and `Max` settings. The modern equivalents are a small house with a yard for `GardenHouse`, libraries for `House`, other small houses for `WoodHut`, butcher shops for `Hall`, and medium or large homes for `House3`. `SingleField` follows legacy `Field1` and uses the larger plot. `DoubleField` follows `Field2` and uses the smaller plot. `Blacksmith` uses weaponsmiths and `Church` uses temples. Copies keep the original pool element type, projection, and processors. The 1.7.10 Forge registry replaced repeated handlers for the same class. The modern port honors each configured group with its own quota.
- `townZombieAttackReductionMode` defaults to one. Mode zero uses vanilla damage. Modes one and two set zombie damage to a villager to 0.5 health points, and mode two blocks zombie targeting of villagers. Golems ignore Witchery village guards.

## Tile Entity Registration Surfaces
- Auto-registration path: `BlockBaseContainer.func_149663_c` -> `GameRegistry.registerTileEntity(clazzTile, blockName)`.
- The altar keeps the original `CoreX`, `CoreY`, and `CoreZ` links and the core's power and `EnhancementLevel`. A Mystic Branch mounted on any member adds one enhancement. The original source exposes that value without using it to alter spells. Existing modern altars without core links still resolve to their previously selected coordinate.
- Altar capacity groups the modern blocks derived from the original sapling, log, leaf, dirt, tallgrass, red flower, and pumpkin IDs. Flattened mushroom stems draw on unused slots in the two original giant mushroom caps because both old stem variants load as the same block.
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
