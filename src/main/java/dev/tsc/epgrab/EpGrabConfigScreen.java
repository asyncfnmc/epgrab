package dev.tsc.epgrab;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

final class EpGrabConfigScreen extends Screen {
    private final Screen parent;

    private InputUtil.Key pearlKey;
    private InputUtil.Key spiritLeapKey;
    private InputUtil.Key decoyKey;
    private InputUtil.Key superboomKey;
    private BindingTarget pendingBinding;
    private Page currentPage = Page.CONTROLS;
    private double achievementScroll;
    private AchievementFilter achievementFilter = AchievementFilter.ALL;
    private boolean filterDropdownOpen;

    private ButtonWidget controlsTabButton;
    private ButtonWidget achievementsTabButton;
    private ButtonWidget pearlButton;
    private ButtonWidget spiritLeapButton;
    private ButtonWidget decoyButton;
    private ButtonWidget superboomButton;
    private ButtonWidget doneButton;
    private ButtonWidget cancelButton;
    private ButtonWidget returnButton;
    private ButtonWidget filterDropdownButton;
    private ButtonWidget filterAllButton;
    private ButtonWidget filterPearlsButton;
    private ButtonWidget filterLeapsButton;
    private ButtonWidget filterDecoysButton;
    private ButtonWidget filterBoomsButton;
    private ButtonWidget filterKismetsButton;
    private ButtonWidget filterSocialButton;

    EpGrabConfigScreen(Screen parent) {
        this(parent, false);
    }

