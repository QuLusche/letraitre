package fr.qulusche.letraitre;

import fr.qulusche.letraitre.commands.StartCommand;
import fr.qulusche.letraitre.commands.VoteCommand;
import fr.qulusche.letraitre.listeners.InventoryClickListener;
import fr.qulusche.letraitre.listeners.InventoryCloseListener;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public final class Main extends JavaPlugin {

	public static Main plugin;

	@Getter
	private final Random random = new Random();

	@Getter
	private GameManager gameManager;

	@Override
	public void onEnable() {
		plugin = this;

		gameManager = new GameManager(this);

		getCommand("start").setExecutor(new StartCommand());
		getCommand("vote").setExecutor(new VoteCommand());

		getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
		getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);


		getLogger().info("Plugin has been enabled!");
	}

	@Override
	public void onDisable() {
		plugin = null;

		getLogger().info("Plugin has been disabled!");
	}
}
