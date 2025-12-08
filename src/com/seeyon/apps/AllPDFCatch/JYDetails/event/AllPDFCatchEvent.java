
package com.seeyon.apps.AllPDFCatch.JYDetails.event;

import com.seeyon.apps.AllPDFCatch.JYDetails.trigger.cap4.FormTriggerPDFCatchManager;
import com.seeyon.apps.AllPDFCatch.Utils.AffairBaindService;
import com.seeyon.apps.AllPDFCatch.Utils.WorkflowEventDataKit;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.workflow.event.AbstractWorkflowEvent;
import com.seeyon.ctp.workflow.event.WorkflowEventData;
import java.io.File;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AllPDFCatchEvent extends AbstractWorkflowEvent {
    private static final Log LOGGER = CtpLogFactory.getLog(AllPDFCatchEvent.class);
    private final FormTriggerPDFCatchManager formTriggerPDFCatchManager = new FormTriggerPDFCatchManager();
    @Autowired
    private AffairBaindService affairBaindService;

    public AllPDFCatchEvent() {
    }

    public String getId() {
        return this.formTriggerPDFCatchManager.getId();
    }

    public String getLabel() {
        return "AllPDFCatch---所有回单抓取插件";
    }

    public ApplicationCategoryEnum getAppName() {
        return ApplicationCategoryEnum.form;
    }

    public void onStart(WorkflowEventData data) {
        WorkflowEventDataKit kit = new WorkflowEventDataKit(data);
        if (kit.isValid()) {
            FormDataMasterBean masterBean = kit.getMasterBean();

            try {
                System.out.println(">>> 开始处理回单抓取流程...");
                File myFile = new File("C:\\Temp\\_25337000000561511662_天猫超市.pdf");
                affairBaindService.processAttachment(masterBean,
                        "field0011",
                        myFile,
                        String.valueOf(data.getSummaryId()));
                System.out.println(">>> 流程处理成功！");
            } catch (Exception e) {
                System.out.println("流程处理失败" + e);
            }

        }
    }
}
