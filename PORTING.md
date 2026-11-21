# Witchery 1.7.10 -> 1.20.1 Porting Plan

## Working Style
- Breadth first first, depth second.
- Prefer creating full vertical skeletons (all systems present, minimal behavior) before polish.
- Check `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING_MEMORY.md` before starting work each session.
- Check `/Users/cyberpwn/development/workspace/AuramMods/Witchery/PORTING_MANIFEST.md` before hunting for source details.
- Run `./gradlew runData` before a separate `./gradlew jar` invocation when generated resources change. The data run can stage the previous resources before writing the new files. The `jar` invocation reprocesses them and reobfuscates without deploying into Auram.

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
- [x] Restore the Brew of Love interaction with owned zombies. The muted client threw it near the two zombies it previously raised. Both became owned Zombie Villagers and one wild baby spawned. The headless tests verified that a nearby unowned adult was unaffected and the converted parents kept their equipment.
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
- [x] Restore legacy potion bottling skill and include it in Seer Stone readings. Filling a two-level brew advanced the muted client's skill to one and the stone reported that value. GameTests verify the first yield threshold, power accounting, and the saved capability.
- [ ] Restore the coven witch's additional combat and ritual behaviors.

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
- [x] Restore Witch Hunter equipment and its interactions with ordinary poppets. The spirit enhancement for death protection remains on the crossbow checklist.

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
- [x] Acquire the Owl and Toad powers through their survival entities. The muted client mutated both species, bound a wild member of each species and sacrificed it with the Infernal Hand. Each power started with ten reserve charges.

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
- [x] Reflect corruption as lightning with 350 wear to Poppet Protection. An undefended cast consumes up to ten matching Voodoo Protection Poppets, including wards on shelves. All 77 Forge GameTests passed, including the full rite through heart activation, legacy metadata and both Taglock indices. Java 17 data generation and JAR reobfuscation succeeded.
- [ ] Connect Witch Hunter equipment, the spirit poppet enhancement and other legacy damage exemptions when their owning systems are restored.

## Ent and Ent Branch (2026-11-20)

- [x] Restore the Ent at `witchery:ent`. Harvesting Witchwood can awaken one in the surrounding forest. The rate starts at 1% and increases with adjacent Witchwood logs up to 5%. Awakening needs supported space in a loaded chunk.
- [x] Restore the original Ent model, attack animation, axe vulnerability, Ent Branch drop and Witchwood sapling drop. The branch supplies the existing Mystic Branch rite and the Mutating Sprig recipe still to be ported.
- [x] Verify the packaged Ent in muted survival. The player harvested Hawthorn with an axe and awakened a hostile Ent, which attacked and killed the player. The respawned player defeated the same Ent with a supplied axe after it was positioned in the test arena, then picked up its Ent Branch and Alder Sapling.
- [x] Package on Java 17 and pass all 80 required Forge GameTests. The tests cover harvesting, spawn distance, the 15-damage cap before the triple axe bonus, both drops, and legacy ownership.
- [x] Restart the dedicated server and verify the saved branch, Alder Sapling variant and legacy Ent owner. The muted client offered the collected Ent Branch and Mystic Unguent inside two white rings at night, and the rite produced a Mystic Branch.

## Mutating Sprig, Grassper and Critter Snare (2026-11-20)

- [x] Brew the Mutating Sprig with an Ent Branch, nether wart, Mutandis Extremis and 2,200 base altar power including the ingredients. The packaged survival client collected the cauldron output and created four Grasspers from an empty chest over source water using vines. The pattern also accepts short grass and ferns, which were variants of the original tallgrass block.
- [x] Restore the grass and mycelium swap, the cross of clay from submerged dirt, and the cobweb mutation that uses four Alder Saplings and a zombie over a source of water. One use costs one durability. The muted client produced four Critter Snares and the zombie was consumed.
- [x] Restore the Grassper's model, single item inventory, comparator and use as a ritual offering holder. The muted client placed and released an ingot, and inserting another produced comparator output 15. The new rite support uses the original five-block square and saves the Grassper position for refunds.
- [x] Restore bat, silverfish, tiny slime and tiny magma cube capture and sneak-release. Captured state and the bat shop stock flag carry through block item drops and legacy metadata. The packaged server captured a moving bat and kept its stocked state.
- [x] Save a live Mystic Branch rite after the first Grassper offering. The packaged restart emptied the second Grassper and completed the rite. A separate Grassper still held one ingot and emitted comparator output 15. The client released the bat from the restored snare, and the bat kept its shop stock flag.
- [x] Pass all 84 required Forge GameTests and package the reobfuscated JAR on Java 17.
- [ ] Restore the remaining Sprig mutations, including Leech Chest, Blood Roses, Parasytic Louse and Winged Monkey.

## Owl and Toad (2026-11-20)

- [x] Mutate two captured bats into Owls with a wolf and two caught tiny slimes into Toads with an ocelot. The muted client performed both mutations with a cobweb, water, three Mutandis Extremis and a Charged Attuned Stone. Both living offerings and both pairs of snares were consumed. The Grasspers emptied and the Sprig took one wear per mutation.
- [x] Restore the original models, colors, animations and sound definitions. The muted player tamed a new Owl with pork and a Toad with rotten flesh, dyed both, and handed the Owl a named paper parcel. The client displayed both bodies and the Owl's parcel.
- [x] Restore feathers, Slimeballs, Owlet's Wing, and Toe of Frog drops with the Arthana bonuses. Legacy feather and skin colors, temporary lifespan, poison and familiar flags load from entity data.
- [x] Verify live Infernal bindings and creature power acquisition. The wild Owl and Toad gave IDs 24 and 25, both with ten reserve charges. Binding and sacrificing each consumed six infusion charges. All 88 required Forge GameTests passed. Java 17 data generation and JAR reobfuscation succeeded.
- [x] Restart the packaged dedicated server and reconnect the muted client. Both tamed animals kept their owner and dye colors. The Owl kept its named parcel, and the player kept Toad power 25, ten reserve charges and 168 infusion charges.
- [x] Bind, dismiss and call Owl and Toad familiars. The muted client bound a blue Toad, redirected one point of a ten-point hit, and called it back with its name and color. Binding the purple Owl returned the Toad to its ordinary health. The client recovered the Owl's named parcel when dismissing it. The recalled Owl retained its UUID, purple color and 50 health after the packaged server restarted.
- [x] Apply the active Toad's five-percent bonus to eligible Kettle brews. A seeded GameTest used a glass bottle on Ink with an active Toad, active Owl, active Cat and dismissed Toad. Only the active Toad yielded a bonus bottle. All 89 required Forge GameTests, data generation and JAR reobfuscation passed.
- [ ] Apply the remaining Toad Brew Mastery bonuses as the related brews and brewing equipment are ported.

## Flying broom and Soaring (2026-11-20)

- [x] Craft the Broom with two sticks and three Hawthorn saplings. Brew Flying Ointment in the Kettle with a long Swiftness potion. The muted client sacrificed both in the small and medium white circles. Both items stayed in the circle during the daylight attempt. The same rite yielded an Enchanted Broom at midnight.
- [x] Place, dye, mount and fly the Enchanted Broom on dedicated Forge. The muted rider flew more than 110 blocks in one run. Looking up raised the broom over six blocks during a shorter run, and looking down produced descending motion. Releasing forward with the Owl active braked the broom.
- [x] Brew the Infused Brew Base in the Kettle and offer it with a Broom, Feather, Arthana and a wild Owl. The muted player received an Infused Brew of Soaring. Forge GameTests verified that the Owl and Soaring bonuses stack and that Soaring expires after two hours.
- [x] Restart the packaged server after drinking Soaring. The same blue broom and brew timestamp survived. The muted client flew again with both bonuses, dismounted and recovered the dyed item with an axe. All 91 required GameTests, data generation and JAR reobfuscation passed.
- [x] Restore the Wild Bramble recipe for a second Infused Brew Base. Restore the HUD icons and remaining-time display for Soaring and the Grave.

## Waystone rites (2026-11-20)

