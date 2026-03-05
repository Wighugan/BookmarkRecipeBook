package net.wighugan.bookmarkrecipebook.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.util.Identifier;
import net.wighugan.bookmarkrecipebook.BookmarkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RecipeBookWidget.class)
public abstract class RecipeBookWidgetMixin {
    @Shadow private List<RecipeGroupButtonWidget> tabButtons;
    @Shadow public abstract boolean isOpen();
    @Shadow public abstract void refreshResults(boolean resetCurrentPage);
    @Shadow private RecipeGroupButtonWidget currentTab;

    @Unique private TexturedButtonWidget bookmarkTabButtonUnselected;
    @Unique private TexturedButtonWidget bookmarkTabButtonSelected;

    @Inject(method = "reset", at = @At("TAIL"))
    private void onReset(CallbackInfo ci) {
        BookmarkManager.isBookmarkModeActive = false;

        if (this.tabButtons == null || this.tabButtons.isEmpty()) return;

        int tempWidth = this.tabButtons.get(0).getWidth();
        int tempHeight = this.tabButtons.get(0).getHeight();
        RecipeGroupButtonWidget lastTab = this.tabButtons.get(this.tabButtons.size() - 1);

        ButtonTextures UNSELECTED_TEXTURES = new ButtonTextures(
                Identifier.of("bookmark-recipe-book", "recipe_book/bookmark_tab"),
                Identifier.of("bookmark-recipe-book", "recipe_book/bookmark_tab")
        );

        ButtonTextures SELECTED_TEXTURES = new ButtonTextures(
                Identifier.of("bookmark-recipe-book", "recipe_book/bookmark_tab_selected"),
                Identifier.of("bookmark-recipe-book", "recipe_book/bookmark_tab_selected")
        );

        this.bookmarkTabButtonUnselected = new TexturedButtonWidget(
                0, 0, tempWidth, tempHeight, UNSELECTED_TEXTURES,
                button -> this.onBookmarkTabClicked()
        );

        this.bookmarkTabButtonSelected = new TexturedButtonWidget(
                0, 0, tempWidth, tempHeight, SELECTED_TEXTURES,
                button -> this.onBookmarkTabClicked()
        );
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.isOpen() && this.bookmarkTabButtonUnselected != null && this.tabButtons != null && !this.tabButtons.isEmpty()) {
            if (BookmarkManager.isBookmarkModeActive) {
                for (RecipeGroupButtonWidget vanillaTab : this.tabButtons) {
                    vanillaTab.setToggled(false);
                }
            }
            this.updateBookmarkTabPosition();
            TexturedButtonWidget activeTabButton = BookmarkManager.isBookmarkModeActive ? this.bookmarkTabButtonSelected : this.bookmarkTabButtonUnselected;
            activeTabButton.render(context, mouseX, mouseY, delta);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!this.isOpen()) return;
        this.updateBookmarkTabPosition();
        TexturedButtonWidget activeTabButton = BookmarkManager.isBookmarkModeActive ? this.bookmarkTabButtonSelected : this.bookmarkTabButtonUnselected;
        if (activeTabButton != null && activeTabButton.mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
            return;
        }

        if (this.tabButtons != null) {
            for (RecipeGroupButtonWidget vanillaTab : this.tabButtons) {
                if (vanillaTab.mouseClicked(mouseX, mouseY, button)) {
                    if (BookmarkManager.isBookmarkModeActive) {
                        BookmarkManager.isBookmarkModeActive = false;
                        this.refreshResults(true);
                        vanillaTab.setToggled(true);
                    }
                }
            }
        }
    }

    @Unique
    private void onBookmarkTabClicked() {
        BookmarkManager.isBookmarkModeActive = true;

        if (this.currentTab == null && this.tabButtons != null) {
            for (RecipeGroupButtonWidget tab : this.tabButtons) {
                if (tab.visible) {
                    this.currentTab = tab;
                    break;
                }
            }
        }
        this.refreshResults(true);
    }

    @Unique
    private void updateBookmarkTabPosition() {
        if (this.tabButtons == null || this.bookmarkTabButtonUnselected == null || this.bookmarkTabButtonSelected == null) return;

        RecipeGroupButtonWidget lastVisibleTab = null;

        for (int i = this.tabButtons.size() - 1; i >= 0; i--) {
            if (this.tabButtons.get(i).visible) {
                lastVisibleTab = this.tabButtons.get(i);
                break;
            }
        }

        if (lastVisibleTab != null) {
            int newX = lastVisibleTab.getX();
            int newY = lastVisibleTab.getY() + 27;
            this.bookmarkTabButtonUnselected.setX(newX);
            this.bookmarkTabButtonUnselected.setY(newY);
            this.bookmarkTabButtonSelected.setX(newX);
            this.bookmarkTabButtonSelected.setY(newY);
        }
    }
}