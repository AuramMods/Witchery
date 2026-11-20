# Witchery 1.7.10 -> 1.20.1 Porting Plan

## Working Style
- Breadth first first, depth second.
- Prefer creating full vertical skeletons (all systems present, minimal behavior) before polish.
- Check `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING_MEMORY.md` before starting work each session.
- Check `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING_MANIFEST.md` before hunting for source details.

## Witch's Oven Milestone (2026-11-19)

- [x] Register all six slab item IDs as their corresponding block items while preserving the existing registry names. Single slabs use the modern double state when stacked. Legacy double blocks also drop two single slabs.
- [x] Run data generation without the Auram extras and package the results. Java 17 `runData` generated 112 block loot tables and the legacy block tag. The reobfuscated `jar` task succeeded.
- [x] On a disposable headless server, verify the single ice and snow drops, all three legacy double-slab drops, and the modern double witchwood slab state. All six runtime loot checks passed.
- [x] Register oven-related ingredient variants with explicit legacy metadata mappings. Generate and package nine recipes, including the original oven and clay jar patterns and sapling-to-ash smelting.
- [x] Verify soft-jar firing, vanilla and Witchery sapling ash, and witch-log charcoal in hopper-fed vanilla furnaces on a headless dedicated server. All four runtime checks passed.
- [x] Wire the oven block entity, inventory, fuel, funnel timing, fume rolls, and menu. Both old block IDs preserve facing and use the modern `lit` state for rendering.
- [x] Verify server-side oven behavior across a restart. An active batch resumed from 100 ticks of progress at shutdown and completed after reopening the same world. Hopper-fed tests exercised all seven fume outputs and verified that acacia ash leaves the jar intact.
- [x] Verify oven inventory conversion using legacy stacks after their item IDs have been resolved to namespaced strings. Known ingredient damage values map to modern IDs and preserve unrelated tags. Unconverted metadata survives another save.
- [x] Verify oak fume and ash from the reobfuscated JAR on installed Forge 47.4.10. The headless test passed with Witchery alone and with the bundled Auram development mods. CoFH Core logged a client-mixin warning in the latter profile.
- [x] Verify the real client menu and shift-clicking. An isolated muted Forge client joined a dedicated server in survival, crafted the oven and four soft clay jars from supplied base materials, fired the jars, placed the oven, inserted inputs, and collected eight ash and two oak fumes through the regular UI.
- [x] Restore the oven's multipart model and texture atlas. Verified the new baked model on the isolated Forge client.
- [x] Grow all three saplings via bone meal, preserve variant drops, and craft four matching planks per log. Verified all three trees, blocked growth, log and leaf loot, client-side planks crafting, and leaf decay with a real client nearby.
- [x] Restore the crop inputs for cauldron brewing. The original weighted grass seeds, eight growable crops, and live mandrake harvest are in place. Packaged headless checks passed for all crop and grass loot cases, and the muted client verified planting, growth, mandrake nausea, root pickup, earmuff protection, and artichoke water placement.
- [x] Restore Anointing Paste, the Witch's Cauldron, and its first unpowered brew. Three buckets of water and fire underneath allow mandrake root and Exhale of the Horned One to brew six Mutandis when an egg is added last.
- [x] Verify the cauldron with the reobfuscated JAR on headless Forge. All 15 checks passed, including rejected ingredients and an early egg, two-bucket behavior, either ingredient order, and six Mutandis after restarting an active ritual at 39 of 210 ticks.
- [x] Verify the normal client interactions. In the isolated muted survival session, craft the paste from supplied seeds, anoint and fill the cauldron with three buckets, drop in the root from a harvested mandrake and the fume from the oven test, add an egg, collect six Mutandis, and mutate an oak sapling to alder.
- [x] Harvest the mutated alder sapling and verify its saved wood variant. Check that Mutandis changes the potted specimen in place through the normal client interaction.
- [ ] Verify the full sapling acquisition route from naturally sourced materials in a new survival world.
- [ ] Finish oven parity for smelting experience and legacy sapling smelting configuration.
- [x] Restore the six-block altar, its rowan-log recipe, nature-based power, initial altar artifacts, and multiplayer power display. Fifteen packaged headless checks passed, including invalid shapes, capacity caps, bonuses, and saving 200 power across a restart.
- [x] In the muted survival session, craft three altar blocks from supplied ingredients, place them to complete a partially built altar, and verify the power display updates from 40 capacity at x1 to 80 at x3 after adding a skull and torch.
- [x] Restore the distillery with altar power, clay jars, two ingredient slots, and four output slots. Its original layout and model display progress and loaded jars. Six distillation recipes work, including the gypsum and Tear of the Goddess needed for ritual chalk.
- [x] Verify 19 packaged headless distillery checks. The first batch resumed at 82 of 800 ticks after restart, paused at 82 when the altar was broken, and finished when the altar reformed. Checks covered six recipe outputs, the 16-block range, jars, blocked output slots, old ingredient metadata, and gypsum clearing an unheated cauldron. The gypsum recipe also passed with the bundled Auram development mods.
- [x] On the muted multiplayer client, craft the distillery and quicklime from supplied materials, place it by the altar, shift-click ingredients and jars into the menu, and collect gypsum, Oil of Vitriol, and slime through the output slots.
- [x] Restore the final two distillation recipes. Demon Heart with Diamond Vapour yields two stacks of two Infernal Blood and one Refined Evil. With netherrack it yields Soul Sand and two separate Infernal Blood jars. Both recipes passed in packaged Forge, including the previous port's Demon Heart item ID.
- [x] Restore Ritual Chalk crafting and the Golden, Otherwhere, and Infernal Chalk conversions. Drawing uses the original glyph textures and chalk sound. All 11 packaged server checks passed, including support breakage, an idle ritual without an altar, and the Golden Chalk power discount from two white rings.
- [x] Restore the Heart Glyph and the Rite of Charging. The rite needs a 16-glyph white ring and a 28-glyph white ring. Five offerings and 2,000 altar power produce a Charged Attuned Stone. All 10 packaged headless checks passed, including redstone activation and cancellation, restart, and refunding offerings when the altar is out of range.
- [x] Restore Arthana crafting, placement, the 32-block altar range upgrade, and the vanilla creature drops for brewing and skulls. Ten packaged server checks passed for the range boundary, persistence, blade damage, and machine pause after removal.
- [x] Run Forge GameTest in a disposable profile. Four tests passed for placing and recovering a damaged Arthana, bat and wolf drops with Looting, stacked chalk splitting and durability, and starting and completing the Rite of Charging through the Heart Glyph use interaction.
- [x] Verify chalk, Heart Glyph, and placed-item rendering in the muted multiplayer client. Craft Ritual Chalk and an Arthana, draw the white and Heart Glyphs, and mount the blade. Drop the five offerings into a prepared ring, activate it with right-click, and pick up the Charged Attuned Stone. The altar was 21 blocks away.
- [x] Restore Mutandis Extremis. Brew nether wart with Mutandis as the trigger, retain crop growth when mutating to another crop, and support the original mycelium and blue clay transformations. Five packaged cauldron checks and the two Forge GameTests for Extremis passed.
- [x] Restore the Witchery Kettle and its original crafting pattern. Fill it with a bucket of water over fire, throw six ingredients into the basin, and bottle the result through the use interaction or Forge item automation. Colored liquid, stored bottles, and the suspended kettle render from the supplied texture atlas.
- [x] Restore the Drop of Luck cauldron ritual and the first three kettle recipes. Brew of Ink splashes blindness and clears nearby mobs' targets. Brew of the Depths supplies air underwater for five minutes and inflicts Wither II out of water. Redstone Soup consumes 1,000 altar power and grants Health Boost II for two minutes.
- [x] Run 18 packaged headless kettle checks on Forge. Test either ingredient order, mismatched and cold batches, hopper input, the one-time altar charge, normalized legacy inventory and fluid data, and restart with partial and finished brews.
- [x] Run all nine disposable Forge GameTests. The three kettle tests cover manual and automated bottling, fill and drain, Ink projectile impact and splash distances, and the two drinkable effects.
- [x] In the muted multiplayer client, craft and place the kettle from supplied materials, light the fire, fill the kettle, throw the six Ink ingredients, and bottle all three portions by hand. Throw Ink at a husk and verify the splash on a second husk. Drink a supplied Brew of the Depths to verify Wither II on land and sustained air underwater.
- [x] Restore the Candelabra, Chalice, and Filled Chalice recipes, supplied models and textures, drops, and original altar bonuses. The Candelabra adds two to the recharge multiplier. The empty and filled chalices add one and two to the capacity multiplier, respectively.
- [x] Pass 12 packaged artifact checks and all 11 disposable Forge GameTests. Verify block and item conversion fixtures, single drops when a chalice loses its support, and filled state and altar bonuses across restart.
- [x] In muted survival, craft two chalices and a candelabra from supplied ingredients, fill one chalice with supplied Redstone Soup, and place all three on an altar. Verify the rendering and the altar's threefold capacity and recharge.
- [x] Restore crafting, recipes, the original menu, and powered animation for the Spinning Wheel. It consumes 0.6 altar power per tick for 300 ticks. The original four recipes yield cobweb, Golden Thread, Fanciful Thread, and Tormented Twine.
- [x] Verify all four spinning recipes, hopper input, blocked output, legacy fibre and modifier data, and a power interruption across restart on packaged Forge. All 15 checks and all 12 disposable Forge GameTests passed. The packaged Golden Thread recipe also passed with the bundled Auram development mods.
- [x] Craft and place the wheel in muted survival, shift-click a hay bale and Whiff of Magic into the original menu, and collect three Golden Threads. The test used supplied starting materials and an existing altar.
- [x] Restore the ingredient path for Brew of Sleeping. Mature snowbell yields an Icy Needle at the original 20% rate. Milk or cake crafts three Purified Milk jars, and the original kettle recipe produces three Brews of Love. The Love splash puts adult animals in love and pairs adult villagers. Six packaged checks and all 14 disposable Forge GameTests passed.
- [ ] Implement the Brew of Love interaction for enslaved zombies when the enslavement system is ported.
- [x] Restore the original kettle recipe for Brew of Sleeping, tangible sleeping bodies, and separate Overworld and Dream inventories, including health and hunger. An Icy Needle wakes the sleeper, as does lethal Dream damage or destruction of the sleeping body.
- [x] Generate Somnian Cotton in suitable Dream biomes, spread it by source Flowing Spirit, and harvest Wispy Cotton in a normal dream or Disturbed Cotton in a nightmare. Clean packaged world generation produced cotton in 16 of 1,296 inspected Dream chunks and zero of 2,025 Overworld chunks. All 15 disposable Forge GameTests passed.
- [x] In the muted survival client, brew and bottle three Brews of Sleeping, enter a nightmare, and restart while the body and Dream inventory are active. Wake with an Icy Needle and verify that damaged gear and Overworld health return, that Disturbed Cotton crosses dimensions, and that ordinary Dream items stay behind. Reenter to confirm those items return and verify that destroying the body or taking lethal damage wakes the sleeper.
- [x] Restore the ordinary Nightmare encounter with personal targeting, a 30-second post-kill delay, armor swipes, a protective charm, and the original model, animation and sounds. The muted multiplayer client attracted one naturally, demonstrated its defense window, killed it and brought Mellifluous Hunger back to the Overworld. All 17 disposable Forge GameTests passed.
- [x] Restore the five Dreamweave wall hangings, four recipes, the shared wall block, original textures and effects at dawn. The muted client crafted the Dreamweave of Nightmares using the two specified splash potions, mounted it, and used a bed to verify Weakness and the nearby Fleet Feet weave's Slowness. The legacy weave fixture restored the correct variant and loot. All 19 disposable Forge GameTests passed.
- [x] Verify normal and nightmare cotton in the muted multiplayer client. The Dreamweave of Nightmares and the legacy cotton and source-fluid arrangement produced a normal dream. Harvest and carry home one of each cotton, supplement each stack for the four-fibre recipes, and collect Fanciful Thread and Tormented Twine from powered Spinning Wheels.
- [x] Brew Flowing Spirit only in the Dream, distil it into Focused Will, Condensed Fear, and two stacks of four Brews of Hollow Tears, and craft the Dreamweave of Intensity. Fourteen packaged checks and all 19 disposable Forge GameTests passed. The muted client bottled three portions in the Dream, carried them across waking, and threw one to make a flowing pool that gave Regeneration. Fluid sprites prevent the client crash observed in the earlier scaffold.
- [x] Restore the rite that creates the Necromantic Stone, the Rite of Manifestation, and the two-by-two snow-frame Spirit Portal. Seven packaged checks covered both portal axes, invalid frames, collapse on block removal, and the night-only stone rite. The full 19-test Forge suite passed.
- [x] Activate Manifestation by hand using the prepared purple ring and offerings. The player received 150 seconds, retained the stone, and walked through the portal to manifest. The muted client verified early return by Icy Needle, dropping Overworld items on return, timer and sleeping-body persistence across restart, expiry, and restoration of the original Overworld inventory after waking.
- [x] Restore the Demon, its original model, both large Infernal summoning rites, and the trade that supplies Demon Hearts. Nine packaged checks passed for summoning, offering refunds, heart drops, and distillation. The 21-test Forge suite also verified the stock of two hearts, trade persistence, the charm, and Demon Heart use.
- [x] Use the prepared Infernal ring by hand in muted survival. The summoned Demon sold both hearts for two magma creams each with Devil's Tongue, which lost ten durability. Sneak-place the hearts. Drink a Sleeping brew beside them, a Dreamweave of Nightmares, three fires and a Flowing Spirit source. Both types of fireball, an ordinary Nightmare, and a Demon spawned during the demonic dream.
- [x] Restore the two crafting recipes for taglocks, collection from creatures and beds, the filled item model, and the server's collection restrictions. The muted client crafted eight Bone Needles, made three kits, sampled a cow, and collected its own taglocks at a bed.
- [x] Brew Grotesque using the original six ingredients and 500 altar power. The muted survival client bottled all three portions, drank one, and verified that the Demon mask rendered and the aura repelled a cow.
- [x] Restore Waking Nightmare and its two rites. The muted player used a real taglock and brewed Grotesque in the medium Infernal ring. The rite refused to start under clear skies, then applied the persistent curse and spent 10,000 power during a thunderstorm.
- [x] Verify the Waking Nightmare curse across death and a dedicated restart, reject an empty kit, and refund all five cure offerings for an offline target. In the muted client, the separate status effect spawned a Waking Nightmare. The rite's curse kept it pursuing the same player after the effect was cleared. The player used the small white ring and a second self taglock to remove the curse and Weakness, and the Nightmare despawned.
- [x] Restore the Dream's pink daylight, green nightmare fog, red demonic fog, and starless sky. Two isolated muted clients on one dedicated server verified that another player's nightmare changes the entire dimension's atmosphere. The normal dreamer rejoined during a demonic nightmare and saw the red fog. When the other player woke, the atmosphere returned to pink.
- [ ] Finish the remaining altar artifacts, kettle recipes, the other powered cauldron recipes, and the remaining chalk rites.

