package com.jaden.processmongo.controller;

import com.jaden.processmongo.dao.FileInfo;
import com.jaden.processmongo.service.ConvertService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/convert/v1")
public class ConvertController {

    private final ConvertService convertService;

    @ApiOperation(value = "Convert to images", notes = "이미지 변환 요청 (테스트에서는 accessKet, fileName 값이 필요함)")
    @PostMapping(path = "/images")
    public ResponseEntity<?> convertToImages(@RequestBody Map<String, Object> accessInfo) {
        FileInfo fileInfo;
        try {
            fileInfo = convertService.convertToImages(accessInfo);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e);
        }
        if (fileInfo != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(fileInfo);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Convert Failed. Please notify us.");
    }

    @ApiOperation(value = "Get images list", notes = "이미지 목록 요청")
    @GetMapping(path = "/images")
    @ResponseBody
    public ResponseEntity<?> getImageList(@RequestParam String accessKey) {
        FileInfo fileInfo = convertService.getImageList(accessKey);
        if (fileInfo != null && fileInfo.getImages().size() > 0) {
            return ResponseEntity.ok().body(fileInfo.getImages());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document converting is on going.");
    }

    @ApiOperation(value = "Get a image", notes = "이미지 다운로드")
    @GetMapping(path = "/images/{accessKey}/{imageName}")
    @ResponseBody
    public ResponseEntity<?> getImage(@PathVariable String accessKey, @PathVariable String imageName) {
        File imageFile = convertService.getImage(accessKey, imageName);
        if (imageFile.length() > 0) {
            try {
                return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG)
                        .body(IOUtils.toByteArray(new FileInputStream(imageFile)));
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No image");
    }
}
