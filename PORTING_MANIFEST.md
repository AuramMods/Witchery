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

## Block Model Parity Anchors (current focus)
- `witchery:wolftrap`
  - Legacy class: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/blocks/BlockBeartrap.java` (`new BlockBeartrap(true)`).
  - Legacy render model: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/client/model/ModelBeartrap.java`.
  - Legacy texture: `assets/witchery/textures/blocks/beartrap.png`.
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
| `TRAPPED_PLANT` | `witchery:plantmine` |
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
| `LIGHT` | `witchery:light` |
| `DAYLIGHT_COLLECTOR` | `witchery:daylightcollector` |
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
| `LEONARDS_URN` | `witchery:leonardsurn` |
| `PLAYER_COMPASS` | `witchery:playercompass` |
| `MOON_CHARM` | `witchery:mooncharm` |
| `HORN_OF_THE_HUNT` | `witchery:hornofthehunt` |
| `CREATIVE_WOLF_TOKEN` | `witchery:wolftoken` |
| `BLOOD_GOBLET` | `witchery:glassgoblet` |
| `SUN_GRENADE` | `witchery:sungrenade` |
| `VAMPIRE_BOOK` | `witchery:vampirebook` |
| `DUP_GRENADE` | `witchery:dupgrenade` |
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
| 8 | `broom` |
| 9 | `broomEnchanted` |
| 10 | `attunedStone` |
| 11 | `attunedStoneCharged` |
| 12 | `waystone` |
| 13 | `waystoneBound` |
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
| 41 | `flyingOintment` |
| 42 | `ghostOfTheLight` |
| 43 | `soulOfTheWorld` |
| 44 | `spiritOfOtherwhere` |
| 45 | `infernalAnimus` |
| 46 | `bookOven` |
| 47 | `bookDistilling` |
| 48 | `bookCircleMagic` |
| 49 | `bookInfusions` |
| 50 | `oddPorkchopRaw` |
| 51 | `oddPorkchopCooked` |
| 52 | `doorRowan` |
| 53 | `doorAlder` |
| 54 | `doorKey` |
| 55 | `rock` |
| 56 | `web` |
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
| 83 | `mysticunguent` |
| 84 | `doorKeyring` |
| 85 | `brewFrogsTongue` |
| 86 | `brewCursedLeaping` |
| 87 | `brewHitchcock` |
| 88 | `brewInfection` |
| 89 | `owletsWing` |
| 90 | `toeOfFrog` |
| 91 | `appleWormy` |
| 92 | `quartzSphere` |
| 93 | `happenstanceOil` |
| 94 | `seerStone` |
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
| 112 | `subduedSpirit` |
| 113 | `focusedWill` |
| 114 | `condensedFear` |
| 115 | `brewHollowTears` |
| 116 | `brewSolidStone` |
| 117 | `brewSolidDirt` |
| 118 | `brewSolidSand` |
| 119 | `brewSolidSandstone` |
| 120 | `brewSolidErosion` |
| 121 | `infusionBase` |
| 122 | `brewSoaring` |
| 123 | `brewGrave` |
| 124 | `brewRevealing` |
| 125 | `brewSubstitution` |
| 126 | `brewCongealedSpirit` |
| 127 | `bookBurning` |
| 128 | `graveyardDust` |
| 129 | `binkyhead` |
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
| 140 | `contract` |
| 141 | `contractTorment` |
| 142 | `contractBlaze` |
| 143 | `contractResistFire` |
| 144 | `contractEvaporate` |
| 145 | `contractFieryTouch` |
| 146 | `contractSmelting` |
| 147 | `waystoneCreatureBound` |
| 148 | `kobolditedust` |
| 149 | `kobolditenugget` |
| 150 | `kobolditeingot` |
| 151 | `pentacle` |
| 152 | `doorIce` |
| 153 | `annointingPaste` |
| 154 | `subduedSpiritVillage` |
| 155 | `boltSilver` |
| 156 | `wolfsbane` |
| 157 | `silverdust` |
| 158 | `muttonraw` |
| 159 | `muttoncooked` |
| 160 | `vbookPage` |
| 161 | `darkCloth` |
| 162 | `stake` |
| 163 | `warmBlood` |
| 164 | `lilithsBlood` |
| 165 | `heartofgold` |

## Poppet Variants (`Witchery.Items.POPPET`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/item/ItemPoppet.java`

| Meta | Key | Display Name |
|---:|---|---|
| 0 | `poppet` | Poppet |
| 1 | `protectEarth` | Earth Protection Poppet |
| 2 | `protectWater` | Water Protection Poppet |
| 3 | `protectFire` | Fire Protection Poppet |
| 4 | `protectStarvation` | Hunger Protection Poppet |
| 5 | `protectTool` | Tool Protection Poppet |
| 6 | `protectDeath` | Death Protection Poppet |
| 7 | `protectVoodoo` | Voodoo Protection Poppet |
| 8 | `voodoo` | Voodoo Poppet |
| 9 | `vampiric` | Vampiric Poppet |
| 10 | `protectPoppet` | Poppet Protection |
| 11 | `protectArmor` | Armor Protection Poppet |

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

## Entity Registry (`WitcheryEntities`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryEntities.java`