- [x] Craft a Waystone from Flint and a Bone Needle. The muted player bound it at a small white circle with Ender Dew and Glowstone Dust, then offered the result at a separate small Otherwhere circle. The rite moved the player to the original location.
- [x] Bind a Creature-Bound Waystone to a nearby player or creature. Both original recipes are available. The muted client bound a named pig using the 500-power recipe. The pig and stone retained their matching identity after a packaged server restart. The rite then carried the player to the pig's new position.
- [x] Verify legacy location and creature tags, safe placement, Nether travel and Ender Inhibition. All 94 required Forge GameTests, data generation and JAR reobfuscation passed.
- [x] Deliver a parcel or throw a brew with a tamed Owl. The muted player assigned a location stone, launched the Owl with an empty hand, and restarted the server while it was airborne. The Owl delivered the named paper and returned to sit near its launch point. A second route made it throw Ink at a bound pig. A route of more than 160 blocks delivered a second parcel outside the player's simulation distance. All 96 required GameTests passed.
- [x] Bind or use dropped Waystones at the original Otherwhere glyph patterns. The muted client tossed ten into an eight-glyph ring and got eight bound stones and two unbound stones. A powered ring bound a named pig. Dropping the bound stack inside a twelve-glyph ring moved the client, loose paper and seven remaining stones to the exact saved point. All 99 required GameTests passed.
- [ ] Restore the remaining Waystone uses in rites and equipment.

## Crystal Ball readings (2026-11-20)

- [x] Restore the placeable Crystal Ball, animated renderer and fortune-teller training through its night rite. The muted survival client crafted the Quartz Sphere, brewed Happenstance Oil and performed the rite with a Gold Ingot. Placing and using the ball gave the player an undead forecast.
- [x] Restore the 500-power reading, five-second cooldown, and legacy saved forecast. A click during cooldown left the saved time unchanged. Reading again after cooldown repeated the same forecast and kept its original timestamp. All 101 required GameTests, data generation and JAR reobfuscation passed.
- [x] Restore the first seven outcomes using the legacy IDs and weights. Arrows, falls, zombie, Ent and wolf damage, water and a Nether visit can fulfill their corresponding readings. The GameTests covered a matching fall, wrong damage and the overdue gravel cavity.
- [x] Restart the packaged dedicated server while the undead forecast is active. The player retained training, the same forecast ID and timestamp, and the Crystal Ball's cooldown timestamp. A live zombie's attack cleared the forecast after reconnecting.
- [x] Restore Iron, Diamond, Emerald, Coal and Buried Treasure predictions in survival harvesting. The muted player loaded old-format Coal and Treasure forecasts in a disposable save. Mining Deepslate yielded 16 extra coal, and digging dirt exposed a mineshaft chest with a Golden Apple. A loot preview left the Coal forecast active. All 103 required GameTests passed, including age thresholds, ore drops, and protected chest placement.
- [x] Restore the Love and both rescue forecasts. A summoned farmer approached the standing player and produced a baby villager. Separate zombie attacks summoned a temporary Owl and a wild Wolf. Each attacked the zombie that hit its player. All 105 required GameTests, data generation and JAR reobfuscation passed.
- [x] Verify a reading for a nearby player on a dedicated server. The trained player used the ball and the second muted client received the Wet forecast. The reader's record stayed empty. Entering water cleared the second player's reading.
- [x] Restore both Baba Yaga forecasts, her hostile and allied encounters, boss HUD and animated mortar-and-pestle model. On dedicated Forge one forecast spawned an ally with the second player's name, and the other led to a hostile potion attack. The ally departed at the end of its visit. A name-only legacy ally fixture dropped gifts. A shortened fight yielded Baba's Hat and an enchanted book. All 107 required GameTests passed, including the wooden sword counter and original damage cap.
- [x] Restore the rest of Baba Yaga's thrown brews and brew gifts. The GameTests exercised each of her 12 legacy brew choices. The visit and death tables use the original collections.
- [x] Restore Crystal Ball viewing with a location-bound Waystone. The muted clients saw a marked platform and its entity 126 blocks away while an observer still saw the player at the ball. Releasing use and the Eye's 200-tick expiry both returned the view. A distant Waystone and a Nether Waystone failed to open it. The same package worked with clean Forge and an isolated Embeddium 0.3.31 and Oculus 1.8.0 profile. All 108 required GameTests passed.
- [x] Restore Crystal Ball viewing with a filled Taglock Kit. A sample taken from another player's bed let the muted client follow that player across a chunk boundary over 180 blocks away. A self sample raised an Eye above the ball. A cow sample showed the cow's point of view. Transferring the cow to the Nether closed the view and left it alive. After a dedicated restart during a later viewing, the player returned beside the ball with the same filled kit and the cow retained its UUID. All 109 required GameTests passed.

## Witch Clothing and Raising (2026-11-20)

- [x] Restore Impregnated Leather, the Witch's Hat and Robes, the Necromancer's Robes, and the original worn models and sprites. The muted client crafted the leather and all three pieces and wore both hats and the Necromancer's Robes. At the crafting table a dyed robe lost its 37 damage and kept its enchantment, color and name. Imported legacy robe wear and color passed a fixture check.
- [x] Restore the robes' protection against their original attackers and the four garments' kettle bonuses. The headless tests verified the Creeper and undead targeting rules, both of Baba's bonus rolls, and the two robes' separate recipe families. The muted survival client wore the Witch's Hat and Necromancer's Robes and bottled four Brews of Raising from a three-serving batch.
- [x] Restore the Brew of Raising and the Necromantic Stone's general undead commands. The muted client filled a heated kettle and brewed the original six ingredients with 500 altar power. A thrown portion raised two owned zombies, and the Necromantic Stone sent them to attack a husk. All 113 required GameTests, data generation and JAR reobfuscation passed.
- [x] Restore Baba's Hat equipment and its charge-powered evasion. The muted client equipped the hat previously earned from Baba Yaga. A test hit from a zombie left health unchanged, reduced charge by five and moved the player to a nearby open block. Environmental fire did not consume any infusion charge.
- [x] Restart the packaged server after these interactions. Both zombies kept their UUIDs and owner, the repaired robe kept its tags, and Baba's Hat and the remaining infusion charge stayed saved.
- [ ] Connect the garments to the advanced cauldron, Polynesia trades, and the later player forms and crossbow effects.

## Terrain Brews (2026-11-20)

- [x] Restore kettle recipes and projectile impacts for Vines, Webs, Thorns and Sprouting. The muted survival client crafted the Web ingredient, filled the heated kettle, bottled Brew of Webs and threw it onto the floor. It also threw supplied portions of the other three. Vines attached on the wall face, Thorns produced three cactus segments, and Sprouting produced a horizontal birch branch.
- [x] Verify the active Toad bonus, the three distinct Sprouting saplings, and Baba Yaga's Webs and Thorns projectiles. All 118 required GameTests, data generation and JAR reobfuscation passed.
- [x] Restart the packaged server after the terrain changed. The webs, vines, cactus, birch branch, two owned Zombie Villagers, and two unbottled Kettle servings persisted.
- [ ] Connect the terrain impacts to the legacy plant mines and circle rites.

## Familiar Brews and Baba Yaga (2026-11-20)

- [x] Restore the recipes and effects for Frog's Tongue, Cursed Leaping, Hitchcock, Bats and Wasting. The muted client pulled a cow over two blocks, gained roughly eleven blocks of height with Jump Boost IV, and sent four temporary owls at a cow with Hitchcock. Bats released ten marked bats and applied Slowness VI to a nearby chicken. Wasting cut food from twenty to ten, applied Hunger and Poison, and cleared a nearby poppy.
- [x] Craft a Bat Ball from four bat-filled snares and four slimeballs. The muted client took one from the crafting table. The GameTest also refused a slime-filled snare.
- [x] Restore familiar-specific Kettle bottling. Cursed Leaping requires a Cat, Frog's Tongue a Toad and Hitchcock an Owl. Hopper extraction and wrong or dismissed familiars preserve the ready result and glass. The muted client tried Hitchcock before and after a dedicated restart with its Owl dismissed. Calling the Owl at the ritual circle allowed the same batch to be bottled. All 123 required GameTests, data generation and JAR reobfuscation passed.

## Plant Mutations and the Grave (2026-11-20)

- [x] Restore the legacy Sprig patterns for Wild Bramble, Ender Bramble and Wormwood. The muted survival client mutated three separate prepared arrangements with one charge each. Breaking a Wild Bramble segment returned an item with its saved variant. Wild contact applied cactus damage.
- [x] Double Infused Brew Base using the harvested Wild Bramble and the original five other ingredients. The muted client tossed the harvested item into the heated Kettle and manually bottled both portions. A GameTest refused Ender Bramble and verified that the second recipe has no hat bonus.
- [x] Restore the Grave rite. The muted client dropped one of its bottled portions into the small and medium white rings with bone, Web, and a Necromantic Stone. The ritual consumed its zombie, produced the infused brew, and returned the stone. Drinking opened the original skull and 120-minute display.
- [x] Restore sneaking use of the Necromantic Stone during the Grave. The muted client bound a zombie, was refused when it tried a skeleton inside the cooldown, and bound the skeleton when the cooldown finished. Infusion charge stayed at 158 throughout.
- [x] Verify Ender Bramble travel through actual player movement. Stepping inside moved the muted client from the test platform to safe terrain roughly 370 blocks away. Waiting until the next server tick kept the vanilla movement handler from reverting the teleport.
- [x] Verify the Grave across a dedicated restart. The remaining-time HUD resumed at 117 minutes and the zombie kept its UUID and owner. The start time, cooldown timestamp, and 158 infusion charge persisted. Data generation, the reobfuscated build, and 129 required Forge GameTests passed.

