package com.jaden.processmongo.dao;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder
@ToString
public class ImageInfo {
    private String id;
    private String accessKey; // kage 경로 정보, 테스트에서는 파일 폴더 위치
    private String imageName;
    private String extension;
    private long length;
}
