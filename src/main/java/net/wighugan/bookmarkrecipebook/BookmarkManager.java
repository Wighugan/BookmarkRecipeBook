package net.wighugan.bookmarkrecipebook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;

public class BookmarkManager {
    public static final Set<Identifier> BOOKMARKED_RECIPES = new HashSet<>();
    public static boolean isBookmarkModeActive = false;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File SAVE_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "bookmarked_recipes.json");
    public static void toggleBookmark(Identifier recipeId) {
        if (BOOKMARKED_RECIPES.contains(recipeId)) {
            BOOKMARKED_RECIPES.remove(recipeId);
            System.out.println("Removed bookmark: " + recipeId);
        } else {
            BOOKMARKED_RECIPES.add(recipeId);
            System.out.println("Added bookmark: " + recipeId);
        }
        save();

    }

    public static void save() {
        try (FileWriter writer = new FileWriter(SAVE_FILE)) {
            Set<String> stringIds = new HashSet<>();
            for (Identifier id : BOOKMARKED_RECIPES) {
                stringIds.add(id.toString());
            }
            GSON.toJson(stringIds, writer);
        } catch (Exception e) {
            System.err.println("Failed to save bookmarks!");
            e.printStackTrace();
        }
    }

    public static void load() {
        if (!SAVE_FILE.exists()) return;
        try (FileReader reader = new FileReader(SAVE_FILE)) {
            Type type = new TypeToken<HashSet<String>>(){}.getType();
            Set<String> stringIds = GSON.fromJson(reader, type);

            if (stringIds != null) {
                BOOKMARKED_RECIPES.clear();
                for (String id : stringIds) {
                    BOOKMARKED_RECIPES.add(Identifier.of(id)); // convert the string back into minecraft identifier
                }
                System.out.println("[Bookmark Mod] Successfully loaded " + BOOKMARKED_RECIPES.size() + " saved recipes!");
            }
        } catch (Exception e) {
            System.err.println("Failed to load bookmarks!");
            e.printStackTrace();
        }
    }
}