## Leech Chests, Blood Roses and the Boline (2026-11-20)

- [x] Restore the Leech Chest mutation. The muted survival client used a Sprig on an empty trapped chest with four vines over four cardinal water sources. GameTests verified refusal with a filled chest or a missing water source.
- [x] Restore the four-part lid, three sample sacs and chest menus. Two muted survival clients opened the same chest. It retained both names, refused the collector's own sample, and kept the second player's sample when they left for the Nether. A normal Taglock Kit collected that player's name and UUID once they returned.
- [x] Recover Leech Chest samples along with a harvested chest. A dedicated restart preserved three diamonds, the remaining opener, and the second player's filled kit. The muted client mined the chest with a pickaxe and picked up the chest with its saved sample and a separate stack of three diamonds.
- [x] Re-place the harvested chest with its sample and join it to a second chest. The muted client opened the combined six-row menu.
- [x] Restore the Blood Rose mutation, the original empty and full textures, and the legacy Boline recipe. The muted player crafted the Boline using iron, bone and emerald. GameTests verified the original red flower variants, replacement of older samples, and replantable cuttings.
- [x] Verify the Blood Rose across restart. The muted client mutated its empty Leech Chest into four roses. A second player walked through one, which displayed its full texture and kept their sample after restart. The first player collected the sample and drained the rose. It cut the rose with the crafted Boline without wear, and the second player replanted it empty. The rose also allowed the first player to collect its own sample. Data generation, JAR reobfuscation, and all 133 required Forge GameTests passed.

## Coffin (2026-11-20)

- [x] Craft the original placeable coffin from a bed, three planks and five logs. The muted survival client crafted the item and placed the head and foot together. Placement tests refused an obstructed or unsupported second half and retained the item.
- [x] Restore both lid models, the crouch-use toggle, the open collision shape and the original `Opened` save field. The muted client was refused while closed and slept after opening it. A second player could not close it while occupied.
- [x] Restore Taglock collection through the coffin. The second player collected a kit from each half after the first player woke. Both kits bound to the first player's UUID at the coffin's registered respawn point.
- [x] Preserve the earlier port's single decorative block and facing on world upgrade. A GameTest imported the original `Opened` field. Data generation, JAR reobfuscation and all 136 required Forge GameTests passed.
- [x] Verify the open coffin on a dedicated restart. Both lids stayed open, both Taglocks retained their binding, and the first client respawned beside the head with full health. Mining the foot removed both halves and let the player pick up exactly one coffin. A GameTest verified the same item count when breaking the head.
- [x] Restore vampire daytime sleep and dusk wake-up. The muted client slept at daytime tick 6,095 in a sheltered coffin and woke at tick 13,034. Its 125 blood power stayed intact.

## Blood Goblet and Lilith (2026-11-20)

- [x] Restore the Glass Goblet recipe, filled item and original ritual layout. The muted survival client crafted a goblet from three bottles and glass, then used a Boline on a chicken in the prepared circle. The filled goblet and the skull survived a daytime activation attempt. Using it at night emptied the same goblet and summoned Elle.
- [x] Restore Elle's lava search, transformation and Lilith's encounter. Elle found a clear lava surface and spent 400 ticks transforming. The muted client defeated the 200-health boss with an enchanted sword and a splash potion in the disposable arena. Lilith survived the defeat, restored her health and accepted the same goblet. GameTests verified the damage cap and the transition to a friendly encounter.
- [x] Collect and drink Lilith's filled goblet. The muted client picked up the reward, drank it to reach rank one, saw 125 blood power on the original droplet meter, and kept an empty goblet. Java 17 data generation, reobfuscation and all 140 required Forge GameTests passed.
- [x] Verify rank one after a dedicated restart. The 125 blood power, empty goblet and open coffin remained. The muted client kept its blood in shade at noon, then stepped into open daylight, lost its blood reserve and caught fire.

## Vampire Feeding and Mesmerize (2026-11-20)

- [x] Restore the hotbar feeding power and blood display. The muted survival client selected the fangs with the scroll wheel and saw droplets under the crosshair as it targeted a villager. Awake villagers yielded seven blood per bite. Hunger drew five blood per food point. The client filled the 750-point reserve and reached rank two.
- [x] Restore Mesmerize and vampire vision. The muted client switched to the eye, spent 50 blood to paralyze a villager, and then fed from outside normal bite range. This transferred ten blood. Sneak-use in the air enabled Night Vision, which stayed active after further feeding.
- [x] Complete the rank-two feeding trial. The muted client earned three careful sips from each of two fresh villagers by ending successive bites at 276, 269 and 262 blood. The sixth sip reached rank three and cleared the active power. GameTests also covered overfeeding resets, sleep recovery, feeding permission checks, animal blood limits and the paralysis rules.
- [x] Restart the dedicated server with the packaged JAR. Rank three, the full 1,000-point reserve, vision and the two villagers' 262-point reserves survived. Java 17 data generation, JAR reobfuscation and all 145 required Forge GameTests passed.

## Observations of an Immortal and Rank Four (2026-11-20)

- [x] Craft the journal from six nether wart, a Nether Star, a book and garlic. The muted survival client crafted it and read its six starting pages. Closing the book kept a bookmark and raised the learned cap to rank three.
- [x] Restore Torn Pages and the upgrades. The muted client collected three Torn Pages by killing three weakened Withers in the disposable world, crafted each page into the same book, and read the new speed chapter. The book showed nine pages and the player's learned cap rose to four. GameTests verified the nine-page upgrade limit and the old metadata and bookmark conversions.
- [x] Restore the rank-three crouching strike. One empty-hand strike in the muted client took a pig from ten to nine health and launched it about six blocks.
- [x] Finish the rank-three nighttime trial in survival. The counter paused at 158 during a sheltered daytime check. A dedicated restart preserved the accumulated time, rank cap, three attached pages and bookmark. The client reached rank four after 300 nighttime intervals through regular server ticks. The journal reopened at the saved chapter and displayed its ritual diagram.
- [x] Restore rank-four speed. The muted client selected the new hotbar icon, spent ten blood for Speed II and Jump Boost II, and spent ten more for Speed IV and Jump Boost III. A third use while active spent no blood. Data generation, JAR reobfuscation and all 151 required Forge GameTests passed.
- [ ] Restore the village bookshop loot for Observations of an Immortal and its Torn Pages.

## Daylight Collector and Sun Grenades (2026-11-20)

- [x] Craft and place the Daylight Collector. The muted survival client used the original two gold ingots, two iron ingots, daylight detector and redstone repeater. Ten Quartz Spheres went into ten collectors. The empty stand, partial charge and finished sphere use the original crossed textures.
- [x] Advance the collector from stage one through eight with an adjacent vanilla daylight detector. The dedicated test advanced the world's time through successive morning levels and observed both the vanilla signal and collector charge. GameTests verified early removal returns a sphere, skipped levels and regular redstone cannot charge it, and a finished sphere stays charged when the detector falls.
- [x] Resume collector charging after a restart. The stands retained charge eight and all filled to 15 under daylight. The muted client collected all ten Sun Grenades and the stands returned to empty.
- [x] Restore Sun Grenade flight, temporary light and the original 1,200-tick fuse. The muted client threw all ten grenades. A dedicated restart preserved the grounded projectiles, their remaining fuse and their light. Their bursts advanced the client to rank five, the temporary light disappeared and the client recovered all ten Quartz Spheres.
- [x] Verify rank-five daytime tolerance. Under a roof at noon the vampire retained all 975 blood and 20 health. Two intervals in direct sun consumed 120 blood and left the player with 855 blood, full health and no fire.
- [x] Complete rank five's Blaze trial. The muted client added two more Torn Pages to the journal and learned the rank-six cap. It reached rank six after twenty sword kills against weakened test Blazes. The GameTests also verified that ranged kills do not count.
- [x] Restore rank-six stone shattering. After a restart with the final package, the muted client crouched and broke stone, cobblestone and netherrack with an empty hand. The drops and exhaustion appeared in the server state. Deepslate stayed intact. Data generation, JAR reobfuscation and all 155 required Forge GameTests passed.

