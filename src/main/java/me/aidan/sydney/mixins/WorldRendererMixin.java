package me.aidan.sydney.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import me.aidan.sydney.ISU;
import me.aidan.sydney.events.impl.RenderEntityEvent;
import me.aidan.sydney.events.impl.RenderShaderEvent;
import me.aidan.sydney.modules.impl.visuals.BlockHighlightModule;
import me.aidan.sydney.modules.impl.visuals.FreecamModule;
import me.aidan.sydney.modules.impl.visuals.NoInterpolationModule;
import me.aidan.sydney.modules.impl.visuals.NoRenderModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "render", at = @At(value = "HEAD"))
    private void render(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo info, @Local(argsOnly = true) LocalBooleanRef blockOutline) {
        if (ISU.MODULE_MANAGER != null && ISU.MODULE_MANAGER.getModule(BlockHighlightModule.class).isToggled()) {
            blockOutline.set(false);
        }
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V"), index = 3)
    private boolean render$setupTerrain(boolean spectator) {
        return ISU.MODULE_MANAGER.getModule(FreecamModule.class).isToggled() || spectator;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 0, shift = At.Shift.BEFORE))
    private void render$swap(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo info) {
        ISU.EVENT_HANDLER.post(new RenderShaderEvent());
    }

    @Inject(method = "hasBlindnessOrDarkness(Lnet/minecraft/client/render/Camera;)Z", at = @At("HEAD"), cancellable = true)
    private void hasBlindnessOrDarkness(Camera camera, CallbackInfoReturnable<Boolean> info) {
        if (ISU.MODULE_MANAGER.getModule(NoRenderModule.class).isToggled() && ISU.MODULE_MANAGER.getModule(NoRenderModule.class).blindness.getValue()) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    private void renderEntity$HEAD(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo info) {
        if (entity != MinecraftClient.getInstance().player && entity instanceof PlayerEntity && ISU.MODULE_MANAGER.getModule(NoInterpolationModule.class).isToggled()) {
            RenderEntityEvent event = new RenderEntityEvent(entity, vertexConsumers);
            ISU.EVENT_HANDLER.post(event);

            entityRenderDispatcher.render(entity, entity.getX() - cameraX, entity.getY() - cameraY, entity.getZ() - cameraZ, tickDelta, matrices, event.getVertexConsumers(), this.entityRenderDispatcher.getLight(entity, tickDelta));
            info.cancel();
        }
    }

    @WrapOperation(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private <E extends Entity> void renderEntity$render(EntityRenderDispatcher instance, E entity, double x, double y, double z, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Operation<Void> original) {
        RenderEntityEvent event = new RenderEntityEvent(entity, vertexConsumers);
        ISU.EVENT_HANDLER.post(event);

        original.call(instance, entity, x, y, z, tickDelta, matrices, event.getVertexConsumers(), light);
    }

    @Inject(method = "onResized", at = @At("TAIL"))
    private void onResized(int width, int height, CallbackInfo info) {
        if (ISU.SHADER_MANAGER != null) {
            ISU.SHADER_MANAGER.resize(width, height);
        }
    }
}
