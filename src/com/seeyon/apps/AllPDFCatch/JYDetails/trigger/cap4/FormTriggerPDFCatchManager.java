package com.seeyon.apps.AllPDFCatch.JYDetails.trigger.cap4;

import com.seeyon.cap4.form.modules.engin.trigger.FormTriggerCustomDesignManager;
import com.seeyon.cap4.form.util.Enums;

public class FormTriggerPDFCatchManager extends FormTriggerCustomDesignManager {
    public FormTriggerPDFCatchManager() {
    }

    public String getId() {
        return "FormTriggerPDFCatchManager";
    }

    public String getName() {
        return "回单抓取manager";
    }

    public boolean canUse() {
        return true;
    }

    public boolean canUse4FormType(Enums.FormType formType) {
        return this.canUse();
    }
}
