package me.m4nst3in.m4Mundos.listeners;

import me.m4nst3in.m4Mundos.M4Mundos;
import me.m4nst3in.m4Mundos.managers.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class WorldListener implements Listener {

    private final M4Mundos plugin;
    private final Random random = new Random();

    public WorldListener(M4Mundos plugin) {
        this.plugin = plugin;

        // Iniciar tarefa para verificar tempo dos mundos
        startTimeCheckTask();
    }

    /**
     * Gerencia o spawn de mobs hostis baseado no mundo e hora
     */
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        World world = event.getLocation().getWorld();

        // Verificar apenas para mobs hostis
        if (entity instanceof Monster ||
                entity.getType() == EntityType.SLIME ||
                entity.getType() == EntityType.MAGMA_CUBE) {

            // Verificar se o spawn de mobs hostis está permitido
            if (!plugin.getWorldManager().allowHostileMobSpawn(world, world.getTime())) {
                event.setCancelled(true);
            }

            // Aumentar spawn rate de mobs hostis à noite no mundo Chaos
            if (world.getName().equals(WorldManager.WORLD_CHAOS) &&
                    world.getTime() >= 13000 && world.getTime() <= 23000) {

                // Aumentar spawn rate adicionando outro mob se possível (chance de 15%)
                if (random.nextDouble() <= 0.15 && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
                    world.spawnEntity(event.getLocation(), event.getEntityType());
                }
            }
        }
    }

    /**
     * Gerencia os bônus de drops em diferentes mundos
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        World world = event.getBlock().getWorld();

        // Verificar se o mundo tem bônus de drop
        double dropModifier = plugin.getWorldManager().getDropRateModifier(world);
        if (dropModifier <= 1.0) {
            return; // Sem bônus neste mundo
        }

        // Chance de duplicar drops baseado no modificador
        if (random.nextDouble() <= (dropModifier - 1.0)) {
            // Clone os drops naturais do bloco
            for (ItemStack drop : event.getBlock().getDrops(player.getInventory().getItemInMainHand())) {
                world.dropItemNaturally(event.getBlock().getLocation(), drop.clone());
            }
        }
    }

    /**
     * Gerencia mudanças de ciclo de dia (importante para sistemas de PvP)
     */
    @EventHandler
    public void onTimeSkip(TimeSkipEvent event) {
        World world = event.getWorld();

        // When the time changes (especially day to night or vice versa)
        if (world.getName().equals(WorldManager.WORLD_HELIX)) {
            long newTime = (world.getTime() + event.getSkipAmount()) % 24000;

            // Check for transition to night (PvP activates)
            if (world.getTime() < 13000 && newTime >= 13000) {
                for (Player player : world.getPlayers()) {
                    player.sendMessage("§c§l» The sun has set in Helix World! PvP has been activated!");
                    player.sendMessage("§c§l» Monsters have started to spawn! Be careful!");
                }
            }

            // Check for transition to day (PvP deactivates)
            if (world.getTime() >= 13000 && newTime < 13000) {
                for (Player player : world.getPlayers()) {
                    player.sendMessage("§a§l» The sun has risen in Helix World! PvP has been deactivated!");
                    player.sendMessage("§a§l» Monsters will disappear soon.");
                }
            }
        }
    }

    /**
     * Inicia uma tarefa que verifica periodicamente o tempo dos mundos
     * para anunciar mudanças nos estados de PvP
     */
    private void startTimeCheckTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            World helixWorld = Bukkit.getWorld(WorldManager.WORLD_HELIX);
            if (helixWorld != null) {
                long time = helixWorld.getTime();

                // Anunciar quando PvP ativar (início da noite)
                if (time == 13000) {
                    for (Player player : helixWorld.getPlayers()) {
                        player.sendMessage("§c§l» A noite chegou ao Mundo Helix! O PvP foi ativado!");
                        player.sendTitle("§c§lPvP ATIVADO", "§7A noite chegou ao Mundo Helix", 10, 70, 20);
                    }
                }

                // Anunciar quando PvP desativar (início do dia)
                else if (time == 0) {
                    for (Player player : helixWorld.getPlayers()) {
                        player.sendMessage("§a§l» O dia chegou ao Mundo Helix! O PvP foi desativado!");
                        player.sendTitle("§a§lPvP DESATIVADO", "§7O dia chegou ao Mundo Helix", 10, 70, 20);
                    }
                }
            }
        }, 0L, 20L); // Verifica a cada segundo
    }
}