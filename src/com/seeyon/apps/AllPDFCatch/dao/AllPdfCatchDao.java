//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.seeyon.apps.AllPDFCatch.dao;

import java.util.Date;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AllPdfCatchDao {
    @Select({"SELECT ID, FILENAME, MIME_TYPE, CREATE_DATE, FILE_SIZE FROM ctp_file WHERE ID = #{fileId}"})
    Map<String, Object> selectCtpFile(@Param("fileId") Long var1);

    @Insert({"INSERT INTO ctp_attachment (ID, ATT_REFERENCE, SUB_REFERENCE, CATEGORY, TYPE, FILENAME, FILE_URL, MIME_TYPE, CREATEDATE, ATTACHMENT_SIZE, SORT) VALUES (#{id}, #{attReference}, #{subReference}, #{category}, #{type}, #{filename}, #{fileUrl}, #{mimeType}, #{createDate}, #{attachmentSize}, #{sort})"})
    void insertAttachment(@Param("id") Long var1, @Param("attReference") Long var2, @Param("subReference") Long var3, @Param("category") Integer var4, @Param("type") Integer var5, @Param("filename") String var6, @Param("fileUrl") Long var7, @Param("mimeType") String var8, @Param("createDate") Date var9, @Param("attachmentSize") Long var10, @Param("sort") Integer var11);

    @Update({"UPDATE ${tableName} SET ${fieldName} = #{subReference} WHERE ID = #{masterId}"})
    void updateFormMain(@Param("tableName") String var1, @Param("fieldName") String var2, @Param("subReference") Long var3, @Param("masterId") Long var4);
}