## Mirror Milestone (2026-11-19)

- [x] Generate the Mirror's stacked nine-block chambers using the original 16-block cell indices. The walls use the original animated liquid textures, cool tint, collision, and unbreakable behavior.
- [x] Verify 17 packaged headless geometry and restart checks. Upgrade a disposable world from the previous port, preserve its existing Mirror chunk, and verify that a freshly generated chunk contains the new chambers.
- [x] Restore the 2,000-power Rite of the Mirror. The muted survival client used the medium white ring with Hollow Tears, gold, a glass pane, and a living Demon. The rite consumed the Demon and produced a Mirror that mounted as a two-block frame with the original model.
- [x] Walk through the frame to a linked chamber. The Reflection killed the tester, persisted as a single boss through death, re-entry, and a dedicated restart, and copied the tester's skin and armor. A real survival hit respected the damage cap. After shortening the final fight in the test fixture, the player killed the boss with an axe and collected its Demon Heart. Both frames recorded the kill.
- [x] Walk out through the chamber's exit, break and pick up the exterior Mirror, and remount it with its original chamber and defeated state. Walk through the defeated frame and emerge at an opposing frame in the Overworld. The fixed build crossed both dimensions without a movement warning.
- [x] Verify both chat escapes. The muted client returned to its chamber's entrance or left for the Overworld, and a restart preserved the remaining cooldowns. The older port's Mirror block item mounted the same two-block frame.
- [x] Verify vertical linking, the 3,000-power passage through a chamber wall, and exiting through a Mirror carried by another player. The two muted clients confirmed that a carrying player can move while the chamber occupant is inside and that the exit follows the carrier.
- [ ] Restore the Mirror Face, conversations, disguises, and Mirror-related poppet and duplicate grenade behavior. Finish Reflection's advanced attacks and the shorter world setting.

