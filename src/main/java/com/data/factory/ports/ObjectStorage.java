package com.data.factory.ports;

import com.data.factory.exceptions.ObjectStorageException;

import java.util.ArrayList;

public interface ObjectStorage {
    void downloadBlob(String blobPath, String destPath) throws ObjectStorageException;
    void uploadBlob(String localPath, String blobPath) throws ObjectStorageException;
    void deleteBlob(String blobPath) throws ObjectStorageException;
    ArrayList<String> listBlob(String blobPath) throws ObjectStorageException;
}
