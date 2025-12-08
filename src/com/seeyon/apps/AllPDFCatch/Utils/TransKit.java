package com.seeyon.apps.AllPDFCatch.Utils;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import org.apache.commons.logging.Log;

/**
 * 致远 OA 数据转义工具类 (ID -> Name)
 * V2.0: 支持传入 Manager 实例，防止 AppContext 空指针
 */
public class TransKit {

    private static final Log LOGGER = CtpLogFactory.getLog(TransKit.class);

    // ================== 1. 人员转义 ==================

    /**
     * [推荐] 人员转义：使用传入的 Manager 实例
     * @param memberIdObj 人员ID
     * @param orgManager 组织机构管理器实例
     */
    public static String getMemberName(Object memberIdObj, OrgManager orgManager) {
        if (memberIdObj == null || "".equals(memberIdObj.toString().trim())) return "";
        try {
            Long memberId = Long.parseLong(memberIdObj.toString());
            if (orgManager != null) {
                V3xOrgMember member = orgManager.getMemberById(memberId);
                if (member != null) return member.getName();
            }
        } catch (Exception e) {
            LOGGER.warn("TransKit 人员转义异常: " + e.getMessage());
        }
        return memberIdObj.toString();
    }

    /**
     * [备用] 人员转义：自动去 AppContext 获取 Bean
     */
    public static String getMemberName(Object memberIdObj) {
        try {
            OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
            return getMemberName(memberIdObj, orgManager);
        } catch (Exception e) {
            return memberIdObj == null ? "" : memberIdObj.toString();
        }
    }

    // ================== 2. 枚举/下拉转义 ==================

    /**
     * [推荐] 枚举转义：使用传入的 Manager 实例
     * @param enumIdObj 枚举项ID
     * @param enumManager 枚举管理器实例
     */
    public static String getEnumShowValue(Object enumIdObj, EnumManager enumManager) {
        if (enumIdObj == null || "".equals(enumIdObj.toString().trim())) return "";
        try {
            Long enumId = Long.parseLong(enumIdObj.toString());
            if (enumManager != null) {
                CtpEnumItem enumItem = enumManager.getEnumItem(enumId);
                if (enumItem != null) return enumItem.getShowvalue();
            }
        } catch (Exception e) {
            LOGGER.warn("TransKit 枚举转义异常: " + e.getMessage());
        }
        return enumIdObj.toString();
    }

    /**
     * [备用] 枚举转义：自动去 AppContext 获取 Bean (注意 Bean ID 是 enumManagerNew)
     */
    public static String getEnumShowValue(Object enumIdObj) {
        try {
            EnumManager enumManager = (EnumManager) AppContext.getBean("enumManagerNew");
            return getEnumShowValue(enumIdObj, enumManager);
        } catch (Exception e) {
            return enumIdObj == null ? "" : enumIdObj.toString();
        }
    }
}