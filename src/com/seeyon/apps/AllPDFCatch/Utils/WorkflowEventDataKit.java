package com.seeyon.apps.AllPDFCatch.Utils;

import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.ctp.workflow.event.WorkflowEventData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 节点事件数据获取工具
 * <p>
 * 封装了从 WorkflowEventData 中提取主表和明细表数据的逻辑，
 * 简化 onBeforeFinishWorkitem 等事件中的代码书写。
 */
public class WorkflowEventDataKit {

    private FormDataMasterBean masterBean;

    // 构造函数：传入事件数据对象，自动解析出 masterBean
    public WorkflowEventDataKit(WorkflowEventData data) {
        if (data != null) {
            // 获取 businessData map
            Map<String, Object> businessData = data.getBusinessData();
            if (businessData != null) {
                // 提取主表 Bean
                this.masterBean = (FormDataMasterBean) businessData.get("formDataBean");
            }
        }
    }

    /**
     * 获取主表字段的值
     * @param fieldName 字段名 (e.g., "field0001")
     * @return 字段值 Object
     */
    public Object get(String fieldName) {
        if (masterBean == null) return null;
        // 调用底层 getFieldValue
        return masterBean.getFieldValue(fieldName);
    }

    /**
     * 获取主表字段的值 (转为 String，空值返回空字符串，防止空指针)
     * @param fieldName 字段名
     * @return String 值
     */
    public String getString(String fieldName) {
        Object val = get(fieldName);
        return val == null ? "" : String.valueOf(val);
    }

    /**
     * 设置/修改 主表字段的值
     * @param fieldName 字段名
     * @param value 新值
     */
    public void set(String fieldName, Object value) {
        if (masterBean != null) {
            // 调用底层 addFieldValue 触发变更标记
            masterBean.addFieldValue(fieldName, value);
        }
    }

    /**
     * 获取明细表（重复表）的所有行数据
     * @param tableName 明细表表名 (e.g., "formson_0018")
     * @return 明细行列表，如果无数据返回空 List (不会返回 null，方便遍历)
     */
    public List<FormDataSubBean> getDetailRows(String tableName) {
        if (masterBean == null) return new ArrayList<>();

        // 调用底层 getSubData
        List<FormDataSubBean> rows = masterBean.getSubData(tableName);

        return rows != null ? rows : new ArrayList<>();
    }

    /**
     * 判断当前工具类是否初始化成功（即是否有有效的主表数据）
     */
    public boolean isValid() {
        return masterBean != null;
    }

    /**
     * 获取原始的 MasterBean (万一你需要用它做其他复杂操作)
     */
    public FormDataMasterBean getRawMasterBean() {
        return masterBean;
    }
}