package com.denisindenbom.discordauth.utils;

import org.bukkit.ChatColor;

public class FormatText
{
    public String format(String text)
    {
        if (text == null) return "";

        String newText = text.replace("<c0>", "" + ChatColor.BLACK).
                replace("<c1>", "" + ChatColor.DARK_BLUE).
                replace("<c2>", "" + ChatColor.DARK_GREEN).
                replace("<c3>", "" + ChatColor.DARK_AQUA).
                replace("<c4>", "" + ChatColor.DARK_RED).
                replace("<c5>", "" + ChatColor.DARK_PURPLE).
                replace("<c6>", "" + ChatColor.GOLD).
                replace("<c7>", "" + ChatColor.GRAY).
                replace("<c8>", "" + ChatColor.DARK_GRAY).
                replace("<c9>", "" + ChatColor.BLUE).
                replace("<ca>", "" + ChatColor.GREEN).
                replace("<cb>", "" + ChatColor.AQUA).
                replace("<cc>", "" + ChatColor.RED).
                replace("<cd>", "" + ChatColor.LIGHT_PURPLE).
                replace("<ce>", "" + ChatColor.YELLOW).
                replace("<cf>", "" + ChatColor.WHITE);

        return newText;
    }

    public String format(String text, String target, String replacement)
    {
        if (text == null) return "";
        return format(text).replace(target, replacement);
    }
}
