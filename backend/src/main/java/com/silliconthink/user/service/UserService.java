package com.silliconthink.user.service;

import com.silliconthink.user.entity.UserDO;
import com.silliconthink.user.entity.UserOauthDO;

public interface UserService {

    UserDO findByUsername(String username);

    UserDO findById(Long id);

    UserDO createLocalUser(String username, String passwordHash, String displayName);

    UserDO createOAuthUser(String username, String displayName);

    void assertEnabled(UserDO user);

    UserOauthDO findOauthBinding(String provider, String providerUserId);

    UserOauthDO bindOauth(Long userId, String provider, String providerUserId, String providerUsername);
}