| ID | Field | Class | Legacy Name |
|---:|---|---|---|
| 92 | `DEMON` | `EntityDemon` | `demon` |
| 93 | `BROOM` | `EntityBroom` | `broom` |
| 94 | `BREW` | `EntityWitchProjectile` | `brew` |
| 95 | `SPECTRAL_FAMILIAR` | `EntityFamiliar` | `familiar` |
| 96 | `MANDRAKE` | `EntityMandrake` | `mandrake` |
| 97 | `TREEFYD` | `EntityTreefyd` | `treefyd` |
| 98 | `HUNTSMAN` | `EntityHornedHuntsman` | `hornedHuntsman` |
| 99 | `SPELL` | `EntitySpellEffect` | `spellEffect` |
| 100 | `ENT` | `EntityEnt` | `ent` |
| 101 | `ILLUSION_CREEPER` | `EntityIllusionCreeper` | `illusionCreeper` |
| 102 | `ILLUSION_SPIDER` | `EntityIllusionSpider` | `illusionSpider` |
| 103 | `ILLUSION_ZOMBIE` | `EntityIllusionZombie` | `illusionZombie` |
| 104 | `OWL` | `EntityOwl` | `owl` |
| 105 | `TOAD` | `EntityToad` | `toad` |
| 106 | `CAT_FAMILIAR` | `EntityWitchCat` | `cat` |
| 107 | `LOUSE` | `EntityParasyticLouse` | `louse` |
| 108 | `EYE` | `EntityEye` | `eye` |
| 109 | `BABA_YAGA` | `EntityBabaYaga` | `babayaga` |
| 110 | `COVEN_WITCH` | `EntityCovenWitch` | `covenwitch` |
| 111 | `PLAYER_CORPSE` | `EntityCorpse` | `corpse` |
| 112 | `NIGHTMARE` | `EntityNightmare` | `nightmare` |
| 113 | `SPECTRE` | `EntitySpectre` | `spectre` |
| 114 | `POLTERGEIST` | `EntityPoltergeist` | `poltergeist` |
| 115 | `BANSHEE` | `EntityBanshee` | `banshee` |
| 116 | `SPIRIT` | `EntitySpirit` | `spirit` |
| 117 | `DEATH` | `EntityDeath` | `death` |
| 118 | `CROSSBOW_BOLT` | `EntityBolt` | `bolt` |
| 119 | `WITCH_HUNTER` | `EntityWitchHunter` | `witchhunter` |
| 120 | `BINKY_HORSE` | `EntityDeathsHorse` | `deathhorse` |
| 121 | `LORD_OF_TORMENT` | `EntityLordOfTorment` | `lordoftorment` |
| 122 | `SOULFIRE` | `EntitySoulfire` | `soulfire` |
| 123 | `IMP` | `EntityImp` | `imp` |
| 124 | `DARK_MARK` | `EntityDarkMark` | `darkmark` |
| 125 | `MINDRAKE` | `EntityMindrake` | `mindrake` |
| 126 | `GOBLIN` | `EntityGoblin` | `goblin` |
| 127 | `GOBLIN_MOG` | `EntityGoblinMog` | `goblinmog` |
| 128 | `GOBLIN_GULG` | `EntityGoblinGulg` | `goblingulg` |
| 129 | `BREW2` | `EntityBrew` | `brew2` |
| 130 | `ITEM_WAYSTONE` | `EntityItemWaystone` | `item` |
| 131 | `DROPLET` | `EntityDroplet` | `droplet` |
| 132 | `SPLATTER` | `EntitySplatter` | `splatter` |
| 133 | `LEONARD` | `EntityLeonard` | `leonard` |
| 134 | `LOST_SOUL` | `EntityLostSoul` | `lostsoul` |
| 135 | `WOLFMAN` | `EntityWolfman` | `wolfman` |
| 136 | `HELLHOUND` | `EntityHellhound` | `hellhound` |
| 137 | `WERE_VILLAGER` | `EntityVillagerWere` | `werevillager` |
| 138 | `VILLAGE_GUARD` | `EntityVillageGuard` | `villageguard` |
| 139 | `VAMPIRE_VILLAGER` | `EntityVampire` | `vampire` |
| 140 | `GRENADE` | `EntityGrenade` | `grenade` |
| 141 | `LILITH` | `EntityLilith` | `lilith` |
| 142 | `FOLLOWER` | `EntityFollower` | `follower` |
| 143 | `WINGED_MONKEY` | `EntityWingedMonkey` | `wingedmonkey` |
| 144 | `ATTACK_BAT` | `EntityAttackBat` | `attackbat` |
| 145 | `MIRROR_FACE` | `EntityMirrorFace` | `mirrorface` |
| 146 | `REFLECTION` | `EntityReflection` | `reflection` |

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

## Prediction Registry (`PredictionManager`)
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/WitcheryRecipes.java`

| ID | Class | Category | Key |
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

## Recipe System Registries
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

## Dimensions
Source: `/Users/cyberpwn/development/workspace/AuramMods/Witchery/old-1.7.10/com/emoniph/witchery/Witchery.java`

- Dream: `WorldProviderDreamWorld`
- Torment: `WorldProviderTorment`
- Mirror: `WorldProviderMirror`

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