## Poppy Trial and Bat Form (2026-11-20)

- [x] Complete the rank-six poppy exchange. The muted client offered Lilith a poppy with a learned cap of six and kept the flower after she declined. It collected another Torn Page from a weakened Wither, crafted the page into the journal and read it to learn the rank-seven cap. Lilith then accepted the same poppy and raised the vampire to rank seven. This exchange used a defeated Lilith fixture. The original summoning ritual and fight were verified in the earlier rank-one milestone.
- [x] Restore the rank-seven bat form. The muted client selected the original bat icon, spent 50 blood to transform, and took off directly from the ground. It climbed from Y 86.5 to Y 95.8 with the jump key and flew into a one-block-high tunnel. A second muted client saw the animated bat and smaller hitbox. GameTests verified the one-point upkeep, depletion, form data migration and restoration of prior flight permissions.
- [x] Restart while the bat is inside the tunnel. The saved form and flight resumed from the same position. The client flew out and returned to its normal size with flight cleared and no second transformation charge. Java 17 data generation, reobfuscation and all 159 required Forge GameTests passed.

## Vampire Village Visits and the Cage Trial (2026-11-20)

- [x] Record distinct village visits at rank seven. The muted client read the journal with eight restored pages and entered a generated plains village in a fresh test world. A second stop in a nearby chunk of the same village kept the count at one. A snowy village raised it to two. GameTests verified that occupied homes in a player-built settlement count together and the original saved visit coordinates load.
- [x] Preserve village visits across a dedicated restart. The two recorded villages and learned cap remained after rejoin. Visiting a generated taiga village and a second plains village advanced the player to rank eight and cleared the completed list.
- [x] Verify rank-eight Mesmerize and cage detection. An affected villager followed the muted player about five blocks. A villager in a cage with part of its roof missing reached 280 blood and gave no credit toward the trial.
- [x] Reach rank nine by taking six careful sips across two covered cages. Each villager ended with 260 blood. A dedicated restart preserved rank nine, 1,079 blood power and both villagers' reserves. Data generation, JAR reobfuscation and all 163 required Forge GameTests passed.

## Vampire Conversion and Rank Ten (2026-11-20)

- [x] Earn the ninth Torn Page, finish the journal and learn the rank-ten cap. The muted survival client collected the drop from a weakened Wither, crafted it into the journal and read the final chapter.
- [x] Convert a villager with the player's own blood. The client filled the goblet with 125 blood and kept it when the villager was unmesmerized, still had blood, or had no coffin nearby. It drained the existing cage villager from 260 to zero while the villager stayed alive, mesmerized it again and placed a coffin nearby. The offering created a vampire, returned the empty goblet and completed rank ten.
- [x] Preserve the converted vampire and the player's progression. A dedicated restart kept the same creature UUID and home position, the empty goblet, rank ten and 1,114 blood.
- [x] Verify the vampire's nightly hunt. The mob selected an occupied village, took five nonlethal bites and returned home. The hunted villager's blood went from 500 to 400.
- [x] Preserve Witchery progress through player respawn. After a deliberate death in the headless server, the muted client respawned at rank ten with a cap of ten and 1,114 blood. Java 17 data generation, the reobfuscated JAR and all 167 required Forge GameTests passed.
- [x] Restore guard conversion and the response to witnessed bites. See Village Guard Recruitment and Feeding below.

## Blood Crucible and Thunderstorm (2026-11-20)

- [x] Craft and place the Blood Crucible in survival. The muted client assembled the original stone brick recipe and confirmed that an empty vessel refused Globe Artichoke.
- [x] Fill the vessel with four completed vampire hunts. The client saw the blood surface after the first five-point deposit. A dedicated restart preserved the deposit. Three later hunts raised the same vessel to 10, 15 and 20 blood.
- [x] Offer a Globe Artichoke at rank ten. The vessel emptied, one artichoke was consumed and Thunderstorm gained five uses. The muted client selected the original storm icon and cast through the gameplay controls. The world switched to rain and thunder, and four charges and 1,114 blood remained. An attempt during the storm kept all four charges.
- [x] Restart during the active Thunderstorm. The same player logged in with four charges and 1,114 blood, the vessel stayed empty and the saved thunderstorm continued. Java 17 data generation, JAR reobfuscation and all 171 required Forge GameTests passed.

## Bat Swarm (2026-11-20)

- [x] Offer Wool of Bat to a full Blood Crucible and obtain five uses of Bat Swarm. The muted survival client used a filled-vessel fixture after the separate nightly hunting test.
- [x] Summon fifteen flying Attack Bats through the fifth power slot. The muted client aimed at an Iron Golem twelve blocks away. The bats lowered it from 100 to 84 health while the caster kept full health. GameTests covered four-point magic damage and expiration on contact with both vulnerable and protected targets.
- [x] Preserve swarm ownership and expiry on a dedicated restart. All fifteen bats reloaded and the tracked bat retained its original UUID and owner. The swarm expired as the saved timers ran down. Java 17 data generation, JAR reobfuscation and all 175 required Forge GameTests passed.

## Coffin and Village Travel (2026-11-20)

- [x] Offer a bone at a full Blood Crucible to charge the final ultimate. The muted rank-ten client spent one bone and received five uses. The bowl emptied.
- [x] Travel to a saved coffin and from the coffin to a village. The client used daytime sleep to set its real respawn at the coffin. A cast from over forty blocks away brought it back to the coffin. A cast while nearby moved it to the settlement where a villager had claimed a bed. Each cast spent one use.
- [x] Travel home from the Nether. A dimension change now sends the Witchery capability to the replacement client player. The muted client kept its selected power when entering the Nether and returned to the same Overworld coffin. All 178 required Forge GameTests passed, including the bone offering, return to the saved spawn and the village destination.
- [x] Restart the dedicated server and verify the saved coffin, travel choice and final remaining use. The client rejoined beside its coffin with one travel charge and 1,062 blood.

## Blooded Wool and Dark Cloth (2026-11-20)

- [x] Turn white wool into Blooded Wool with a Bone Needle at rank four or higher. The muted survival client kept red wool untouched, converted and harvested five white wool blocks, and spent exactly 625 blood. The same Bone Needle remained in its hand. GameTests covered rank, low blood, dyed wool and a canceled Forge placement.
- [x] Smelt the harvested Blooded Wool into five Dark Cloth. A dedicated restart resumed the active furnace that had reached 68 ticks. The muted client collected all five pieces through the furnace menu. Data generation, reobfuscation and all 181 required Forge GameTests passed.

## Vampire Clothing (2026-11-20)

- [x] Restore all nine wearable pieces and their recipes, including fitted, reinforced and kilt variants. The muted survival client crafted the top hat with the five Dark Cloth from the furnace and dyed it blue in a standard crafting table. GameTests checked all nine recipes and their armor properties.
- [x] Apply the legacy feeding and Mesmerize bonuses. A bite on an awake villager drew seven blood with the hat alone and ten in the light outfit. The same rank-ten player cast enhanced Mesmerize and kept 404 blood after paying its 50-blood cost.
- [x] Check the player, naturally equipped vampire and chain armor on the muted client. After a dedicated restart, the same vampire wore its original three pieces, and the player retained four pieces, the blue dye and seven armor. Java 17 data generation, JAR reobfuscation and all 185 required Forge GameTests passed.

## Vampire Mortality (2026-11-20)

- [x] Restore the one-health recovery from ordinary lethal damage. A normal fall hit damaged the muted survival player, and otherwise lethal fall damage left both the player and a vampire mob at one health. The player gained five exhaustion.
- [x] Restore lethal fire and the clothing's flame protection. GameTests checked that two light garments save one hit in four and four garments save three hits in four. Fire Resistance stayed active while the burning player took damage in the packaged server. A lethal fire hit killed the unarmored player, who respawned at rank ten with 404 blood.
- [x] Verify dedicated-server persistence of vampire death drops and sunlight mortality. The same dropped sword kept its 14,400-tick lifespan after a restart. Rejoining at rank ten with zero blood and stepping into noon sunlight killed the player with the sunlight death message. Java 17 data generation, JAR reobfuscation and all 190 required Forge GameTests passed.
- [x] Restore the shifted werewolf and Wolfman death exceptions and Silver Sword and Silver Bolt lethality against vampire hybrids. On the packaged server, a 12-point hit attributed to an ordinary villager left a vampire at one health. The same hit attributed to a Wolfman killed it. GameTests also verified player attacks before and after shifting and both silver weapons against pure vampires and hybrids in human form.
- [ ] Review the old accessory-mod exceptions with their modern integrations.

