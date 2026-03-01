package fr.qulusche.letraitre.commands;

import fr.qulusche.letraitre.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VoteCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {

		if (command.getName().equalsIgnoreCase("vote")) {
			if (!(commandSender instanceof Player sender)) {
				commandSender.sendMessage(ChatColor.RED+"Only players can vote.");
				return true;
			}

			if (Main.plugin.getGameManager().getTraitor() == null) {
				sender.sendMessage(ChatColor.GRAY+"La partie n'a pas encore commencé, tu ne peux pas voter.");
				return true;
			}

			Main.plugin.getGameManager().getVoteManager().addVoteWaiting(sender.getUniqueId());

			return true;
		}

		return false;
	}
}
