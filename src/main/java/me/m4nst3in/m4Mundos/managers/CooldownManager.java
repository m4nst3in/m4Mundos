package me.m4nst3in.m4Mundos.managers;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Long> worldTeleportCooldowns = new HashMap<>();
    private static final long COOLDOWN_TIME = 5 * 60 * 1000; // 5 minutos em milissegundos

    /**
     * Verifica se um jogador está em cooldown
     * @param player O jogador a ser verificado
     * @return true se estiver em cooldown, false caso contrário
     */
    public boolean isOnCooldown(Player player) {
        if (!worldTeleportCooldowns.containsKey(player.getUniqueId())) {
            return false;
        }

        long lastTeleportTime = worldTeleportCooldowns.get(player.getUniqueId());
        long currentTime = System.currentTimeMillis();

        return currentTime - lastTeleportTime < COOLDOWN_TIME;
    }

    /**
     * Define um cooldown para o jogador
     * @param player O jogador para aplicar o cooldown
     */
    public void setCooldown(Player player) {
        worldTeleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Pega o tempo restante do cooldown em segundos
     * @param player O jogador para verificar
     * @return Tempo restante em segundos
     */
    public int getRemainingCooldown(Player player) {
        if (!isOnCooldown(player)) {
            return 0;
        }

        long lastTeleportTime = worldTeleportCooldowns.get(player.getUniqueId());
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastTeleportTime;

        return (int) ((COOLDOWN_TIME - elapsedTime) / 1000);
    }

    /**
     * Remove o cooldown de um jogador
     * @param player O jogador para remover o cooldown
     */
    public void removeCooldown(Player player) {
        worldTeleportCooldowns.remove(player.getUniqueId());
    }
}