## Cane Sword (2026-11-20)

- [x] Restore the crafting recipe, the drawn and sheathed models, the original sound clips and the guard. The muted survival client crafted the sword from five Dark Cloth, a gold ingot and a diamond sword. Sneak-use changed the attack damage from two to eight. The drawn guard reduced an incoming hit without wearing the sword.
- [x] Fill, save and use the blood reserve. A sheathed kill left the reserve empty. Two weakened villager fixtures with 180 and 140 blood filled it to 180 and then the 250 cap when killed with the drawn sword. After a dedicated restart, the player retained the drawn blade and saw 250 in its tooltip. The client sheathed and used it, moving all 250 into blood power to reach 654. Java 17 data generation, JAR reobfuscation and all 193 required Forge GameTests passed.

## Silver Vat and Silver Sword (2026-11-20)

- [x] Craft and place the original Silver Vat in survival. Four neighboring blast furnaces produced gold and the first two dust from raw-gold and coal fixtures. The vat kept a separate count for each output, and all produced gold remained available in the furnaces.
- [x] Restart with two dust stored and a blast furnace at 55 of 100 cooking ticks. The vat resumed producing dust from the same batches. The muted client saw its connected spouts and silver in the water and collected eight dust with a click.
- [x] Craft the Silver Sword with the recovered dust and a gold sword. The client equipped it with five attack damage. A separate tag fixture verified the recipe for two mod-provided silver ingots. All 196 required GameTests passed, including ordinary furnace output, the legacy dust mapping, comparators and hopper extraction. Data generation and JAR reobfuscation succeeded.

## Rite of Removing Vampirism (2026-11-20)

- [x] Restore the original small white circle and six offerings. The muted survival client sampled its coffin with a Taglock Kit and offered the Silver Sword it crafted from vat dust.
- [x] Require an active cat familiar, a six-member coven called with a Seer Stone, and the tagged vampire within eight blocks in the same world. The muted client recovered all six offerings without an active cat, with one coven member, and when twelve blocks away. It bound a cat through the familiar rite and used a saved six-member coven fixture for the full-coven test.
- [x] Spend 10,000 altar power on an eligible attempt. GameTests exercised the one-in-four failure and the cure, and confirmed that both consume the offerings and power. The dedicated survival attempt cured the rank-ten vampire, cleared its blood, vision and remaining ultimate charge, and gave it 50 human blood. It stood in daylight without taking damage and remained human after a dedicated restart. Java 17 data generation, JAR reobfuscation and all 198 required Forge GameTests passed.

## Garlic Garland (2026-11-20)

- [x] Restore the five-bulb garland and its recipe. The muted survival client crafted it from five garlic and three string and hung it in a doorway. The ward's facing controls its outline and model, and entities can move through the occupied space.
- [x] Check repulsion in the dedicated world. The cured player walked through the doorway, then drank a filled-goblet fixture and was pushed back on the same approach. A vampire mob moved about one and a half blocks away from the ward and kept full health. The mounted facing and repulsion persisted through restart. Java 17 data generation, JAR reobfuscation and all 200 required Forge GameTests passed.

## Village Guard Recruitment and Feeding (2026-11-20)

- [x] Recruit an adult villager with a crouching leather tunic offer. The muted client was refused before trading and then raised the villager's trading reputation to 14 in its normal menu. The two-resident settlement still refused. With seven additional resident fixtures and an occupied bed, Alden accepted guard duty. The client retained the same tunic through the refusal and successful offer.
- [x] Verify the recruited guard's equipment and behavior. Alden kept its name, began with 40 health and 500 blood, and received a bow, iron chestplate and leather gear. The client saw the bow pose as the guard shot a zombie from 20 to 13.3 health. The nearby creeper stayed at 20. GameTests covered the original tall guard data, flaming arrows and friendly-fire immunity.
- [x] Restart the dedicated server and verify the same UUID, bow, armor, 40 health and 500 blood. Java 17 data generation, JAR reobfuscation and all 202 required Forge GameTests passed.
- [x] Feed on a guard and show its blood on the vampire HUD. The muted client lured a guard with strengthened Mesmerize, drained its last seven blood and observed two health damage. After a bite on a villager, a sighted guard targeted the player and fired a bow. The occluded and paralyzed guards stayed idle.
- [x] Turn a drained guard with a goblet of the player's own blood. Offering without a coffin kept the filled goblet. The client then placed the coffin and made the offering. The goblet emptied and the player advanced from rank nine to ten. The new vampire kept its original conversion location as home when it left to hunt.
- [x] Collect guard blood with a drawn Cane Sword. A direct strike on a weakened guard loaded 190 blood. A dedicated restart kept the reserve, empty goblet, placed coffin, rank ten and the converted vampire's UUID and equipment. The survival profile began this check at rank nine and used a guard with seven remaining blood. Java 17 data generation, JAR reobfuscation and all 206 required Forge GameTests passed.
- [x] Restore general village defense. In a settlement anchored by a villager's claimed bed, a guard ignored a neutral wolf and then shot it after it killed another villager. With hostile gossip on the observer's account, the guard acquired that player as a target. Two muted clients also verified that the guard sided with a trusted player in a melee exchange. GameTests checked that ordinary aggressors stay in village memory for 300 ticks and that villagers are immune to a guard's stray arrows.
- [x] Restart the dedicated server and check the same guard's UUID and renewed pursuit from the resident's saved hostile gossip. Java 17 data generation, JAR reobfuscation and all 209 required Forge GameTests passed.

## Witch Hunter's Crossbow (2026-11-20)

- [x] Restore crafting for the crossbow, all five kinds of ammunition and the Null Catalyst. A muted survival client used the original patterns to craft the crossbow and nine Wooden Bolts.
- [x] Verify crouch loading, ammunition selection and the original model. The client loaded one Wooden Bolt, fired it into a wall, then switched between wooden, anti-magic, holy and splitting ammunition. Switching returned the previous bolt. The three splitting trajectories struck different blocks on the same wall. Each use cost two durability.
- [x] Preserve the chambered and preferred bolt through a dedicated restart. A chambered Silver Bolt and one already lodged in a wall both survived. The client recovered that projectile as a Silver Bolt. GameTests also covered legacy item metadata, the holy damage bonus and the powered anti-magic dispel.
- [x] Verify the optional silver-ingot recipe with a temporary `forge:ingots/silver` fixture. The recipe became available when the tag was supplied, and the client crafted six silver bolts with one tagged ingot and six wooden bolts. Java 17 data generation, JAR reobfuscation and all 213 required Forge GameTests passed.
- [x] Power Anti-Magic Bolts with the full Witch Hunter set. In a two-client test, a shot drained the shooter's 20 infusion charge, took the target's charge from 20 to 10, and left Poison active after dispelling Resistance.
- [x] Connect Silver Bolts and silvered armor to lycanthropy. The dedicated server recorded both the Silver Bolt selection against a Wolfman and the protection, durability and retaliatory damage from silvered armor.
- [ ] Restore the spirit enhancement for ordinary death wards hit by powered bolts.

## Witch Hunter Clothing (2026-11-20)

- [x] Craft Nullified Leather, a Witch Hunter hat and both armor upgrades through the survival crafting menu. The client dyed the hat blue and wore it through a damage check. Both upgrades preserved its dye and wear. The complete outfit uses the original hat, coat and armor textures in third-person.
- [x] Verify the garlicked coat's protection using the same attributed hit from a vampire. The player had 15.12 health and one durability damage with the plain coat, and 16 health and zero durability damage with the garlicked coat. A zombie hit wore the upgraded coat normally. A vampire wearing a garlicked hat also took periodic fire damage.
- [x] Restore the full-set magic and Waking Nightmare curse rolls and disable bound poppets while all four pieces are worn. GameTests checked the original probabilities, powered bolt interaction with an ordinary death ward, and the armor upgrade paths.
- [x] Preserve the treated hat across a dedicated restart. The saved stack kept its blue dye and one durability damage. An Ender Pearl was consumed at Z 99.30 with the full outfit. Removing the boots let a second pearl carry the player to Z 105.15. Java 17 data generation, JAR reobfuscation and all 217 required Forge GameTests passed.

## Witch Hunter Pursuits (2026-11-20)

