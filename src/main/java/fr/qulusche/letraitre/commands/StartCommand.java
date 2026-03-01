package fr.qulusche.letraitre.commands;

import fr.qulusche.letraitre.GameManager;
import fr.qulusche.letraitre.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class StartCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {

		if (command.getName().equalsIgnoreCase("start")) {

			GameManager gameManager = Main.plugin.getGameManager();
			gameManager.chooseTraitor();
			return true;
		}

		return false;
	}
}
