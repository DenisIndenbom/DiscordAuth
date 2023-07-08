package com.denisindenbom.discordauth.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

public class FormatText
{
    private final Pattern hexPattern = Pattern.compile("\\<#.*?\\>");

    public String format(String text)
    {
        if (text == null) return "";

        text = text.replace("<c0>", String.valueOf(ChatColor.BLACK)).
                                    replace("<c1>", String.valueOf(ChatColor.DARK_BLUE)).
                                    replace("<c2>", String.valueOf(ChatColor.DARK_GREEN)).
                                    replace("<c3>", String.valueOf(ChatColor.DARK_AQUA)).
                                    replace("<c4>", String.valueOf(ChatColor.DARK_RED)).
                                    replace("<c5>", String.valueOf(ChatColor.DARK_PURPLE)).
                                    replace("<c6>", String.valueOf(ChatColor.GOLD)).
                                    replace("<c7>", String.valueOf(ChatColor.GRAY)).
                                    replace("<c8>", String.valueOf(ChatColor.DARK_GRAY)).
                                    replace("<c9>", String.valueOf(ChatColor.BLUE)).
                                    replace("<ca>", String.valueOf(ChatColor.GREEN)).
                                    replace("<cb>", String.valueOf(ChatColor.AQUA)).
                                    replace("<cc>", String.valueOf(ChatColor.RED)).
                                    replace("<cd>", String.valueOf(ChatColor.LIGHT_PURPLE)).
                                    replace("<ce>", String.valueOf(ChatColor.YELLOW)).
                                    replace("<cf>", String.valueOf(ChatColor.WHITE));

        Matcher matcher = this.hexPattern.matcher(text);

        while (matcher.find())
        {
            String hex = matcher.group();
            String replacement = String.valueOf(net.md_5.bungee.api.ChatColor.of(hex.replace("<", "").replace(">", "")));

            text = text.replace(hex, replacement);
        }

        return text;
    }

    public String format(String text, String target, String replacement)
    {
        if (text == null) return "";
        return format(text).replace(target, replacement);
    }
}
