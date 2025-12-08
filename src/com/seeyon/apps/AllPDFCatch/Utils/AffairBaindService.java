

package com.seeyon.apps.AllPDFCatch.Utils;

import com.seeyon.apps.AllPDFCatch.dao.AllPdfCatchDao;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AffairBaindService {
    private static final Log LOGGER = CtpLogFactory.getLog(AffairBaindService.class);
    @Autowired
    private NativeFileUploader nativeFileUploader;
    @Autowired
    private AllPdfCatchDao allPdfCatchDao;

    public AffairBaindService() {
    }

    public void processAttachment(FormDataMasterBean masterBean, String fieldName, File localFile, String summaryId) {
        if (localFile != null && localFile.exists()) {
            try {
                String fileIdStr = this.nativeFileUploader.uploadFile(localFile);
                Long fileId = Long.parseLong(fileIdStr);
                System.out.println(">>> 文件已上传成功: 返回的fileID:" + fileId);
                Long referenceId = Strings.isNotBlank(summaryId) ? Long.parseLong(summaryId) : masterBean.getId();
                Long subReference = UUIDLong.longUUID();
                Object currentVal = masterBean.getFieldValue(fieldName);
                if (currentVal != null && Strings.isNotBlank(currentVal.toString())) {
                    try {
                        subReference = Long.parseLong(currentVal.toString());
                        System.out.println(">>> 复用现有 SubReference: " + subReference);
                        LOGGER.info(">>> 复用现有 SubReference: " + subReference);
                    } catch (Exception var14) {
                    }
                }

                Map<String, Object> fileInfo = this.allPdfCatchDao.selectCtpFile(fileId);
                if (fileInfo == null) {
                    throw new RuntimeException("上传后无法查询到文件记录 ID=" + fileId);
                } else {
                    Object dateObj = fileInfo.get("CREATE_DATE");
                    Date createDate = new Date();
                    if (dateObj != null) {
                        if (dateObj instanceof LocalDateTime) {
                            createDate = Timestamp.valueOf((LocalDateTime)dateObj);
                        } else if (dateObj instanceof Date) {
                            createDate = (Date)dateObj;
                        }
                    }

                    this.allPdfCatchDao.insertAttachment(UUIDLong.longUUID(), referenceId, subReference, 66, 0, (String)fileInfo.get("FILENAME"), fileId, (String)fileInfo.get("MIME_TYPE"), createDate, Long.parseLong(fileInfo.get("FILE_SIZE").toString()), 0);
                    System.out.println(">>> 附件记录已强行插入 [Ref=" + referenceId + "]");
                    LOGGER.info(">>> 附件记录已强行插入 [Ref=" + referenceId + "]");
                    String tableName = masterBean.getFormTable().getTableName();
                    this.allPdfCatchDao.updateFormMain(tableName, fieldName, subReference, masterBean.getId());
                    System.out.println(">>> 底表字段已更新 [" + tableName + "." + fieldName + "]");
                    LOGGER.info(">>> 底表字段已更新 [" + tableName + "." + fieldName + "." + subReference + "]");
                    masterBean.addFieldValue(fieldName, subReference);
                }
            } catch (Exception e) {
                System.out.println(">>> 附件处理服务异常: " + e.getMessage());
                LOGGER.error(">>> 附件处理服务异常", e);
                throw new RuntimeException("附件处理失败: " + e.getMessage(), e);
            }
        } else {
            System.out.println(">>> 文件不存在，跳过处理: " + (localFile == null ? "null" : localFile.getPath()));
            LOGGER.warn(">>> 文件不存在，跳过处理: " + (localFile == null ? "null" : localFile.getPath()));
        }
    }
}
