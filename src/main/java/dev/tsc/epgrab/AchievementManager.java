package dev.tsc.epgrab;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class AchievementManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path SAVE_PATH = FabricLoader.getInstance().getConfigDir().resolve("epgrab-achievements.json");

    private static final List<AchievementDefinition> ACHIEVEMENTS = List.of(
        new AchievementDefinition("pearls_grabbed_1000", Category.PEARLS_GRABBED, 1_000, IconKind.PEARL, "Pearls Snatched!", "Grab 1,000 ender pearls."),
        new AchievementDefinition("pearls_grabbed_5000", Category.PEARLS_GRABBED, 5_000, IconKind.PEARL, "Sack Sifter", "Grab 5,000 ender pearls."),
        new AchievementDefinition("pearls_grabbed_10000", Category.PEARLS_GRABBED, 10_000, IconKind.PEARL, "Pocket Rummager", "Grab 10,000 ender pearls."),
        new AchievementDefinition("pearls_grabbed_25000", Category.PEARLS_GRABBED, 25_000, IconKind.PEARL, "Pearl Plunderer", "Grab 25,000 ender pearls."),
        new AchievementDefinition("pearls_grabbed_100000", Category.PEARLS_GRABBED, 100_000, IconKind.PEARL, "Bottomless Pockets", "Grab 100,000 ender pearls."),

        new AchievementDefinition("pearls_thrown_1", Category.PEARLS_THROWN, 1, IconKind.PEARL, "Junior's First Pitch", "Throw your first ender pearl."),
        new AchievementDefinition("pearls_thrown_1000", Category.PEARLS_THROWN, 1_000, IconKind.PEARL, "Pearl Pitcher", "Throw 1,000 ender pearls."),
        new AchievementDefinition("pearls_thrown_5000", Category.PEARLS_THROWN, 5_000, IconKind.PEARL, "Arc Specialist", "Throw 5,000 ender pearls."),
        new AchievementDefinition("pearls_thrown_10000", Category.PEARLS_THROWN, 10_000, IconKind.PEARL, "Void Fastball", "Throw 10,000 ender pearls."),
        new AchievementDefinition("pearls_thrown_25000", Category.PEARLS_THROWN, 25_000, IconKind.PEARL, "Warp Lobber", "Throw 25,000 ender pearls."),
        new AchievementDefinition("pearls_thrown_100000", Category.PEARLS_THROWN, 100_000, IconKind.PEARL, "Pearl Machine", "Throw 100,000 ender pearls."),

        new AchievementDefinition("leaps_grabbed_1000", Category.LEAPS_GRABBED, 1_000, IconKind.PEARL, "Leaps Snatched!", "Grab 1,000 spirit leaps."),
        new AchievementDefinition("leaps_grabbed_5000", Category.LEAPS_GRABBED, 5_000, IconKind.PEARL, "Leap Hoarder", "Grab 5,000 spirit leaps."),
        new AchievementDefinition("leaps_grabbed_10000", Category.LEAPS_GRABBED, 10_000, IconKind.PEARL, "Ghost Shopper", "Grab 10,000 spirit leaps."),
        new AchievementDefinition("leaps_grabbed_25000", Category.LEAPS_GRABBED, 25_000, IconKind.PEARL, "Sack Raider", "Grab 25,000 spirit leaps."),
        new AchievementDefinition("leaps_grabbed_100000", Category.LEAPS_GRABBED, 100_000, IconKind.PEARL, "Leap Bank", "Grab 100,000 spirit leaps."),

        new AchievementDefinition("leaps_crushed_1", Category.LEAPS_CRUSHED, 1, IconKind.PEARL, "Baby's First Leap", "Use your first spirit leap."),
        new AchievementDefinition("leaps_crushed_1000", Category.LEAPS_CRUSHED, 1_000, IconKind.PEARL, "Leap Crusher", "Crush 1,000 spirit leaps."),
        new AchievementDefinition("leaps_crushed_5000", Category.LEAPS_CRUSHED, 5_000, IconKind.PEARL, "Blink Addict", "Crush 5,000 spirit leaps."),
        new AchievementDefinition("leaps_crushed_10000", Category.LEAPS_CRUSHED, 10_000, IconKind.PEARL, "Rift Skipper", "Crush 10,000 spirit leaps."),
        new AchievementDefinition("leaps_crushed_25000", Category.LEAPS_CRUSHED, 25_000, IconKind.PEARL, "Ghost Hopper", "Crush 25,000 spirit leaps."),
        new AchievementDefinition("leaps_crushed_100000", Category.LEAPS_CRUSHED, 100_000, IconKind.PEARL, "Leap Goblin", "Crush 100,000 spirit leaps."),

        new AchievementDefinition("decoys_grabbed_1000", Category.DECOYS_GRABBED, 1_000, IconKind.DECOY, "Decoys Snatched!", "Grab 1,000 decoys."),
        new AchievementDefinition("decoys_grabbed_5000", Category.DECOYS_GRABBED, 5_000, IconKind.DECOY, "Dummy Dealer", "Grab 5,000 decoys."),
        new AchievementDefinition("decoys_grabbed_10000", Category.DECOYS_GRABBED, 10_000, IconKind.DECOY, "Bait Barterer", "Grab 10,000 decoys."),
        new AchievementDefinition("decoys_grabbed_25000", Category.DECOYS_GRABBED, 25_000, IconKind.DECOY, "Fake Friend", "Grab 25,000 decoys."),
        new AchievementDefinition("decoys_grabbed_100000", Category.DECOYS_GRABBED, 100_000, IconKind.DECOY, "Decoy Depot", "Grab 100,000 decoys."),

        new AchievementDefinition("decoys_used_1", Category.DECOYS_USED, 1, IconKind.DECOY, "Baby's First Bait", "Use your first decoy."),
        new AchievementDefinition("decoys_used_1000", Category.DECOYS_USED, 1_000, IconKind.DECOY, "Bait Launcher", "Use 1,000 decoys."),
        new AchievementDefinition("decoys_used_5000", Category.DECOYS_USED, 5_000, IconKind.DECOY, "Red Herring", "Use 5,000 decoys."),
        new AchievementDefinition("decoys_used_10000", Category.DECOYS_USED, 10_000, IconKind.DECOY, "Mob Misdirector", "Use 10,000 decoys."),
        new AchievementDefinition("decoys_used_25000", Category.DECOYS_USED, 25_000, IconKind.DECOY, "Panic Button", "Use 25,000 decoys."),
        new AchievementDefinition("decoys_used_100000", Category.DECOYS_USED, 100_000, IconKind.DECOY, "Decoy Delinquent", "Use 100,000 decoys."),

        new AchievementDefinition("superboom_grabbed_1000", Category.SUPERBOOMS_GRABBED, 1_000, IconKind.SUPERBOOM, "Booms Snatched!", "Grab 1,000 Superboom TNT."),
        new AchievementDefinition("superboom_grabbed_5000", Category.SUPERBOOMS_GRABBED, 5_000, IconKind.SUPERBOOM, "Fuse Collector", "Grab 5,000 Superboom TNT."),
        new AchievementDefinition("superboom_grabbed_10000", Category.SUPERBOOMS_GRABBED, 10_000, IconKind.SUPERBOOM, "Blast Banker", "Grab 10,000 Superboom TNT."),
        new AchievementDefinition("superboom_grabbed_25000", Category.SUPERBOOMS_GRABBED, 25_000, IconKind.SUPERBOOM, "Powder Keg", "Grab 25,000 Superboom TNT."),
        new AchievementDefinition("superboom_grabbed_100000", Category.SUPERBOOMS_GRABBED, 100_000, IconKind.SUPERBOOM, "TNT Trust Fund", "Grab 100,000 Superboom TNT."),

        new AchievementDefinition("superboom_used_1", Category.SUPERBOOMS_USED, 1, IconKind.SUPERBOOM, "Junior Demolitionist", "Use your first Superboom TNT."),
        new AchievementDefinition("superboom_used_1000", Category.SUPERBOOMS_USED, 1_000, IconKind.SUPERBOOM, "Wall Remover", "Use 1,000 Superboom TNT."),
        new AchievementDefinition("superboom_used_5000", Category.SUPERBOOMS_USED, 5_000, IconKind.SUPERBOOM, "Spleef Technician", "Use 5,000 Superboom TNT."),
        new AchievementDefinition("superboom_used_10000", Category.SUPERBOOMS_USED, 10_000, IconKind.SUPERBOOM, "Boom Broker", "Use 10,000 Superboom TNT."),
        new AchievementDefinition("superboom_used_25000", Category.SUPERBOOMS_USED, 25_000, IconKind.SUPERBOOM, "Door Buster", "Use 25,000 Superboom TNT."),
        new AchievementDefinition("superboom_used_100000", Category.SUPERBOOMS_USED, 100_000, IconKind.SUPERBOOM, "Catacomb Contractor", "Use 100,000 Superboom TNT."),

        new AchievementDefinition("kismets_used_1", Category.KISMETS_USED, 1, IconKind.KISMET, "First Feather", "Use your first Kismet Feather."),
        new AchievementDefinition("kismets_used_50", Category.KISMETS_USED, 50, IconKind.KISMET, "Reroll Regular", "Use 50 Kismet Feathers."),
        new AchievementDefinition("kismets_used_100", Category.KISMETS_USED, 100, IconKind.KISMET, "Chest Chiropractor", "Use 100 Kismet Feathers."),
        new AchievementDefinition("kismets_used_250", Category.KISMETS_USED, 250, IconKind.KISMET, "Croesus Customer", "Use 250 Kismet Feathers."),
        new AchievementDefinition("kismets_used_500", Category.KISMETS_USED, 500, IconKind.KISMET, "Feathercycler", "Use 500 Kismet Feathers."),
        new AchievementDefinition("kismets_used_1000", Category.KISMETS_USED, 1_000, IconKind.KISMET, "Destiny Gambler", "Use 1,000 Kismet Feathers."),

        new AchievementDefinition("met_asyncfn", Category.SOCIAL, 0, IconKind.ASYNCFN_HEAD, "Interdimensionally Bitflipped", "Share a lobby with asyncfn."),
        new AchievementDefinition("met_milodamonke", Category.SOCIAL, 0, IconKind.MILO_HEAD, "idk i just work here", "Share a lobby with MiloDaMonke.")
    );

    private static SaveData data = new SaveData();
    private static boolean initialized;
    private static int lastUseTick = Integer.MIN_VALUE;
    private static Category lastUseCategory;

    private AchievementManager() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;
        load();
        reconcileMilestones();
        save();
    }

    public static void recordPearlsGrabbed(int amount) {
        recordAmount(Category.PEARLS_GRABBED, amount);
    }

    public static void recordLeapsGrabbed(int amount) {
        recordAmount(Category.LEAPS_GRABBED, amount);
    }

    public static void recordDecoysGrabbed(int amount) {
        recordAmount(Category.DECOYS_GRABBED, amount);
    }

    public static void recordSuperboomsGrabbed(int amount) {
        recordAmount(Category.SUPERBOOMS_GRABBED, amount);
    }

    public static void recordPearlThrow() {
        recordUse(Category.PEARLS_THROWN);
    }

    public static void recordSpiritLeapUse() {
        recordUse(Category.LEAPS_CRUSHED);
    }

    public static void recordDecoyUse() {
        recordUse(Category.DECOYS_USED);
    }

    public static void recordSuperboomUse() {
        recordUse(Category.SUPERBOOMS_USED);
    }

    public static void recordKismetUse() {
        recordUse(Category.KISMETS_USED, false);
    }

    public static void recordSkyblockItemUse(String itemId) {
        switch (itemId) {
            case "ENDER_PEARL" -> recordPearlThrow();
            case "DECOY" -> recordDecoyUse();
            case "SUPERBOOM_TNT" -> recordSuperboomUse();
            default -> {
            }
        }
    }

    public static void scanForCreators(MinecraftClient client) {
        initialize();

        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        if (networkHandler == null) {
            return;
        }

        boolean changed = false;
        for (PlayerListEntry entry : networkHandler.getPlayerList()) {
            if (CreatorBadge.PRIMARY_CREATOR_UUID.equals(entry.getProfile().id()) && !isUnlocked("met_asyncfn")) {
                unlock(getAchievement("met_asyncfn"));
                changed = true;
            }

            if (CreatorBadge.SECONDARY_CREATOR_UUID.equals(entry.getProfile().id()) && !isUnlocked("met_milodamonke")) {
                unlock(getAchievement("met_milodamonke"));
                changed = true;
            }
        }

        if (changed) {
            save();
        }
    }

    public static long pearlsGrabbed() {
        initialize();
        return data.pearlsGrabbed;
    }

    public static long pearlsThrown() {
        initialize();
        return data.pearlsThrown;
    }

    public static long spiritLeapsGrabbed() {
        initialize();
        return data.spiritLeapsGrabbed;
    }

    public static long spiritLeapsCrushed() {
        initialize();
        return data.spiritLeapsUsed;
    }

    public static long decoysGrabbed() {
        initialize();
        return data.decoysGrabbed;
    }

    public static long decoysUsed() {
        initialize();
        return data.decoysUsed;
    }

    public static long superboomsGrabbed() {
        initialize();
        return data.superboomsGrabbed;
    }

    public static long superboomsUsed() {
        initialize();
        return data.superboomsUsed;
    }

    public static long kismetsUsed() {
        initialize();
        return data.kismetsUsed;
    }

    public static long progressFor(Category category) {
        return switch (category) {
            case PEARLS_GRABBED -> pearlsGrabbed();
            case PEARLS_THROWN -> pearlsThrown();
            case LEAPS_GRABBED -> spiritLeapsGrabbed();
            case LEAPS_CRUSHED -> spiritLeapsCrushed();
            case DECOYS_GRABBED -> decoysGrabbed();
            case DECOYS_USED -> decoysUsed();
            case SUPERBOOMS_GRABBED -> superboomsGrabbed();
            case SUPERBOOMS_USED -> superboomsUsed();
            case KISMETS_USED -> kismetsUsed();
            case SOCIAL -> 0;
        };
    }

    public static int unlockedCount() {
        initialize();
        return data.unlocked.size();
    }

    public static int totalCount() {
        return ACHIEVEMENTS.size();
    }

    public static boolean isUnlocked(String id) {
        initialize();
        return data.unlocked.contains(id);
    }

    public static List<AchievementDefinition> achievements() {
        return ACHIEVEMENTS;
    }

    public static ItemStack createDisplayStack(AchievementDefinition achievement) {
        return switch (achievement.iconKind()) {
            case PEARL -> new ItemStack(Items.ENDER_PEARL);
            case DECOY -> new ItemStack(Items.SNOWBALL);
            case SUPERBOOM -> new ItemStack(Items.TNT);
            case KISMET -> new ItemStack(Items.FEATHER);
            case ASYNCFN_HEAD -> createCreatorHead(CreatorBadge.PRIMARY_CREATOR_UUID);
            case MILO_HEAD -> createCreatorHead(CreatorBadge.SECONDARY_CREATOR_UUID);
        };
    }

    private static ItemStack createCreatorHead(java.util.UUID uuid) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        stack.set(DataComponentTypes.PROFILE, ProfileComponent.ofDynamic(uuid));
        return stack;
    }

    private static void reconcileMilestones() {
        unlockMilestones(Category.PEARLS_GRABBED, data.pearlsGrabbed);
        unlockMilestones(Category.PEARLS_THROWN, data.pearlsThrown);
        unlockMilestones(Category.LEAPS_GRABBED, data.spiritLeapsGrabbed);
        unlockMilestones(Category.LEAPS_CRUSHED, data.spiritLeapsUsed);
        unlockMilestones(Category.DECOYS_GRABBED, data.decoysGrabbed);
        unlockMilestones(Category.DECOYS_USED, data.decoysUsed);
        unlockMilestones(Category.SUPERBOOMS_GRABBED, data.superboomsGrabbed);
        unlockMilestones(Category.SUPERBOOMS_USED, data.superboomsUsed);
        unlockMilestones(Category.KISMETS_USED, data.kismetsUsed);
    }

    private static void recordAmount(Category category, int amount) {
        initialize();
        if (!DungeonContext.isTrackedContext(MinecraftClient.getInstance())) {
            return;
        }

        long increment = Math.max(0, amount);
        if (increment == 0) {
            return;
        }

        switch (category) {
            case PEARLS_GRABBED -> data.pearlsGrabbed += increment;
            case LEAPS_GRABBED -> data.spiritLeapsGrabbed += increment;
            case DECOYS_GRABBED -> data.decoysGrabbed += increment;
            case SUPERBOOMS_GRABBED -> data.superboomsGrabbed += increment;
            default -> {
                return;
            }
        }

        unlockMilestones(category, progressFor(category));
        save();
    }

    private static void recordUse(Category category) {
        recordUse(category, true);
    }

    private static void recordUse(Category category, boolean requireTrackedContext) {
        initialize();
        MinecraftClient client = MinecraftClient.getInstance();
        if (requireTrackedContext && !DungeonContext.isTrackedContext(client)) {
            return;
        }

        if (client != null && client.player != null) {
            int playerAge = client.player.age;
            if (lastUseTick == playerAge && lastUseCategory == category) {
                return;
            }
            lastUseTick = playerAge;
            lastUseCategory = category;
        }

        switch (category) {
            case PEARLS_THROWN -> data.pearlsThrown++;
            case LEAPS_CRUSHED -> data.spiritLeapsUsed++;
            case DECOYS_USED -> data.decoysUsed++;
            case SUPERBOOMS_USED -> data.superboomsUsed++;
            case KISMETS_USED -> data.kismetsUsed++;
            default -> {
                return;
            }
        }

        unlockMilestones(category, progressFor(category));
        save();
    }

    private static void unlockMilestones(Category category, long value) {
        for (AchievementDefinition achievement : ACHIEVEMENTS) {
            if (achievement.category() == category && value >= achievement.threshold()) {
                unlock(achievement);
            }
        }
    }

    private static void unlock(AchievementDefinition achievement) {
        if (achievement == null || !data.unlocked.add(achievement.id())) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        client.getToastManager().add(new AchievementToast(achievement));

        Text title = Text.empty()
            .append(Text.literal("xx ").formatted(Formatting.WHITE, Formatting.OBFUSCATED))
            .append(Text.literal("Achievement Unlocked!").formatted(Formatting.WHITE, Formatting.BOLD))
            .append(Text.literal(" xx").formatted(Formatting.WHITE, Formatting.OBFUSCATED));
        Text subtitle = Text.literal(achievement.title()).styled(style -> style.withColor(0x5FD0D0).withBold(true));

        client.inGameHud.setTitleTicks(10, 50, 15);
        client.inGameHud.setTitle(title);
        client.inGameHud.setSubtitle(subtitle);

        if (client.player != null) {
            client.player.sendMessage(
                Text.empty()
                    .append(Text.literal("[epgrab] ").formatted(Formatting.DARK_AQUA))
                    .append(title.copy())
                    .append(Text.literal(" "))
                    .append(subtitle.copy()),
                false
            );
        }
    }

    private static AchievementDefinition getAchievement(String id) {
        for (AchievementDefinition achievement : ACHIEVEMENTS) {
            if (achievement.id().equals(id)) {
                return achievement;
            }
        }

        return null;
    }

    private static void load() {
        if (!Files.exists(SAVE_PATH)) {
            data = new SaveData();
            return;
        }

        try {
            String json = Files.readString(SAVE_PATH, StandardCharsets.UTF_8);
            SaveData loaded = GSON.fromJson(json, SaveData.class);
            data = loaded != null ? loaded.sanitized() : new SaveData();
        } catch (Exception exception) {
            LOGGER.error("Failed to load epgrab achievements from {}", SAVE_PATH, exception);
            data = new SaveData();
        }
    }

    private static void save() {
        try {
            Files.createDirectories(SAVE_PATH.getParent());
            Path tempPath = SAVE_PATH.resolveSibling(SAVE_PATH.getFileName() + ".tmp");
            Files.writeString(tempPath, GSON.toJson(data.sanitized()), StandardCharsets.UTF_8);

            try {
                Files.move(tempPath, SAVE_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(tempPath, SAVE_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            LOGGER.error("Failed to save epgrab achievements to {}", SAVE_PATH, exception);
        }
    }

    public record AchievementDefinition(String id, Category category, long threshold, IconKind iconKind, String title, String description) {
    }

    public enum Category {
        PEARLS_GRABBED,
        PEARLS_THROWN,
        LEAPS_GRABBED,
        LEAPS_CRUSHED,
        DECOYS_GRABBED,
        DECOYS_USED,
        SUPERBOOMS_GRABBED,
        SUPERBOOMS_USED,
        KISMETS_USED,
        SOCIAL
    }

    public enum IconKind {
        PEARL,
        DECOY,
        SUPERBOOM,
        KISMET,
        ASYNCFN_HEAD,
        MILO_HEAD
    }

    private static final class SaveData {
        private long pearlsGrabbed;
        private long pearlsThrown;
        private long spiritLeapsGrabbed;
        private long spiritLeapsUsed;
        private long decoysGrabbed;
        private long decoysUsed;
        private long superboomsGrabbed;
        private long superboomsUsed;
        private long kismetsUsed;
        private Set<String> unlocked = new LinkedHashSet<>();

        private SaveData sanitized() {
            if (unlocked == null) {
                unlocked = new LinkedHashSet<>();
            } else if (!(unlocked instanceof LinkedHashSet<?>)) {
                unlocked = new LinkedHashSet<>(unlocked);
            }

            if (pearlsGrabbed < 0) pearlsGrabbed = 0;
            if (pearlsThrown < 0) pearlsThrown = 0;
            if (spiritLeapsGrabbed < 0) spiritLeapsGrabbed = 0;
            if (spiritLeapsUsed < 0) spiritLeapsUsed = 0;
            if (decoysGrabbed < 0) decoysGrabbed = 0;
            if (decoysUsed < 0) decoysUsed = 0;
            if (superboomsGrabbed < 0) superboomsGrabbed = 0;
            if (superboomsUsed < 0) superboomsUsed = 0;
            if (kismetsUsed < 0) kismetsUsed = 0;

            return this;
        }
    }
}
