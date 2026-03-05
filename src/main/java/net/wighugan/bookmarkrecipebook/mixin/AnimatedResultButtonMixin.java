package net.wighugan.bookmarkrecipebook.mixin;

import net.minecraft.client.gui.screen.recipebook.AnimatedResultButton;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.recipe.RecipeEntry;
import net.wighugan.bookmarkrecipebook.BookmarkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(AnimatedResultButton.class)
public class AnimatedResultButtonMixin {
    @Shadow private RecipeResultCollection resultCollection;
    @Inject(method = "getResults", at = @At("HEAD"), cancellable = true)
    private void onGetResults(CallbackInfoReturnable<List<RecipeEntry<?>>> cir) {
        if (BookmarkManager.isBookmarkModeActive && this.resultCollection != null) {
            cir.setReturnValue(this.resultCollection.getAllRecipes());
        }
    }
}