- [x] Restore the Witch Hunter entity, crossbow attacks and original skins. A disliked rank-ten vampire drew two hunters from a village with a claimed bed and hostile gossip. The test server raised only the report probability for this check. Both hunters carried crossbows and shot Holy Bolts at the player. The server also confirmed that a hunter sheds Poison and limits an ordinary hit to nine damage. After a restart at the default report probability, both hunters retained their UUIDs, crossbows and player target.
- [x] Restore black magic notices from Voodoo and Vampiric Poppets and the Curse of Waking Nightmare. The muted survival client collected a pig's Taglock, crafted and used a bound Voodoo Poppet, and the server saved a pursuit timestamp. GameTests confirmed that a notice waits at least 2,400 ticks and can spawn at most two hunters with the original target. Java 17 data generation, JAR reobfuscation and all 220 required Forge GameTests passed.
- [x] Connect transformed werewolf targets and Silver Bolt selection. A packaged hunter selected a named Wolfman and fired bolts of type 4.
- [ ] Wire the remaining black magic triggers as their rites are restored.

## Hellhounds and Wolf Trophies (2026-11-20)

- [x] Restore Hellhound behavior, the original model and texture, and Nether spawns. The packaged dedicated server spawned 13 Hellhounds near a muted survival client with natural spawning enabled. The original groups of one to three and weight 25 are defined by a Forge biome modifier.
- [x] Restore Dog Tongue and Hellhound Head drops. The survival client collected three heads from the attributed Looting III fixture. It mounted one on a wall and placed an ordinary Wolf Head on top of a stone block. Both rendered with their original textures, and GameTests checked the legacy metadata, placement and drop variants.
- [x] Start the original Hellhound cure with a thrown Weakness potion and an ordinary golden apple. The client spent one apple and the dedicated server recorded 4,449 ticks of conversion time and Strength. A restart preserved both placed trophies and the Hellhound's UUID and countdown. The countdown completed naturally and the server recorded a wolf with ten seconds of Nausea. Java 17 data generation, JAR reobfuscation and all 223 required Forge GameTests passed.

## Curse of the Wolf and Player Wolf Form (2026-11-20)

- [x] Restore both original recipes, full-moon timing, active cat and six-member coven. The muted client called its cat, collected its Taglock at a bed, sacrificed a Hellhound Head it had picked up, and activated the medium infernal circle with a Seer Stone. A waning night refunded all six offerings. At the full moon, Witch Hunter clothing blocked the curse. Removing one piece let the same offerings curse the player and the server retained vampire rank ten under the default hybrid setting.
- [x] Restore the rank-one wolf form, smaller dimensions, armor drop, Night Vision, jump and movement stats, and Silver Sword damage. The dedicated server recorded speed changing from 0.10 to 0.15 and the additional step height of 0.4. A second muted client saw the first as a wolf. With the active familiar's damage sharing, a 12-point ordinary strike took 3.6 health and a seven-point silver strike took 10.8 health.
- [x] Reconnect after a dedicated-server restart, then collect another Taglock and perform the cure while still in wolf form. The save retained rank one, wolf form and speed. The successful cure consumed all six offerings, restored normal movement and kept vampire rank ten. Java 17 data generation, JAR reobfuscation and all 226 required Forge GameTests passed.
- [x] Restore Wolfsbane identification. The muted survival client used up one flower on an unshifted werevillager and saw the flame burst. A distant attempt left the stack unchanged. A nearby healthy villager used up one flower without flames. Java 17 data generation, JAR reobfuscation and all 248 required Forge GameTests passed.
- [x] Restore the Wolfsbane effect in drinkable and splash brews. A basic drink suppresses rank-one transformation for 60 seconds. The original strength modifiers raise the supported rank. The splash retains the direct-hit and proximity duration rules.

## Werevillagers and Wolfmen (2026-11-20)

- [x] Restore the Werevillager and Wolfman, including the original Wolfman texture, combat stats, silver vulnerability, and the village cure. The Curse of the Wolf now infects villagers, correcting the legacy rite's plain-villager conversion.
- [x] Keep profession, appearance, XP, trade history, the name and the original Taglock signature through transformation. The muted survival client cursed Felix, a snow librarian, observed his 80-health Wolfman form, and traded with him after daylight. The trade took his XP from 42 to 44 and uses from one to two.
- [x] Restart during a second transformation. The same Wolfman kept its UUID, damage and saved trade. The muted client used the Taglock it collected from the infected villager to cure the saved Wolfman. The resulting vanilla villager kept the original signature, level-three librarian profession, XP 44 and two trade uses.

## Wolf Altar and Moon Charm (2026-11-20)

- [x] Craft the Wolf Altar using three wolf trophies, three stone blocks and Wolfsbane. The muted survival client combined one Wolf Head with two Hellhound Heads in the original pattern, placed the altar and saw the original statue face the player. Using it while uninfected applied Mining Fatigue.
- [x] Complete the first three offerings at the placed altar. At rank one, two gold remained unspent and three gold yielded the Moon Charm and rank two. Taking 31 raw mutton to the altar advanced the player to rank three and left one mutton. Ten Dog Tongues advanced the player to rank four.
- [x] Verify the original three-second use time and automatic transformation suppression with a worn charm. The client released early without wear, then used one point of durability to return to human form during a full moon. GameTests exercised gold repair, the crouching Wolfman toggle at rank five, and form suppression with a worn charm. Java 17 data generation, JAR reobfuscation and all 232 required Forge GameTests passed.
- [x] Restart the dedicated server at the same full moon. The reconnected player kept rank four, human form and the charm with one durability point spent. The placed Wolf Altar retained its block entity.

## Horned Huntsman Trial (2026-11-20)

- [x] Claim one Horn of the Hunt at rank four and hold it for two seconds to summon the Huntsman. The muted client picked up the altar's horn, kept it after an early release, and saw it break after a successful call. The boss appeared with its original model, texture and spear, and a boss bar tracked its entrance.
- [x] Restart the dedicated server during the entrance. The same Huntsman reached 400 health after loading. GameTests verified the serialized entrance countdown, player-attributed defeat and altar turn-in, the 15-point raw damage cap, ranged arrows, and the skull, blood and enchanted book drops.
- [x] Land a real sword hit on the packaged Huntsman and turn in the trial. The test arena used Strength and a stationary boss prepared at 12 health for the final strike. It dropped three Wither Skeleton Skulls, an enchanted book and Infernal Blood. The survival client returned to the altar, advanced to rank five and used a crouching Moon Charm to become a Wolfman with 40 maximum health.
- [x] Restore the Huntsman's Spear and its 3D held model, knockback resistance, retaliating wolves and Nightmare vulnerability. Java 17 data generation, JAR reobfuscation and all 234 required Forge GameTests passed, including the legacy Moon Charm wear and wolf trial imports.

## Werewolf Hunts, Howls and Taming (2026-11-20)

- [x] Restore the five remaining trials and original turn-ins at the Wolf Altar. GameTests used attributed kills, taming and the howl handler to validate all five ranks. In the muted survival client, a grounded kill earned zero progress and ten airborne kills advanced the player to rank six.
- [x] Restore shift, look straight up and use for howling. The muted client crouched and howled in Wolfman form during the full moon. The dedicated restart preserved eight recorded chunks. The client repeated a saved location, added eight new chunks and turned in the trial.
- [x] Tame wolves by interacting in wolf form. The muted client tamed six wild wolves without using bones. The dedicated server verified all six owner UUIDs and the player advanced to rank eight.
- [x] Verify both howl abilities on the dedicated server. A wolf howl called two wolves with the player's ownership and Mortal Coil. The wolves expired naturally. After the shared cooldown elapsed, a Wolfman howl paralyzed a nearby villager.
- [x] Finish the final two trials in the muted survival client. A piglin kill in wolf form left progress at zero. Thirty attributed kills in Wolfman form advanced the player to rank nine. The final villager kill earned one point and the Wolf Altar awarded rank ten.
- [x] Preserve legacy quest state, progress and signed chunk coordinates. Java 17 data generation, JAR reobfuscation and all 239 required Forge GameTests passed. They covered all five ranks, the temporary wolf pack, Wolfman fear, the shared cooldown and natural pack expiry.

## Werewolf Combat and Digging (2026-11-20)

- [x] Apply the original silvered armor response to werewolf attacks. The packaged server gave the target the same eight-point Wolfman attack with each coat. The plain coat left 15.12 health and one point of wear. The silvered coat left 16 health and zero wear. It also took the Wolfman from 80 to 79.38 health.
- [x] Restore armor rending and bite transmission. The muted rank-ten Wolfman struck the second client, putting 44 wear on the iron helmet while the other pieces took two each. An infectious Wolfman converted a villager at three remaining health, keeping the original Taglock identity. A later nonlethal hit by the rank-ten player infected the second client at two health. The second client put on a silvered hat while human and took periodic damage.
- [x] Restore wolf digging and the bone discovery cooldown. The first tap in the muted client left the dirt in place. A crouching tap removed it. Further digging produced a bone that the player picked up and saved the find time. Java 17 data generation, JAR reobfuscation and all 243 required Forge GameTests passed.

