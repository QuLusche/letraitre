package fr.qulusche.letraitre;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.bukkit.GameMode.SPECTATOR;

public class GameManager {

	private final Logger logger;

	private final Main plugin;
	@Getter
	private final VoteManager voteManager;

	@Getter
	private UUID traitor;

	public GameManager(Main plugin) {
		this.plugin = plugin;
		this.voteManager = new VoteManager(plugin, this);
		this.logger = Logger.getLogger("GameManager");
	}

	public void chooseTraitor() {
		List<Player> players = new ArrayList<>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				players.add(player);
			}
		}
		if (players.size() < 2) {
			traitor = null;
			throw  new IllegalStateException("Not enough players to choose a traitor. At least 2 players are required.");
		}

		Player chosen = players.get(plugin.getRandom().nextInt(players.size()));
		chosen.sendTitle(ChatColor.GRAY+"Attention...", ChatColor.RED+"...tu es le traitre !", 10, 70, 20);
		traitor = chosen.getUniqueId();

		for (Player player : players) {
			if (!player.getUniqueId().equals(traitor)) {
				player.sendTitle(ChatColor.GRAY+"Attention...", ChatColor.GRAY+"...un traitre rode !", 10, 70, 20);
			}
		}

		voteManager.clearVote();

		logger.info("Traitor chosen: " + chosen.getName());
	}

	public void traitorDiscovered() {
		Player traitorPlayer = Bukkit.getPlayer(traitor);

		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getGameMode() == SPECTATOR) continue;

			if (!player.getUniqueId().equals(traitor)) {
				player.sendMessage(ChatColor.GREEN + "Le traitre " + ChatColor.RED + traitorPlayer.getName() + ChatColor.GREEN + " a été découvert !");

				player.getInventory().addItem(new ItemStack(Material.DIAMOND, 5));
				player.sendMessage(ChatColor.GREEN+"Tu as reçu 5 diamants en récompense !");

				logger.info("Rewarded player: " + player.getName());
			}
			else {
				player.sendMessage(ChatColor.RED + "Tu as été découvert comme traitre !");
			}
		}

		traitor = null;

		chooseTraitor();
	}

	private static final int PENALTY_PV = 2;

	public void traitorSaved() {

		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getGameMode() == SPECTATOR) continue;

			if (!player.getUniqueId().equals(traitor)) {
				player.sendMessage(ChatColor.RED + "Vous n'avez pas réussi à découvrir le traitre !");

				if (player.getMaxHealth() - PENALTY_PV <= 0) {
					for (ItemStack item: player.getInventory().getContents()) {
						if (item == null) continue;
						player.getWorld().dropItem(player.getLocation(), item);
					}
					player.getInventory().clear();
					player.setGameMode(GameMode.SPECTATOR);
					logger.info("Eliminated player: " + player.getName() + " (max health would have dropped to " + (player.getMaxHealth() - PENALTY_PV) + ")");
					player.sendMessage(ChatColor.RED + "Tu as perdu ton dernier 1 coeur de vie en pénalité et tu es éliminé du jeu !");
				} else {
					player.setMaxHealth(player.getMaxHealth() - 2);
					player.sendMessage(ChatColor.RED + "Tu as perdu 1 coeur de vie en pénalité !");
				}

				logger.info("Penalized player: " + player.getName() + " (new max health: " + player.getMaxHealth() + ")");
			}
			else {
				player.sendMessage(ChatColor.GREEN + "Tu n'as pas été découvert comme traitre !");

				player.getInventory().addItem(new ItemStack(Material.DIAMOND, 3));
				player.getInventory().addItem(getMoobEgg());

				player.sendMessage(ChatColor.GREEN + "Tu as reçu 5 diamants et un oeuf de mob en récompense !");
				logger.info("Rewarded traitor: " + player.getName());
			}
		}
	}

	private ItemStack getMoobEgg() {
		List<ItemStack> items = List.of(
			new ItemStack(Material.ZOMBIE_SPAWN_EGG),
			new ItemStack(Material.SKELETON_SPAWN_EGG),
			new ItemStack(Material.CREEPER_SPAWN_EGG),
			new ItemStack(Material.SPIDER_SPAWN_EGG),
			new ItemStack(Material.CAVE_SPIDER_SPAWN_EGG),
			new ItemStack(Material.ENDERMAN_SPAWN_EGG),
			new ItemStack(Material.BLAZE_SPAWN_EGG),
			new ItemStack(Material.WITCH_SPAWN_EGG),
			new ItemStack(Material.SLIME_SPAWN_EGG),
			new ItemStack(Material.GHAST_SPAWN_EGG),
			new ItemStack(Material.MAGMA_CUBE_SPAWN_EGG),
			new ItemStack(Material.ENDERMITE_SPAWN_EGG),
			new ItemStack(Material.VINDICATOR_SPAWN_EGG),
			new ItemStack(Material.RAVAGER_SPAWN_EGG)
		);
		return items.get(plugin.getRandom().nextInt(items.size()));
	}
}
