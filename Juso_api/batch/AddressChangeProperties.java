package com.addressApi.batch;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * @FileName  : AddressChangeProperties.java
 * @Project   : Juso_api
 * @Date      : 2025. 6. 24.
 *
 * @프로그램 설명 : 주소 갱신 일배치에 필요한 외부 설정값을 주입받는 프로퍼티 클래스
 */
@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "address-change")
@Component
public class AddressChangeProperties {
    private Schedule schedule;
    private Path path;
    private boolean enabled;

    @Getter @Setter
    public static class Schedule {         
        private String cron;			// Quartz 크론 표현식 (예: "0 0 1 * * ?")
        private boolean enabled = true;	// 배치 실행 여부 설정 (true면 실행, false면 스킵)
    }

    @Getter @Setter
    public static class Path {                 
        private String zipDir;			// ZIP 파일이 다운로드되는 경로        
        private String extractDir;		// ZIP 압축이 해제되는 경로
    }
}