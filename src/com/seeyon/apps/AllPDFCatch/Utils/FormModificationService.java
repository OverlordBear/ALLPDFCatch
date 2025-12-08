package com.seeyon.apps.AllPDFCatch.Utils;

import com.seeyon.cap4.form.bean.*;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.AppContext;
import org.apache.commons.logging.Log;
import com.seeyon.ctp.common.log.CtpLogFactory;
import org.springframework.stereotype.Component;
import java.util.List;
//注意，此工具类只适用于第三方工具类，如contrlller
@Component("allPDFCatchFormModificationService")
public class FormModificationService {

    private static final Log LOGGER = CtpLogFactory.getLog(FormModificationService.class);

    public void updateExternal(Long formId, Long recordId) throws Exception {
        CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");

        // 1. 【查】获取主对象
        FormDataMasterBean masterBean = cap4FormManager.findDataById(recordId, formId, null); //

        if (masterBean != null) {
            // --- 主表操作 ---
            // Object mainVal = masterBean.getFieldValue("field0001");

            // ==========================================
            // 2. 【查明细表】并横向打印
            // ==========================================
            // 获取明细表所有行 (假设表名是 formson_0001)
            List<FormDataSubBean> detailRows = masterBean.getSubData("formson_0001"); //

            if (detailRows != null) {
                System.out.println("====== 明细表数据开始 ======");
                // 打印表头（可选）
                System.out.println("行ID \t\t | 列1(field0010) \t | 列2(field0011) \t | 列3(field0012)");

                // 遍历每一行
                for (FormDataSubBean row : detailRows) {
                    // 这一行的所有列数据取出来
                    Object col1 = row.getFieldValue("field0010"); //
                    Object col2 = row.getFieldValue("field0011");
                    Object col3 = row.getFieldValue("field0012");

                    // 拼接成一行字符串打印 (使用 \t 制表符或者空格隔开)
                    String oneRowText = "ID:" + row.getId() + " \t | " + col1 + " \t\t | " + col2 + " \t\t | " + col3;

                    // 这一句保证了它是横向排列的：一次打印一行
                    System.out.println(oneRowText);
                }
                System.out.println("====== 明细表数据结束 ======");
            }

            // 3. 【存】
            cap4FormManager.saveOrUpdateFormData(masterBean, formId, true);
        }
    }
}