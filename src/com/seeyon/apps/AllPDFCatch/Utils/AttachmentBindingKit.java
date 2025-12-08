package com.seeyon.apps.AllPDFCatch.Utils;

import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.util.UUIDLong;
import org.apache.commons.logging.Log;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component("testPdfCatchattachmentBindingKit")
public class AttachmentBindingKit {

    private static final Log LOGGER = CtpLogFactory.getLog(AttachmentBindingKit.class);

    /**
     * 核心方法：绑定附件
     *
     * @param masterBean 表单主数据对象
     * @param fieldName  表单附件控件的字段名 (如 field0010)
     * @param fileId    TestAPISeeyonFileUploaders 上传后返回的文件ID
     * @param originFile 本地原始文件对象 (用于获取文件名、大小等信息)
     */
    public void bindAttachment(FormDataMasterBean masterBean, String fieldName, String fileId, File originFile) {
        try {
            // 1. 获取官方 Manager
            AttachmentManager attachmentManager = (AttachmentManager) AppContext.getBean("attachmentManager");

            // 2. 准备 SubReference (关联ID)
            // 逻辑：如果该字段已有附件，就复用旧的 subReference；如果是空的，就生成一个新的。
            Long subReference = null;
            Object currentVal = masterBean.getFieldValue(fieldName);

            if (currentVal != null && !"".equals(currentVal.toString())) {
                try {
                    subReference = Long.parseLong(currentVal.toString());
                } catch (NumberFormatException e) {
                    subReference = UUIDLong.longUUID();
                }
            } else {
                subReference = UUIDLong.longUUID();
            }

            // 3. 构建 Attachment 对象 (对应 ctp_attachment 表结构)
            Attachment attachment = new Attachment();
            attachment.setIdIfNew(); // 自动生成 ID

            // 3.1 设置关联键
            attachment.setReference(masterBean.getId()); // 主数据ID
            attachment.setSubReference(subReference);    // 字段关联ID
            attachment.setCategory(ApplicationCategoryEnum.form.getKey()); // 分类：表单 (值为2)

            // 3.2 设置文件信息 (核心：把你的 fileUrl 填进去)
            attachment.setFileUrl(Long.parseLong(fileId)); // [核心] 关联物理文件
            attachment.setFilename(originFile.getName());
            attachment.setMimeType(getMimeType(originFile.getName()));
            attachment.setCreatedate(new Date());
            attachment.setSize(originFile.length());
            attachment.setType(0); // 0 代表普通附件
            attachment.setCategory(66);

            // 4. 调用 Manager 保存到数据库 (ctp_attachment 表)
            List<Attachment> atts = new ArrayList<>();
            atts.add(attachment);

            // String create(Collection<Attachment> var1);
            attachmentManager.create(atts);

            // 5. 更新表单字段值 (formmain_xxx 表)
            // 这一步至关重要！必须把 subReference 填回表单，否则表单不知道去查哪个附件。
            masterBean.addFieldValue(fieldName, subReference);
            System.out.println("附件绑定成功！SubRef: " + subReference + ", fileId: " + fileId);

            LOGGER.info("附件绑定成功！SubRef: " + subReference + ", fileId: " + fileId);

        } catch (Exception e) {
            System.out.println("附件绑定异常");
            LOGGER.error("附件绑定异常", e);
            throw new RuntimeException(e);
        }
    }

    private String getMimeType(String fileName) {
        String name = fileName.toLowerCase();
        if (name.endsWith(".pdf")) return "application/pdf";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".doc") || name.endsWith(".docx")) return "application/msword";
        return "application/octet-stream";
    }
}