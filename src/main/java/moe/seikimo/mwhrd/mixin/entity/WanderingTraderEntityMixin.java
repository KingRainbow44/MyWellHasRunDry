package moe.seikimo.mwhrd.mixin.entity;

import moe.seikimo.mwhrd.game.quest.Quests;
import moe.seikimo.mwhrd.interfaces.player.IPlayer;
import moe.seikimo.mwhrd.interfaces.player.IStoryPlayer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity {
    public WanderingTraderEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interactMob", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/entity/passive/WanderingTraderEntity;sendOffers(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/text/Text;I)V"
    ), cancellable = true)
    public void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        // Check if the player is sneaking.
        if (player.isSneaking()) {
            // If the player is not in a guild, continue.
            if (!(player instanceof IPlayer mwhrdPlayer)) {
                return;
            }

            if (mwhrdPlayer.mwhrd$getData().getGuild() == null) {
                player.sendMessage(Text.translatable("text.mwhrd.quest.700000.hint")
                    .formatted(Formatting.ITALIC, Formatting.GRAY), false);
                cir.setReturnValue(ActionResult.CONSUME);
                return;
            }

            cir.setReturnValue(ActionResult.SUCCESS);

            // Cancel the method and engage with dialogue instead.
            if (player instanceof IStoryPlayer storyPlayer) {
                var questManager = storyPlayer.mwhrd$getQuestManager();
                if (!questManager.startDialogue(Quests.MCA_QUEST_ENTRYPOINT)) {
                    // Return a 'CONSUME' value if the dialogue failed.
                    cir.setReturnValue(ActionResult.CONSUME);
                }
            }
        }
    }
}
