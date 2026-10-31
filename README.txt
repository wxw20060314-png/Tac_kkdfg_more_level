TaCZ More Level is a large content-expansion addon for [TaCZ] Timeless and Classics Zero.

It is not a pile of loose features, but a full growth and combat system built for gunfight survival: weapon tiers, ballistic and legendary armor, armed AI, Boss encounters, recruitable escorts, black-market economy, medical and tactical gear, plus military structures and outposts across the Overworld, Nether, and End—together forming a metro-escape–style gunfight loop in a wasteland setting.

Inspired by classic tactical shooters and “metro escape” gameplay, the mod adds numerical depth, battlefield pressure, and exploration goals on top of TaCZ. You keep upgrading your loadout, moving between camps and bases for loot, and facing deadlier enemies and legendary threats with higher-tier guns and armor.

Weapon Quality System
On top of TaCZ guns, a strict four-tier quality ladder (Improved / Refined / Perfect / Excellent). Higher tiers mean stronger base damage and armor penetration—essential for late-game armor and Bosses:

Improved — Basic tuning; modest damage and penetration gains
Refined — Service-grade refinement; clearly stronger in combat
Perfect — High penetration that can tear through ordinary armor
Excellent — Top-tier military output and armor pierce
Progress through upgrade vouchers and related routes.
※ [1.4.2] Stronger gun-pack integration: auto-generate four-tier guns and recipes for currently loaded TaCZ gun packs (see Tips at the end).

Armor & Legendary Gear
Adds Type I–VII tactical ballistic armor with its own damage model (bullet reduction, penetration mitigation)—not reskinned leather/diamond.

Above the tiered sets are multiple legendary kits and skills (e.g. Shockwave, Power Mech, Space-Time, Stealth, Special Ops, Blast Type VII, and more).

Armed NPC Threat Ladder
Six ranks of armed NPCs drive dynamic pressure: Refugee, Militia, Mercenary, Assault, Special Forces, Super Special Forces (plus Lurker and others). Each rank wears matching armor, carries matching gun quality, and uses different movement and reaction patterns.

Bosses & Signature Encounters
Includes multiple Boss / elite fights—e.g. War Machine, Annihilator, Lev Marshal, Bomb Baby, Bomb Sight, Ceiling Duel, and more.
※ [1.4.2 new] Scorpion — Humanoid gun elite Boss with unique voice lines and multiple skins.
※ [1.4.2 new] Slime Commander / Slime Warrior — Split pressure and lineage aggro; the Commander is the apex of that line and drops serum and other materials.

Escort Partners
A summonable, manageable escort system (Standard / Beauty / Elite / Demon Lord escorts and guards, etc.), with contact and storage items so you can keep trainable combat partners in high-pressure fights.

World Structures & Outposts
Multi-dimension military and trade sites to explore: soldier camps, merchant camps, military bases, warehouse arenas, underground test facilities; Nether soldier camps / wind bases / bomb factories (WIP); End soldier camps, End black-market camps, and more. Camps often hold supplies and navigation maps that point you toward higher-reward targets.

Tactical Gear & Logistics
Throwables & traps: frag grenades, molotovs, lethal mines, freeze mines, etc.
Economy & intel: black market / Mysterious Merchant, navigation maps.
※ [1.4.2 new · Medicine]
Healing: Bandage, First Aid Kit, Medical Box (channel heal; the latter two also apply regeneration)
Injectables: Junior / Medium / Advanced Hardening Needles; Junior / Medium / Advanced Comprehensive Immunity Needles
Crafting: Syringe; Syringe + Nether Star + 2/4/6 Slime Serum → Hardening Needle; Syringe + Dragon Egg + 2/4/6 Serum → Immunity Needle
Summary
TaCZ More Level aims to be a full gunfight content suite beside TaCZ: from common soldiers to Bosses, four-tier guns to Type VII armor, escorts to the black market, wilderness camps to cross-dimension builds—a long-term wasteland gunfight progression line.

Required dependencies:

[TaCZ] Timeless and Classics Zero 1.1.8+
GeckoLib
Install on both client and server.

Tips (please read)
1) [1.4.2] How gun-pack integration works (important)
This scans TaCZ guns loaded in the current world and writes Improved / Refined / Perfect / Excellent content into a standalone gun pack. It does not auto hot-reload.

Install TaCZ, this mod, and third-party gun packs, then enter a world.
(Optional) /tacz_morelevel gun_upgrade test — preview how many guns can be generated.
/tacz_morelevel gun_upgrade all — writes to: game directory/tacz/tacz_morelevel_gun_upgrades/.
You must run /tacz reload manually before the tiered guns and recipes take effect.
After adding/removing gun packs, prefer: /tacz_morelevel gun_upgrade clear → all again → /tacz reload.
On multiplayer, keep that generated folder consistent across sides (usually generate on the server and sync, or run the same packs on each side).

2) Oversized packets
Large upgrade packs may cause disconnects (Packet too big / Badly compressed packet). Install Packet Fixer or XXL Packets on both client and server (chat also posts links after generation).
