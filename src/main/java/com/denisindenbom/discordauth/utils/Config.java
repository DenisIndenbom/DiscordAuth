package com.denisindenbom.discordauth.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class Config
{
	static public @NotNull String require(@NotNull FileConfiguration config,
	                                      String path) throws IllegalArgumentException
	{
		String value = config.getString(path);
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Missing required config value: " + path);
		}
		return value;
	}
}
