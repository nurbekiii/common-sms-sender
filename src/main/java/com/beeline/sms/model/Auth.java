package com.beeline.sms.model;

/**
 * @author NIsaev on 20.05.2019
 */
public class Auth {
    private String login;
    private String password;
    private String alias;

    public Auth(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public Auth(String login, String password, String alias) {
        this.login = login;
        this.password = password;
        this.alias = alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Auth auth = (Auth) o;

        if (!login.equals(auth.login)) return false;
        return password.equals(auth.password);

    }

    @Override
    public int hashCode() {
        int result = login.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
