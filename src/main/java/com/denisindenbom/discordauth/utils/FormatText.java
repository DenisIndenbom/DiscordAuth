package com.denisindenbom.discordauth.utils;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatText
{
	private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

	private static final Map<String, ChatColor> COLOR_TAGS = new HashMap<>();

	static {
		COLOR_TAGS.put("<c0>", ChatColor.BLACK);
		COLOR_TAGS.put("<c1>", ChatColor.DARK_BLUE);
		COLOR_TAGS.put("<c2>", ChatColor.DARK_GREEN);
		COLOR_TAGS.put("<c3>", ChatColor.DARK_AQUA);
		COLOR_TAGS.put("<c4>", ChatColor.DARK_RED);
		COLOR_TAGS.put("<c5>", ChatColor.DARK_PURPLE);
		COLOR_TAGS.put("<c6>", ChatColor.GOLD);
		COLOR_TAGS.put("<c7>", ChatColor.GRAY);
		COLOR_TAGS.put("<c8>", ChatColor.DARK_GRAY);
		COLOR_TAGS.put("<c9>", ChatColor.BLUE);
		COLOR_TAGS.put("<ca>", ChatColor.GREEN);
		COLOR_TAGS.put("<cb>", ChatColor.AQUA);
		COLOR_TAGS.put("<cc>", ChatColor.RED);
		COLOR_TAGS.put("<cd>", ChatColor.LIGHT_PURPLE);
		COLOR_TAGS.put("<ce>", ChatColor.YELLOW);
		COLOR_TAGS.put("<cf>", ChatColor.WHITE);
	}

	public static @NotNull String format(String text)
	{
		if (text == null || text.isEmpty()) {
			return "";
		}

		// Replace legacy color tags
		for (Map.Entry<String, ChatColor> entry : COLOR_TAGS.entrySet()) {
			text = text.replace(entry.getKey(), entry.getValue().toString());
		}

		// Replace hex colors
		Matcher matcher = HEX_PATTERN.matcher(text);
		StringBuilder buffer = new StringBuilder();

		while (matcher.find()) {
			try {
				String hex = "#" + matcher.group(1);
				String color = net.md_5.bungee.api.ChatColor.of(hex).toString();
				matcher.appendReplacement(buffer, Matcher.quoteReplacement(color));
			}
			catch (IllegalArgumentException e) {
				// Ignore invalid hex values
			}
		}

		matcher.appendTail(buffer);
		return buffer.toString();
	}

	public static @NotNull String format(String text, String target, String replacement)
	{
		if (text == null) {
			return "";
		}
		return format(text).replace(target, replacement);
	}
}
