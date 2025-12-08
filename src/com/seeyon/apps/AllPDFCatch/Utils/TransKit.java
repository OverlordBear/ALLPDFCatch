

package com.seeyon.apps.AllPDFCatch.Utils;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import org.apache.commons.logging.Log;

public class TransKit {
    private static final Log LOGGER = CtpLogFactory.getLog(TransKit.class);

    public TransKit() {
    }

    public static String getMemberName(Object memberIdObj, OrgManager orgManager) {
        if (memberIdObj != null && !"".equals(memberIdObj.toString().trim())) {
            try {
                Long memberId = Long.parseLong(memberIdObj.toString());
                if (orgManager != null) {
                    V3xOrgMember member = orgManager.getMemberById(memberId);
                    if (member != null) {
                        return member.getName();
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("TransKit 人员转义异常: " + e.getMessage());
            }

            return memberIdObj.toString();
        } else {
            return "";
        }
    }

    public static String getMemberName(Object memberIdObj) {
        try {
            OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
            return getMemberName(memberIdObj, orgManager);
        } catch (Exception var2) {
            return memberIdObj == null ? "" : memberIdObj.toString();
        }
    }

    public static String getEnumShowValue(Object enumIdObj, EnumManager enumManager) {
        if (enumIdObj != null && !"".equals(enumIdObj.toString().trim())) {
            try {
                Long enumId = Long.parseLong(enumIdObj.toString());
                if (enumManager != null) {
                    CtpEnumItem enumItem = enumManager.getEnumItem(enumId);
                    if (enumItem != null) {
                        return enumItem.getShowvalue();
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("TransKit 枚举转义异常: " + e.getMessage());
            }

            return enumIdObj.toString();
        } else {
            return "";
        }
    }

    public static String getEnumShowValue(Object enumIdObj) {
        try {
            EnumManager enumManager = (EnumManager)AppContext.getBean("enumManagerNew");
            return getEnumShowValue(enumIdObj, enumManager);
        } catch (Exception var2) {
            return enumIdObj == null ? "" : enumIdObj.toString();
        }
    }
}
