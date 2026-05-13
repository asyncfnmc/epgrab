package dev.tsc.epgrab;

import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.UUID;

public final class CreatorBadge {
    public static final UUID PRIMARY_CREATOR_UUID = UUID.fromString("4a4c627c-b9f9-4d54-adf5-d802d6c5753e");
    public static final UUID SECONDARY_CREATOR_UUID = UUID.fromString("fdda8e0a-27ed-4548-b41e-984f262d1b9b");
    public static final Set<UUID> CREATOR_UUIDS = Set.of(
        PRIMARY_CREATOR_UUID,
        SECONDARY_CREATOR_UUID
    );
    private static final char BADGE_CHARACTER = '\uE000';
    private static final StyleSpriteSource.Font BADGE_FONT = new StyleSpriteSource.Font(Identifier.of("epgrab", "creator_badge"));

    private CreatorBadge() {
    }

    public static boolean isCreator(UUID uuid) {
        return CREATOR_UUIDS.contains(uuid);
    }

    public static Text appendBadge(Text baseText) {
        if (baseText == null || baseText.getString().indexOf(BADGE_CHARACTER) >= 0) {
            return baseText;
        }

        return baseText.copy()
            .append(Text.literal(" "))
            .append(Text.literal(String.valueOf(BADGE_CHARACTER)).setStyle(Style.EMPTY.withFont(BADGE_FONT).withColor(0xFFFFFF)));
    }
}
