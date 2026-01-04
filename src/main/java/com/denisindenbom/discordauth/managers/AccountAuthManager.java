package com.denisindenbom.discordauth.managers;

import com.denisindenbom.discordauth.units.Account;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AccountAuthManager
{
	private final ConcurrentMap<String, Account> authenticatedAccounts = new ConcurrentHashMap<>();

	public AccountAuthManager() {}

	public void addAccount(Account account)
	{
		if (account != null && account.name() != null) {
			authenticatedAccounts.put(account.name(), account);
		}
	}

	public void removeAccountByName(String name)
	{
		if (name != null) {
			authenticatedAccounts.remove(name);
		}
	}

	public boolean accountExists(String name)
	{
		return name != null && authenticatedAccounts.containsKey(name);
	}
}
