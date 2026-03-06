package net.wighugan.bookmarkrecipebook.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.recipebook.AnimatedResultButton;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.recipe.RecipeEntry;
import net.wighugan.bookmarkrecipebook.BookmarkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(AnimatedResultButton.class)
public abstract class AnimatedResultButtonMixin {
    @Shadow private RecipeResultCollection resultCollection;
    @Shadow public abstract List<RecipeEntry<?>> getResults();

    @Inject(method = "getResults", at = @At("HEAD"), cancellable = true)
    private void onGetResults(CallbackInfoReturnable<List<RecipeEntry<?>>> cir) {
        if (BookmarkManager.isBookmarkModeActive && this.resultCollection != null) {
            cir.setReturnValue(this.resultCollection.getAllRecipes());
        }
    }

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void onRenderWidget(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        List<RecipeEntry<?>> safeResults = this.getResults();
        if (safeResults == null || safeResults.isEmpty()) {
            ((AnimatedResultButton) (Object) this).visible = false;
            ci.cancel();
        }
    }
}