## Torment Milestone (2026-11-19)

- [x] Generate the six original 31-by-31-cell mazes at their legacy elevations. Restore the invisible solid force walls, Torment Stone floors, cache rooms, and two-block exit portals. The surrounding dimension is void.
- [x] Restore the original three Refilling Chests per floor. They use modern dungeon loot, populate when first loaded, and refill only when empty after an hour of loaded time. Their inventory and `WITCLifeTicks` survive dedicated restarts.
- [x] Verify 59 packaged headless checks covering all six entries, exits, and central caches, plus the maze boundaries, first fill, repeat refill, and save persistence. A separate generator fixture found a route from entry to exit and each cache on every floor of 100 seeds.
- [x] Verify the chest lid, inventory, and loot collection in the muted survival client. Preserve a saved marker in a previous port's Torment dimension. Generated Nether chunks extend beyond that marker and still occupy part of the original maze footprint.
- [x] Restore the animated red exit portal and collision travel. The muted client entered all six original floor elevations and exited to the configured Overworld respawn point. All 27 Forge GameTests passed, including the normal and rare exit requests with fixed random seeds.
- [x] Route upgraded saves to an intact maze. The muted client used the separate maze at X 1032, checked that the old diamond marker remained, and walked through the new exit to the original Overworld respawn point. After a dedicated restart it entered the same maze footprint and the marker was still present.
- [x] Restore the Lord of Torment's 500 health, ordinary and demonic damage caps, fire immunity, ranged volley, and maze phase. The muted survival client dealt a capped axe hit and triggered the phase transition with another player in range. Both arrived at the same floor and only one 250-health Lord spawned. All 29 required GameTests passed.
- [x] Verify the same 250-health Lord after a dedicated restart. The muted client defeated it with an axe after a fixture shortened the remaining fight, collected two enchanted books, a Demon Heart and the Brew of Soul Torment, and drank the brew to save its Tormentum knowledge. It exited through the generated portal.
- [x] Restore the Contract of Torment's standing-stone channel. The muted survival client tested interruption and a complete channel using legacy infusion charge loaded from the fixture. A complete channel spent 80 charge, consumed the contract, and summoned one 500-health Lord of Torment. The standing stones remained with mob griefing disabled.
- [x] Restore survival infusion acquisition and recharging for the Contract of Torment. The muted client brewed and bottled Soul of the World, cast the Overworld infusion with a bound Death Protection Poppet, and then drew 20 charge from that infusion with the contract.
- [x] Restore the Infernal infusion. The muted client brewed Infernal Animus with a used Voodoo Poppet, sacrificed it inside small and medium Infernal rings, and acquired 200 charge with a Death Protection Poppet.
- [x] Restore the Mystic Branch and the four castable Soul Brew spells.

