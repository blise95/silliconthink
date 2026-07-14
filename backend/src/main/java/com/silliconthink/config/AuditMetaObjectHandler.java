package com.silliconthink.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.silliconthink.auth.security.AuthUser;
import com.silliconthink.common.SystemConstants;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long operator = currentUserIdOrSystem();
        strictInsertFill(metaObject, "createDate", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateDate", LocalDateTime.class, now);
        strictInsertFill(metaObject, "createBy", Long.class, operator);
        strictInsertFill(metaObject, "updateBy", Long.class, operator);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updateDate", LocalDateTime.class, LocalDateTime.now());
        strictUpdateFill(metaObject, "updateBy", Long.class, currentUserIdOrSystem());
    }

    private Long currentUserIdOrSystem() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUser authUser) {
            return authUser.getUserId();
        }
        return SystemConstants.SYSTEM_USER_ID;
    }
}
