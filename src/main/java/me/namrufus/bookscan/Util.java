package me.namrufus.bookscan;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class Util {
    public static List<String> getAllLinesInBook(BookMeta bookMeta) {
        List<String> result = new ArrayList<String>();

        for (String page : bookMeta.getPages()) {
            for (String line : page.split("\n")) {
                result.add(line);
            }
        }

        return result;
    }

    public static int getItemCountInInventory(Player player, Material material, byte data) {
        int count = 0;

        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null) continue;
            if (itemStack.getType() == material) {
                if (data != -1 || itemStack.getData().getData() == data) {
                    count += itemStack.getAmount();
                }
            }
        }

        return count;
    }
}