## Familiars and Covens (2026-11-19)

- [x] Restore cat familiars through the 8,000-power Rite of Binding a Familiar. The muted survival client tamed a vanilla cat with cod, bound it with a medium white circle, and kept its name. The rite returned all five offerings and altar power when the circle had no eligible pet.
- [x] Restore familiar fire immunity, armor, healing, and damage sharing. The client observed a ten-point hit split into nine for the owner and one for the cat, then healed the cat with cod. All 31 required Forge GameTests passed, including legacy owner tags and the offline owner's protection.
- [x] Restore the 1,000-power Rite of Calling a Familiar and sneaking dismissal. The muted client recalled the same cat from Torment, dismissed the seated cat, and called one replacement with the saved name and 50 health.
- [x] Verify the owner's familiar record, cat UUID, and health across a dedicated restart. The disposable world confirmed that a fatal hit on a bound cat dismisses it and kills its online owner, as in the legacy mod.
- [x] Restore the Coven Witch entity, original skins and voice, forest and swamp spawn weights, and seven recruitment quests. An active familiar unlocks an offer. The accepted quest records the recipient, and the six-member coven uses the legacy player data shape. All 34 required Forge GameTests passed, including a legacy name-only offer, a restarted fetch quest, and the fight quest's bound trophy.
- [x] Restore the Quartz Sphere, Happenstance Oil and the night-only Rite of the Crystal Ball. The kettle test verifies the extended Night Vision potion. In the muted survival client, Angelique Redfern refused the player without a familiar and offered the Crystal Ball quest after the player recalled Bramble. The player crafted the sphere, bottled the oil, cast the rite, handed over the ball, and recruited Angelique. Her membership persisted on the dedicated server after restart.
- [x] Restore the night-only Rite of the Seer Stone, readings and coven calls. The muted survival client made the stone, read the player's familiar, coven and spell knowledge, and called Angelique. Activating the Rite of the Necromantic Stone with the Seer Stone moved the same witch to the circle. She left after the rite, and she remained in the player's coven. All 35 required Forge GameTests passed.
- [ ] Restore owl and toad familiars, their specialized abilities, and the cat's curse bonuses.
- [x] Restore the medium Infernal Imp rite and ownership contract. The muted survival client verified the refund when activating without the Seer Stone and summoned a 50-health Imp with the stone. It signed the contract with its own Taglock Kit, verified the refusal at six levels, paid 25 levels, and bound Zul.
- [x] Restore the Imp's gift progression, cooldown and original Soul Brew knowledge. The muted client verified the cooldown, advanced the fixture clock for subsequent gifts, and received Hunger, Fear, Anguish and the Contract of Torment. The three brews added their spell knowledge. All 38 required Forge GameTests passed, including signed ownership, saved gift progress and power, and the full contract channel.
- [ ] Restore the Imp's other signed spell contracts.
- [ ] Restore legacy potion bottling skill and include it in Seer Stone readings.
- [ ] Restore the Crystal Ball's predictions and the coven witch's additional combat and ritual behaviors.

## Overworld Infusion and Death Protection Poppet (2026-11-19)

- [x] Restore the Poppet, Death Protection Poppet, and Taglock binding recipes. The muted survival client crafted both shapes, filled a Taglock Kit at the bed, and bound the poppet to itself. The lethal infusion consumed the poppet and restored its owner from six to ten health.
- [x] Restore Soul of the World in the kettle, using Redstone Soup, extended Regeneration, an Enchanted Golden Apple, Attuned Stone, Mandrake Root, and Rowan Sapling. The muted client filled the kettle and bottled two servings. The Forge tests distinguish Rowan from Alder and Hawthorn.
- [x] Restore the Overworld infusion's two white rings, 4,000 altar power and 200 personal charges. All 40 required Forge GameTests passed, including the old poppet metadata, a bound poppet's lethal damage protection, and the charge cap.
- [x] Restore the recharging rite with a standard Regeneration potion and upkeep of 40 altar power per second. The dedicated restart retained the active rite and the player's 180 charge outside the circle. The muted client reentered, reached the 200-charge cap, and stopped the rite at the heart.
- [x] Restore the Light and Otherwhere infusion recipes.
- [x] Restore the infusion charge HUD and the Witch's Hand drop.
- [x] Restore the Poppet Shelf.
- [x] Restore the Vampiric Poppet and Poppet Protection.

## Infernal Infusion and Voodoo Poppets (2026-11-19)

- [x] Restore crafting and Taglock binding for Voodoo Poppets and their protection. The muted client bound a pig, pricked it to spend one Bone Needle and half a point of health, and pushed it with a charged poppet. All 42 required Forge GameTests passed, including the old signed poppet's saved wear.
- [x] Restore Infernal Animus in the kettle using Redstone Soup, a strong Harming potion, Voodoo Poppet, Demon Heart, Refined Evil and Blaze Rod. A hopper fed the used, bound Voodoo Poppet into the kettle, and the muted client obtained two portions using strong splash Harming. GameTests also cover the strong drinking potion.
- [x] Restore the 4,000-power Infernal infusion rite. The muted client replaced its Overworld infusion with ID 4 and 200 charge. The bound Death Protection Poppet was consumed and restored ten health.
- [x] Verify Voodoo protection in the muted dedicated-server client. A self-targeted cast reflected from a matching ward and added 350 wear to both poppets.
- [x] Extend Voodoo protection to Poppet Shelves.
- [ ] Restore Witch Hunter equipment and its interactions with wards.

