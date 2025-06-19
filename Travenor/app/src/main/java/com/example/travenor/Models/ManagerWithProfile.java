package com.example.travenor.Models;


import android.provider.ContactsContract;

public class ManagerWithProfile {
    private ContactsContract.Profile profiles;

    public ContactsContract.Profile getProfile() {
        return profiles;
    }

    public void setProfile(ContactsContract.Profile profile) {
        this.profiles = profile;
    }
}