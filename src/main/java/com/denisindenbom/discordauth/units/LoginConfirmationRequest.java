package com.denisindenbom.discordauth.units;

public class LoginConfirmationRequest
{
    private final String id;
    private final Account account;

    public LoginConfirmationRequest(String id, Account account)
    {
        this.id = id;
        this.account = account;
    }

    public String getId()
    {return this.id;}

    public Account getAccount()
    {return this.account;}
}
