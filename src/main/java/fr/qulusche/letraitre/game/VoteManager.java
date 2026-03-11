package fr.qulusche.letraitre.game;

import fr.qulusche.letraitre.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.bukkit.GameMode.SPECTATOR;

public class VoteManager {

	public static final String TITLE_VOTE = "Vote en cours...";
	private final Logger logger;

	private final Main plugin;
	private final GameManager gameManager;

	private final HashMap<UUID, Long> voteWaiting = new HashMap<>();
	private final int VOTE_DURATION = 30;

	private final HashMap<UUID, UUID> votes = new HashMap<>();
	private int voteSend;
	private int voteDone;

	private boolean voteInProgress = false;

	public VoteManager(Main plugin, GameManager gameManager) {
		this.plugin = plugin;
		this.gameManager = gameManager;

		this.logger = plugin.getLogger();
	}

	public boolean addVoteWaiting(UUID voter) {
		if (voteWaiting.containsKey(voter)) {
			Bukkit.getPlayer(voter).sendMessage(ChatColor.RED + "Tu as déjà demandé un vote, attends qu'un second joueur vote pour que le vote commence !");
			logger.warning("Player " + Bukkit.getPlayer(voter).getName() + " is already waiting for a vote.");
			voteWaiting.put(voter, System.currentTimeMillis());
			return false;
		}

		if (voteInProgress) {
			Bukkit.getPlayer(voter).sendMessage(ChatColor.RED + "Un vote est déjà en cours, tu ne peux pas en demander un autre pour le moment !");
			logger.warning("Player " + Bukkit.getPlayer(voter).getName() + " tried to start a new vote while one is already in progress.");
			return false;
		}

		voteWaiting.put(voter, System.currentTimeMillis());
		logger.info("Vote waiting for player: " + voter);

		if (checkVoteWaiting()) {
			logger.info("Vote triggered with " + voteWaiting.size() + " votes waiting.");
			for (UUID uuid : voteWaiting.keySet())
				logger.info(" - " + Bukkit.getPlayer(uuid).getName() + " voted at " + (voteWaiting.get(uuid) - System.currentTimeMillis()) + "ms ago.");

			startVote();
		}
		else {
			Bukkit.getPlayer(voter).sendMessage(ChatColor.GRAY + "Vote demandé, en attente d'un second vote pour commencer le vote !");
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getUniqueId().equals(voter)) continue;
				player.sendMessage(ChatColor.GRAY + "Un vote a été demandé par " + ChatColor.YELLOW + Bukkit.getPlayer(voter).getName() + ChatColor.GRAY + ". Il manque un second vote pour commencer le vote, vous avez 30 secondes !");
			}
		}

		return true;
	}

	public boolean checkVoteWaiting() {
		long now = System.currentTimeMillis();

		for (UUID voter : new ArrayList<>(voteWaiting.keySet())) {
			long voteTime = voteWaiting.get(voter);
			if (now - voteTime > (VOTE_DURATION * 1000)) {
				logger.info("Removing expired vote waiting for player: " + voter + " (voted " + (now - voteTime) + "ms ago)");
				voteWaiting.remove(voter);
			}
		}

		return voteWaiting.size() >= 2;
	}

	private void startVote() {
		voteSend = 0;
		voteInProgress = true;

		List<Player> voters = (List<Player>) Bukkit.getOnlinePlayers().stream()
				.filter(p -> p.getGameMode() != SPECTATOR)
				.toList();

		voteSend = voters.size();

		for (Player player : voters) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				openVoteMenu(player);
			}, 20 * 10);
		}
	}

	public static void openVoteMenu(Player player) {

		player.setInvulnerable(true);

		Inventory voteInventory = Bukkit.createInventory(null, 9,TITLE_VOTE);

		int i = 0;
		for (Player target : Bukkit.getOnlinePlayers()) {
			if (target.getGameMode() == SPECTATOR) continue;
			if (target.getUniqueId().equals(player.getUniqueId())) continue;

			ItemStack head = createPlayerHead(target);
			voteInventory.setItem(i++, head);
		}

		player.openInventory(voteInventory);
	}

	private static ItemStack createPlayerHead(Player target) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) head.getItemMeta();

		if (meta != null) {
			meta.setOwningPlayer(target);
			meta.setDisplayName("§e" + target.getName());

			List<String> lore = new ArrayList<>();
			lore.add("§7Cliquez pour voter");
			meta.setLore(lore);

			head.setItemMeta(meta);
		}

		return head;
	}

	public void endVote() {
		voteInProgress = false;

		HashMap<UUID, Integer> voteCounts = new HashMap<>();
		for (UUID voted : votes.values()) {
			if (voted == null) continue;
			voteCounts.put(voted, voteCounts.getOrDefault(voted, 0) + 1);
		}

		UUID mostVoted = null;
		int maxVotes = 0;
		for (UUID voted : voteCounts.keySet()) {
			int count = voteCounts.get(voted);
			if (count > maxVotes) {
				maxVotes = count;
				mostVoted = voted;
			}
		}
		logger.info("Vote ended. Most voted player: " + (mostVoted != null ? mostVoted : "None") + " with " + maxVotes + " votes.");

		if (mostVoted != null) {
			Player votedPlayer = Bukkit.getPlayer(mostVoted);
			if (votedPlayer == null) {
				logger.warning("Most voted player with UUID " + mostVoted + " is not online.");
				return;
			}

			if (votedPlayer.getUniqueId().equals(gameManager.getTraitor())) {
				gameManager.traitorDiscovered();
			} else {
				gameManager.traitorSaved();
			}
		}

		clearVote();
	}

	public void vote(Player voter, UUID votedPlayerUUID) {
		if (hasVoted(voter)) return;

		voter.playSound(voter.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);

		voteDone++;
		votes.put(voter.getUniqueId(), votedPlayerUUID);
		logger.info("Player " + voter.getName() + " voted for player with UUID " + votedPlayerUUID);

		checkEndVote();
	}

	public void noVote(Player player) {
		voteDone++;
		votes.put(player.getUniqueId(), null);
		logger.info(player.getUniqueId()+" voted no one.");

		checkEndVote();
	}

	private void checkEndVote() {
		if (voteDone >= voteSend) {
			logger.info("All votes done (" + voteDone + "/" + voteSend + "). Ending vote.");
			endVote();
		} else {
			logger.info("Votes in progress: " + voteDone + "/" + voteSend);
		}
	}

	public boolean hasVoted(Player player) {
		return votes.containsKey(player.getUniqueId());
	}

	public void clearVote() {
		voteSend = 0;
		voteDone = 0;
		voteWaiting.clear();
		votes.clear();
		logger.info("Vote has been cleared.");
	}
}