## Bear Trap and Wolf Trap (2026-11-20)

- [x] Craft and place the original Bear Trap and upgrade a second one to a Wolf Trap. Both muted clients saw the original trap model. The armed Bear Trap was faint and lacked an outline for the non-owner. A live pig moved through it and took four damage and Paralysis III.
- [x] Arm the Wolf Trap beside a Wolf Altar at the full moon. The muted survival client leashed a sheep and tethered it to the fence. The trap recorded the summoned Wolfman's UUID. It stayed armed when an unrelated Wolfman moved through, then sprang and made its own Wolfman infectious when that creature moved through it.
- [x] Verify the original loot and saved-tag formats. GameTests checked timed arming, the legacy owner UUID string, bait gating, capture and the absence of a Wolf Trap item drop. Java 17 data generation, JAR reobfuscation and all 246 required Forge GameTests passed.
- [x] Restart the packaged dedicated server. Both placed traps retained their owner and sprung state. The Wolf Trap retained its specific Wolfman's UUID, that Wolfman remained infectious, and the sheep kept its fence leash.

## General Cauldron Brewing (2026-11-20)

- [x] Restore ordered ingredients, capacity, per-effect strength and duration, inversion, instant splash, altar costs, bottling yields and comparator output. The existing Mutandis ritual still uses its original trigger and output.
- [x] Verify a three-bucket Wolfsbane batch in the muted survival client. The client filled the cauldron with buckets and dropped in Mandrake Root followed by Wolfsbane. The liquid changed color. An actual dedicated restart preserved its heat, water and ingredient order. The client bottled and drank it, received the effect and recovered the glass bottle.
- [x] Verify powered brewing and splash throws. The live cauldron kept the unpowered mixture and bottle. A nearby altar let the client bottle the inverted sugar brew. A real Wolfsbane splash applied the effect to a directly hit pig and a nearby pig for different durations. The pig outside the radius received no effect.
- [x] Verify two effects in the same brew and their status icons. Glowstone strengthened Wolfsbane while Ender Inhibition remained at its base strength. An Ender Pearl thrown under Ender Inhibition consumed the pearl and left the player in place. In the control throw, clearing the effect restored teleportation. The player's bottling skill survived the restart.
- [x] Run Java 17 data generation, JAR reobfuscation and all 253 required Forge GameTests. The tests cover solo and mixed brews, the first bottling threshold, named legacy ingredient metadata, splash scaling, Ender Inhibition and the fixed Mutandis recipe.
- [ ] Port the remaining brew actions and effects, including brew fluid containers, additional modifiers and castable cauldron rituals.

## Gaseous and Liquid Brews (2026-11-20)

- [x] Restore Bat Wool and Wormwood dispersal, replacement of the previous dispersal ingredient, and lifetime modifiers. Pressing the bottle's use control releases the mixture at impact. The saved block entities carry the original effect list, color, lifetime, expansion and thrower.
- [x] Restore gas spread and Gas Mask. Gas spreads in six directions and preserves its data in each new cell. Gravel brews Gas Mask, which survives milk and blocks harmful gas effects. The muted client brewed and threw a poisonous cloud. The server recorded Poison on the unmasked pig and only Gas Mask on the masked pig.
- [x] Restore liquid flow and contact. The muted client threw a Liquid Brew of Wolfsbane onto a raised pig. The dyed liquid fell beside the support, reached the ground, and applied Wolfsbane to both the elevated and ground-level pigs. The source, falling stream, and runoff all returned to air.
- [x] Save and restart during an active gas brew. Both the source cell and a neighboring cell retained the poison mixture, color, thrower and age, then continued aging.
- [x] Run Java 17 data generation, JAR reobfuscation, and all 256 required Forge GameTests. The new tests check dispersal precedence, gas expansion and expiry, Gas Mask, and liquid flow and recession.

## Triggered Brews and Cursed Controls (2026-11-20)

- [x] Restore Zombie Head as the zero-power triggered dispersal ingredient. Thrown brews arm the original seven controls. An identical recipe adds a charge, and a different recipe replaces the saved mixture.
- [x] Retain each control's appearance and redstone behavior. Manual button, lever and door use or a new pressure plate press apply the stored mixture at full duration. The last charge restores the original block in its current state, including an active redstone pulse and both halves of an open door.
- [x] Preserve the original cursed control block and block entity IDs and their saved mixture keys. Tested named legacy brew ingredient decoding includes Zombie Head skull metadata.
- [x] Verify two separate Wolfsbane batches in the muted survival client. Both throws armed the same wall button. The first press applied Wolfsbane, lit a lamp through the support and spent one charge. A dedicated-server restart retained the remaining charge and original mixture. The final press reapplied Wolfsbane, restored the vanilla button during the pulse and turned the lamp off on release.
- [x] Run Java 17 data generation, JAR reobfuscation and all 261 required Forge GameTests. The new tests cover recharging, replacing a mixture, the last button pulse, all three pressure plates, both door halves and legacy skull metadata.
- [x] Restore the separate cauldron ritual that puts triggered brew charges on inventory items.

## Cauldron Item Curses (2026-11-20)

- [x] Activate a full, boiling triggered brew by dropping in Tongue of Dog. The cauldron warms up for 20 pulses before it spends power. Inner and outer white chalk rings reduce cost and avoid the ordinary circle risk. Infernal chalk trades power for risk.
- [x] Preserve the active mixture and pulse count across a dedicated restart. If the cauldron has no offering at the casting pulse, return the tongue and keep the underlying brew.
- [x] Attach the curse to one item from a stack. With an active cat familiar, the muted survival client recovered one stick with two charges from an offering of three. The remaining two sticks kept their ordinary stack.
- [x] Verify that a right click in air applies Wolfsbane, an immediate second click preserves the remaining charge, and the next click after five seconds reapplies Wolfsbane and clears the curse while keeping the item. The successful cast used a dropped-item fixture for the tongue and stick stack.
- [x] Run Java 17 data generation, JAR reobfuscation and all 264 required Forge GameTests. Coverage includes the 250-power debit with two white circles, the named 1.7.10 active-brew save, a missing offering, stack splitting, cooldown, and both interaction paths.
- [ ] Restore additional terrain actions and Leonard's encounter and summoning ritual.
- [ ] Restore the hallucinations from the Insanity effect that can occur during a risky cast.

## Cauldron Dispersal and Waystone Targets (2026-11-20)

- [x] Restore the gas wave and liquid rain. Gas expands from the target over successive pulses. The liquid rite spawns falling projectiles under the original entity ID, with the original Quartz Sphere sprite and saved brew color. Every pulse draws altar power.
- [x] Restore instant block-targeted casts and coal's Extinguish effect. A cast puts out fire in an enlarged area. Ordinary thrown bottles use the smaller radius. A powered cast can still be bottled during warmup and keeps the legacy active marker.
- [x] Restore the Bound Waystone's 100-power modifier and same-dimension distance limits. A cast without coven assistance reaches 32 blocks. Failed range checks return the tongue and preserve the base brew. Gas and instant casts use the bound coordinates. The legacy rain uses the bound dimension and the cauldron's horizontal coordinates.
- [x] Run Java 17 data generation, JAR reobfuscation and all 268 required Forge GameTests. Coverage includes the gas wave before and after reload, valid and distant waystones, liquid projectile saves and contact effects, and active-brew bottling.
- [x] Verify the bound gas wave with the muted background client and dropped-ingredient fixtures. The pig at the waystone received Wolfsbane first. The wave reached a second pig farther out and left the pig beside the cauldron unaffected.
- [x] Verify the packaged liquid cast and Extinguish rite. The client rendered tinted droplets and their impacts. The server recorded the falling brew's ordered ingredients, and the cauldron emptied after ten pulses. With the waystone as the target, fire five blocks away went out and fire eight blocks away stayed lit.

## Fixed Cauldron Recipes and Outputs (2026-11-20)

