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

	@Shadow public abstract boolean isOpen();

	@Unique
	private TexturedButtonWidget bookmarkTabButton;

	@Inject(method = "reset", at = @At("TAIL"))
	private void onReset(CallbackInfo ci) {
		if (this.tabButtons == null || this.tabButtons.isEmpty()) return;

		RecipeGroupButtonWidget lastTab = this.tabButtons.get(this.tabButtons.size() - 1);

		ButtonTextures BOOKMARK_TEXTURES = new ButtonTextures(
				Identifier.of("bookmark-recipe-book", "recipe_book/bookmark_tab"),
				Identifier.of("bookmark-recipe-book", "recipe_book/bookmark_tab_selected")
		);

		this.bookmarkTabButton = new TexturedButtonWidget(
				0, 0, lastTab.getWidth(), lastTab.getHeight(),
				BOOKMARK_TEXTURES,
				button -> {
					System.out.println("Bookmark tab clicked!");
				}
		);
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (this.isOpen() && this.bookmarkTabButton != null && this.tabButtons != null && !this.tabButtons.isEmpty()) {

			RecipeGroupButtonWidget lastTab = this.tabButtons.get(this.tabButtons.size() - 1);

			this.bookmarkTabButton.setX(lastTab.getX());
			this.bookmarkTabButton.setY(lastTab.getY() + 27);

			this.bookmarkTabButton.render(context, mouseX, mouseY, delta);
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (this.isOpen() && this.bookmarkTabButton != null) {
			if (this.bookmarkTabButton.mouseClicked(mouseX, mouseY, button)) {
				cir.setReturnValue(true);
			}
		}
	}
}