package com.jaden.processmongo.service;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.jaden.processmongo.config.FilePathInfo;
import com.jaden.processmongo.dao.FileInfo;
import com.jaden.processmongo.dao.ImageInfo;
import com.jaden.processmongo.repository.FileInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConvertService {

    private final FilePathInfo filePathInfo;
    private final FileInfoRepository fileInfoRepository;

    public FileInfo convertToImages(final Map<String, Object> accessInfo) throws IOException, InterruptedException {
        // 1. test :: accessInfo 에서 파일명(구분자) 가져오기
        String accessKey = String.valueOf(accessInfo.get("accessKey"));
        String fileName = String.valueOf(accessInfo.get("fileName"));

        String inputPath = filePathInfo.getInput() + fileName;
        String outputPath = filePathInfo.getOutput() + accessKey + "/";

        // 2. 외부 명령 실행 => 실제는 kage 에서 첨부파일을 직접 가져와서 변환 실행
        if (convertProcessor(inputPath, outputPath)) {
            // 3. document.json 정보 추출 및 FileInfo 생성
            FileInfo fileInfo = makeFileInfo(accessKey, fileName);
            if (fileInfo != null) {
                // 4. db에 save 및 리턴
                return fileInfoRepository.save(fileInfo);
            }
        }
        return null;
    }

    public FileInfo getImageList(final String accessKey) {
        return fileInfoRepository.findFileInfoByAccessKeyEquals(accessKey);
    }

    public File getImage(final String accessKey, final String imageName) {
        File image = new File(filePathInfo.getLocalOutput()+accessKey+"/"+imageName);
        return image;
    }

    private boolean convertProcessor(String inputPath, String outputPath) throws IOException, InterruptedException {
        // docker exec nervous_cray sh -c "./doc-conv/sdk/convert.sh /doc-conv/input/sample1.pptx /doc-conv/output/ image"
        log.info(">>>>> START IMAGE CONVERT");
        String[] command = new String[] {
                "docker", "exec", "nervous_cray",
                "sh", "-c",
                "/doc-conv/sdk/convert.sh " + inputPath + " " + outputPath + " image"
        };

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = builder.start();
        int result = process.waitFor();

        log.info("<<<<< END IMAGE CONVERT");
        return result == 0;
    }

    private FileInfo makeFileInfo(String accessKey, String fileName) throws FileNotFoundException {
        log.info(">>>>> READ document.json");
        File outputFolder = new File(filePathInfo.getLocalOutput() + accessKey);
        if (outputFolder.exists() && outputFolder.isDirectory()) {
            File[] files = outputFolder.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null && files.length == 1) {
                Gson gson = new Gson();
                JsonReader reader = new JsonReader(new FileReader(files[0]));
                Map<String, Object> result = gson.fromJson(reader, Map.class);
                Map<String, Object> conversionInfo = (Map<String, Object>) result.get("conversionInfo");
                Object success = conversionInfo.get("success");
                boolean isSuccess = false;
                if (success instanceof Boolean)
                    isSuccess = (Boolean) success;
                else if (success instanceof String)
                    isSuccess = Boolean.parseBoolean(String.valueOf(success));
                log.info("convert success :: " + isSuccess);

                // TODO hwp 파일을 변환하면 무조건 false 이기에 일단 막음
//                if (isSuccess) {
                    Map<String, Object> pageInfo = (Map<String, Object>) result.get("pageInfo");
                    Map<String, Object> fileInfo = (Map<String, Object>) result.get("fileInfo");
                    double totalPagesD = (double)(pageInfo.get("totalPages"));
                    int totalPages = (int)totalPagesD;
                    String prefix = String.valueOf(fileInfo.get("prefix"));
                    String extension = String.valueOf(fileInfo.get("extension"));
                    double digitD = (double)(fileInfo.get("digit"));
                    int digit = (int)digitD;

                    log.info("convert totalPages :: " + totalPages);
                    log.info(">>>>> READ document.json");

                    FileInfo retFileInfo = FileInfo.builder().id(accessKey)
                            .fileName(fileName).accessKey(accessKey)
                            .images(new ArrayList<ImageInfo>() {})
                            .build();

                    IntStream.range(1, totalPages+1).forEachOrdered(num -> {
                        ImageInfo info = ImageInfo.builder()
                                .id(UUID.randomUUID().toString())
                                .accessKey(accessKey)
                                .extension(extension)
                                .imageName(prefix+String.format("%0"+digit+"d.%s", num, extension))
                                .build();
                        retFileInfo.getImages().add(info);
                    });

                    return retFileInfo;
//                } else {
//                    log.info("<<<<< FAIL CONVERT TO IMAGES");
//                }
            } else {
                log.info("<<<<< FAIL READ document.json :: not exists");
            }
        } else {
            log.info("<<<<< FAIL READ document.json :: not exists or not folder");
        }
        return null;
    }
}
