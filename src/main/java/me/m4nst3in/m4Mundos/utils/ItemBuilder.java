package me.m4nst3in.m4Mundos.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();
    }

    /**
     * Define o nome do item
     * @param name Nome do item
     * @return ItemBuilder
     */
    public ItemBuilder setName(String name) {
        meta.setDisplayName(name);
        return this;
    }

    /**
     * Define a lore do item
     * @param lore Lore do item
     * @return ItemBuilder
     */
    public ItemBuilder setLore(String... lore) {
        meta.setLore(Arrays.asList(lore));
        return this;
    }

    /**
     * Define a lore do item usando uma lista
     * @param lore Lista com a lore
     * @return ItemBuilder
     */
    public ItemBuilder setLore(List<String> lore) {
        meta.setLore(lore);
        return this;
    }

    /**
     * Adiciona uma linha à lore
     * @param line Linha a ser adicionada
     * @return ItemBuilder
     */
    public ItemBuilder addLoreLine(String line) {
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(line);
        meta.setLore(lore);
        return this;
    }

    /**
     * Adiciona um encantamento ao item
     * @param enchant Encantamento
     * @param level Nível do encantamento
     * @return ItemBuilder
     */
    public ItemBuilder addEnchant(Enchantment enchant, int level) {
        meta.addEnchant(enchant, level, true);
        return this;
    }

    /**
     * Remove um encantamento do item
     * @param enchant Encantamento a ser removido
     * @return ItemBuilder
     */
    public ItemBuilder removeEnchant(Enchantment enchant) {
        meta.removeEnchant(enchant);
        return this;
    }

    /**
     * Adiciona uma flag ao item
     * @param flag Flag a ser adicionada
     * @return ItemBuilder
     */
    public ItemBuilder addItemFlag(ItemFlag flag) {
        meta.addItemFlags(flag);
        return this;
    }

    /**
     * Remove uma flag do item
     * @param flag Flag a ser removida
     * @return ItemBuilder
     */
    public ItemBuilder removeItemFlag(ItemFlag flag) {
        meta.removeItemFlags(flag);
        return this;
    }

    /**
     * Define se o item é indestrutível
     * @param unbreakable true para indestrutível
     * @return ItemBuilder
     */
    public ItemBuilder setUnbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }

    /**
     * Define um valor persistente no item
     * @param key Chave
     * @param value Valor
     * @return ItemBuilder
     */
    public ItemBuilder setPersistentData(String key, String value) {
        NamespacedKey namespacedKey = new NamespacedKey("m4mundos", key);
        meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);
        return this;
    }

    /**
     * Define a textura de uma cabeça de jogador
     * @param value Base64 texture value
     * @return ItemBuilder
     */
    public ItemBuilder setSkullOwner(String value) {
        if (item.getType() == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) meta;

            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", value));
            skullMeta.setPlayerProfile(profile);
        }
        return this;
    }

    /**
     * Constrói o item com as configurações aplicadas
     * @return ItemStack configurado
     */
    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}