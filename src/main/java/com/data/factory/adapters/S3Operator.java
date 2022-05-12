package com.data.factory.adapters;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import com.data.factory.exceptions.ObjectStorageException;
import com.data.factory.ports.ObjectStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class S3Operator implements ObjectStorage {
    private static final Logger LOG = LoggerFactory.getLogger(S3Operator.class);
    private String accessKeyID;
    private String secretAccessKey;
    private Regions region;
    private static String INVALID_ARGUMENT = "%s cannot be null or empty.";

    public S3Operator(String accessKeyID, String secretAccessKey, String region) throws ObjectStorageException {
        if( accessKeyID == null || accessKeyID.isEmpty()) throw new ObjectStorageException(String.format(INVALID_ARGUMENT, "accessKeyID"));
        if( secretAccessKey == null || secretAccessKey.isEmpty()) throw new ObjectStorageException(String.format(INVALID_ARGUMENT, "secretAccessKey"));
        if (region == null || region.isEmpty()) throw new ObjectStorageException(String.format(INVALID_ARGUMENT, "region"));

        this.accessKeyID = accessKeyID;
        this.secretAccessKey = secretAccessKey;
        this.region = this.getRegion(region);
    }

    private Regions getRegion(String region) throws ObjectStorageException {
        if (region == null || region.isEmpty()) throw new ObjectStorageException(String.format(INVALID_ARGUMENT, "region"));
        switch (region){
            case "us-west-1":
                return Regions.US_WEST_1;
            case "us-west-2":
                return Regions.US_WEST_2;
            case "us-east-1":
                return Regions.US_EAST_1;
            case "us-east-2":
                return Regions.US_EAST_2;
            default:
                throw new ObjectStorageException(String.format("Region %s not implemented.", region));
        }
    }

    private AWSCredentials getCredentials() {
        return new BasicAWSCredentials( this.accessKeyID, this.secretAccessKey );
    }

    private AmazonS3 getAWSS3Client() throws ObjectStorageException {
        try {
            return AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(getCredentials()))
                    .withRegion(this.region)
                    .build();
        } catch (Exception e){
            throw new ObjectStorageException(String.format("%s %s", e.getClass(), e.getMessage()));
        }
    }

    private ArrayList<String> extractBucket(String pathBlob) throws ObjectStorageException {
        ArrayList<String> splitter = new ArrayList<>();
        String regex = "^s3://([a-zA-Z0-9_-]+)/([a-zA-Z0-9_/.-]+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pathBlob);
        if (matcher.find()){
            splitter.add(matcher.group(1));
            splitter.add(matcher.group(2));
        } else throw new ObjectStorageException(String.format("pathBlob %s not matched with %s regex.", pathBlob, regex));
        return splitter;
    }

    @Override
    public void downloadBlob(String blobPath, String destPath) throws ObjectStorageException {
        if (blobPath == null || blobPath.isEmpty()) throw new ObjectStorageException(String.format(INVALID_ARGUMENT, "blobPath"));
        if (destPath == null || destPath.isEmpty()) throw new ObjectStorageException(String.format(INVALID_ARGUMENT, "blobPath"));
        try{
            AmazonS3 client = this.getAWSS3Client();
            LOG.info("Downloading {} into {}", blobPath, destPath);
            client.getObject(blobPath, destPath);
        } catch (Exception e){
            throw new ObjectStorageException(String.format("%s %s", e.getClass(), e.getMessage()));
        }
    }

    @Override
    public void uploadBlob(String localPath, String blobPath) throws ObjectStorageException {
        if (localPath == null || localPath.isEmpty()) throw new ObjectStorageException(String.format(INVALID_ARGUMENT, "blobPath"));
        if (blobPath == null || blobPath.isEmpty()) throw new ObjectStorageException(String.format(INVALID_ARGUMENT, "blobPath"));
        try{
            ArrayList<String> bucket = this.extractBucket(blobPath);
            AmazonS3 client = this.getAWSS3Client();
            File localFile = new File(localPath);
            LOG.info("Uploading {} into {}", localPath,blobPath);
            client.putObject(bucket.get(0), bucket.get(1), localFile);
        } catch (Exception e){
            throw new ObjectStorageException(String.format("%s %s", e.getClass(), e.getMessage()));
        }
    }

    @Override
    public void deleteBlob(String blobPath) throws ObjectStorageException {
        if (blobPath == null || blobPath.isEmpty()) throw new ObjectStorageException(String.format(INVALID_ARGUMENT, "blobPath"));
        try{
            ArrayList<String> bucket = this.extractBucket(blobPath);
            AmazonS3 client = this.getAWSS3Client();
            LOG.info("Deleting {}",blobPath);
            client.deleteObject(bucket.get(0), bucket.get(1));
        } catch (Exception e){
            throw new ObjectStorageException(String.format("%s %s", e.getClass(), e.getMessage()));
        }
    }

    @Override
    public ArrayList<String> listBlob(String blobPath) throws ObjectStorageException {
        if (blobPath == null || blobPath.isEmpty()) throw new ObjectStorageException(String.format(INVALID_ARGUMENT, "blobPath"));
        try {
            ArrayList<String> blobs = new ArrayList<>();
            ArrayList<String> bucket = this.extractBucket(blobPath);
            AmazonS3 client = this.getAWSS3Client();
            ObjectListing objectListing = client.listObjects(bucket.get(0),bucket.get(1));
            for(S3ObjectSummary os : objectListing.getObjectSummaries()) blobs.add(os.getKey());
            return blobs;
        } catch (Exception e){
            throw new ObjectStorageException(String.format("%s %s", e.getClass(), e.getMessage()));
        }
    }
}