package me.m4nst3in.m4Mundos.gui;

import me.m4nst3in.m4Mundos.M4Mundos;
import me.m4nst3in.m4Mundos.managers.WorldManager;
import me.m4nst3in.m4Mundos.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MundosGUI {

    private final M4Mundos plugin;
    private final Inventory inventory;
    private static final ConcurrentHashMap<UUID, MundosGUI> openInventories = new ConcurrentHashMap<>();

    // IDs dos itens na GUI para identificação
    private static final int INFO_SLOT = 11;
    private static final int HELIX_SLOT = 13;
    private static final int CHAOS_SLOT = 14;

    public MundosGUI(M4Mundos plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, 27, "§8§lSistema de Mundos");
        setupItems();
    }

    /**
     * Configura os itens na GUI
     */
    private void setupItems() {
        // Decoração com vidros
        ItemStack blackGlass = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build();

        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, blackGlass);
        }

        // Item de informações e tutorial
        ItemStack infoItem = new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§e§lInformações e Tutorial")
                .setLore(
                        "§7Este é o sistema de mundos do servidor!",
                        "",
                        "§6§lMundo Helix (PvE):",
                        "§7- Mundo para construção e exploração",
                        "§7- PvP desativado durante o dia",
                        "§7- PvP ativado durante a noite",
                        "§7- Mobs hostis apenas à noite",
                        "§7- Taxa normal de recursos",
                        "",
                        "§c§lMundo Chaos (PvP):",
                        "§7- Mundo para clans e PvP",
                        "§7- PvP sempre ativado",
                        "§7- Mobs causam o dobro de dano",
                        "§7- Fome aumenta 1.5x mais rápido",
                        "§7- Bônus de 50% na obtenção de recursos",
                        "§7- Limitado a 20.000 blocos em cada direção",
                        "",
                        "§e§lDica: §7Existe um delay de 5 minutos",
                        "§7para teleportar entre mundos diferentes."
                )
                .setSkullOwner("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNmYzUyMjY0ZDhhZDllNjU0ZjQxNWJlZjAxYTIzOTQ3ZWRiY2NjY2Y2NDkzNzMyODliZWE0ZDE0OTU0MWY3MCJ9fX0=")
                .build();

        // Item do mundo Helix
        ItemStack helixItem = new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§a§lMundo Helix (PvE)")
                .setLore(
                        "§7Mundo para construção e exploração.",
                        "§7PvP desativado durante o dia.",
                        "§7Taxa normal de recursos.",
                        "",
                        "§eClique para teleportar!"
                )
                .setSkullOwner("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODdmNzczMDg1OTlkNzJjZGE0OWJlOGUwMDQ2MGZjNmQyNDJmYWE0NjkxMDE2ZWIzNGViMWZiOTUzYTIzMDNmMSJ9fX0=")
                .build();

        // Item do mundo Chaos
        ItemStack chaosItem = new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§c§lMundo Chaos (PvP)")
                .setLore(
                        "§7Mundo para clans e PvP.",
                        "§7PvP sempre ativado!",
                        "§7Mobs causam o dobro de dano!",
                        "§7Fome aumenta 1.5x mais rápido!",
                        "§7Bônus de 50% na obtenção de recursos.",
                        "",
                        "§eClique para teleportar!"
                )
                .setSkullOwner("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2E3Y2ZiYWI2MjljM2FmNTM1NmYwZGVhZDlmNWM2M2UzOGM2ZWRlYTljOGZlMDc1NDExZDFkZTg0YTQ2YTg4OSJ9fX0=")
                .build();

        // Colocar itens nos slots específicos
        inventory.setItem(INFO_SLOT, infoItem);
        inventory.setItem(HELIX_SLOT, helixItem);
        inventory.setItem(CHAOS_SLOT, chaosItem);
    }

    /**
     * Abre o inventário para um jogador
     * @param player O jogador
     */
    public void openInventory(Player player) {
        player.openInventory(inventory);
        openInventories.put(player.getUniqueId(), this);
    }

    /**
     * Trata os cliques na GUI
     * @param player O jogador que clicou
     * @param slot O slot clicado
     */
    public void handleClick(Player player, int slot) {
        switch (slot) {
            case INFO_SLOT:
                // Apenas informativo, não faz nada quando clicado
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_TRADE, 1.0f, 1.0f);
                break;

            case HELIX_SLOT:
                attemptTeleport(player, WorldManager.WORLD_HELIX);
                break;

            case CHAOS_SLOT:
                attemptTeleport(player, WorldManager.WORLD_CHAOS);
                break;

            default:
                break;
        }
    }

    /**
     * Tenta teleportar o jogador para um mundo
     * @param player O jogador
     * @param worldName O nome do mundo
     */
    private void attemptTeleport(Player player, String worldName) {
        player.closeInventory();

        // Verificar se o jogador está no mesmo mundo
        if (player.getWorld().getName().equals(worldName)) {
            player.sendMessage("§cVocê já está neste mundo!");
            return;
        }

        // Verificar cooldown
        if (plugin.getCooldownManager().isOnCooldown(player)) {
            int remainingTime = plugin.getCooldownManager().getRemainingCooldown(player);
            player.sendMessage(String.format("§cVocê precisa esperar mais %d minutos e %d segundos para teleportar entre mundos!",
                    remainingTime / 60, remainingTime % 60));
            return;
        }

        // Tentar teleportar
        if (plugin.getWorldManager().teleportToRandomLocation(player, worldName)) {
            plugin.getCooldownManager().setCooldown(player);
            player.sendMessage("§aTeleportado com sucesso!");
        } else {
            player.sendMessage("§cNão foi possível encontrar um local seguro para teleportar. Tente novamente!");
        }
    }

    /**
     * Remove o jogador do mapa de inventários abertos
     * @param player O jogador
     */
    public static void removePlayer(Player player) {
        openInventories.remove(player.getUniqueId());
    }

    /**
     * Obtém uma instância da GUI para um jogador específico
     * @param player O jogador
     * @return A instância da GUI ou null
     */
    public static MundosGUI getOpenedGUI(Player player) {
        return openInventories.get(player.getUniqueId());
    }
}