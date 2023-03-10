package com.cagdas.awsimageupload.profile;

import com.cagdas.awsimageupload.bucket.BucketName;
import com.cagdas.awsimageupload.filestore.FileStore;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.apache.http.entity.ContentType.*;


@Service
public class UserProfileService {

    private final UserProfileDataAccessService userProfileDataAccessService;
    private final FileStore fileStore;

    @Autowired
    public UserProfileService(UserProfileDataAccessService userProfileDataAccessService,
                              FileStore fileStore) {
        this.userProfileDataAccessService = userProfileDataAccessService;
        this.fileStore = fileStore;
    }

    List<UserProfile> getUserProfiles(){
        return  userProfileDataAccessService.getUserProfiles();
    }

    public void uploadUserProfileImage(UUID userProfileId, MultipartFile file) {
        // 1. Check image is not empty
        isFileEmpty(file);
        // 2. ıf file is an Image
        isImage(file);
        // 3. User exists in our database
        UserProfile user = getUserProfileOrThrow(userProfileId);
        // 4. Grab some metadata from file if any

        Map<String, String> metadata = extractMetadata(file);
        // 5. Store the image in s3 and databaase with s3 image link

        String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(), user.getUserProfileId());
        String filename = String.format("%s-%s", file.getOriginalFilename(), UUID.randomUUID());

        try {
            fileStore.save(path,filename, Optional.of(metadata), file.getInputStream());
            user.setUserProfileImageLink(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] downloadUserProfileImage(UUID userProfileId) {
        UserProfile user = getUserProfileOrThrow(userProfileId);
        String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(),
                user.getUserProfileId());
        return user.getUserProfileImageLink()
                .map(key -> fileStore.download(path,key))
                .orElse(new byte[0]);
    }

    private static void isImage(MultipartFile file) {
        if(!Arrays.asList(IMAGE_JPEG.getMimeType(),IMAGE_PNG.getMimeType(),IMAGE_GIF.getMimeType()).contains(file.getContentType())){
            throw new IllegalStateException("File must be an image [" + file.getContentType() + "]");
        }
    }

    private static Map<String, String> extractMetadata(MultipartFile file) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
        return metadata;
    }

    private UserProfile getUserProfileOrThrow(UUID userProfileId) {
        return userProfileDataAccessService
                .getUserProfiles()
                .stream()
                .filter(userProfile -> userProfile.getUserProfileId().equals(userProfileId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("User profile %s not found", userProfileId)));
    }

    private static void isFileEmpty(MultipartFile file) {
        if (file.isEmpty()){
            throw new IllegalStateException("Can not upload empty file [ " + file.getSize() + "]");
        }
    }


}
