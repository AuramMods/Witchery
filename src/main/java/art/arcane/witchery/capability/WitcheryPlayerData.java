package art.arcane.witchery.capability;

import art.arcane.witchery.vampirism.VampireUltimate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public final class WitcheryPlayerData {
    private static final String KEY_INITIALIZED = "Initialized";
    private static final String KEY_SYNC_REVISION = "SyncRevision";
    private static final String KEY_CREATURE_TYPE_ORDINAL = "CreatureTypeOrdinal";
    private static final String KEY_HUMAN_BLOOD = "HumanBlood";
    private static final String KEY_VAMPIRE_LEVEL = "VampireLevel";
    private static final String KEY_WEREWOLF_LEVEL = "WerewolfLevel";
    private static final String KEY_BLOOD_POWER = "BloodPower";
    private static final String KEY_BLOOD_RESERVE = "BloodReserve";
    private static final String KEY_VAMPIRE_LEVEL_CAP = "VampireLevelCap";
    private static final String KEY_VAMPIRE_QUEST_COUNTER = "VampireQuestCounter";
    private static final String KEY_VAMPIRE_QUEST_CHUNKS = "VampireQuestChunks";
    private static final String KEY_VAMPIRE_VISION = "VampireVision";
    private static final String KEY_VAMPIRE_ULTIMATE = "VampireUltimate";
    private static final String KEY_VAMPIRE_ULTIMATE_CHARGES = "VampireUltimateCharges";
    private static final String KEY_PREVIOUS_MAY_FLY = "VampirePreviousMayFly";
    private static final String KEY_PREVIOUS_FLYING = "VampirePreviousFlying";
    private static final String KEY_GROTESQUE_TICKS = "GrotesqueTicks";
    private static final String KEY_NIGHTMARE_LEVEL = "NightmareLevel";
    private static final String KEY_GHOST = "Ghost";
    private static final String KEY_OTHER_PLAYER_SKIN = "OtherPlayerSkin";
    private static final String KEY_PREPARED_SPELL_EFFECT_ID = "PreparedSpellEffectId";
    private static final String KEY_PREPARED_SPELL_LEVEL = "PreparedSpellLevel";
    private static final String KEY_LAST_ITEM_UPDATE_SLOT = "LastItemUpdateSlot";
    private static final String KEY_LAST_ITEM_UPDATE_DAMAGE = "LastItemUpdateDamage";
    private static final String KEY_LAST_ITEM_UPDATE_PAGE = "LastItemUpdatePage";
    private static final String KEY_LAST_MARKUP_BOOK_SLOT = "LastMarkupBookSlot";
    private static final String KEY_LAST_MARKUP_BOOK_PAGES = "LastMarkupBookPages";
    private static final String KEY_LAST_HOWL_GAME_TIME = "LastHowlGameTime";

    private boolean initialized;
    private int syncRevision;
    private int creatureTypeOrdinal;
    private int humanBlood;
    private int vampireLevel;
    private int werewolfLevel;
    private int bloodPower;
    private int bloodReserve;
    private int vampireLevelCap;
    private int vampireQuestCounter;
    private final List<VampireVillage> vampireQuestVillages = new ArrayList<>();
    private boolean vampireVision;
    private VampireUltimate vampireUltimate = VampireUltimate.NONE;
    private int vampireUltimateCharges;
    private boolean previousMayFly;
    private boolean previousFlying;
    private int selectedVampirePower;
    private long nextVampireAbility;
    private int foodBeforeEating = -1;
    private int grotesqueTicks;
    private int nightmareLevel;
    private boolean ghost;
    private String otherPlayerSkin = "";
    private int preparedSpellEffectId;
    private int preparedSpellLevel;
    private int lastItemUpdateSlot = -1;
    private int lastItemUpdateDamage;
    private int lastItemUpdatePage;
    private int lastMarkupBookSlot = -1;
    private final List<String> lastMarkupBookPages = new ArrayList<>();
    private long lastHowlGameTime = Long.MIN_VALUE;

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public int getSyncRevision() {
        return syncRevision;
    }

    public void mergeSyncRevision(int syncRevision) {
        this.syncRevision = Math.max(this.syncRevision, Math.max(syncRevision, 0));
    }

    public void bumpSyncRevision() {
        syncRevision++;
    }

    public int getCreatureTypeOrdinal() {
        return creatureTypeOrdinal;
    }

    public void setCreatureTypeOrdinal(int type) {
        creatureTypeOrdinal = Mth.clamp(type, 0, 5);
    }

    public int getHumanBlood() {
        return humanBlood;
    }

    public void setHumanBlood(int amount) {
        humanBlood = Mth.clamp(amount, 0, 500);
    }

    public int getVampireLevel() {
        return vampireLevel;
    }

    public void setVampireLevel(int level) {
        int rank = Mth.clamp(level, 0, 10);
        if (rank != vampireLevel) {
            vampireLevel = rank;
            vampireQuestCounter = 0;
            vampireQuestVillages.clear();
            selectedVampirePower = 0;
        }
    }

    public int getWerewolfLevel() {
        return werewolfLevel;
    }

    public void setWerewolfLevel(int level) {
        werewolfLevel = Mth.clamp(level, 0, 10);
        setBloodPower(bloodPower);
    }

    public int getBloodPower() {
        return bloodPower;
    }

    public void setBloodPower(int amount) {
        bloodPower = Mth.clamp(amount, 0, getMaxBloodPower());
    }

    public int getBloodReserve() {
        return vampireLevel > 0 ? bloodReserve : 0;
    }

    public void setBloodReserve(int amount) {
        bloodReserve = Mth.clamp(amount, 0, 250);
    }

    public int getMaxBloodPower() {
        return 500 + (werewolfLevel >= 2 ? vampireLevel / 2 : vampireLevel) * 250;
    }

    public int getVampireLevelCap() {
        return vampireLevelCap;
    }

    public boolean increaseVampireLevelCap(int levelCap) {
        if (levelCap <= vampireLevelCap) return false;
        vampireLevelCap = Mth.clamp(levelCap, 3, 10);
        return true;
    }

    public int getVampireQuestCounter() {
        return vampireQuestCounter;
    }

    public void setVampireQuestCounter(int counter) {
        vampireQuestCounter = Math.max(0, counter);
    }

    public boolean visitVampireVillage(String dimension, int chunkX, int chunkZ) {
        long location = (long) chunkX << 32 | (long) chunkZ & 0xffffffffL;
        var village = new VampireVillage(dimension, location);
        if (vampireQuestVillages.contains(village)) return false;
        vampireQuestVillages.add(village);
        return true;
    }

    public boolean hasVampireVision() {
        return vampireVision;
    }

    public void setVampireVision(boolean active) {
        vampireVision = active;
    }

    public VampireUltimate getVampireUltimate() {
        return vampireUltimate;
    }

    public int getVampireUltimateCharges() {
        return vampireUltimateCharges;
    }

    public void setVampireUltimate(VampireUltimate ultimate, int charges) {
        vampireUltimate = ultimate;
        vampireUltimateCharges = ultimate == VampireUltimate.NONE ? 0 : Mth.clamp(charges, 0, VampireUltimate.MAX_CHARGES);
    }

    public void rememberFlight(boolean mayFly, boolean flying) {
        previousMayFly = mayFly;
        previousFlying = flying;
    }

    public boolean previouslyAllowedToFly() {
        return previousMayFly;
    }

    public boolean previouslyFlying() {
        return previousFlying;
    }

    public int getSelectedVampirePower() {
        return selectedVampirePower;
    }

    public void setSelectedVampirePower(int power) {
        selectedVampirePower = power;
    }

    public boolean beginVampireAbility(long gameTime) {
        if (gameTime < nextVampireAbility) return false;
        nextVampireAbility = gameTime + 10;
        return true;
    }

    public void beginEating(int foodLevel) {
        foodBeforeEating = foodLevel;
    }

    public boolean gainedFood(int foodLevel) {
        boolean gained = foodBeforeEating >= 0 && foodLevel > foodBeforeEating;
        foodBeforeEating = -1;
        return gained;
    }

    public int getGrotesqueTicks() {
        return grotesqueTicks;
    }

    public int getNightmareLevel() {
        return nightmareLevel;
    }

    public boolean isGhost() {
        return ghost;
    }

    public String getOtherPlayerSkin() {
        return otherPlayerSkin;
    }

    public int getPreparedSpellEffectId() {
        return preparedSpellEffectId;
    }

    public int getPreparedSpellLevel() {
        return preparedSpellLevel;
    }

    public int getLastItemUpdateSlot() {
        return lastItemUpdateSlot;
    }

    public int getLastItemUpdateDamage() {
        return lastItemUpdateDamage;
    }

    public int getLastItemUpdatePage() {
        return lastItemUpdatePage;
    }

    public int getLastMarkupBookSlot() {
        return lastMarkupBookSlot;
    }

    public List<String> getLastMarkupBookPages() {
        return List.copyOf(lastMarkupBookPages);
    }

    public long getLastHowlGameTime() {
        return lastHowlGameTime;
    }

    public void applyPlayerStyle(
            String playerSkin,
            int grotesqueTicks,
            int nightmareLevel,
            boolean ghost,
            int creatureTypeOrdinal,
            int humanBlood
    ) {
        this.otherPlayerSkin = playerSkin == null ? "" : playerSkin;
        this.grotesqueTicks = grotesqueTicks;
        this.nightmareLevel = nightmareLevel;
        this.ghost = ghost;
        this.creatureTypeOrdinal = creatureTypeOrdinal;
        this.humanBlood = humanBlood;
    }

    public void applyPreparedSpell(int effectId, int level) {
        preparedSpellEffectId = effectId;
        preparedSpellLevel = level;
    }

    public void applyItemUpdate(int slot, int damage, int page) {
        lastItemUpdateSlot = slot;
        lastItemUpdateDamage = damage;
        lastItemUpdatePage = page;
    }

    public void applyMarkupBookSync(int slot, List<String> pages) {
        lastMarkupBookSlot = slot;
        lastMarkupBookPages.clear();
        if (pages != null) {
            for (String page : pages) {
                lastMarkupBookPages.add(page == null ? "" : page);
            }
        }
    }

    public void markHowled(long gameTime) {
        lastHowlGameTime = gameTime;
    }

    public void copyFrom(WitcheryPlayerData source) {
        initialized = source.initialized;
        syncRevision = source.syncRevision;
        creatureTypeOrdinal = source.creatureTypeOrdinal;
        humanBlood = source.humanBlood;
        vampireLevel = source.vampireLevel;
        werewolfLevel = source.werewolfLevel;
        bloodPower = source.bloodPower;
        bloodReserve = source.bloodReserve;
        vampireLevelCap = source.vampireLevelCap;
        vampireQuestCounter = source.vampireQuestCounter;
        vampireQuestVillages.clear();
        vampireQuestVillages.addAll(source.vampireQuestVillages);
        vampireVision = source.vampireVision;
        vampireUltimate = source.vampireUltimate;
        vampireUltimateCharges = source.vampireUltimateCharges;
        previousMayFly = source.previousMayFly;
        previousFlying = source.previousFlying;
        grotesqueTicks = source.grotesqueTicks;
        nightmareLevel = source.nightmareLevel;
        ghost = source.ghost;
        otherPlayerSkin = source.otherPlayerSkin;
        preparedSpellEffectId = source.preparedSpellEffectId;
        preparedSpellLevel = source.preparedSpellLevel;
        lastItemUpdateSlot = source.lastItemUpdateSlot;
        lastItemUpdateDamage = source.lastItemUpdateDamage;
        lastItemUpdatePage = source.lastItemUpdatePage;
        lastMarkupBookSlot = source.lastMarkupBookSlot;
        lastMarkupBookPages.clear();
        lastMarkupBookPages.addAll(source.lastMarkupBookPages);
        lastHowlGameTime = source.lastHowlGameTime;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(KEY_INITIALIZED, initialized);
        tag.putInt(KEY_SYNC_REVISION, syncRevision);
        tag.putInt(KEY_CREATURE_TYPE_ORDINAL, creatureTypeOrdinal);
        tag.putInt(KEY_HUMAN_BLOOD, humanBlood);
        tag.putInt(KEY_VAMPIRE_LEVEL, vampireLevel);
        tag.putInt(KEY_WEREWOLF_LEVEL, werewolfLevel);
        tag.putInt(KEY_BLOOD_POWER, bloodPower);
        tag.putInt(KEY_BLOOD_RESERVE, bloodReserve);
        tag.putInt(KEY_VAMPIRE_LEVEL_CAP, vampireLevelCap);
        tag.putInt(KEY_VAMPIRE_QUEST_COUNTER, vampireQuestCounter);
        ListTag villages = new ListTag();
        for (VampireVillage village : vampireQuestVillages) {
            CompoundTag record = new CompoundTag();
            record.putString("Dimension", village.dimension());
            record.putLong("Location", village.location());
            villages.add(record);
        }
        tag.put(KEY_VAMPIRE_QUEST_CHUNKS, villages);
        tag.putBoolean(KEY_VAMPIRE_VISION, vampireVision);
        tag.putInt(KEY_VAMPIRE_ULTIMATE, vampireUltimate.ordinal());
        tag.putInt(KEY_VAMPIRE_ULTIMATE_CHARGES, vampireUltimateCharges);
        tag.putBoolean(KEY_PREVIOUS_MAY_FLY, previousMayFly);
        tag.putBoolean(KEY_PREVIOUS_FLYING, previousFlying);
        tag.putInt(KEY_GROTESQUE_TICKS, grotesqueTicks);
        tag.putInt(KEY_NIGHTMARE_LEVEL, nightmareLevel);
        tag.putBoolean(KEY_GHOST, ghost);
        tag.putString(KEY_OTHER_PLAYER_SKIN, otherPlayerSkin);
        tag.putInt(KEY_PREPARED_SPELL_EFFECT_ID, preparedSpellEffectId);
        tag.putInt(KEY_PREPARED_SPELL_LEVEL, preparedSpellLevel);
        tag.putInt(KEY_LAST_ITEM_UPDATE_SLOT, lastItemUpdateSlot);
        tag.putInt(KEY_LAST_ITEM_UPDATE_DAMAGE, lastItemUpdateDamage);
        tag.putInt(KEY_LAST_ITEM_UPDATE_PAGE, lastItemUpdatePage);
        tag.putInt(KEY_LAST_MARKUP_BOOK_SLOT, lastMarkupBookSlot);

        ListTag pageList = new ListTag();
        for (String page : lastMarkupBookPages) {
            pageList.add(StringTag.valueOf(page == null ? "" : page));
        }
        tag.put(KEY_LAST_MARKUP_BOOK_PAGES, pageList);
        tag.putLong(KEY_LAST_HOWL_GAME_TIME, lastHowlGameTime);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        initialized = tag.getBoolean(KEY_INITIALIZED);
        syncRevision = tag.getInt(KEY_SYNC_REVISION);
        CompoundTag bloodData = tag.contains("WitcheryExtendedPlayer", Tag.TAG_COMPOUND)
                ? tag.getCompound("WitcheryExtendedPlayer") : tag;
        setCreatureTypeOrdinal(tag.contains(KEY_CREATURE_TYPE_ORDINAL, Tag.TAG_ANY_NUMERIC)
                ? tag.getInt(KEY_CREATURE_TYPE_ORDINAL) : bloodData.getInt("CreatureType"));
        setHumanBlood(bloodData.getInt(KEY_HUMAN_BLOOD));
        setVampireLevel(bloodData.getInt(KEY_VAMPIRE_LEVEL));
        setWerewolfLevel(bloodData.getInt(KEY_WEREWOLF_LEVEL));
        setBloodPower(bloodData.getInt(KEY_BLOOD_POWER));
        setBloodReserve(bloodData.getInt(KEY_BLOOD_RESERVE));
        vampireLevelCap = Mth.clamp(bloodData.getInt(KEY_VAMPIRE_LEVEL_CAP), 0, 10);
        setVampireQuestCounter(bloodData.getInt(KEY_VAMPIRE_QUEST_COUNTER));
        vampireQuestVillages.clear();
        for (Tag entry : bloodData.getList(KEY_VAMPIRE_QUEST_CHUNKS, Tag.TAG_COMPOUND)) {
            CompoundTag record = (CompoundTag) entry;
            if (!record.contains("Location", Tag.TAG_LONG)) continue;
            String dimension = record.contains("Dimension", Tag.TAG_STRING) ? record.getString("Dimension") : "minecraft:overworld";
            vampireQuestVillages.add(new VampireVillage(dimension, record.getLong("Location")));
        }
        vampireVision = bloodData.getBoolean(KEY_VAMPIRE_VISION);
        setVampireUltimate(VampireUltimate.fromId(bloodData.getInt(KEY_VAMPIRE_ULTIMATE)),
                bloodData.getInt(KEY_VAMPIRE_ULTIMATE_CHARGES));
        previousMayFly = tag.getBoolean(KEY_PREVIOUS_MAY_FLY);
        previousFlying = tag.getBoolean(KEY_PREVIOUS_FLYING);
        selectedVampirePower = bloodData.getInt("SelectedVampirePower");
        grotesqueTicks = tag.getInt(KEY_GROTESQUE_TICKS);
        nightmareLevel = tag.getInt(KEY_NIGHTMARE_LEVEL);
        ghost = tag.getBoolean(KEY_GHOST);
        otherPlayerSkin = tag.getString(KEY_OTHER_PLAYER_SKIN);
        preparedSpellEffectId = tag.getInt(KEY_PREPARED_SPELL_EFFECT_ID);
        preparedSpellLevel = tag.getInt(KEY_PREPARED_SPELL_LEVEL);
        lastItemUpdateSlot = tag.getInt(KEY_LAST_ITEM_UPDATE_SLOT);
        lastItemUpdateDamage = tag.getInt(KEY_LAST_ITEM_UPDATE_DAMAGE);
        lastItemUpdatePage = tag.getInt(KEY_LAST_ITEM_UPDATE_PAGE);
        lastMarkupBookSlot = tag.getInt(KEY_LAST_MARKUP_BOOK_SLOT);
        lastMarkupBookPages.clear();

        ListTag pageList = tag.getList(KEY_LAST_MARKUP_BOOK_PAGES, Tag.TAG_STRING);
        for (int i = 0; i < pageList.size(); i++) {
            lastMarkupBookPages.add(pageList.getString(i));
        }

        lastHowlGameTime = tag.getLong(KEY_LAST_HOWL_GAME_TIME);
    }

    private record VampireVillage(String dimension, long location) {}
}
