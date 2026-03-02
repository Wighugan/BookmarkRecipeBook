package net.wighugan.bookmarkrecipebook.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.util.Identifier;
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

	// We add this to check if the recipe book is actually open
	@Shadow public abstract boolean isOpen();

	@Unique
	private TexturedButtonWidget bookmarkTabButton;

	@Inject(method = "reset", at = @At("TAIL"))
	private void onReset(CallbackInfo ci) {
		ButtonTextures BOOKMARK_TEXTURES = new ButtonTextures(
				Identifier.of("bookmark-recipe-book", "recipe_book/bookmark_tab"),
				Identifier.of("bookmark-recipe-book", "recipe_book/bookmark_tab_selected")
		);

		// We set the initial X and Y to 0, 0 because we will dynamically overwrite them anyway
		this.bookmarkTabButton = new TexturedButtonWidget(
				0, 0, 22, 22,
				BOOKMARK_TEXTURES,
				button -> {
					System.out.println("Bookmark tab clicked!");
				}
		);
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		// Only draw our tab if the recipe book is open and the vanilla tabs exist
		if (this.isOpen() && this.bookmarkTabButton != null && this.tabButtons != null && !this.tabButtons.isEmpty()) {

			// Grab the last vanilla tab
			RecipeGroupButtonWidget lastTab = this.tabButtons.get(this.tabButtons.size() - 1);

			// DYNAMIC UPDATE: This fixes the resizing and animation bugs!
			// We force our button to constantly follow the last tab's exact position.
			this.bookmarkTabButton.setX(lastTab.getX());
			this.bookmarkTabButton.setY(lastTab.getY() + 27);

			this.bookmarkTabButton.render(context, mouseX, mouseY, delta);
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		// Prevent clicking our invisible button when the recipe book is closed
		if (this.isOpen() && this.bookmarkTabButton != null) {
			if (this.bookmarkTabButton.mouseClicked(mouseX, mouseY, button)) {
				cir.setReturnValue(true);
			}
		}
	}
}