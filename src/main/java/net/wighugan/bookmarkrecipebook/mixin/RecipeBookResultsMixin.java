package net.wighugan.bookmarkrecipebook.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.recipebook.AnimatedResultButton;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.recipe.RecipeEntry;
import net.wighugan.bookmarkrecipebook.BookmarkManager;
import net.wighugan.bookmarkrecipebook.BookmarkRecipeBookClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(RecipeBookResults.class)
public abstract class RecipeBookResultsMixin {
    @Shadow private void refreshResultButtons() {}
    @Shadow @Final private List<AnimatedResultButton> resultButtons;
    @Shadow private int currentPage;
    @Shadow private int pageCount;
    @Shadow private List<RecipeResultCollection> resultCollections;
    @Shadow protected abstract void hideShowPageButtons();
    @Shadow public abstract boolean mouseClicked(double mouseX, double mouseY, int button, int areaLeft, int areaTop, int areaWidth, int areaHeight);

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onRecipeContainerClicked(double mouseX, double mouseY, int button, int areaLeft, int areaTop, int areaWidth, int areaHeight, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) return;

        net.minecraft.client.util.InputUtil.Key boundKey = net.minecraft.client.util.InputUtil.fromTranslationKey(BookmarkRecipeBookClient.bookmarkKey.getBoundKeyTranslationKey());
        boolean isCustomKeyPressed = net.minecraft.client.util.InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), boundKey.getCode());
        if (boundKey.getCode() != -1) {
            isCustomKeyPressed = net.minecraft.client.util.InputUtil.isKeyPressed(net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle(), boundKey.getCode());
        }
        if (isCustomKeyPressed && this.resultButtons != null) {
            for (AnimatedResultButton resultButton : this.resultButtons) {
                if (resultButton.isMouseOver(mouseX, mouseY)) {
                    RecipeResultCollection collection = resultButton.getResultCollection();
                    if (collection != null && !collection.getAllRecipes().isEmpty()) {
                        RecipeEntry<?> recipe = collection.getAllRecipes().get(0);
                        BookmarkManager.toggleBookmark(recipe.id());
                        if (BookmarkManager.isBookmarkModeActive) {
                            this.refreshResultButtons();
                            this.hideShowPageButtons();
                        }
                    }
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }

    @Inject(method = "setResults", at = @At("HEAD"), cancellable = true)
    private void onSetResults(List<RecipeResultCollection> results, boolean resetCurrentPage, CallbackInfo ci) {
        if (BookmarkManager.isBookmarkModeActive) {
            // Force the results to only show bookmarked recipes
            this.resultCollections = getBookmarkedCollections();
            if (resetCurrentPage) {
                this.currentPage = 0;
            }
            this.refreshResultButtons();
            ci.cancel();
        }
    }

    @Inject(method = "refreshResultButtons", at = @At("HEAD"), cancellable = true)
    private void onRefreshResultButtons(CallbackInfo ci) {
        if (!BookmarkManager.isBookmarkModeActive) return;

        ci.cancel();

        if (this.resultButtons == null || this.resultButtons.isEmpty()) {
            return;
        }
        List<RecipeResultCollection> bookmarked = getBookmarkedCollections();

        // hide the recipes require 3x3
        bookmarked.removeIf(collection -> collection == null || collection.getAllRecipes().isEmpty());

        this.pageCount = (int)Math.ceil((double)bookmarked.size() / 20.0);
        if (this.pageCount < 1) this.pageCount = 1;
        if (this.pageCount <= this.currentPage) {
            this.currentPage = 0;
        }

        int startIndex = this.currentPage * 20;
        for (int i = 0; i < this.resultButtons.size(); i++) {
            AnimatedResultButton button = this.resultButtons.get(i);
            int recipeIndex = startIndex + i;
            if (recipeIndex < bookmarked.size()) {
                button.showResultCollection(bookmarked.get(recipeIndex), (RecipeBookResults) (Object) this);
                button.visible = true;
            } else {
                button.visible = false;
            }
        }

        this.hideShowPageButtons();
    }

    @Unique
    private List<RecipeResultCollection> getBookmarkedCollections() {
        MinecraftClient client = MinecraftClient.getInstance();
        List<RecipeResultCollection> filteredList = new ArrayList<>();
        if (client.player == null) return filteredList;

        ClientRecipeBook vanillaRecipeBook = client.player.getRecipeBook();

        for (RecipeBookGroup group : RecipeBookGroup.values()) {
            for (RecipeResultCollection collection : vanillaRecipeBook.getResultsForGroup(group)) {

                // Only retain the collections containing at least one bookmarked recipe
                for (RecipeEntry<?> recipe : collection.getAllRecipes()) {
                    if (BookmarkManager.BOOKMARKED_RECIPES.contains(recipe.id())) {
                        if (!filteredList.contains(collection)) {
                            filteredList.add(collection);
                        }
                        break;
                    }
                }
            }
        }
        return filteredList;
    }
}