package com.wangziyang.mes.system.request;

import com.wangziyang.mes.common.BasePageReq;

/**
 * <p>
 * 生产班组分页查询参数
 * </p>
 *
 * @author SongPeng
 * @since 2020-03-03
 */
public class WorkTeamPageReq extends BasePageReq {

    private String name;

    private String code;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
