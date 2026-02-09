package art.arcane.witchery.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public final class WitcheryPlayerData {
    private static final String KEY_INITIALIZED = "Initialized";
    private static final String KEY_SYNC_REVISION = "SyncRevision";
    private static final String KEY_CREATURE_TYPE_ORDINAL = "CreatureTypeOrdinal";
    private static final String KEY_HUMAN_BLOOD = "HumanBlood";
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

    public int getHumanBlood() {
        return humanBlood;
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
        creatureTypeOrdinal = tag.getInt(KEY_CREATURE_TYPE_ORDINAL);
        humanBlood = tag.getInt(KEY_HUMAN_BLOOD);
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
}
