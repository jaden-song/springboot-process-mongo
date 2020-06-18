package com.jaden.processmongo.dao;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Getter
@Setter
@ToString
@Builder
public class FileInfo {

    @Id
    private String id;
    private String accessKey; // 카카오 워크에서 관리되는 첨부파일 유니크 구분자 (kage access key 등), 테스트에서는 파일 폴더 위치
    private String fileName;
    private String extension; // 확장자
    private List<ImageInfo> images;
}
