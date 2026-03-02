package net.wighugan.bookmarkrecipebook;

import net.minecraft.util.Identifier;
import java.util.HashSet;
import java.util.Set;

public class BookmarkManager {
    public static final Set<Identifier> BOOKMARKED_RECIPES = new HashSet<>();
    public static boolean isBookmarkModeActive = false;
    public static void toggleBookmark(Identifier recipeId) {
        if (BOOKMARKED_RECIPES.contains(recipeId)) {
            BOOKMARKED_RECIPES.remove(recipeId);
            System.out.println("Removed bookmark: " + recipeId);
        } else {
            BOOKMARKED_RECIPES.add(recipeId);
            System.out.println("Added bookmark: " + recipeId);
        }
    }
}