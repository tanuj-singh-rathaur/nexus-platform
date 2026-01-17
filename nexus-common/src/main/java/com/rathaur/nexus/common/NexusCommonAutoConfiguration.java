package com.rathaur.nexus.common;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
//This scans "common" and ALL sub-packages (security, utils, future stuff...)
@ComponentScan(basePackages = "com.rathaur.nexus.common") 
public class NexusCommonAutoConfiguration {
    // This class is empty. Its only job is to trigger the scan.
}