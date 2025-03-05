package me.m4nst3in.m4Mundos.listeners;

import me.m4nst3in.m4Mundos.M4Mundos;
import me.m4nst3in.m4Mundos.gui.MundosGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private final M4Mundos plugin;

    public PlayerListener(M4Mundos plugin) {
        this.plugin = plugin;
    }

    /**
     * Gerencia os cliques na GUI de mundos
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        InventoryView view = event.getView();

        // Verificar se é a GUI de mundos
        if (view.getTitle().equals("§8§lSistema de Mundos")) {
            event.setCancelled(true);

            MundosGUI gui = MundosGUI.getOpenedGUI(player);
            if (gui != null && event.getRawSlot() < 27) {
                gui.handleClick(player, event.getRawSlot());
            }
        }
    }

    /**
     * Gerencia o fechamento da GUI
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();

        if (event.getView().getTitle().equals("§8§lSistema de Mundos")) {
            MundosGUI.removePlayer(player);
        }
    }

    /**
     * Gerencia o PvP baseado no mundo e hora
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Verificar se é PvP (jogador atacando jogador)
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player attacker = (Player) event.getDamager();

        // Verificar se o PvP está ativo neste mundo e nesta hora
        if (!plugin.getWorldManager().isWorldPvPActive(attacker.getWorld())) {
            event.setCancelled(true);
            attacker.sendMessage("§cO PvP está desativado neste mundo agora!");
        }
    }

    /**
     * Gerencia o dano de mobs baseado no mundo
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Aplicar modificador de dano apenas para danos causados por mobs
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK ||
                event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK ||
                event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {

            double modifier = plugin.getWorldManager().getMobDamageModifier(player.getWorld());
            if (modifier != 1.0) {
                double damage = event.getDamage();
                event.setDamage(damage * modifier);
            }
        }
    }

    /**
     * Gerencia a taxa de fome baseado no mundo
     */
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        int currentFoodLevel = player.getFoodLevel();
        int newFoodLevel = event.getFoodLevel();

        // Se a fome está diminuindo (jogador ficando com mais fome)
        if (newFoodLevel < currentFoodLevel) {
            double modifier = plugin.getWorldManager().getHungerModifier(player.getWorld());
            if (modifier > 1.0) {
                // Aumenta a perda de fome pelo modificador
                int foodLoss = currentFoodLevel - newFoodLevel;
                int modifiedLoss = (int) Math.ceil(foodLoss * modifier);
                event.setFoodLevel(Math.max(0, currentFoodLevel - modifiedLoss));
            }
        }
    }

    /**
     * Informa ao jogador sobre as características do mundo quando ele muda de mundo
     */
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();

        if (worldName.equals(plugin.getWorldManager().WORLD_HELIX)) {
            player.sendMessage("§a§l» Bem-vindo ao Mundo Helix!");
            player.sendMessage("§7Este é um mundo PvE com PvP ativo apenas durante a noite.");
            player.sendMessage("§7Ideal para construção e exploração pacífica.");
        } else if (worldName.equals(plugin.getWorldManager().WORLD_CHAOS)) {
            player.sendMessage("§c§l» Bem-vindo ao Mundo Chaos!");
            player.sendMessage("§7Este é um mundo PvP com perigos aumentados!");
            player.sendMessage("§7Os mobs causam o dobro de dano e você fica com fome mais rápido.");
            player.sendMessage("§7Em compensação, há um bônus de 50% na chance de obter recursos.");
        }
    }
}