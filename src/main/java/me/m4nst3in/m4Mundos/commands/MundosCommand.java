package me.m4nst3in.m4Mundos.commands;

import me.m4nst3in.m4Mundos.M4Mundos;
import me.m4nst3in.m4Mundos.gui.MundosGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MundosCommand implements CommandExecutor {

    private final M4Mundos plugin;

    public MundosCommand(M4Mundos plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando!");
            return true;
        }

        Player player = (Player) sender;

        // Verifica permissão
        if (!player.hasPermission("m4mundos.use")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando!");
            return true;
        }

        // Abrir GUI de mundos
        new MundosGUI(plugin).openInventory(player);

        return true;
    }
}