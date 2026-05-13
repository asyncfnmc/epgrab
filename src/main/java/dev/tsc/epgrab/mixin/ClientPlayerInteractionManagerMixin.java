package dev.tsc.epgrab.mixin;

import dev.tsc.epgrab.AchievementManager;
import dev.tsc.epgrab.SkyblockItemUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Unique
    private String epgrab$pendingItemUseId;
    @Unique
    private String epgrab$pendingBlockUseId;

    @Inject(method = "interactItem", at = @At("HEAD"))
    private void epgrab$captureItemUse(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        epgrab$pendingItemUseId = epgrab$getTrackableItemId(player.getStackInHand(hand));
    }

    @Inject(method = "interactItem", at = @At("RETURN"))
    private void epgrab$recordUseFromItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        try {
            if (epgrab$pendingItemUseId != null && cir.getReturnValue().isAccepted()) {
                AchievementManager.recordSkyblockItemUse(epgrab$pendingItemUseId);
            }
        } finally {
            epgrab$pendingItemUseId = null;
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void epgrab$captureBlockUse(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        epgrab$pendingBlockUseId = epgrab$getTrackableItemId(player.getStackInHand(hand));
    }

    @Inject(method = "interactBlock", at = @At("RETURN"))
    private void epgrab$recordUseFromBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        try {
            if (epgrab$pendingBlockUseId != null && cir.getReturnValue().isAccepted()) {
                AchievementManager.recordSkyblockItemUse(epgrab$pendingBlockUseId);
            }
        } finally {
            epgrab$pendingBlockUseId = null;
        }
    }

    @Unique
    private static String epgrab$getTrackableItemId(ItemStack stack) {
        String skyblockItemId = SkyblockItemUtils.getSkyblockItemId(stack);
        if (skyblockItemId == null) {
            return null;
        }

        return switch (skyblockItemId) {
            case "ENDER_PEARL", "DECOY", "SUPERBOOM_TNT" -> skyblockItemId;
            default -> null;
        };
    }
}
