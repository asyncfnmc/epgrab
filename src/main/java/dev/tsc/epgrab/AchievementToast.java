package dev.tsc.epgrab;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

final class AchievementToast implements Toast {
    private static final long DISPLAY_TIME_MS = 5_000L;
    private static final int PADDING = 8;
    private static final int ICON_X = 8;
    private static final int TEXT_X = 30;

    private final AchievementManager.AchievementDefinition achievement;
    private final ItemStack iconStack;
    private final Text headerText;
    private final Text subtitleText;
    private final int width;
    private Visibility visibility = Visibility.SHOW;
    private long startTime;
    private boolean started;

    AchievementToast(AchievementManager.AchievementDefinition achievement) {
        this.achievement = achievement;
        this.iconStack = AchievementManager.createDisplayStack(achievement);
        this.headerText = Text.empty()
            .append(Text.literal("xx ").formatted(net.minecraft.util.Formatting.WHITE, net.minecraft.util.Formatting.OBFUSCATED))
            .append(Text.literal("Achievement Unlocked!").formatted(net.minecraft.util.Formatting.WHITE, net.minecraft.util.Formatting.BOLD))
            .append(Text.literal(" xx").formatted(net.minecraft.util.Formatting.WHITE, net.minecraft.util.Formatting.OBFUSCATED));
        this.subtitleText = Text.literal(achievement.title()).styled(style -> style.withColor(0x5FD0D0).withBold(true));

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            int headerWidth = client.textRenderer.getWidth(headerText);
            int subtitleWidth = client.textRenderer.getWidth(subtitleText);
            this.width = Math.max(Toast.BASE_WIDTH, Math.max(headerWidth, subtitleWidth) + TEXT_X + PADDING);
        } else {
            this.width = Toast.BASE_WIDTH;
        }
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        if (!started) {
            started = true;
            startTime = time;
        }

        double duration = DISPLAY_TIME_MS * manager.getNotificationDisplayTimeMultiplier();
        visibility = time - startTime >= duration ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long time) {
        int width = getWidth();
        int height = getHeight();

        context.fill(0, 0, width, height, 0xEE1F1F1F);
        context.fill(0, 0, width, 1, 0xFF5FD65F);
        context.fill(0, height - 1, width, height, 0xFF5FD65F);
        context.fill(0, 0, 1, height, 0xFF5FD65F);
        context.fill(width - 1, 0, width, height, 0xFF5FD65F);

        context.drawItemWithoutEntity(iconStack, ICON_X, 8);
        context.drawTextWithShadow(textRenderer, headerText, TEXT_X, 7, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, subtitleText, TEXT_X, 18, 0xFFFFFFFF);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return 32;
    }

    @Override
    public Object getType() {
        return achievement.id();
    }
}
