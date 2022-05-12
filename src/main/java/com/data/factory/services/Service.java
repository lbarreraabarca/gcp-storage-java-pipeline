package com.data.factory.services;


import com.data.factory.exceptions.ServiceException;
import com.data.factory.models.Contract;
import com.data.factory.ports.ObjectStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

public class Service {
    private static final Logger log = LoggerFactory.getLogger(Service.class);

    private ObjectStorage storage;
    private static String INVALID_ATTRIBUTE = "%s cannot be null or empty.";

    public Service(ObjectStorage storage) throws ServiceException {
        if (storage == null) throw new ServiceException(String.format(INVALID_ATTRIBUTE, "storage"));
        else this.storage = storage;
    }

    public ResponseEntity<String> invoke(Contract request) throws ServiceException {
        try {
            storage.uploadBlob(request.getLocalPath(), request.getLandingPath());
            ArrayList<String> blobs = storage.listBlob(request.getLandingPath());
            for (String blob : blobs) log.info(String.format("Blob :%s", blob));
            //storage.deleteBlob(request.getLandingPath());
            return new ResponseEntity<>("OK", HttpStatus.OK);
        } catch (Exception e){
            throw new ServiceException(String.format("%s %s", e.getClass(), e.getMessage()));
        }
    }
}
