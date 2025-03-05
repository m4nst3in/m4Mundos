package me.m4nst3in.m4Mundos;

import com.onarandombox.MultiverseCore.MultiverseCore;
import me.m4nst3in.m4Mundos.commands.MundosCommand;
import me.m4nst3in.m4Mundos.listeners.PlayerListener;
import me.m4nst3in.m4Mundos.listeners.WorldListener;
import me.m4nst3in.m4Mundos.managers.CooldownManager;
import me.m4nst3in.m4Mundos.managers.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class M4Mundos extends JavaPlugin {

    private MultiverseCore multiverseCore;
    private WorldManager worldManager;
    private CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        // Verificar e carregar dependência do MultiverseCore
        if (!setupMultiverseCore()) {
            getLogger().severe("MultiverseCore não encontrado! Desativando plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Inicializar gerenciadores
        this.cooldownManager = new CooldownManager();
        this.worldManager = new WorldManager(this);

        // Registrar comandos
        getCommand("mundos").setExecutor(new MundosCommand(this));
        getCommand("mundo").setExecutor(new MundosCommand(this));
        getCommand("world").setExecutor(new MundosCommand(this));
        getCommand("worlds").setExecutor(new MundosCommand(this));

        // Registrar listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);

        // Inicializar mundos
        worldManager.setupWorlds();

        getLogger().info("§aPlugin M4Mundos foi ativado com sucesso!");
    }

    @Override
    public void onDisable() {
        getLogger().info("§cPlugin M4Mundos foi desativado!");
    }

    private boolean setupMultiverseCore() {
        Plugin plugin = getServer().getPluginManager().getPlugin("Multiverse-Core");

        if (plugin != null && plugin instanceof MultiverseCore) {
            multiverseCore = (MultiverseCore) plugin;
            return true;
        }

        return false;
    }

    public MultiverseCore getMultiverseCore() {
        return multiverseCore;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
}