- [x] Load all 26 legacy fixed recipes. Restore the Nether Wart and End Stone conversions, four strengths of Brew Fuel and Endless Water, both pit blocks, the Player Compass, cooking, and the Witch's Hand conversion. Retain the existing chalk, Mutandis, Mutandis Extremis, Mutating Sprig and Drop of Luck recipes.
- [x] Include the ingredient power costs and original precedence for recipes that share a trigger. Custom cauldron recipes keep their existing exact-match and fixed-power defaults.
- [x] Wire up the four burn times and four water capacities, manual and dispenser pouring, and the two pass-through pit blocks. Bind the Player Compass with a filled Taglock Kit and update its needle using the original 32 directional frames.
- [x] Verify dropped-ingredient casts in the packaged muted client. The five-use Endless Water brew filled the Witch's Cauldron. Its water supplied the strongest fuel recipe. The Witch's Oven consumed that fuel, began a 50,000-tick burn, and finished the sapling. A Player Compass from the cauldron bound to a pig in the inventory crafting grid. Its needle changed by a half turn when the player turned around.
- [x] Verify a dedicated restart with a long oven burn, the used water brew, and the bound compass. The oven kept its full-sized timer and resumed burning. The bottle retained its wear and the compass still tracked the tagged pig.
- [x] Run Java 17 data generation, JAR reobfuscation, and all 273 required Forge GameTests. Coverage includes all recipe variants, ingredient power accounting, recipe precedence, saved cast resumption, named legacy item metadata, pouring and dispensing, compass binding, and pit collision.

## Taglock Cauldron Casts and Protection Statues (2026-11-20)

- [x] Trigger a full cauldron with a Taglock Kit and 1,000 base altar power. The cast can use a drinking brew. It resolves the tagged creature or player at release, applies the full effect to that target, and refunds the original kit and preserves the brew if the target is missing or too far away.
- [x] Respect Witch Hunter clothing, configured area protection and Voodoo Protection Poppets in their original order. Both statues protect against targeted brews. The Statue of Broken Curses also protects against the directed Waking Nightmare rite. Both server options default to disabled with a 32-block horizontal radius. Statue ownership and the local list of player killers retain their legacy save fields.
- [x] Capture a pig's taglock in the muted background survival client and use the bound item in a dropped-ingredient cast. The server applied Wolfsbane to the pig after it moved during warmup and left the neighboring pig unaffected.
- [x] Save a second cast three pulses into warmup. The dedicated server resumed and completed it after restart. The enabled directed statue seven blocks below the pig kept its facing, owner and killer list. The cast used the brew and applied no Wolfsbane.
- [x] Run Java 17 data generation, JAR reobfuscation and all 278 required Forge GameTests. Coverage includes range failure, blank kits, named legacy cast resumption, loaded statue behavior, player killer records and the poppet's wear.
- [ ] Restore the wolf and mandrake statue geometry.

## Rites of Transposition (2026-11-20)

- [x] Use the medium Otherwhere circle, Waystone, bound Taglock Kit, Ender Dew, Iron Axe and 3,000 altar power to summon a creature or player. The muted survival client captured a pig's taglock. After the fixture moved that pig to the Nether and dropped the offerings, the client activated the circle and recovered the same pig at its center. The nearby pig stayed in place.
- [x] Keep the Dream restriction, Witch Hunter resistance, Statue of Occluded Summons, enhanced carried Voodoo Protection Poppet, and Ender Inhibition IV. A failed attempt returns the offerings and spends the altar charge. Existing enhanced fetish items and block entities provide the original 16-block boost and retain the enhancement when broken and placed again.
- [x] Verify redstone activation with the muted client connected. It arrived from the Nether on the first attempt. With the migrated scarecrow and bound poppet in the second fixture, the player stayed put, the poppet took 350 wear, and all four offerings returned.
- [x] Use the large Otherwhere circle and the five original offerings to extract ore from a 17-by-17 area. The rite scans 30 layers and adds five per coven member. Six members also enable gold. Both stone and deepslate ore variants drop their own blocks. The active layer survives a restart and can be restored from the legacy rite 7 save.
- [x] Save the native ore rite after six layers. On restart, it finished thirty layers and left exactly two Iron Ore items and one Deepslate Iron Ore item. Gold, iron on layer 31, and iron outside the range all stayed in place.
- [x] Run Java 17 data generation, JAR reobfuscation and all 284 required Forge GameTests. Coverage includes Nether summoning, the Ender Inhibition threshold, refunds, both area and carried defenses, and the legacy six-member ore rite.
- [ ] Restore the spirit-binding progression for new enhanced fetishes and finish the other spirit effects. The current enhancement can be used with migrated items and blocks.

## Dream Spirits and the Spectral Stone (2026-11-20)

- [x] Restore the flying Dream Spirit, its original model and translucent effects, Focused Will attraction, and groups of two to five natural spawns. A defeated Spirit yields a glinting Subdued Spirit that carries through waking. Using the item on a block releases a Spirit again.
- [x] Restore the structure guide. Drop an Attuned Stone or Subdued Spirit in a tiny white chalk ring to send a short-lived Spirit toward the nearest village or Nether fortress. The reusable Subdued Village Spirit retains its separate item ID and returns its token on expiry. Both guide variants save their remaining lifetime and destination.
- [x] Verify a client toss of two Attuned Stones. The guide flew toward the village found by the server, erased the ring and left one Attuned Stone. It disappeared after ten seconds.
- [x] Brew Congealed Spirit from five separate Subdued Spirits and one Brew of Hollow Tears for 2,000 altar power. The muted survival client brought four hunted Spirits through an Icy Needle wake. The test added a fifth Spirit to the kettle fixture, and the client bottled the finished brew. Drinking it gives 30 seconds of Night Vision II.
- [x] Restore the night-only Rite of Necromancy for a blank Spectral Stone. It uses the small white circle, 6,000 altar power, Congealed Spirit, Condensed Fear, Spectral Dust, a Boline and a Necromantic Stone. This rite consumes the Necromantic Stone.
- [x] Save the rite after its first three offerings. The dedicated server resumed it after restart. The muted client rejoined and collected one blank Spectral Stone.
- [x] Run Java 17 data generation, JAR reobfuscation and all 290 required Forge GameTests. Coverage includes Dream spawn rules, legacy guide data and expiry, item release, separate kettle offerings, bottling and the resumed night rite.
- [x] Restore the spectral creatures and the charged Spectral Stone's binding and release.

## Brazier and Spectral Binding (2026-11-20)

- [x] Craft the Brazier with a Necromantic Stone, iron ingots and sticks. It accepts three separate ingredients by hand or automation and ignites with flint and steel or a redstone pulse. A water bottle or bucket empties it and returns the container. Invalid offerings fizzle. The comparator reflects the occupied slots.
- [x] Port all eight recipes. Spectres and Banshees burn for 600 ticks and Poltergeists for 900. Graveyard Mist, Strength, Resistance and Invisibility draw altar power while burning. Wilting ages crops backward, heals nearby undead and extends its own burn without drawing power. Graveyard Dust and Wormy Apples keep legacy ingredient subtypes 128 and 91. The existing Arthana drops provide Spectral Dust before the player makes a Brazier.
- [x] Replace the three spectral entity placeholders with individual combat and haunting behaviors, translucent models and legacy sounds. Spectres drain health on contact, Earmuffs block the Banshee's scream, and a Poltergeist tosses items or attacks wall hangings. Near its summoner it can eject one item from a chest when mob griefing is permitted.
- [x] Use a medium white chalk ring, a blank Spectral Stone, Spectral Dust, a Boline and 5,000 altar power to bind up to three creatures of one kind. Holding the charged stone for at least two seconds releases the creatures under the player's ownership. The stone becomes blank after it is emptied and preserves any charges it could not place.
- [x] Verify the packaged Poltergeist recipe with the muted survival client. It placed the Brazier, inserted Wormwood, Refined Evil and Focused Will and lit the batch. The dedicated server saved 634 burn ticks and resumed the same batch after restart. The recipe finished and produced the creature that was then moved into the binding circle fixture.
- [x] Verify binding, release and the Poltergeist's chest behavior. The muted client tossed the offerings, activated the rite, collected the resulting stone and preserved it on an early release. A full charge released an owned Poltergeist and emptied the stone. The summoned creature ejected a diamond from a test chest and the client collected it.
- [x] Finish a Banshee batch in the muted client and verify the scream pose. The player lost two health in range. After equipping Earmuffs the player stayed at 18 health through the following scream pulses.
- [x] Run Java 17 data generation, JAR reobfuscation and all 294 required Forge GameTests. The four new cases cover powered and paused summons, redstone and flint ignition, water cleanup, original spectral offerings, legacy stone metadata and a reloaded rite that binds and releases three creatures.
- [ ] Restore brazier ignition by the Mystic Branch's Incendio, the Revealing brew's spectral effects, and the rites and abilities for spirit-enchanted fetishes.

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
  - The Altar, Witch's Oven, Distillery and Spinning Wheel use dedicated menus. The five remaining registry entries use `LegacyPlaceholderMenu` and `LegacyPlaceholderScreen`.
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
