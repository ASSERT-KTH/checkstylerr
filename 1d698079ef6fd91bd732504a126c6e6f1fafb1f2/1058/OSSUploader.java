package com.duodian.admore.crm.utils;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectMetadata;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by james on 2018/1/19.
 */
public class OSSUploader implements InitializingBean {

    private static String DEFAULT_BUCKET_NAME = null;
    private static OSSClient client = null;

    @Value("${oss.bucketName}")
    private String bucketName;
    @Value("${oss.endpoint}")
    private String endpoint;
    @Value("${oss.accessKeyId}")
    private String accessKeyId;
    @Value("${oss.accessKeySecret}")
    private String accessKeySecret;

    @Override
    public void afterPropertiesSet() throws Exception {
        DEFAULT_BUCKET_NAME = bucketName;
        client = new OSSClient(endpoint,accessKeyId,accessKeySecret);
    }

    public static void upload(String fileLink,File file){
        //upload object.
        client.putObject(DEFAULT_BUCKET_NAME,fileLink,file);
    }

    public static void upload(String fileLink, InputStream inputStream, String cacheValue){
        upload(DEFAULT_BUCKET_NAME,fileLink,inputStream,cacheValue);
    }

    public static void upload(String fileLink,InputStream inputStream){
        upload(DEFAULT_BUCKET_NAME,fileLink,inputStream);
    }

    public static void upload(String bucket,String fileLink,InputStream inputStream){
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(inputStream.available());

            client.putObject(bucket,fileLink,inputStream,metadata);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(),e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public static void upload(String bucket,String fileLink,InputStream inputStream,String cacheValue){
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(inputStream.available());
            metadata.setCacheControl(cacheValue);
            client.putObject(bucket,fileLink,inputStream,metadata);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(),e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
    public static void upload(String bucket,String fileLink,String content){
        try {
            InputStream inputStream = IOUtils.toInputStream(content,"UTF-8");
            upload(bucket,fileLink,inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(),e);
        }
    }

    public static InputStream download(String fileLink){
        return download(DEFAULT_BUCKET_NAME,fileLink);
    }

    public static InputStream download(String bucket,String fileLink){
        if (client.doesObjectExist(bucket,fileLink)){
            return client.getObject(bucket,fileLink).getObjectContent();
        }
        return null;
    }

    public static Boolean exist(String bucket,String fileLink){
        return client.doesObjectExist(bucket,fileLink);
    }

    public static Boolean exist(String fileLink){
        return client.doesObjectExist(DEFAULT_BUCKET_NAME, fileLink);
    }

    public static void delete(String fileLink){
        delete(DEFAULT_BUCKET_NAME,fileLink);
    }

    public static void delete(String bucket,String fileLink){
        if (client.doesObjectExist(DEFAULT_BUCKET_NAME,fileLink)){
            client.deleteObject(DEFAULT_BUCKET_NAME,fileLink);
        }
    }

    public static ObjectListing listObjects(ListObjectsRequest listObjectsRequest) {
        return client.listObjects(listObjectsRequest);
    }
}
