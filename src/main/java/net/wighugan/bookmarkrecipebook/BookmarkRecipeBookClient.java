package net.wighugan.bookmarkrecipebook;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class BookmarkRecipeBookClient implements ClientModInitializer {
    public static KeyBinding bookmarkKey;

    @Override
    public void onInitializeClient() {
        bookmarkKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.bookmark_recipe_book.bookmark", // The translation key
                InputUtil.Type.KEYSYM, // Type of input
                GLFW.GLFW_KEY_COMMA, // The default key
                "category.bookmark_recipe_book.main"
        ));
    }
}
