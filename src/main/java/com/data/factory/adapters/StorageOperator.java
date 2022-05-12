package com.data.factory.adapters;

import com.data.factory.exceptions.ObjectStorageException;
import com.data.factory.ports.ObjectStorage;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StorageOperator implements ObjectStorage {
    private String projectId;

    public StorageOperator(String projectId) throws ObjectStorageException {
        if (projectId == null || projectId.isEmpty()) throw new ObjectStorageException("projectId cannot be null or empty.");
        else this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }

    public Storage getStorageService() throws IOException {
        return StorageOptions
                .newBuilder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setProjectId(this.projectId)
                .build()
                .getService();
    }

    private ArrayList<String> extractBucket(String pathBlob) throws ObjectStorageException {
        ArrayList<String> splitter = new ArrayList<>();
        String regex = "^gs://([a-zA-Z0-9_-]+)/([a-zA-Z0-9_/.-]+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pathBlob);
        if (matcher.find()){
            splitter.add(matcher.group(1));
            splitter.add(matcher.group(2));
        } else throw new ObjectStorageException(String.format("pathBlob %s not matched with %s regex.", pathBlob, regex));
        return splitter;
    }

    @Override
    public void uploadBlob(String sourcePath, String destBlob) throws ObjectStorageException {
        if (sourcePath ==  null || sourcePath.isEmpty()) throw new ObjectStorageException("sourceBlob cannot be null or empty.");
        if (destBlob ==  null || destBlob.isEmpty()) throw new ObjectStorageException("destFilePath cannot be null or empty.");
        ArrayList<String> bucket = this.extractBucket(destBlob);
        try {
            Storage storage = this.getStorageService();
            BlobId blobId = BlobId.of(bucket.get(0), bucket.get(1));
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            storage.create(blobInfo, Files.readAllBytes(Paths.get(sourcePath)));
        } catch (IOException exception){
            throw new ObjectStorageException(String.format("cannot upload file %s to %s blob. %s", sourcePath, destBlob, exception.getMessage()));
        }
    }

    @Override
    public void deleteBlob(String blobPath) throws ObjectStorageException {
        if (blobPath == null || blobPath.isEmpty()) throw new ObjectStorageException("blobPath cannot be null or empty.");
        try {
            Storage storage = this.getStorageService();
            ArrayList<String> bucketBlob = this.extractBucket(blobPath);
            Page<Blob> blobs = storage.list(bucketBlob.get(0), Storage.BlobListOption.prefix(bucketBlob.get(1)));
            for (Blob blob : blobs.iterateAll()) blob.delete();
        } catch (ObjectStorageException | IOException exception){
            throw new ObjectStorageException(String.format("%s %s", exception.getClass(), exception.getMessage()));
        }
    }

    @Override
    public ArrayList<String> listBlob(String blobPath) throws ObjectStorageException {
        if (blobPath == null || blobPath.isEmpty()) throw new ObjectStorageException("blobPath cannot be null or empty.");
        try {
            Storage storage = this.getStorageService();
            ArrayList<String> bucketBlob = this.extractBucket(blobPath);
            ArrayList<String> blobList = new ArrayList<>();
            Page<Blob> blobs = storage.list(bucketBlob.get(0), Storage.BlobListOption.prefix(bucketBlob.get(1)));
            for (Blob blob : blobs.iterateAll()) blobList.add(blob.getName());
            return blobList;
        } catch (ObjectStorageException | IOException exception){
            throw new ObjectStorageException(String.format("%s %s", exception.getClass(), exception.getMessage()));
        }
    }

    @Override
    public void downloadBlob(String blobPath, String destPath) throws ObjectStorageException {
        if (blobPath ==  null || blobPath.isEmpty()) throw new ObjectStorageException("sourceBlob cannot be null or empty.");
        if (destPath ==  null || destPath.isEmpty()) throw new ObjectStorageException("destPath cannot be null or empty.");
        ArrayList<String> bucket = this.extractBucket(blobPath);
        try{
            Storage storage = this.getStorageService();
            Blob blob = storage.get(BlobId.of(bucket.get(0), bucket.get(1)));
            blob.downloadTo(Paths.get(destPath));
        } catch (Exception e) {
            throw new ObjectStorageException(String.format("%s %s", e.getClass(), e.getMessage()));
        }
    }
}
