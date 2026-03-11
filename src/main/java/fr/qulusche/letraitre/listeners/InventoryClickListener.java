package fr.qulusche.letraitre.listeners;

import fr.qulusche.letraitre.Main;
import fr.qulusche.letraitre.game.VoteManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class InventoryClickListener implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!event.getView().getTitle().startsWith(VoteManager.TITLE_VOTE)) {
			return;
		}

		event.setCancelled(true);

		ItemStack clickedItem = event.getCurrentItem();

		if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) {
			return;
		}

		Player voter = (Player) event.getWhoClicked();
		SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();

		if (meta != null && meta.getOwningPlayer() != null) {
			UUID votedPlayerUUID = meta.getOwningPlayer().getUniqueId();

			Main.plugin.getGameManager().getVoteManager().vote(voter, votedPlayerUUID);

			voter.closeInventory();

			voter.sendMessage(ChatColor.GREEN+"Tu as voté pour " + meta.getOwningPlayer().getName() +ChatColor.GREEN+ " !");
		}
	}
}