## Mystic Branch and the Soul Brew Spells (2026-11-19)

- Brew one serving of Mystic Unguent with Redstone Soup, extended Strength, Diamond, Rowan Sapling, Creeper Heart and Infernal Blood at 3,000 altar power. Sacrifice it with an Ent Branch inside the small and medium white circles at night with another 3,000 power. The Imp's extra gifts can supply the Ent Branch.
- Hold use with the Mystic Branch, draw the directional strokes, then release to cast. The server recognizes six original variants per spell and checks infusion, learned knowledge, charge and cooldown. All four learned Soul Brew spells work with any infusion. The legacy Infernal restriction applies to curses that have no knowledge key.
- [x] The muted survival client filled the kettle, bottled the unguent, and dropped both offerings at the heart. The rite left its offerings at noon and produced a Mystic Branch at midnight. The branch and infused player's 200 charges remained after a dedicated restart.
- [x] The muted client drew and released Carnosa Diem, Ignianima and Morsmordre. The player's health fell from 20 to 18 and its charge reached 199 after Carnosa Diem. Ignianima lowered a pig's health from 10 to 7 and cost two charge. Morsmordre summoned a Dark Mark and cost 20 charge.
- [x] The muted client rendered the Dark Mark's original model and smoke. Tormentum moved a second survival client to a maze floor, removed the nearby pig, and spent 25 charge. Repeating the gesture during the cooldown preserved charge. The dedicated server saved the original `tormentumLastUse` field, remaining charge and the second player's Torment dimension.
- [x] Java 17 data generation and reobfuscation succeeded on Forge 47.4.10. All 46 required Forge GameTests passed, including the long symbol variants, unknown spell rejection, Dark Mark lifespan, and Tormentum's impact against creatures and charge cooldown.
- [ ] Restore the remaining symbols, enhanced casting with Worship, and the in-game symbol book.

## Light and Otherwhere Infusions (2026-11-20)

- [x] Restore the Fire Protection Poppet with a Poppet, two Embermoss and two Wool of Bat. The muted survival client crafted it, filled a Taglock Kit at its bed, and bound the poppet to itself. Lethal fire consumed the ward and granted three seconds of Fire Resistance. The Forge tests verify that the fire ward protects before a Death Protection Poppet.
- [x] Brew Ghost of the Light with Redstone Soup, extended Invisibility, extended Fire Resistance, a Fire Protection Poppet, a Torch and Dog Tongue at 4,000 altar power. The muted client fed the crafted poppet into the kettle and bottled two servings. Its daytime white-circle rite granted Light infusion ID 1 and 200 charge. The player used the Mystic Branch to cast Carnosa Diem with the new infusion.
- [x] Brew Spirit of Otherwhere with Redstone Soup, extended Swiftness, two separate Eyes of Ender, Drop of Luck and Wool of Bat at 4,000 altar power. The muted client used extended splash Swiftness, bottled two servings, and completed the daytime rite inside small and medium Otherwhere circles. The rite granted infusion ID 3 and 200 charge.
- [x] Both rites consumed a bound Death Protection Poppet and restored six health to ten. All 49 required Forge GameTests passed, including the legacy variants, and Java 17 data generation and JAR reobfuscation succeeded.
- [x] A dedicated restart retained Otherwhere ID 3, all 200 charge, and the remaining portions of both kettle brews. The consumed ward stayed gone.

## Witch's Hand and Otherwhere Powers (2026-11-20)

- [x] Restore the Witch's Hand at `witchery:witchhand`. Witches drop it on player kills, with the original higher chance when killed with an Arthana. A muted survival player killed a witch with an Arthana and picked up its Hand on a dedicated server.
- [x] Restore the personal charge meter using the four original block icons and the configurable left or right placement. The muted client displayed the Nether Portal meter and Otherwhere glyph on joining with 200 charge.
- [x] Restore Otherwhere targeting, distance while channeling, blink, recall, lifting a creature and the caster, and recall with a companion. Forge GameTests verified blink and lift, including the four legacy recall fields and the Hand's saved cooldown. All 52 required tests passed, and Java 17 data generation and reobfuscation succeeded.
- [x] The muted survival player blinked from Z 28.5 to Z 37.69 for one charge, remembered a position, returned to it for two charge and raised a pig and the caster for two charge. Ender Inhibition III preserved the player's position and charge. Sneak-attacking a pig in the Nether recalled both the player and the pig to the marked Overworld point for four charge.
- [x] The dedicated save retained the Hand, its cooldown tag, both recalled entities, all four legacy recall fields and 191 charge after a full restart.

## Light Infusion Hand Powers (2026-11-20)

- [x] Restore sustained cloaking for one charge every 30 ticks and clear nearby hostile targets. Stopping item use ends the cloak. A separate Invisibility potion retains its duration.
- [x] Restore the Light shield, cage and side projections. A brief sneak-use draws a three-by-three shield, cages an aimed creature or projects a line from the face of a block. Attacking lifts a creature four blocks into a cage for five charge. Owner-aware collision uses the original barrier texture and loaded ten-second lifetime. Legacy `owner`, `blocksPlayers` and `remainingTicks` fields load into the current block entity.
- [x] The muted client returned to Light using its second serving of Ghost of the Light and the rite. It held and released the Hand to cloak for two charge, walked through and rendered its own shield, raised a pig into the cage, caged another pig at its original elevation, and projected a 15-block line from a stone face. The shield expired in the loaded world.
- [x] Java 17 data generation and reobfuscation succeeded. All 55 required Forge GameTests passed, including cloak charge and aggression, owner-specific collision, lifetime and preserving charge when a raised cage is obstructed.

## Overworld Infusion Hand Powers (2026-11-20)

