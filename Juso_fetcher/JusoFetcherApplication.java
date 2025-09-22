package com.jusoFetcher;

import org.springframework.boot.SpringAppliocation;
import org.springframework.boot.autoconfigure.SpringBootAppliocation;
import org.springframework.boot.builder.SpringAppliocationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
* @FileName : JusoFetcherApplication.java
* @Project  : Juso_fetcher
* @Date     : 2025. 6. 20.
* @작성자	  : 현태호
*
* @프로그램 설명 : 주소 연계 배치 프로그램의 메인 클래스
* Spring Boot 기반으로 스케줄러를 등록하고 도로명주소 변동 자료를 연계 API로부터 자동 다운로드
*/
@SpringBootApplication
@EnableScheduling
public class JusoFetcherApplication extends SpringBootServletInitializer {

    /**
     * 외부 서블릿 컨테이너(톰캣)에서 애플리케이션이 실행될 때 호출되는 설정 메서드
     * WAR 파일로 배포되는 경우, main 메서드 대신 이 메서드를 통해 애플리케이션이 초기화됨
     *
     * @param builder 애플리케이션 구성을 위한 {@link SpringApplicationBuilder}
     * @return 애플리케이션의 설정 정보를 포함한 builder 객체
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    	return builder.sources(JusoFetcherApplication.class);
    }
    
    public static void main(String[] args) {
    	SpringApplication.run(JusoFetcherApplication.class, args);
    }
}