package com.seeyon.apps.AllPDFCatch.JYDetails.trigger.cap4;

import com.seeyon.cap4.form.bean.FormTriggerActionBean;
import com.seeyon.cap4.form.bean.FormTriggerBean.ActionParam;
import com.seeyon.cap4.form.modules.engin.trigger.FormTriggerCustomBaseAction;
import com.seeyon.cap4.form.modules.engin.trigger.TriggerActionContext;
import com.seeyon.ctp.common.exceptions.BusinessException;

public class FormTriggerPDFCatchTriggerAction extends FormTriggerCustomBaseAction {
    public FormTriggerPDFCatchTriggerAction() {
    }

    public String getId() {
        return "FormTriggerPDFCatchTriggerAction";
    }

    public void init() {
    }

    public void execute(TriggerActionContext context) throws BusinessException {
        FormTriggerActionBean actionBean = context.getActionBean();
        String configValue = (String)actionBean.getParam(ActionParam.ConfigValue.getKey());
    }
}