- [x] Restore the soft-landing power, the sneaking impact explosion, metal disarming, repelling an armored target, and the channeled magnet. Preserve the original costs and prevent invalid movement when an item lines up with the caster along one axis.
- [x] Restore single-ore conversion and channeled ore conversion through Forge's ore and ingot tags. Convert mined-in-place deepslate and Nether ore to their host stone. The regular hand controls can raise a supported column, launch a damaging rock from a block face or channel a staged shockwave. The moving terrain preserves block states and skips blocks with inventories or unbreakable blocks.
- [x] Data generation and JAR reobfuscation succeeded with Java 17. All 60 required Forge GameTests passed, including real projectile damage, separate ore outputs, preserving charge on unsupported falls and the 1.7.10 rock metadata.
- [x] In the muted packaged client, use the Overworld rite with the fixture's bottled offering and bound ward. Convert gold and iron ore and collect the ingots with the magnet. Disarm a zombie and push its chainmail with a sneak attack. A rock dealt six damage, a lifted column rose three blocks with its nearby chest intact, and a two-second shockwave dealt eight damage. A plain grass landing and a sneaking snow landing each preserved health.
- [x] A dedicated restart retained infusion ID 2, 151 charge, the Witch's Hand, the raised terracotta and its adjacent chest.

## Infernal Infusion Hand Powers (2026-11-20)

- [x] Restore minion binding, hostile and movement orders, and sacrificing a bound creature. The muted survival client performed the Infernal rite, bound a zombie, ordered it to attack a pig, and moved it toward an aimed block. The binding used five charge, the attack order used one, and the movement order preserved charge.
- [x] Restore the 25 creature-power IDs and their activation and passive abilities. A separate meter uses the original creature icons and tracks up to 20 charges. The muted client sacrificed a pig, displayed its icon and charge, and restored six health to twenty. With the pig charge spent, another attempt preserved the infusion charge. Sacrificing a bat replaced that power and enabled Night Vision.
- [x] Build the reobfuscated Java 17 JAR and run all 65 required Forge GameTests. The tests cover legacy minion names, sacrifices, the secondary charge cap, effects, fireballs, arrows and cobwebs. Legacy ingredient 56 migrates to `witchery:thrown_web` and the existing `witchery:web` block item retains its ID.
- [x] The dedicated restart retained the minion's owner, the bat power and nine creature charges. On the packaged server with `allow-flight=false`, the muted survival client held jump for 6.8 seconds, rose from Y 110 to Y 168.8, remained connected and took two damage after landing.
- [ ] Verify acquiring the Owl and Toad powers through their survival entities. Their entity ports remain pending.

## Poppet Shelf (2026-11-20)

- [x] Restore the recipe with four Attuned Stones, four Nether Brick blocks, and green wool. The muted client crafted and placed a shelf, opened its nine slots, inserted three bound wards and rendered all three on the shelf. A comparator responded to its contents.
- [x] Use Forge ticking tickets to keep shelves available at a distance. A shelf 480 blocks away consumed its Fire Protection Poppet to absorb lethal fire. After a dedicated restart, the same shelf and its two remaining wards were available before any player logged in. Its Death Protection Poppet absorbed lethal damage while the owner was in the Nether.
- [x] Keep the original dimension restriction by default. Restore legacy inventory and owner data. The 68 required Forge GameTests covered the recipe, legacy ward subtype, inventory priority, comparator and ticket removal. Java 17 data generation and reobfuscation succeeded.
- [x] Cast with a Voodoo Poppet in the muted client and verify that the remote ward reflected it. Both poppets took 350 wear. The survival client broke the shelf with an axe, collected the shelf and the worn ward, and the chunk unloaded after the player left. A packaged restart retained a real shelf ticket and removed an injected stale ticket at an empty location.

## Specialized Protection Poppets (2026-11-20)

- [x] Restore crafting, models and Taglock binding for Earth, Water, Hunger, Tool, and Armor Protection Poppets. Their legacy metadata loads into the corresponding modern items.
- [x] Restore lethal falling and falling-block protection without healing, lethal drowning protection with Water Breathing, and Hunger protection at two remaining health with Saturation. The Death Protection Poppet supplies its longer matching effect when no specialized ward is available.
- [x] Restore tools and armor to full durability at 90% wear using the matching bound ward. The Forge tests verify that lower wear preserves the ward. All 72 required Forge GameTests passed, and Java 17 data generation and JAR reobfuscation succeeded.
- [x] The muted survival client crafted Earth Protection, collected its own Taglock at the bed, and bound the ward. A lethal fall damage test consumed it and preserved six health. Water raised six health to ten and added Water Breathing. Hunger raised three health to ten and added Saturation after a starvation hit. Using a worn pickaxe repaired it before the mined block added one wear. An injury repaired the worn chestplate.
- [x] A dedicated restart retained the repaired pickaxe and chestplate. The spent Tool and Armor wards stayed gone.

## Vampiric Poppet and Corrupting Voodoo Protection (2026-11-20)

- [x] Restore both original crafting recipes, ordered two-Taglock binding for the Vampiric Poppet, and single-Taglock binding for Poppet Protection. The muted survival client crafted and bound both. The Vampiric Poppet received its first signature from the player's bed and its second from a live pig.
- [x] Transfer entity-caused injury through the Vampiric Poppet at 66 wear per use, cap each transfer to a creature at 15 damage, and clear its Taglock when that creature dies. Three zombie damage tests left the player at seven health and wore the poppet to 198. The surviving signature and wear persisted after restart. Cyclic bindings stop when a recipient repeats.
- [x] Restore the Corrupting Voodoo Protection rite with five offerings, 7,000 altar power and 28 infernal glyphs. The muted client cast it with its familiar dismissed. The heart returned all five offerings and the Poppet Protection kept its original wear.
- [x] Reflect corruption as lightning with 350 wear to Poppet Protection. An undefended cast consumes up to ten matching Voodoo Protection Poppets, including wards on shelves. All 76 Forge GameTests passed, including these mechanics, legacy metadata and both Taglock indices. Java 17 data generation and JAR reobfuscation succeeded.
- [ ] Connect Witch Hunter equipment, the spirit poppet enhancement and other legacy damage exemptions when their owning systems are restored.

## Auram Pack Validation (2026-11-20)

