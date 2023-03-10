package com.cagdas.awsimageupload.datastore;


import com.cagdas.awsimageupload.profile.UserProfile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class FakeUserProfileDataStore {

    private static final List<UserProfile> USER_PROFILES = new ArrayList<>();

    static {
        USER_PROFILES.add(new UserProfile(UUID.fromString("3ccaf552-a2a1-44fa-ac07-3c3dcd85e79e"),"janetjones", null));
        USER_PROFILES.add(new UserProfile(UUID.fromString("dc627a26-c3d3-4f39-8b34-4565002c2eb4"),"antoniojunior", null));
    }

    public List<UserProfile> getUserProfiles () {
        return USER_PROFILES;
    }
}
