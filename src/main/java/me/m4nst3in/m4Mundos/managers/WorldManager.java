package me.m4nst3in.m4Mundos.managers;

import com.onarandombox.MultiverseCore.MultiverseCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import me.m4nst3in.m4Mundos.M4Mundos;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class WorldManager {

    private final M4Mundos plugin;
    private final MultiverseCore mvCore;
    private final Random random = new Random();

    public static final String WORLD_HELIX = "mundo_helix";
    public static final String WORLD_CHAOS = "mundo_chaos";

    public WorldManager(M4Mundos plugin) {
        this.plugin = plugin;
        this.mvCore = plugin.getMultiverseCore();
    }

    /**
     * Configura os mundos necessários para o plugin
     */
    public void setupWorlds() {
        MVWorldManager worldManager = mvCore.getMVWorldManager();

        // Verificar/criar mundo Helix se não existir
        if (!worldManager.isMVWorld(WORLD_HELIX)) {
            plugin.getLogger().info("Criando mundo Helix...");
            worldManager.addWorld(
                    WORLD_HELIX,
                    World.Environment.NORMAL,
                    null, // Seed aleatória
                    WorldType.NORMAL,
                    true, // Gerar estruturas
                    null  // Gerador
            );

            MultiverseWorld helixWorld = worldManager.getMVWorld(WORLD_HELIX);
            helixWorld.setAlias("§aMundo Helix");

            // Usar propriedades básicas do mundo ao invés de métodos específicos do Multiverse
            World bukkitWorld = Bukkit.getWorld(WORLD_HELIX);
            if (bukkitWorld != null) {
                bukkitWorld.setSpawnLocation(0, 100, 0); // Definir spawn manualmente
            }
        }


        // Verificar/criar mundo Chaos se não existir
        if (!worldManager.isMVWorld(WORLD_CHAOS)) {
            plugin.getLogger().info("Criando mundo Chaos...");
            worldManager.addWorld(
                    WORLD_CHAOS,
                    World.Environment.NORMAL,
                    null, // Seed aleatória
                    WorldType.NORMAL,
                    true, // Gerar estruturas
                    null  // Gerador
            );

            MultiverseWorld chaosWorld = worldManager.getMVWorld(WORLD_CHAOS);
            chaosWorld.setAlias("§cMundo Chaos");
        }

        // Configurar propriedades específicas dos mundos
        configureHelixWorld();
        configureChaosWorld();
    }

    /**
     * Configura as propriedades específicas do mundo Helix
     */
    private void configureHelixWorld() {
        World world = Bukkit.getWorld(WORLD_HELIX);
        if (world != null) {
            // Configurações específicas do mundo Helix
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
            world.setGameRule(GameRule.MOB_GRIEFING, true);

            // Pré-carregar algumas chunks para teleporte
            preloadChunks(world, 10);
        }
    }

    /**
     * Configura as propriedades específicas do mundo Chaos
     */
    private void configureChaosWorld() {
        World world = Bukkit.getWorld(WORLD_CHAOS);
        if (world != null) {
            // Configurações específicas do mundo Chaos
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
            world.setGameRule(GameRule.MOB_GRIEFING, true);

            // Configurar barreira do mundo
            world.getWorldBorder().setSize(40000); // 20k em cada direção
            world.getWorldBorder().setCenter(0, 0);

            // Pré-carregar algumas chunks para teleporte
            preloadChunks(world, 10);
        }
    }

    /**
     * Pré-carrega algumas chunks para teleporte seguro
     */
    private void preloadChunks(World world, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                world.loadChunk(x, z, true);
            }
        }
    }

    /**
     * Verifica se o mundo tem PvP ativo com base no horário
     * @param world O mundo a verificar
     * @return true se PvP estiver ativo
     */
    public boolean isWorldPvPActive(World world) {
        // No mundo Chaos, PvP sempre ativo
        if (world.getName().equals(WORLD_CHAOS)) {
            return true;
        }

        // No mundo Helix, PvP ativo apenas durante a noite
        if (world.getName().equals(WORLD_HELIX)) {
            long time = world.getTime();
            return time >= 13000 && time <= 23000; // Noite no Minecraft
        }

        // Outros mundos seguem configuração padrão
        return world.getPVP();
    }

    /**
     * Teleporta um jogador para um local aleatório em um mundo
     * @param player O jogador a ser teleportado
     * @param worldName O nome do mundo destino
     * @return true se teleportou com sucesso
     */
    public boolean teleportToRandomLocation(Player player, String worldName) {
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            return false;
        }

        // Obter chunks carregadas
        List<Chunk> loadedChunks = new ArrayList<>();
        for (Chunk chunk : world.getLoadedChunks()) {
            loadedChunks.add(chunk);
        }

        if (loadedChunks.isEmpty()) {
            // Se não houver chunks carregadas, carregar uma área e teleportar para o centro
            int x = random.nextInt(500) - 250;
            int z = random.nextInt(500) - 250;
            Location location = new Location(world, x, 0, z);
            location.getChunk().load(true);
            location = findSafeLocation(world, x, z);

            if (location != null) {
                player.teleport(location);
                return true;
            }
            return false;
        } else {
            // Escolher uma chunk aleatória já carregada
            Chunk chunk = loadedChunks.get(random.nextInt(loadedChunks.size()));
            int x = chunk.getX() * 16 + random.nextInt(16);
            int z = chunk.getZ() * 16 + random.nextInt(16);
            Location location = findSafeLocation(world, x, z);

            if (location != null) {
                player.teleport(location);
                sendTeleportEffects(player, worldName);
                return true;
            }
            return false;
        }
    }

    /**
     * Encontra uma localização segura para teleporte
     * @param world O mundo
     * @param x Coordenada X
     * @param z Coordenada Z
     * @return Location segura ou null se não encontrada
     */
    private Location findSafeLocation(World world, int x, int z) {
        // First, try to find a surface location
        int seaLevel = 62; // Default sea level in most Minecraft worlds

        // Try to get the actual sea level if available
        try {
            seaLevel = world.getSeaLevel();
        } catch (Exception ignored) {}

        // Search upward from sea level first (more likely to find surface)
        for (int y = seaLevel; y < world.getMaxHeight() - 10; y++) {
            Location loc = checkLocationSafety(world, x, y, z);
            if (loc != null) return loc;
        }

        // If no surface location found, search downward from sea level as fallback
        // But avoid deep caves by limiting how far down we go
        int minY = Math.max(world.getMinHeight() + 10, seaLevel - 30);
        for (int y = seaLevel - 1; y >= minY; y--) {
            Location loc = checkLocationSafety(world, x, y, z);
            if (loc != null) {
                // Extra check: Make sure there's sky access
                if (world.getHighestBlockYAt(x, z) <= y + 3) {
                    return loc;
                }
            }
        }

        return null; // No safe location found
    }

    private Location checkLocationSafety(World world, int x, int y, int z) {
        Location location = new Location(world, x, y, z);
        Location below = new Location(world, x, y - 1, z);

        if (!location.getBlock().getType().isSolid() &&
                !location.getBlock().getType().toString().contains("LAVA") &&
                !location.getBlock().getType().toString().contains("WATER") &&
                below.getBlock().getType().isSolid()) {

            // Check for player space (two blocks)
            Location above = new Location(world, x, y + 1, z);
            if (!above.getBlock().getType().isSolid()) {
                return new Location(world, x + 0.5, y, z + 0.5); // Center in block
            }
        }

        return null;
    }

    /**
     * Envia efeitos de teleporte para o jogador
     * @param player O jogador
     * @param worldName Nome do mundo de destino
     */
    private void sendTeleportEffects(Player player, String worldName) {
        String title;
        String subtitle;

        if (worldName.equals(WORLD_HELIX)) {
            title = "§a§lMundo Helix";
            subtitle = "§7Mundo PvE - PvP apenas durante a noite";
        } else if (worldName.equals(WORLD_CHAOS)) {
            title = "§c§lMundo Chaos";
            subtitle = "§7Mundo PvP - Perigo constante!";
        } else {
            title = "§e§lTeleportado";
            subtitle = "§7Você foi teleportado!";
        }

        player.showTitle(Title.title(
                Component.text(title),
                Component.text(subtitle),
                Title.Times.times(java.time.Duration.ofMillis(500),
                        java.time.Duration.ofMillis(3000),
                        java.time.Duration.ofMillis(500))
        ));

        // Efeitos sonoros e visuais
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player.spawnParticle(Particle.PORTAL, player.getLocation(), 50, 0.5, 1, 0.5, 0.1);
    }

    /**
     * Obtém o modificador de dano para mobs em um mundo específico
     * @param world O mundo
     * @return Multiplicador de dano
     */
    public double getMobDamageModifier(World world) {
        if (world.getName().equals(WORLD_CHAOS)) {
            return 2.0; // Dobro do dano para mobs no mundo Chaos
        }
        return 1.0; // Dano normal para outros mundos
    }

    /**
     * Obtém o modificador de fome para um mundo específico
     * @param world O mundo
     * @return Multiplicador de fome
     */
    public double getHungerModifier(World world) {
        if (world.getName().equals(WORLD_CHAOS)) {
            return 1.5; // 1.5x taxa de fome no mundo Chaos
        }
        return 1.0; // Taxa normal para outros mundos
    }

    /**
     * Verifica se um mundo permite spawn de mobs hostis
     * @param world O mundo
     * @param currentTime O tempo atual do mundo
     * @return true se permitir spawn de mobs hostis
     */
    public boolean allowHostileMobSpawn(World world, long currentTime) {
        if (world.getName().equals(WORLD_HELIX)) {
            // No mundo Helix, mobs hostis só spawnam à noite
            return currentTime >= 13000 && currentTime <= 23000;
        } else if (world.getName().equals(WORLD_CHAOS)) {
            // No mundo Chaos, mais mobs hostis à noite, mas também durante o dia
            return true;
        }

        // Para outros mundos, seguir comportamento padrão
        return true;
    }

    /**
     * Obtém o modificador de taxa de drop para um mundo específico
     * @param world O mundo
     * @return Multiplicador de drops
     */
    public double getDropRateModifier(World world) {
        if (world.getName().equals(WORLD_CHAOS)) {
            return 1.5; // 50% a mais de chance de drops no mundo Chaos
        }
        return 1.0; // Taxa normal para outros mundos
    }
}