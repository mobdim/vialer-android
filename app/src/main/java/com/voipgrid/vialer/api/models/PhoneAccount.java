package com.voipgrid.vialer.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by eltjo on 03/08/15.
 */
public class PhoneAccount implements Destination {

    @SerializedName("account_id")
    private String accountId;

    private String password;

    @SerializedName("internal_number")
    private String number;

    private String id;

    private String description;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getNumber() {
        return number;
    }

    @Override
    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return getNumber() + " / " + getDescription();
    }
}