- [x] Reproduce the Evolved Mekanism 1.2.1-fix3 startup crash in the matched Auram profile before adding Witchery. Both pack runs reported the null `QIODriveTier` during Evolved Mekanism construction.
- [ ] Verify the packaged port at the Auram title screen and in fresh survival after the pack can start.

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
    - fixed startup-freeze crash for `plantmine` (`offset type + collision shape` cache validation) by marking collision-enabled `LegacyNonSolidShapeBlock` variants as `dynamicShape`.
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
  - added matching spawn-egg item model files for all registered legacy entity eggs:
    - generated `55` `models/item/*_spawn_egg.json` files with `minecraft:item/template_spawn_egg` parent.
    - corrected generation scope to `LegacyRegistryData.ENTITIES` only (removed accidental packet/effect/block ID over-generation).
  - fixed reported placeholder-texture assignments for high-visibility block models:
    - `tormentstone` now maps to `minecraft:block/mycelium_top` (legacy mycelium-top fallback).
    - `refillingchest` now maps to `witchery:block/leechchest` instead of placeholder `refillingchest`.
    - `brewliquid` now maps to animated `brew_still`/`brew_flow`.
    - `mirrorblock`/`mirrorblock2` now map to `mirror`/`mirror2`.
    - `wolftrap` now maps to `minecraft:block/iron_block` instead of placeholder `wolftrap`.
    - `trent`/`scarecrow` were upgraded from temporary cube placeholders to explicit multi-part fetish models with per-face UV mapping against their legacy `64x64` texture atlases.
  - network skeleton scaffolding now mirrors legacy packet surface:
    - added `WitcheryNetwork` `SimpleChannel` bootstrap and registration from mod init path.
    - added normalized packet-intent inventory for all 19 legacy pipeline messages with legacy ID + direction metadata.
    - replaced generic intent transport with typed no-payload packet stubs per legacy message intent, each registered on the channel with direction validation warnings.
    - implemented codec-backed scaffold payloads + handlers for high-priority sync intents:
      - `player_sync` (uuid + sync revision + serialized `WitcheryPlayerData` snapshot)
      - `extended_player_sync` (uuid + initialized + sync revision + serialized `WitcheryPlayerData` snapshot)
      - `partial_extended_player_sync` (uuid + sync revision + serialized `WitcheryPlayerData` snapshot)
    - expanded codec-backed payloads + handlers for additional high-impact intents:
      - `item_update` (slot index + item damage + page index, aligned to legacy packet shape)
      - `sync_entity_size` (entity id + width + height)
      - `set_client_player_facing` (yaw + pitch)
    - expanded codec-backed payloads + handlers for interaction intents:
      - `cam_pos` (active + updatePosition + entity id)
      - `push_target` (entity id + velocity vector)
      - `sound` (sound id + x/y/z + volume/pitch)
    - expanded codec-backed payloads + handlers for remaining utility intents:
      - `particles` (particle id + sound id + x/y/z + width/height + color)
      - `player_style` (username + grotesque/nightmare/ghost + creature type + blood + skin)
      - `spell_prepared` (effect id + level)
      - `sync_markup_book` (slot + pages list)
      - `clear_fall_damage` and `howl` now use explicit no-payload codec records + dedicated handlers.
    - clientbound packet handlers now include first functional client behavior:
      - `particles` now routes through a client packet bridge with legacy-aware `ParticleEffect`/`SoundEffect` id mapping, old-style effect count scaling, and colored spell particle support.
      - `player_sync`, `extended_player_sync`, and `partial_extended_player_sync` now apply incoming sync state on client with stale-revision guards and serialized `WitcheryPlayerData` snapshot hydration (plus mirrored persistent tags).
      - `player_style` now resolves target players client-side by username and applies staged style fields to capability + persistent tags.
      - `sound` now resolves and plays mapped client sounds (with legacy default volume/pitch behavior).
      - `push_target` now applies client-side motion vectors to the target entity.
      - `set_client_player_facing` now applies client-side player rotation.
      - `sync_entity_size` now stages synced width/height values on the client target entity for follow-up resize parity work.
      - `cam_pos` now follows legacy packet semantics (`active`, `updatePosition`, `entityId`) and stages camera-active/target state via explicit client camera state (`WitcheryClientCameraState`), with client tick hooks applying camera-entity overrides and reset fallback.
    - serverbound packet handlers now include first functional scaffold behavior:
      - `clear_fall_damage` now clears sender fall distance.
      - `item_update` now applies real inventory stack page mutation (`CurrentPage`) with legacy-style slot/damage validation.
      - `sync_markup_book` now applies real inventory stack page-list mutation (`pageStack` string list).
      - `spell_prepared`, `item_update`, `sync_markup_book`, and `howl` also stage state into `WitcheryPlayerData` (+ minimal persistent tags for spell/howl) and bump sync revision.
  - dimension migration skeleton now has stable resource-key anchors:
    - added `WitcheryDimensions` keys for `dream`, `torment`, and `mirror` across `Level`, `LevelStem`, and `DimensionType`.
    - added breadth-first datapack scaffold files for `dream`, `torment`, and `mirror` (`data/witchery/dimension_type/*.json` and `data/witchery/dimension/*.json`).
    - added runtime travel hook scaffold in `WitcheryDimensionTravelHooks` with route-resolution helpers and player routing entrypoint (`routePlayer`).
    - wired portal collision scaffold triggers through `PlayerTickEvent` -> `WitcheryDimensionTravelHooks.resolvePortalCollisionTrigger(...)` for `spiritportal` and `tormentportal` with cooldown gating.
    - narrowed right-click routing to mirror-rite scaffolds only (`mirrorblock*`, `mirrorwall`, `circleglyphotherwhere`) via `resolveRiteTriggerFromBlock(...)`.
    - rite-completion activation behavior and worldgen/provider tuning are still TODO.
  - event-bus hook skeleton now has explicit Forge-side anchor points:
    - added `WitcheryEventHooks` subscribers for entity capability attach, player login/clone, player tick, player right-click block, level load, entity travel-to-dimension, and player changed-dimension.
    - preserves breadth visibility for upcoming `ExtendedPlayer` replacement and world runtime bootstrap migration.
  - capability/data-attachment scaffold now exists for player data migration:
    - added `WitcheryCapabilities` MOD-bus registration for `WitcheryPlayerData`.
    - added `WitcheryPlayerDataProvider` attachment + clone-copy wiring through `WitcheryEventHooks`.
    - expanded `WitcheryPlayerData` NBT payload groups for legacy migration staging:
      - style/state: `creatureTypeOrdinal`, `humanBlood`, `grotesqueTicks`, `nightmareLevel`, `ghost`, `otherPlayerSkin`
      - effect state: `preparedSpellEffectId`, `preparedSpellLevel`
      - inventory/book staging: `lastItemUpdate*`, `lastMarkupBook*`
      - misc runtime staging: `lastHowlGameTime`
    - typed sync sends from event hooks:
      - login: `extended_player_sync` + `player_sync`
      - clone: `partial_extended_player_sync`
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
  - Fresh client launch smoke run after `plantmine` fix no longer reports `IllegalStateException: witchery:plantmine has a collision shape and an offset type`.

