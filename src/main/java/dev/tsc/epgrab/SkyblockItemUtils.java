package dev.tsc.epgrab;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public final class SkyblockItemUtils {
    private SkyblockItemUtils() {
    }

    @Nullable
    public static String getSkyblockItemId(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) {
            return null;
        }

        NbtCompound root = customData.copyNbt();

        String directId = root.getString("id", "");
        if (!directId.isEmpty()) {
            return directId;
        }

        if (root.contains("ExtraAttributes")) {
            NbtCompound extraAttributes = root.getCompoundOrEmpty("ExtraAttributes");
            String extraAttributesId = extraAttributes.getString("id", "");
            if (!extraAttributesId.isEmpty()) {
                return extraAttributesId;
            }
        }

        if (root.contains("tag")) {
            NbtCompound tag = root.getCompoundOrEmpty("tag");

            String tagDirectId = tag.getString("id", "");
            if (!tagDirectId.isEmpty()) {
                return tagDirectId;
            }

            if (tag.contains("ExtraAttributes")) {
                NbtCompound extraAttributes = tag.getCompoundOrEmpty("ExtraAttributes");
                String extraAttributesId = extraAttributes.getString("id", "");
                if (!extraAttributesId.isEmpty()) {
                    return extraAttributesId;
                }
            }
        }

        return null;
    }

    public static int countInventoryItem(ClientPlayerEntity player, String itemId) {
        int total = 0;

        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (itemId.equals(getSkyblockItemId(stack))) {
                total += stack.getCount();
            }
        }

        return total;
    }
}
