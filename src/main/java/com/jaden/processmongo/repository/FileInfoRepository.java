package com.jaden.processmongo.repository;


import com.jaden.processmongo.dao.FileInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileInfoRepository extends MongoRepository<FileInfo, String> {

    FileInfo findFileInfoByAccessKeyEquals(String fileKey);
}
