package com.addressApi.batch;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @FileName : AddressBatchQuartzJob.java
 * @Project  : Juso_api
 * @Date     : 2025. 6. 16.
 
 * @프로그램 설명 : Quartz 기반 주소 일배치 Job
 * 압축파일(zip) 해제 -> 텍스트 파일 파싱 -> mvRsnCd(이동사유코드)를 기준으로 insert/update/delete 수행
 * 스프링배치 없이 Quartz + Plain JDBC 방식으로 동작하며, 
 * AddressChangeWriter 내부에서 mvRsnCd에 따라 분기 처리
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class AddressBatchQuartzJob implements Job {

    private final ZipExtractor zipExtractor;
    private final AddressChangeFileReader fileReader;
    private final AddressChangeWriter writer;
    private final AddressChangeProperties properties;

    /**
     * Quartz에서 실행되는 메인 배치 메서드
     * 
     * @param context Quartz JobExecutionContext
     * @throws JobExecutionException 실행 중 예외 발생 시
     */
    @Override 
    public void execute(JobExecutionContext context) throws JobExecutionException { 
        log.info("주소 일배치 시작"); 
        
        try { 
            String zipRootDir = properties.getPath().getZipDir(); 
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")); 
            String zipDir = zipRootDir + "/" + today; 
            String extractDir = properties.getPath().getExtractDir(); 
            
            // 1) 도로명주소한글 (JUSUKR → *_mst.txt) 
            try { 
                log.info("[1/2] JUSUKR 처리 시작"); 
                File krFile = zipExtractor.extractTextFile(zipDir, extractDir, "JUSUKR", "_mst.txt"); 
                List<AddressChangeRow> krRows = fileReader.read(krFile); 
                writer.write(krRows, DatasetType.ROAD_KR); 
                log.info("[1/2] JUSUKR 처리 완료 - {}건", krRows.size()); 
            } catch (Exception e) { 
                log.error("[1/2] JUSUKR 처리 중 예외(다음 단계 계속): {}", e.getMessage(), e); 
            } 
            
            // 2) 상세주소동표시 (JUSDG → *_dong.txt) 
            try { 
                log.info("[2/2] JUSDG 처리 시작"); 
                File dgFile = zipExtractor.extractTextFile(zipDir, extractDir, "JUSDG", "_dong.txt"); 
                List<AddressChangeRow> dgRows = fileReader.read(dgFile); 
                writer.write(dgRows, DatasetType.DONG_DETAIL); 
                log.info("[2/2] JUSDG 처리 완료 - {}건", dgRows.size()); 
            } catch (Exception e) { 
                log.error("[2/2] JUSDG 처리 중 예외: {}", e.getMessage(), e); 
            } log.info("주소 일배치 완료");
            
        } catch (Exception e) { 
            log.error("주소 일배치 최상위 예외", e); throw new JobExecutionException(e); 
        } 
    }
}