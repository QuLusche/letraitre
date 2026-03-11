package fr.qulusche.letraitre.listeners;

import fr.qulusche.letraitre.Main;
import fr.qulusche.letraitre.game.VoteManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;


public class InventoryCloseListener implements Listener {

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!event.getView().getTitle().startsWith(VoteManager.TITLE_VOTE)) {
			return;
		}

		Player player = (Player) event.getPlayer();

		if (!Main.plugin.getGameManager().getVoteManager().hasVoted(player)) {
			Main.plugin.getGameManager().getVoteManager().noVote(player);
		}

		player.setInvulnerable(false);
	}
}
