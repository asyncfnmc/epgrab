package dev.tsc.epgrab;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class EpGrabClient implements ClientModInitializer {
    private static final GrabItem ENDER_PEARLS = new GrabItem("ep", "ENDER_PEARL", "ender_pearl", "pearls", "Ender Pearl", 16);
    private static final GrabItem SPIRIT_LEAPS = new GrabItem("sl", "SPIRIT_LEAP", "spirit_leap", "spirit leaps", "Spirit Leap", 16);
    private static final GrabItem DECOYS = new GrabItem("de", "DECOY", "decoy", "decoys", "Decoy", 64);
    private static final GrabItem SUPERBOOMS = new GrabItem("sb", "SUPERBOOM_TNT", "superboom_tnt", "superboom tnt", "Superboom TNT", 64);

    private static final int LOW_STOCK_THRESHOLD = 3;
    private static final int CREATOR_SCAN_INTERVAL_TICKS = 20;
    private static final String KISMET_USE_MESSAGE = "You used a Kismet Feather!";
    private static final List<String> SPIRIT_LEAP_MESSAGE_PREFIXES = List.of(
        "You have teleported to ",
        "You have leaped to ",
        "You leaped to "
    );

    private static final SuccessMessage[] SUCCESS_MESSAGES = {
        new SuccessMessage("Snatched!", Formatting.LIGHT_PURPLE, Formatting.AQUA),
        new SuccessMessage("Grabbed!", Formatting.GREEN, Formatting.YELLOW),
        new SuccessMessage("Fumbled!", Formatting.RED, Formatting.GOLD),
        new SuccessMessage("Scooped!", Formatting.AQUA, Formatting.BLUE),
        new SuccessMessage("Yoinked!", Formatting.GOLD, Formatting.LIGHT_PURPLE),
        new SuccessMessage("Pocketed!", Formatting.DARK_AQUA, Formatting.GREEN)
    };
    private static final StatusTemplate[] ALREADY_MESSAGES = {
        new StatusTemplate("Already topped off on %s.", Formatting.GRAY),
        new StatusTemplate("%s secured.", Formatting.AQUA),
        new StatusTemplate("Already stocked on %s.", Formatting.GREEN),
        new StatusTemplate("No %s needed.", Formatting.YELLOW),
        new StatusTemplate("%s already at target.", Formatting.LIGHT_PURPLE),
        new StatusTemplate("All set on %s.", Formatting.GOLD)
    };

    static final KeyBinding EP_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.epgrab.grab_pearls",
        GLFW.GLFW_KEY_UNKNOWN,
        KeyBinding.Category.MISC
    ));
    static final KeyBinding SL_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.epgrab.grab_spirit_leaps",
        GLFW.GLFW_KEY_UNKNOWN,
        KeyBinding.Category.MISC
    ));
    static final KeyBinding DE_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.epgrab.grab_decoys",
        GLFW.GLFW_KEY_UNKNOWN,
        KeyBinding.Category.MISC
    ));
    static final KeyBinding SB_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.epgrab.grab_superbooms",
        GLFW.GLFW_KEY_UNKNOWN,
        KeyBinding.Category.MISC
    ));

    private static final ArrayDeque<PendingSackMessage> PENDING_SACK_MESSAGES = new ArrayDeque<>();
    private static final LowStockTracker PEARL_LOW_STOCK_TRACKER = new LowStockTracker(ENDER_PEARLS, Formatting.LIGHT_PURPLE);
    private static final LowStockTracker LEAP_LOW_STOCK_TRACKER = new LowStockTracker(SPIRIT_LEAPS, Formatting.AQUA);
    private static final LowStockTracker DECOY_LOW_STOCK_TRACKER = new LowStockTracker(DECOYS, Formatting.GOLD);
    private static final LowStockTracker SUPERBOOM_LOW_STOCK_TRACKER = new LowStockTracker(SUPERBOOMS, Formatting.RED);
    private static int creatorScanCooldown = 0;

    @Override
    public void onInitializeClient() {
        AchievementManager.initialize();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                literal(ENDER_PEARLS.command())
                    .executes(context -> executeGrab(ENDER_PEARLS, ENDER_PEARLS.defaultTargetAmount()))
                    .then(argument("amount", IntegerArgumentType.integer(0))
                        .executes(context -> executeGrab(ENDER_PEARLS, IntegerArgumentType.getInteger(context, "amount")))
                    )
                    .then(literal("settings").executes(context -> openSettings()))
                    .then(literal("achievements").executes(context -> openAchievements()))
                    .then(literal("help").executes(context -> showEpHelp()))
                    .then(literal("pearls")
                        .executes(context -> executeGrab(ENDER_PEARLS, ENDER_PEARLS.defaultTargetAmount()))
                        .then(argument("amount", IntegerArgumentType.integer(0))
                            .executes(context -> executeGrab(ENDER_PEARLS, IntegerArgumentType.getInteger(context, "amount")))
                        )
                    )
                    .then(literal("leaps")
                        .executes(context -> executeGrab(SPIRIT_LEAPS, SPIRIT_LEAPS.defaultTargetAmount()))
                        .then(argument("amount", IntegerArgumentType.integer(0))
                            .executes(context -> executeGrab(SPIRIT_LEAPS, IntegerArgumentType.getInteger(context, "amount")))
                        )
                    )
                    .then(literal("decoys")
                        .executes(context -> executeGrab(DECOYS, DECOYS.defaultTargetAmount()))
                        .then(argument("amount", IntegerArgumentType.integer(0))
                            .executes(context -> executeGrab(DECOYS, IntegerArgumentType.getInteger(context, "amount")))
                        )
                    )
                    .then(literal("superboom")
                        .executes(context -> executeGrab(SUPERBOOMS, SUPERBOOMS.defaultTargetAmount()))
                        .then(argument("amount", IntegerArgumentType.integer(0))
                            .executes(context -> executeGrab(SUPERBOOMS, IntegerArgumentType.getInteger(context, "amount")))
                        )
                    )
                    .then(literal("superbooms")
                        .executes(context -> executeGrab(SUPERBOOMS, SUPERBOOMS.defaultTargetAmount()))
                        .then(argument("amount", IntegerArgumentType.integer(0))
                            .executes(context -> executeGrab(SUPERBOOMS, IntegerArgumentType.getInteger(context, "amount")))
                        )
                    )
            );

            dispatcher.register(
                literal(SPIRIT_LEAPS.command())
                    .executes(context -> executeGrab(SPIRIT_LEAPS, SPIRIT_LEAPS.defaultTargetAmount()))
                    .then(argument("amount", IntegerArgumentType.integer(0))
                        .executes(context -> executeGrab(SPIRIT_LEAPS, IntegerArgumentType.getInteger(context, "amount")))
                    )
            );

            dispatcher.register(
                literal(DECOYS.command())
                    .executes(context -> executeGrab(DECOYS, DECOYS.defaultTargetAmount()))
                    .then(argument("amount", IntegerArgumentType.integer(0))
                        .executes(context -> executeGrab(DECOYS, IntegerArgumentType.getInteger(context, "amount")))
                    )
            );

            dispatcher.register(
                literal(SUPERBOOMS.command())
                    .executes(context -> executeGrab(SUPERBOOMS, SUPERBOOMS.defaultTargetAmount()))
                    .then(argument("amount", IntegerArgumentType.integer(0))
                        .executes(context -> executeGrab(SUPERBOOMS, IntegerArgumentType.getInteger(context, "amount")))
                    )
            );
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (EP_KEYBINDING.wasPressed()) {
                executeGrab(ENDER_PEARLS, ENDER_PEARLS.defaultTargetAmount());
            }

            while (SL_KEYBINDING.wasPressed()) {
                executeGrab(SPIRIT_LEAPS, SPIRIT_LEAPS.defaultTargetAmount());
            }

            while (DE_KEYBINDING.wasPressed()) {
                executeGrab(DECOYS, DECOYS.defaultTargetAmount());
            }

            while (SB_KEYBINDING.wasPressed()) {
                executeGrab(SUPERBOOMS, SUPERBOOMS.defaultTargetAmount());
            }

            tickLowStockNotifications(client);
            tickCreatorAchievement(client);
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> handleIncomingMessage(message));
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> handleIncomingMessage(message));
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> !shouldSuppressSackMessage(message));
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> !shouldSuppressSackMessage(message));
    }

    private static void handleIncomingMessage(Text message) {
        if (isSpiritLeapMessage(message)) {
            AchievementManager.recordSpiritLeapUse();
        }

        if (isKismetUseMessage(message)) {
            AchievementManager.recordKismetUse();
        }
    }

    private static boolean isSpiritLeapMessage(Text message) {
        String text = message.getString().trim();

        for (String prefix : SPIRIT_LEAP_MESSAGE_PREFIXES) {
            if (text.startsWith(prefix) && text.endsWith("!")) {
                return true;
            }
        }

        return false;
    }

    private static boolean isKismetUseMessage(Text message) {
        return KISMET_USE_MESSAGE.equals(message.getString().trim());
    }

    private static void tickCreatorAchievement(MinecraftClient client) {
        if (client.player == null || client.getNetworkHandler() == null) {
            creatorScanCooldown = 0;
            return;
        }

        if (creatorScanCooldown > 0) {
            creatorScanCooldown--;
            return;
        }

        creatorScanCooldown = CREATOR_SCAN_INTERVAL_TICKS;
        AchievementManager.scanForCreators(client);
    }

    private static int openSettings() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> client.setScreen(new EpGrabConfigScreen(client.currentScreen)));
        return 1;
    }

    private static int openAchievements() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> client.setScreen(new EpGrabConfigScreen(client.currentScreen, true)));
        return 1;
    }

    private static int showEpHelp() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null) {
            return 0;
        }

        player.sendMessage(Text.literal("epgrab commands:").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("/ep [amount] §7- grab ender pearls").formatted(Formatting.GRAY), false);
        player.sendMessage(Text.literal("/ep pearls [amount] §7- grab ender pearls").formatted(Formatting.GRAY), false);
        player.sendMessage(Text.literal("/ep leaps [amount] §7- grab spirit leaps").formatted(Formatting.GRAY), false);
        player.sendMessage(Text.literal("/ep decoys [amount] §7- grab decoys").formatted(Formatting.GRAY), false);
        player.sendMessage(Text.literal("/ep superboom [amount] §7- grab superboom tnt").formatted(Formatting.GRAY), false);
        player.sendMessage(Text.literal("/ep settings §7- open settings").formatted(Formatting.GRAY), false);
        player.sendMessage(Text.literal("/ep achievements §7- open achievements").formatted(Formatting.GRAY), false);
        player.sendMessage(Text.literal("/ep help §7- show this list").formatted(Formatting.GRAY), false);
        player.sendMessage(Text.literal("aliases still work: /sl, /de, /sb").formatted(Formatting.DARK_GRAY), false);
        return 1;
    }

    private static int executeGrab(GrabItem item, int desiredAmount) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null) {
            return 0;
        }

        int currentAmount = SkyblockItemUtils.countInventoryItem(player, item.skyblockItemId());
        int missing = desiredAmount - currentAmount;

        if (missing > 0) {
            requestFromSack(player, item, missing);
            player.sendMessage(nextSuccessMessage(missing), false);
        } else {
            player.sendMessage(nextAlreadyMessage(item.displayName()), false);
        }

        return 1;
    }


    private static Text nextSuccessMessage(int amount) {
        SuccessMessage message = SUCCESS_MESSAGES[ThreadLocalRandom.current().nextInt(SUCCESS_MESSAGES.length)];
        return Text.empty()
            .append(Text.literal(message.text()).formatted(message.messageColor()))
            .append(Text.literal(" [").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(String.valueOf(amount)).formatted(message.amountColor()))
            .append(Text.literal("]").formatted(Formatting.DARK_GRAY));
    }

    private static Text nextAlreadyMessage(String itemName) {
        StatusTemplate message = ALREADY_MESSAGES[ThreadLocalRandom.current().nextInt(ALREADY_MESSAGES.length)];
        return Text.literal(message.text().formatted(itemName)).formatted(message.color());
    }

    private static void tickLowStockNotifications(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || !DungeonContext.isTrackedContext(client)) {
            PEARL_LOW_STOCK_TRACKER.reset();
            LEAP_LOW_STOCK_TRACKER.reset();
            DECOY_LOW_STOCK_TRACKER.reset();
            SUPERBOOM_LOW_STOCK_TRACKER.reset();
            return;
        }

        updateLowStockNotification(client, player, PEARL_LOW_STOCK_TRACKER);
        updateLowStockNotification(client, player, LEAP_LOW_STOCK_TRACKER);
        updateLowStockNotification(client, player, DECOY_LOW_STOCK_TRACKER);
        updateLowStockNotification(client, player, SUPERBOOM_LOW_STOCK_TRACKER);
    }

    private static void updateLowStockNotification(MinecraftClient client, ClientPlayerEntity player, LowStockTracker tracker) {
        int currentAmount = SkyblockItemUtils.countInventoryItem(player, tracker.item().skyblockItemId());

        if (!tracker.initialized()) {
            tracker.initialized(true);
            tracker.lastAmount(currentAmount);
            tracker.notified(currentAmount <= LOW_STOCK_THRESHOLD);
            return;
        }

        if (currentAmount > LOW_STOCK_THRESHOLD) {
            tracker.notified(false);
        } else if (tracker.lastAmount() > LOW_STOCK_THRESHOLD && !tracker.notified()) {
            showLowStockPopup(client, tracker, currentAmount);
            tracker.notified(true);
        }

        tracker.lastAmount(currentAmount);
    }

    private static void showLowStockPopup(MinecraftClient client, LowStockTracker tracker, int currentAmount) {
        client.inGameHud.setTitleTicks(5, 35, 10);
        client.inGameHud.setTitle(Text.literal("grab " + tracker.item().displayName() + "!").formatted(tracker.titleColor()));
        client.inGameHud.setSubtitle(Text.literal(currentAmount + " left").formatted(Formatting.WHITE));
    }

    private static boolean shouldSuppressSackMessage(Text message) {
        if (PENDING_SACK_MESSAGES.isEmpty()) {
            return false;
        }

        String text = message.getString();
        Iterator<PendingSackMessage> iterator = PENDING_SACK_MESSAGES.iterator();

        while (iterator.hasNext()) {
            PendingSackMessage pending = iterator.next();
            if (pending.matches(text)) {
                pending.recordAchievement();
                iterator.remove();
                return true;
            }
        }

        return false;
    }

    private record GrabItem(String command, String skyblockItemId, String sackName, String displayName, String moveMessageName, int defaultTargetAmount) {
    }

    private static final class LowStockTracker {
        private final GrabItem item;
        private final Formatting titleColor;
        private int lastAmount = 0;
        private boolean initialized = false;
        private boolean notified = false;

        private LowStockTracker(GrabItem item, Formatting titleColor) {
            this.item = item;
            this.titleColor = titleColor;
        }

        private GrabItem item() {
            return item;
        }

        private Formatting titleColor() {
            return titleColor;
        }

        private int lastAmount() {
            return lastAmount;
        }

        private void lastAmount(int lastAmount) {
            this.lastAmount = lastAmount;
        }

        private boolean initialized() {
            return initialized;
        }

        private void initialized(boolean initialized) {
            this.initialized = initialized;
        }

        private boolean notified() {
            return notified;
        }

        private void notified(boolean notified) {
            this.notified = notified;
        }

        private void reset() {
            lastAmount = 0;
            initialized = false;
            notified = false;
        }
    }

    private record SuccessMessage(String text, Formatting messageColor, Formatting amountColor) {
    }

    private record StatusTemplate(String text, Formatting color) {
    }

    private static void requestFromSack(ClientPlayerEntity player, GrabItem item, int amount) {
        PENDING_SACK_MESSAGES.addLast(new PendingSackMessage(item, amount));
        player.networkHandler.sendChatCommand("gfs " + item.sackName() + " " + amount);
    }

    static InputUtil.Key getBoundKey(KeyBinding keyBinding) {
        return InputUtil.fromTranslationKey(keyBinding.getBoundKeyTranslationKey());
    }

    static void setBoundKey(KeyBinding keyBinding, InputUtil.Key key) {
        keyBinding.setBoundKey(key);
    }

    static void saveKeyBindings() {
        MinecraftClient client = MinecraftClient.getInstance();
        KeyBinding.updateKeysByCode();
        if (client != null) {
            client.options.write();
        }
    }

    private record PendingSackMessage(GrabItem item, int amount) {
        private boolean matches(String message) {
            return message.startsWith("Moved " + amount + " ")
                && message.contains(item.moveMessageName())
                && message.contains(" from your Sacks to your inventory.");
        }

        private void recordAchievement() {
            if (item == ENDER_PEARLS) {
                AchievementManager.recordPearlsGrabbed(amount);
            } else if (item == SPIRIT_LEAPS) {
                AchievementManager.recordLeapsGrabbed(amount);
            } else if (item == DECOYS) {
                AchievementManager.recordDecoysGrabbed(amount);
            } else if (item == SUPERBOOMS) {
                AchievementManager.recordSuperboomsGrabbed(amount);
            }
        }
    }
}
