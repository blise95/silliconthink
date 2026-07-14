package com.silliconthink.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.silliconthink.common.ErrorCode;
import com.silliconthink.common.SystemConstants;
import com.silliconthink.exception.BizException;
import com.silliconthink.user.entity.UserDO;
import com.silliconthink.user.entity.UserOauthDO;
import com.silliconthink.user.mapper.UserMapper;
import com.silliconthink.user.mapper.UserOauthMapper;
import com.silliconthink.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserOauthMapper userOauthMapper;

    @Override
    public UserDO findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getUsername, username));
    }

    @Override
    public UserDO findById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDO createLocalUser(String username, String passwordHash, String displayName) {
        if (findByUsername(username) != null) {
            throw new BizException(ErrorCode.USERNAME_EXISTS);
        }
        UserDO user = new UserDO();
        user.setUsername(username);
        user.setPasswordHash(passwordHash);
        user.setDisplayName(displayName);
        user.setStatus(SystemConstants.USER_STATUS_ENABLED);
        userMapper.insert(user);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDO createOAuthUser(String username, String displayName) {
        String unique = username;
        int suffix = 0;
        while (findByUsername(unique) != null) {
            suffix++;
            unique = username + "_" + suffix;
        }
        UserDO user = new UserDO();
        user.setUsername(unique);
        user.setPasswordHash(null);
        user.setDisplayName(displayName);
        user.setStatus(SystemConstants.USER_STATUS_ENABLED);
        userMapper.insert(user);
        return user;
    }

    @Override
    public void assertEnabled(UserDO user) {
        if (user == null) {
            throw new BizException(ErrorCode.AUTH_FAILED);
        }
        if (user.getStatus() == null || user.getStatus() != SystemConstants.USER_STATUS_ENABLED) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
    }

    @Override
    public UserOauthDO findOauthBinding(String provider, String providerUserId) {
        return userOauthMapper.selectOne(new LambdaQueryWrapper<UserOauthDO>()
                .eq(UserOauthDO::getProvider, provider)
                .eq(UserOauthDO::getProviderUserId, providerUserId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserOauthDO bindOauth(Long userId, String provider, String providerUserId, String providerUsername) {
        UserOauthDO existing = findOauthBinding(provider, providerUserId);
        if (existing != null) {
            return existing;
        }
        UserOauthDO oauth = new UserOauthDO();
        oauth.setUserId(userId);
        oauth.setProvider(provider);
        oauth.setProviderUserId(providerUserId);
        oauth.setProviderUsername(providerUsername);
        userOauthMapper.insert(oauth);
        return oauth;
    }
}
