package me.aidan.sydney.mixins;

import me.aidan.sydney.Sydney;
import me.aidan.sydney.modules.impl.visuals.EntityModifierModule;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.entity.model.EndCrystalEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Model.class)
public abstract class ModelMixin {
    @ModifyArgs(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void render$render(Args args) {
        if ((Object) this instanceof EndCrystalEntityModel && Sydney.MODULE_MANAGER.getModule(EntityModifierModule.class).isToggled() && Sydney.MODULE_MANAGER.getModule(EntityModifierModule.class).crystals.getValue()) {
            args.set(4, Sydney.MODULE_MANAGER.getModule(EntityModifierModule.class).crystalColor.getColor().getRGB());
        }
    }
}
