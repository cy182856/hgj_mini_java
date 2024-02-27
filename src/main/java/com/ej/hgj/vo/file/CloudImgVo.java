package com.ej.hgj.vo.file;

/**
 * @author tty
 * @version 1.0 2020-10-16 13:50
 */
public class CloudImgVo {
    /**
     * 企业客户号
     */
    private String custId;
    /**
     * 业务ID
     */
    private String busiId;
    /**
     * 图片ID
     */
    private String imgId;
    /**
     * 业务日期
     */
    private String busiDate;
    /**
     * 业务流水号
     */
    private String busiSeqId;
    /**
     * 上传的图片文件类型
     */
    private String fileType;


    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getBusiId() {
        return busiId;
    }

    public void setBusiId(String busiId) {
        this.busiId = busiId;
    }

    public String getImgId() {
        return imgId;
    }

    public void setImgId(String imgId) {
        this.imgId = imgId;
    }

    public String getBusiDate() {
        return busiDate;
    }

    public void setBusiDate(String busiDate) {
        this.busiDate = busiDate;
    }

    public String getBusiSeqId() {
        return busiSeqId;
    }

    public void setBusiSeqId(String busiSeqId) {
        this.busiSeqId = busiSeqId;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}

