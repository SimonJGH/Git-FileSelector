package com.yds.scanfile.entity;

import java.io.Serializable;

/**
 * @author YDS
 * @date 2021/5/19
 * @discribe
 */
public class MediaBean implements Serializable {
    public static class Type {
        public static String Image = "Image";
    }

    public String fileType;
    public String fileName;
    public String filePath;
    public int fileSize;
    public int selectFileIndex = -1;


    public MediaBean() {
    }

    public MediaBean(String fileType, String filePath, int fileSize, String fileName) {
        this.fileType = fileType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }
}
