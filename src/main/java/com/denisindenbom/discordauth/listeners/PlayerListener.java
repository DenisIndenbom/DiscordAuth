package com.denisindenbom.discordauth.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;

import org.bukkit.event.Listener;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.entity.EntityType;

import org.bukkit.event.inventory.InventoryClickEvent;

import org.bukkit.scheduler.BukkitRunnable;

import com.denisindenbom.discordauth.DiscordAuth;

import com.denisindenbom.discordauth.utils.FormatText;
import com.denisindenbom.discordauth.utils.MessageSender;

import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener
{
    private final DiscordAuth plugin;

    private final FileConfiguration messagesConfig;
    private final long authTime;
    private final MessageSender messageSender = new MessageSender();

    public PlayerListener(DiscordAuth plugin)
    {
        this.plugin = plugin;

        this.messagesConfig = this.plugin.getMessagesConfig();
        this.authTime = this.plugin.getConfig().getLong("auth-time");

        this.notification();
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event)
    {
        // kick not authorized account
        if (!this.plugin.getAuthDB().accountExists(event.getPlayer().getName()))
        {
            event.getPlayer().kickPlayer(new FormatText().format(this.messagesConfig.getString("error.not_authorized")));
            return;
        }

        // start the timer on the kick
        this.kickTimer(event.getPlayer(), this.authTime);

        this.plugin.registerLoginConfirmationRequest(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event)
    {
        // delete player from list of authorized players
        this.plugin.getAuthManager().removeAccountByName(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerChat(@NotNull AsyncPlayerChatEvent event)
    {
        // check that player is authorized
        if (!accountIsAuth(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(@NotNull PlayerCommandPreprocessEvent event)
    {
        if (accountIsAuth(event.getPlayer())) return;

        this.messageSender.sendMessage(event.getPlayer(), this.messagesConfig.getString("error.not_logged_in"));
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event)
    {
        // check that player is authorized
        if (!accountIsAuth(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event)
    {
        // check that player is authorized
        if (accountIsAuth(event.getPlayer())) return;

        if(event.getTo() == null) return;

        // check that player move correctly
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
           event.getFrom().getBlockZ() == event.getTo().getBlockZ() &&
           event.getFrom().getBlockY() - event.getTo().getBlockY() >= 0) return;

        // canceled event
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerItemDamage(@NotNull PlayerItemDamageEvent event)
    {
        // check that player is authorized
        if (!accountIsAuth(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    void onPlayerUseInventory(@NotNull InventoryClickEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player)) return;

        // check that player is authorized
        if (!accountIsAuth((Player) event.getWhoClicked())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickupItem(@NotNull EntityPickupItemEvent event)
    {
        if (!event.getEntity().getType().equals(EntityType.PLAYER)) return;

        Player player = (Player) event.getEntity();
        // check that player is authorized
        if (!accountIsAuth(player)) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickupArrow(@NotNull PlayerPickupArrowEvent event)
    {
        // check that player is authorized
        if (!accountIsAuth(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(@NotNull PlayerDropItemEvent event)
    {
        // check that player is authorized
        if (!accountIsAuth(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByPlayer(@NotNull EntityDamageByEntityEvent event)
    {
        if (!event.getDamager().getType().equals(EntityType.PLAYER)) return;

        Player damager = (Player) event.getDamager();
        // check that the damager is authorized
        if (!accountIsAuth(damager)) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamage(@NotNull EntityDamageEvent event)
    {
        if (!event.getEntityType().equals(EntityType.PLAYER)) return;

        if (!accountIsAuth((Player) event.getEntity())) event.setCancelled(true);
    }

    private void kickTimer(Player player, long delay)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (accountIsAuth(player)) return;

                String kickMessage = new FormatText().format(messagesConfig.getString("error.timeout"));

                player.kickPlayer(kickMessage);
            }
        }.runTaskLater(this.plugin, delay * 20);
    }

    private void notification()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (Player player : plugin.getServer().getOnlinePlayers())
                {
                    String playerName = player.getName();

                    if (!plugin.getAuthManager().accountExists(playerName)) messageSender.sendMessage(player, messagesConfig.getString("login.log_in"));
                }
            }
        }.runTaskTimer(this.plugin, 10, 200);
    }

    private boolean accountIsAuth(Player player)
    {
        if (player == null) return true;

        return this.plugin.getAuthManager().accountExists(player.getName());
    }

}