    EpGrabConfigScreen(Screen parent, boolean openAchievements) {
        super(Text.literal("epgrab settings"));
        this.parent = parent;
        this.currentPage = openAchievements ? Page.ACHIEVEMENTS : Page.CONTROLS;
        this.pearlKey = EpGrabClient.getBoundKey(EpGrabClient.EP_KEYBINDING);
        this.spiritLeapKey = EpGrabClient.getBoundKey(EpGrabClient.SL_KEYBINDING);
        this.decoyKey = EpGrabClient.getBoundKey(EpGrabClient.DE_KEYBINDING);
        this.superboomKey = EpGrabClient.getBoundKey(EpGrabClient.SB_KEYBINDING);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int top = this.height / 4;
        int achievementPanelLeft = centerX - 156;
        int achievementSummaryBottom = 110;
        int filterDropdownY = achievementSummaryBottom + 8;

        controlsTabButton = addDrawableChild(ButtonWidget.builder(getTabText(Page.CONTROLS), button -> switchPage(Page.CONTROLS))
            .dimensions(centerX - 102, top - 18, 100, 20)
            .build());

        achievementsTabButton = addDrawableChild(ButtonWidget.builder(getTabText(Page.ACHIEVEMENTS), button -> switchPage(Page.ACHIEVEMENTS))
            .dimensions(centerX + 2, top - 18, 100, 20)
            .build());

        pearlButton = addDrawableChild(ButtonWidget.builder(getBindingButtonText(BindingTarget.PEARLS), button -> {
            pendingBinding = BindingTarget.PEARLS;
            refreshButtonMessages();
        }).dimensions(centerX - 100, top + 30, 200, 20).build());

        spiritLeapButton = addDrawableChild(ButtonWidget.builder(getBindingButtonText(BindingTarget.SPIRIT_LEAPS), button -> {
            pendingBinding = BindingTarget.SPIRIT_LEAPS;
            refreshButtonMessages();
        }).dimensions(centerX - 100, top + 60, 200, 20).build());

        decoyButton = addDrawableChild(ButtonWidget.builder(getBindingButtonText(BindingTarget.DECOYS), button -> {
            pendingBinding = BindingTarget.DECOYS;
            refreshButtonMessages();
        }).dimensions(centerX - 100, top + 90, 200, 20).build());

        superboomButton = addDrawableChild(ButtonWidget.builder(getBindingButtonText(BindingTarget.SUPERBOOMS), button -> {
            pendingBinding = BindingTarget.SUPERBOOMS;
            refreshButtonMessages();
        }).dimensions(centerX - 100, top + 120, 200, 20).build());

        filterDropdownButton = addDrawableChild(ButtonWidget.builder(getAchievementDropdownText(), button -> toggleAchievementFilterDropdown())
            .dimensions(centerX - 72, filterDropdownY, 144, 20)
            .build());

        filterAllButton = addDrawableChild(ButtonWidget.builder(getAchievementFilterOptionText(AchievementFilter.ALL), button -> switchAchievementFilter(AchievementFilter.ALL))
            .dimensions(centerX - 72, filterDropdownY + 20, 144, 20)
            .build());

        filterPearlsButton = addDrawableChild(ButtonWidget.builder(getAchievementFilterOptionText(AchievementFilter.PEARLS), button -> switchAchievementFilter(AchievementFilter.PEARLS))
            .dimensions(centerX - 72, filterDropdownY + 40, 144, 20)
            .build());

        filterLeapsButton = addDrawableChild(ButtonWidget.builder(getAchievementFilterOptionText(AchievementFilter.LEAPS), button -> switchAchievementFilter(AchievementFilter.LEAPS))
            .dimensions(centerX - 72, filterDropdownY + 60, 144, 20)
            .build());

        filterDecoysButton = addDrawableChild(ButtonWidget.builder(getAchievementFilterOptionText(AchievementFilter.DECOYS), button -> switchAchievementFilter(AchievementFilter.DECOYS))
            .dimensions(centerX - 72, filterDropdownY + 80, 144, 20)
            .build());

        filterBoomsButton = addDrawableChild(ButtonWidget.builder(getAchievementFilterOptionText(AchievementFilter.SUPERBOOMS), button -> switchAchievementFilter(AchievementFilter.SUPERBOOMS))
            .dimensions(centerX - 72, filterDropdownY + 100, 144, 20)
            .build());

        filterKismetsButton = addDrawableChild(ButtonWidget.builder(getAchievementFilterOptionText(AchievementFilter.KISMETS), button -> switchAchievementFilter(AchievementFilter.KISMETS))
            .dimensions(centerX - 72, filterDropdownY + 120, 144, 20)
            .build());

        filterSocialButton = addDrawableChild(ButtonWidget.builder(getAchievementFilterOptionText(AchievementFilter.SOCIAL), button -> switchAchievementFilter(AchievementFilter.SOCIAL))
            .dimensions(centerX - 72, filterDropdownY + 140, 144, 20)
            .build());

        int bottomButtonsY = this.height - 28;

        doneButton = addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> saveAndClose())
            .dimensions(centerX - 100, bottomButtonsY, 95, 20)
            .build());

        cancelButton = addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> close())
            .dimensions(centerX + 5, bottomButtonsY, 95, 20)
            .build());

        returnButton = addDrawableChild(ButtonWidget.builder(Text.literal("Return"), button -> switchPage(Page.CONTROLS))
            .dimensions(centerX - 50, bottomButtonsY, 100, 20)
            .build());

        refreshPageState();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xB0101010);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 30, 0xFFFFFFFF);

        if (currentPage == Page.CONTROLS) {
            renderControlsPage(context);
        } else {
            renderAchievementsPage(context);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        if (pendingBinding != null) {
            InputUtil.Key key = keyInput.getKeycode() == GLFW.GLFW_KEY_ESCAPE
                ? InputUtil.UNKNOWN_KEY
                : InputUtil.fromKeyCode(keyInput);

            if (pendingBinding == BindingTarget.PEARLS) {
                pearlKey = key;
            } else if (pendingBinding == BindingTarget.SPIRIT_LEAPS) {
                spiritLeapKey = key;
            } else if (pendingBinding == BindingTarget.DECOYS) {
                decoyKey = key;
            } else {
                superboomKey = key;
            }

            pendingBinding = null;
            refreshButtonMessages();
            return true;
        }

        return super.keyPressed(keyInput);
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    private void saveAndClose() {
        EpGrabClient.setBoundKey(EpGrabClient.EP_KEYBINDING, pearlKey);
        EpGrabClient.setBoundKey(EpGrabClient.SL_KEYBINDING, spiritLeapKey);
        EpGrabClient.setBoundKey(EpGrabClient.DE_KEYBINDING, decoyKey);
        EpGrabClient.setBoundKey(EpGrabClient.SB_KEYBINDING, superboomKey);
        EpGrabClient.saveKeyBindings();
        close();
    }

    private void switchPage(Page page) {
        currentPage = page;
        pendingBinding = null;
        achievementScroll = 0;
        filterDropdownOpen = false;
        refreshPageState();
    }

    private void switchAchievementFilter(AchievementFilter filter) {
        achievementFilter = filter;
        achievementScroll = 0;
        filterDropdownOpen = false;
        refreshAchievementFilterButtons();
    }

    private void toggleAchievementFilterDropdown() {
        filterDropdownOpen = !filterDropdownOpen;
        refreshAchievementFilterButtons();
    }

    private void refreshPageState() {
        boolean controlsPage = currentPage == Page.CONTROLS;

        controlsTabButton.setMessage(getTabText(Page.CONTROLS));
        achievementsTabButton.setMessage(getTabText(Page.ACHIEVEMENTS));
        controlsTabButton.visible = controlsPage;
        achievementsTabButton.visible = controlsPage;
        pearlButton.visible = controlsPage;
        spiritLeapButton.visible = controlsPage;
        decoyButton.visible = controlsPage;
        superboomButton.visible = controlsPage;
        doneButton.visible = controlsPage;
        cancelButton.visible = controlsPage;
        returnButton.visible = !controlsPage;
        filterDropdownButton.visible = !controlsPage;
        refreshButtonMessages();
        refreshAchievementFilterButtons();
    }

    private void refreshButtonMessages() {
        pearlButton.setMessage(getBindingButtonText(BindingTarget.PEARLS));
        spiritLeapButton.setMessage(getBindingButtonText(BindingTarget.SPIRIT_LEAPS));
        decoyButton.setMessage(getBindingButtonText(BindingTarget.DECOYS));
        superboomButton.setMessage(getBindingButtonText(BindingTarget.SUPERBOOMS));
    }

    private void refreshAchievementFilterButtons() {
        boolean visible = currentPage == Page.ACHIEVEMENTS;

        filterDropdownButton.setMessage(getAchievementDropdownText());
        filterAllButton.setMessage(getAchievementFilterOptionText(AchievementFilter.ALL));
        filterPearlsButton.setMessage(getAchievementFilterOptionText(AchievementFilter.PEARLS));
        filterLeapsButton.setMessage(getAchievementFilterOptionText(AchievementFilter.LEAPS));
        filterDecoysButton.setMessage(getAchievementFilterOptionText(AchievementFilter.DECOYS));
        filterBoomsButton.setMessage(getAchievementFilterOptionText(AchievementFilter.SUPERBOOMS));
        filterKismetsButton.setMessage(getAchievementFilterOptionText(AchievementFilter.KISMETS));
        filterSocialButton.setMessage(getAchievementFilterOptionText(AchievementFilter.SOCIAL));

        filterAllButton.visible = visible && filterDropdownOpen;
        filterPearlsButton.visible = visible && filterDropdownOpen;
        filterLeapsButton.visible = visible && filterDropdownOpen;
        filterDecoysButton.visible = visible && filterDropdownOpen;
        filterBoomsButton.visible = visible && filterDropdownOpen;
        filterKismetsButton.visible = visible && filterDropdownOpen;
        filterSocialButton.visible = visible && filterDropdownOpen;
    }

    private void renderControlsPage(DrawContext context) {
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Configure epgrab keybinds from Mod Menu."), width / 2, 48, 0xFFA0A0A0);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Click a binding, then press a key. ESC clears it."), width / 2, 60, 0xFF808080);
    }

    private void renderAchievementsPage(DrawContext context) {
        int panelLeft = width / 2 - 156;
        int panelRight = width / 2 + 156;
        int summaryTop = 48;
        int summaryBottom = 110;
        int listTop = getAchievementListTop();
        int listBottom = returnButton.getY() - 8;
        int listHeight = Math.max(0, listBottom - listTop);
        List<AchievementManager.AchievementDefinition> achievements = getFilteredAchievements();

        context.fill(panelLeft, summaryTop, panelRight, summaryBottom, 0x80202020);
        context.drawTextWithShadow(textRenderer,
            Text.literal("Unlocked: " + AchievementManager.unlockedCount() + "/" + AchievementManager.totalCount()).formatted(Formatting.GOLD),
            panelLeft + 8, summaryTop + 7, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer,
            Text.literal("Pearls G/T: " + formatCount(AchievementManager.pearlsGrabbed()) + "/" + formatCount(AchievementManager.pearlsThrown())).formatted(Formatting.LIGHT_PURPLE),
            panelLeft + 8, summaryTop + 20, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer,
            Text.literal("Leaps G/C: " + formatCount(AchievementManager.spiritLeapsGrabbed()) + "/" + formatCount(AchievementManager.spiritLeapsCrushed())).formatted(Formatting.AQUA),
            panelLeft + 8, summaryTop + 32, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer,
            Text.literal("Decoys G/U: " + formatCount(AchievementManager.decoysGrabbed()) + "/" + formatCount(AchievementManager.decoysUsed())).formatted(Formatting.GOLD),
            panelLeft + 170, summaryTop + 20, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer,
            Text.literal("Booms G/U: " + formatCount(AchievementManager.superboomsGrabbed()) + "/" + formatCount(AchievementManager.superboomsUsed())).formatted(Formatting.RED),
            panelLeft + 170, summaryTop + 32, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer,
            Text.literal("Kismets U: " + formatCount(AchievementManager.kismetsUsed())).formatted(Formatting.WHITE),
            panelLeft + 8, summaryTop + 44, 0xFFFFFFFF);

        int contentHeight = achievements.size() * 29;
        double maxScroll = Math.max(0, contentHeight - listHeight);
        achievementScroll = Math.clamp(achievementScroll, 0, maxScroll);

        context.enableScissor(panelLeft, listTop, panelRight, listBottom);

        if (achievements.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("No achievements in this filter.").formatted(Formatting.DARK_GRAY), width / 2, listTop + 12, 0xFFFFFFFF);
        } else {
            int y = listTop - (int) achievementScroll;
            for (AchievementManager.AchievementDefinition achievement : achievements) {
                boolean unlocked = AchievementManager.isUnlocked(achievement.id());
                int cardTop = y;
                int cardBottom = y + 27;

                if (cardBottom >= listTop && cardTop <= listBottom) {
                    int background = unlocked ? 0x80407020 : 0x80303030;
                    int border = unlocked ? 0xFF60D060 : 0xFF707070;

                    context.fill(panelLeft, cardTop, panelRight, cardBottom, background);
                    context.fill(panelLeft, cardTop, panelRight, cardTop + 1, border);
                    context.fill(panelLeft, cardBottom - 1, panelRight, cardBottom, border);
                    context.fill(panelLeft, cardTop, panelLeft + 1, cardBottom, border);
                    context.fill(panelRight - 1, cardTop, panelRight, cardBottom, border);

                    int iconX = panelLeft + 4;
                    int iconY = cardTop + 5;
                    renderAchievementIcon(context, achievement, unlocked, iconX, iconY);

                    context.drawTextWithShadow(textRenderer,
                        Text.literal(achievement.title()).formatted(unlocked ? Formatting.WHITE : Formatting.GRAY),
                        panelLeft + 24, cardTop + 6, 0xFFFFFFFF);

                    String progress = getProgressText(achievement, unlocked);
                    context.drawTextWithShadow(textRenderer,
                        Text.literal(progress).formatted(unlocked ? Formatting.GREEN : Formatting.DARK_GRAY),
                        panelRight - textRenderer.getWidth(progress) - 8, cardTop + 6, 0xFFFFFFFF);

                    context.drawTextWithShadow(textRenderer,
                        Text.literal(achievement.description()).formatted(unlocked ? Formatting.GRAY : Formatting.DARK_GRAY),
                        panelLeft + 24, cardTop + 18, 0xFFFFFFFF);
                }

                y += 29;
            }
        }

        context.disableScissor();

        if (maxScroll > 0) {
            int trackLeft = panelRight + 4;
            int trackRight = trackLeft + 4;
            context.fill(trackLeft, listTop, trackRight, listBottom, 0x80202020);

            int thumbHeight = Math.max(12, (int) (listHeight * (listHeight / (double) contentHeight)));
            int thumbTravel = Math.max(0, listHeight - thumbHeight);
            int thumbTop = listTop + (int) Math.round((achievementScroll / maxScroll) * thumbTravel);
            context.fill(trackLeft, thumbTop, trackRight, thumbTop + thumbHeight, 0xFF60D0D0);
        }
    }

    private List<AchievementManager.AchievementDefinition> getFilteredAchievements() {
        if (achievementFilter == AchievementFilter.ALL) {
            return AchievementManager.achievements();
        }

        List<AchievementManager.AchievementDefinition> filtered = new ArrayList<>();
        for (AchievementManager.AchievementDefinition achievement : AchievementManager.achievements()) {
            if (achievementFilter.matches(achievement.category())) {
                filtered.add(achievement);
            }
        }
        return filtered;
    }

    private String getProgressText(AchievementManager.AchievementDefinition achievement, boolean unlocked) {
        if (achievement.category() == AchievementManager.Category.SOCIAL) {
            return unlocked ? "COMPLETE" : "LOCKED";
        }

        long current = AchievementManager.progressFor(achievement.category());

        if (unlocked) {
            return "COMPLETE";
        }

        return formatCount(current) + "/" + formatCount(achievement.threshold());
    }

    private String formatCount(long value) {
        return String.format("%,d", value);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (currentPage == Page.ACHIEVEMENTS) {
            int listTop = getAchievementListTop();
            int listBottom = returnButton.getY() - 8;
            int panelLeft = width / 2 - 156;
            int panelRight = width / 2 + 156;

            if (mouseX >= panelLeft && mouseX <= panelRight && mouseY >= listTop && mouseY <= listBottom) {
                int contentHeight = getFilteredAchievements().size() * 29;
                int listHeight = Math.max(0, listBottom - listTop);
                double maxScroll = Math.max(0, contentHeight - listHeight);
                achievementScroll = Math.clamp(achievementScroll - verticalAmount * 16.0, 0, maxScroll);
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private int getAchievementListTop() {
        return getAchievementFilterBottom() + 12;
    }

    private int getAchievementFilterBottom() {
        if (filterDropdownButton == null) {
            return 138;
        }

        if (!filterDropdownOpen) {
            return filterDropdownButton.getY() + filterDropdownButton.getHeight();
        }

        return filterSocialButton.getY() + filterSocialButton.getHeight();
    }

    private void renderAchievementIcon(DrawContext context, AchievementManager.AchievementDefinition achievement, boolean unlocked, int x, int y) {
        context.drawItemWithoutEntity(AchievementManager.createDisplayStack(achievement), x, y);

        if (!unlocked) {
            context.fill(x, y, x + 16, y + 16, 0xA0000000);
            drawLockOverlay(context, x + 9, y + 8);
        }
    }

    private void drawLockOverlay(DrawContext context, int x, int y) {
        int color = 0xFFD0D0D0;
        context.fill(x + 1, y, x + 5, y + 1, color);
        context.fill(x, y + 1, x + 1, y + 3, color);
        context.fill(x + 5, y + 1, x + 6, y + 3, color);
        context.fill(x, y + 3, x + 6, y + 7, color);
        context.fill(x + 2, y + 4, x + 4, y + 6, 0xFF5A5A5A);
    }

    private Text getBindingButtonText(BindingTarget target) {
        if (pendingBinding == target) {
            return Text.literal("> press a key <").formatted(Formatting.YELLOW);
        }

        String label;
        InputUtil.Key key;
        if (target == BindingTarget.PEARLS) {
            label = "Pearls";
            key = pearlKey;
        } else if (target == BindingTarget.SPIRIT_LEAPS) {
            label = "Spirit Leaps";
            key = spiritLeapKey;
        } else if (target == BindingTarget.DECOYS) {
            label = "Decoys";
            key = decoyKey;
        } else {
            label = "Superboom TNT";
            key = superboomKey;
        }

        return Text.empty()
            .append(Text.literal(label + ": ").formatted(Formatting.GRAY))
            .append(key.getLocalizedText().copy().formatted(Formatting.AQUA));
    }

    private Text getTabText(Page page) {
        boolean active = currentPage == page;
        Formatting color = active ? Formatting.AQUA : Formatting.GRAY;
        String label = page == Page.CONTROLS ? "Controls" : "Achievements";
        return Text.literal(label).formatted(color);
    }

    private Text getAchievementDropdownText() {
        return Text.empty()
            .append(Text.literal("Filter: ").formatted(Formatting.GRAY))
            .append(Text.literal(achievementFilter.label()).formatted(Formatting.AQUA))
            .append(Text.literal(filterDropdownOpen ? " ▲" : " ▼").formatted(Formatting.DARK_GRAY));
    }

    private Text getAchievementFilterOptionText(AchievementFilter filter) {
        boolean active = achievementFilter == filter;
        return Text.literal(filter.label()).formatted(active ? Formatting.AQUA : Formatting.GRAY);
    }

    private enum BindingTarget {
        PEARLS,
        SPIRIT_LEAPS,
        DECOYS,
        SUPERBOOMS
    }

    private enum Page {
        CONTROLS,
        ACHIEVEMENTS
    }

    private enum AchievementFilter {
        ALL("All") {
            @Override
            boolean matches(AchievementManager.Category category) {
                return true;
            }
        },
        PEARLS("Pearls") {
            @Override
            boolean matches(AchievementManager.Category category) {
                return category == AchievementManager.Category.PEARLS_GRABBED || category == AchievementManager.Category.PEARLS_THROWN;
            }
        },
        LEAPS("Leaps") {
            @Override
            boolean matches(AchievementManager.Category category) {
                return category == AchievementManager.Category.LEAPS_GRABBED || category == AchievementManager.Category.LEAPS_CRUSHED;
            }
        },
        DECOYS("Decoys") {
            @Override
            boolean matches(AchievementManager.Category category) {
                return category == AchievementManager.Category.DECOYS_GRABBED || category == AchievementManager.Category.DECOYS_USED;
            }
        },
        SUPERBOOMS("Booms") {
            @Override
            boolean matches(AchievementManager.Category category) {
                return category == AchievementManager.Category.SUPERBOOMS_GRABBED || category == AchievementManager.Category.SUPERBOOMS_USED;
            }
        },
        KISMETS("Kismets") {
            @Override
            boolean matches(AchievementManager.Category category) {
                return category == AchievementManager.Category.KISMETS_USED;
            }
        },
        SOCIAL("Social") {
            @Override
            boolean matches(AchievementManager.Category category) {
                return category == AchievementManager.Category.SOCIAL;
            }
        };

        private final String label;

        AchievementFilter(String label) {
            this.label = label;
        }

        abstract boolean matches(AchievementManager.Category category);

        String label() {
            return label;
        }
    }
}
