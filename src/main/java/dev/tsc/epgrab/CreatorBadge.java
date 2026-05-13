package dev.tsc.epgrab;

import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.UUID;

public final class CreatorBadge {
    public static final UUID ASYNCFN_UUID = UUID.fromString("4a4c627c-b9f9-4d54-adf5-d802d6c5753e");
    public static final UUID MILODAMONKE_UUID = UUID.fromString("fdda8e0a-27ed-4548-b41e-984f262d1b9b");

    public static final Set<UUID> CREATOR_UUIDS = Set.of(ASYNCFN_UUID);
    public static final Set<UUID> REALLY_CREATIVE_MIND_UUIDS = Set.of(
        ASYNCFN_UUID,
        MILODAMONKE_UUID
    );

    private static final char CREATOR_BADGE_CHARACTER = '\uE000';
    private static final char REALLY_CREATIVE_MIND_BADGE_CHARACTER = '\uE001';
    private static final StyleSpriteSource.Font CREATOR_BADGE_FONT = new StyleSpriteSource.Font(Identifier.of("epgrab", "creator_badge"));
    private static final StyleSpriteSource.Font REALLY_CREATIVE_MIND_BADGE_FONT = new StyleSpriteSource.Font(Identifier.of("epgrab", "really_creative_mind_badge"));

    private CreatorBadge() {
    }

    public static boolean isCreator(UUID uuid) {
        return CREATOR_UUIDS.contains(uuid);
    }

    public static boolean isReallyCreativeMind(UUID uuid) {
        return REALLY_CREATIVE_MIND_UUIDS.contains(uuid);
    }

    public static Text appendBadge(UUID uuid, Text baseText) {
        if (baseText == null) {
            return null;
        }

        String string = baseText.getString();
        boolean hasCreatorBadge = string.indexOf(CREATOR_BADGE_CHARACTER) >= 0;
        boolean hasCreativeBadge = string.indexOf(REALLY_CREATIVE_MIND_BADGE_CHARACTER) >= 0;

        net.minecraft.text.MutableText result = baseText.copy();
        boolean appendedAny = false;

        if (isCreator(uuid) && !hasCreatorBadge) {
            result = appendBadgeCharacter(result, CREATOR_BADGE_CHARACTER, CREATOR_BADGE_FONT, !appendedAny);
            appendedAny = true;
        }

        if (isReallyCreativeMind(uuid) && !hasCreativeBadge) {
            result = appendBadgeCharacter(result, REALLY_CREATIVE_MIND_BADGE_CHARACTER, REALLY_CREATIVE_MIND_BADGE_FONT, !appendedAny);
        }

        return result;
    }

    private static net.minecraft.text.MutableText appendBadgeCharacter(Text baseText, char badgeCharacter, StyleSpriteSource.Font font, boolean includeLeadingSpace) {
        net.minecraft.text.MutableText result = baseText.copy();
        if (includeLeadingSpace) {
            result.append(Text.literal(" "));
        }
        return result.append(Text.literal(String.valueOf(badgeCharacter)).setStyle(Style.EMPTY.withFont(font).withColor(0xFFFFFF)));
    }
}
