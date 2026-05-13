package dev.tsc.epgrab.mixin;

import dev.tsc.epgrab.CreatorBadge;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At("TAIL"))
    private void epgrab$appendCreatorBadge(PlayerLikeEntity player, PlayerEntityRenderState state, float tickDelta, CallbackInfo ci) {
        if (!CreatorBadge.isCreator(player.getUuid())) {
            return;
        }

        if (state.displayName != null) {
            state.displayName = CreatorBadge.appendBadge(state.displayName);
        } else if (state.playerName != null) {
            state.playerName = CreatorBadge.appendBadge(state.playerName);
        }
    }
}
