

package com.seeyon.apps.AllPDFCatch.Utils;

import com.seeyon.ctp.common.log.CtpLogFactory;
import org.apache.commons.logging.Log;
import org.json.JSONObject;

public class JsonParseUtil {
    private static final Log LOGGER = CtpLogFactory.getLog(JsonParseUtil.class);

    public JsonParseUtil() {
    }

    public static boolean isPass(String result) {
        try {
            JSONObject rootJson = new JSONObject(result);
            if (rootJson.has("response")) {
                JSONObject responseJson = rootJson.getJSONObject("response");
                if (responseJson.has("head")) {
                    JSONObject head = responseJson.getJSONObject("head");
                    String resultCode = head.getString("resultcode");
                    if ("SUC0000".equals(resultCode)) {
                        LOGGER.info("接口业务处理成功！resultcode 为 SUC0000。");
                        return true;
                    } else {
                        String errorMsg = head.getString("resultmsg");
                        String fullError = "招商接口业务处理失败！错误码: " + resultCode + ", 错误信息: " + errorMsg;
                        LOGGER.error(fullError);
                        return false;
                    }
                } else {
                    String fullError = "招商接口返回格式异常，'response'内缺少'head'字段。响应内容: " + result;
                    LOGGER.error(fullError);
                    return false;
                }
            } else {
                String fullError = "招商接口返回异常格式，缺少'response'字段。响应内容: " + result;
                LOGGER.error(fullError);
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("报错" + e);
            return false;
        }
    }
}
