package com.chongdianleme.job;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class EmailAuthenticator extends Authenticator
{
    private String username;
    private String userpsd;
    public EmailAuthenticator(String username, String userpsd)
    {
        this.username = username;
        this.userpsd = userpsd;
    }
    public String getUsername()
    {
        return username;
    }
    public void setUsername(String username)
    {
        this.username = username;
    }
    public String getUserpsd()
    {
        return userpsd;
    }
    public void setUserpsd(String userpsd)
    {
        this.userpsd = userpsd;
    }
    @Override
    protected PasswordAuthentication getPasswordAuthentication()
    {
        return super.getPasswordAuthentication();
    }
}