package com.hujiang.project.zhgd.jsms.api.template;

import cn.jiguang.common.resp.BaseResult;
import com.google.gson.annotations.Expose;

public class SendTempSMSResult extends BaseResult {

    @Expose int temp_id;

    public int getTempId() {
        return temp_id;
    }
}
