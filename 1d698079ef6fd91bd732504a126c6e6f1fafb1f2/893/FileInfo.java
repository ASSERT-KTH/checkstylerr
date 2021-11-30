package cn.merryyou.editor.editor.dto;

import lombok.Data;

@Data
public class FileInfo {
    private int success = 1;
    private String message = "上传成功";
    private String url;

    public FileInfo() {
    }

    public FileInfo(int success, String message, String url) {
        this.success = success;
        this.message = message;
        this.url = url;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
