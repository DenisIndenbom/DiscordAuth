package com.denisindenbom.discordauth.managers;

import com.denisindenbom.discordauth.units.Account;
import com.denisindenbom.discordauth.units.LoginConfirmationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LoginConfirmationRequestManager
{
    private final List<LoginConfirmationRequest> requests = new ArrayList<>();

    private final long lifeTimeOfRequest;

    public LoginConfirmationRequestManager(long lifeTimeOfRequest)
    {this.lifeTimeOfRequest = lifeTimeOfRequest;}

    public void registerRequest(LoginConfirmationRequest confirmation)
    {
        this.requests.add(confirmation);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run()
            {
                removeRequest(confirmation.getId());
            }
        }, lifeTimeOfRequest * 1000);
    }

    public void removeRequest(String id)
    {
        for (LoginConfirmationRequest loginConfirmationRequest : this.requests)
        {
            if (loginConfirmationRequest.getId().equals(id))
            {
                this.requests.remove(loginConfirmationRequest);
                break;
            }
        }
    }

    public boolean accountHasRequest(Account account)
    {
        for (LoginConfirmationRequest request : requests)
        {
            if (request.getAccount().getName().equals(account.getName()))
                return true;
        }

        return false;
    }

    public LoginConfirmationRequest getLoginConfirmationRequest(String id)
    {
        LoginConfirmationRequest lc = null;

        for (LoginConfirmationRequest loginConfirmationRequest : this.requests)
        {
            if (loginConfirmationRequest.getId().equals(id))
            {
                lc = loginConfirmationRequest;
                break;
            }
        }

        return lc;
    }

}