### Phase 3 - Systems Migration Skeleton (breadth)
- [x] Network channel + packet stubs (all legacy packet message intents represented).
  - typed no-payload packet records are now registered for all `19` legacy intents.
  - codec-backed payload/handler scaffolding is now implemented for:
    - `player_sync`, `extended_player_sync`, `partial_extended_player_sync`
    - `item_update`, `sync_entity_size`, `set_client_player_facing`
    - `cam_pos`, `push_target`, `sound`
    - `particles`, `player_style`, `spell_prepared`, `sync_markup_book`
  - explicit no-payload codec records + handlers are now in place for:
    - `clear_fall_damage`, `howl`
  - serverbound handler scaffold now applies minimal runtime behavior:
    - clears fall distance for `clear_fall_damage`.
    - applies stack NBT mutations for item/book sync intents (`CurrentPage`, `pageStack`) and stages item/spell/book/howl updates into `WitcheryPlayerData`.
  - clientbound handler scaffold now applies minimal runtime behavior:
    - `particles` spawns local particles/sound via legacy-aware id mapping (`ParticleEffect`/`SoundEffect`) with effect-count scaling + colored spell support.
    - `player_sync`, `extended_player_sync`, and `partial_extended_player_sync` now hydrate staged client capability fields from serialized `WitcheryPlayerData` snapshots with stale-revision guards (plus persistent-tag mirrors for sync/style state).
    - `player_style` applies staged style sync to target player capability/persistent tags on client.
    - `sound`, `push_target`, `set_client_player_facing`, and `sync_entity_size` now route through client packet handlers with first-pass scaffold behavior.
    - `cam_pos` now routes through client packet handlers using `WitcheryClientCameraState` plus `WitcheryClientCameraHooks` (client tick camera-entity override scaffold) instead of temporary player persistent-tag staging.
- [~] GUI/menu stubs for all legacy GUI IDs.
  - menu intents now preserve legacy GUI ID mapping (`0..8`) plus key name in `LegacyRegistryData`.
  - GUI ID 2 opens the Witch's Oven menu. The other eight IDs still use `LegacyPlaceholderMenu` and `LegacyPlaceholderScreen`.
- [~] Dimension/key migration for Dream/Torment/Mirror.
  - resource keys are scaffolded (`Level`, `LevelStem`, `DimensionType`) for `dream`, `torment`, and `mirror`.
  - datapack `dimension_type` + `dimension` JSON scaffolding is now present for all three dimensions.
  - `WitcheryEventHooks` processes dimension travel requests on the server tick, with `WitcheryDimensionTravelHooks` observing Forge dimension changes.
  - Dream uses the sleeping brew, body and separate inventories through `DreamTravel`. The Rite of Manifestation and snow-frame Spirit Portal allow a time-limited visit to the Overworld while the body remains asleep. The Dream has the three atmosphere variants and the demonic hazards.
  - The Mirror uses two-block frames, stacked chambers, and Reflection encounters through `MirrorTravel`. `TormentTravel` uses generated maze entrances and exits, with a separate intact layout for upgraded Nether-like Torment saves.
- [~] Event bus hooks migrated with no-op or minimal behavior.
  - placeholder Forge-bus hooks are in place for attach-capabilities, player login/clone, player tick, player right-click block, entity travel-to-dimension, player changed-dimension, and level load events.
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
- [x] Add placeholder block items for non-itemized legacy blocks where useful for QA visibility.
  - `WitcheryItems.registerLegacyBlockItem(...)` now auto-registers block items for legacy block IDs that do not already map to dedicated item placeholders.
- [x] Re-run client and validate `run/logs/latest.log` after the lowercase resource-location fix.
- [x] Re-run client and validate `run/logs/latest.log` after `icedoor` parent-model and `wolftrap` geometry parity fixes.
- [~] Continue replacing placeholder block models with old 1.7 equivalents (breadth pass before behavior depth).
- [ ] Run fresh client launch smoke and scan `run/logs/latest.log` for:
  - spawn egg model resolution errors (`*_spawn_egg` model lookups).
  - block-model missing texture warnings for the remapped set (`tormentstone`, `refillingchest`, `brewliquid`, `mirrorblock*`, `wolftrap`, `trent`, `scarecrow`).
- [ ] Continue fetish model parity depth pass:
  - evaluate whether `witchsladder` needs the same multi-part UV approach.
  - add fidelity follow-up for fetish-specific rotations/tint behavior currently handled by legacy TESR (`RenderFetish`).
- [ ] Run targeted multiplayer/client validation for expanded player sync payload snapshots (`login`, `clone`, `item_update`, `sync_markup_book`, `howl`).
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
    - replace remaining temporary mirror right-click routing with rite-completion activation hooks.
    - harden camera override lifecycle in `WitcheryClientCameraHooks` for edge cases (dimension swap, target despawn timeout, spectator transitions).
    - audit other custom-shape blocks that copy offset-style vanilla properties to ensure collision-enabled variants stay `dynamicShape`-safe.
    - extend client sync handling beyond revision/initialized to include fuller staged groups as payload coverage grows (style/inventory/spell slices).
    - expand `WitcheryPlayerData` fields toward legacy `ExtendedPlayer` coverage (inventory/state/effect sync groups).
  - validation: `./gradlew compileJava` succeeds after this pass.
