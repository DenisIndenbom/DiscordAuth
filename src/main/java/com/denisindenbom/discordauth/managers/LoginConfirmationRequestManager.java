package com.denisindenbom.discordauth.managers;

import com.denisindenbom.discordauth.units.Account;
import com.denisindenbom.discordauth.units.LoginConfirmationRequest;

import java.util.concurrent.*;

public class LoginConfirmationRequestManager
{
	private final ConcurrentMap<String, LoginConfirmationRequest> requests = new ConcurrentHashMap<>();
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private final long lifeTimeOfRequestSeconds;

	public LoginConfirmationRequestManager(long lifeTimeOfRequestSeconds)
	{
		this.lifeTimeOfRequestSeconds = lifeTimeOfRequestSeconds;
	}

	public void registerRequest(LoginConfirmationRequest confirmation)
	{
		if (confirmation == null || confirmation.id() == null) {
			return;
		}

		requests.put(confirmation.id(), confirmation);

		scheduler.schedule(() -> requests.remove(confirmation.id()), lifeTimeOfRequestSeconds, TimeUnit.SECONDS);
	}

	public void removeRequest(String id)
	{
		if (id != null) {
			requests.remove(id);
		}
	}

	public boolean accountHasRequest(Account account)
	{
		if (account == null) {
			return false;
		}

		return requests.values().stream().anyMatch(req -> req.account().name().equals(account.name()));
	}

	public LoginConfirmationRequest getLoginConfirmationRequest(String id)
	{
		if (id == null) {
			return null;
		}

		return requests.get(id);
	}
}
