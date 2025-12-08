//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.seeyon.apps.AllPDFCatch.Utils;

import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.ctp.workflow.event.WorkflowEventData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkflowEventDataKit {
    private FormDataMasterBean masterBean;

    public WorkflowEventDataKit(WorkflowEventData data) {
        if (data != null) {
            Map<String, Object> businessData = data.getBusinessData();
            if (businessData != null) {
                this.masterBean = (FormDataMasterBean)businessData.get("formDataBean");
            }
        }

    }

    public Object get(String fieldName) {
        return this.masterBean == null ? null : this.masterBean.getFieldValue(fieldName);
    }

    public String getString(String fieldName) {
        Object val = this.get(fieldName);
        return val == null ? "" : String.valueOf(val);
    }

    public void set(String fieldName, Object value) {
        if (this.masterBean != null) {
            this.masterBean.addFieldValue(fieldName, value);
        }

    }

    public List<FormDataSubBean> getDetailRows(String tableName) {
        if (this.masterBean == null) {
            return new ArrayList();
        } else {
            List<FormDataSubBean> rows = this.masterBean.getSubData(tableName);
            return (List<FormDataSubBean>)(rows != null ? rows : new ArrayList());
        }
    }

    public boolean isValid() {
        return this.masterBean != null;
    }

    public FormDataMasterBean getMasterBean() {
        return this.masterBean;
    }
}
