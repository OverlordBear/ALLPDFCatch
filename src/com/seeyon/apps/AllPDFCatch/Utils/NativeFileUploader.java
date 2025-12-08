package com.seeyon.apps.AllPDFCatch.Utils;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import org.apache.commons.logging.Log;
import org.springframework.stereotype.Component;
import java.io.File;
import java.util.Date;

/**
 * 原生文件上传工具类
 * 直接调用致远内核 API 进行文件存储，效率最高，无需网络开销。
 */
@Component
public class NativeFileUploader {

    private static final Log LOGGER = CtpLogFactory.getLog(NativeFileUploader.class);

    /**
     * 上传本地文件到致远文件系统 (ctp_file)
     *
     * @param file 本地文件对象
     * @return 文件在 ctp_file 表中的 ID (String 类型)
     * @throws BusinessException 如果上传失败
     */
    public String uploadFile(File file) throws BusinessException {
        // 1. 参数校验
        if (file == null || !file.exists()) {
            throw new BusinessException("上传失败：文件不存在或为空 -> " + (file == null ? "null" : file.getPath()));
        }

        try {
            // 2. 获取 FileManager (Spring Bean)
            //
            FileManager fileManager = (FileManager) AppContext.getBean("fileManager");

            if (fileManager == null) {
                throw new BusinessException("无法获取 FileManager 实例，请检查 Spring 上下文。");
            }

            // 3. 调用核心保存方法
            // 方法签名: V3XFile save(File file, ApplicationCategoryEnum category, String filename, Date createDate, Boolean isSaveToDB)
            //
            V3XFile v3xFile = fileManager.save(
                    file,                                   // 文件对象
                    ApplicationCategoryEnum.form,           // 分类：表单 (值为2，与Attachment一致)
                    file.getName(),                         // 文件名
                    new Date(),                             // 创建时间
                    true                                    // isSaveToDB: 必须为 true，否则不入库！
            );

            // 4. 返回文件 ID
            String fileId = String.valueOf(v3xFile.getId());
            LOGGER.info("原生上传成功！File: " + file.getName() + " -> ID: " + fileId);

            return fileId;

        } catch (Exception e) {
            LOGGER.error("原生上传发生异常", e);
            // 包装异常抛出，让上层感知
            throw new BusinessException("文件存盘失败：" + e.getMessage(), e);
        }
    }
}