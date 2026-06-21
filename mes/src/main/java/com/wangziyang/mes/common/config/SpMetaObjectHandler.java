package com.wangziyang.mes.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.wangziyang.mes.system.entity.SysUser;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author SongPeng
 * @date 2019/8/27
 */
@Component
public class SpMetaObjectHandler implements MetaObjectHandler {

    Logger logger = LoggerFactory.getLogger(SpMetaObjectHandler.class);

    @Override
    public void insertFill(MetaObject metaObject) {
        logger.info("start insert fill ...");
        this.setInsertData(metaObject);
        this.setUpdateData(metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        logger.info("start update fill ...");
        this.setUpdateData(metaObject);
    }

    private void setInsertData(MetaObject metaObject) {
        String username = getCurrentUsername();
        this.setInsertFieldValByName("createUsername", username, metaObject);
        this.setInsertFieldValByName("createTime", LocalDateTime.now(), metaObject);
    }

    private void setUpdateData(MetaObject metaObject) {
        String username = getCurrentUsername();
        this.setUpdateFieldValByName("updateUsername", username, metaObject);
        this.setUpdateFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }

    private String getCurrentUsername() {
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            if (principal != null) {
                return ((SysUser) principal).getUsername();
            }
        } catch (Exception e) {
            logger.warn("获取当前用户失败", e);
        }
        return "system";
    }
}
