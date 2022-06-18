package com.denisindenbom.discordauth.units;

public class Account
{
    private final String name;
    private final String discordId;

    public Account(String name, String discordId)
    {
        this.name = name;
        this.discordId = discordId;
    }
    public String getName()
    {
        return this.name;
    }

    public String getDiscordId()
    {
        return this.discordId;
    }
}
