package com.jaden.processmongo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "filepathinfo")
public class FilePathInfo {
    private String input;
    private String output;
    private String localOutput;
}
