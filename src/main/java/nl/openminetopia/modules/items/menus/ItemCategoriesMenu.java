package nl.openminetopia.modules.items.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import nl.openminetopia.OpenMinetopia;
import nl.openminetopia.modules.items.ItemsModule;
import nl.openminetopia.utils.ChatUtils;
import nl.openminetopia.utils.menu.PaginatedMenu;
import org.bukkit.entity.Player;

public class ItemCategoriesMenu extends PaginatedMenu {
    public ItemCategoriesMenu() {
        super("<gold>Item Categorieën", 6, 45);
        gui.disableAllInteractions();
        gui.setItem(47, this.previousPageItem());
        gui.setItem(51, this.nextPageItem());

        ItemsModule module = OpenMinetopia.getModuleManager().get(ItemsModule.class);
        module.getCategoriesConfiguration().getCategories().forEach((id, category)-> {
            gui.addItem(ItemBuilder.from(category.icon().clone())
                    .name(ChatUtils.color("<gold>" + category.name()))
                    .asGuiItem(event -> {
                new ItemsMenu(category).open((Player) event.getWhoClicked());
            }));
        });
    }
}
