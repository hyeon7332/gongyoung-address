package com.jusoFetcher;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import kr.go.ads.client.ADSReceiver;
import kr.go.ads.client.ADSUtils;
import kr.go.ads.client.ReceiveData;
import kr.go.ads.client.ReceiveDatas;
import lombok.extern.slf4j.Slf4j;

/**
 * @FileName   : UpdateClient.java
 * @Project    : Juso_fetcher
 * @Date       : 2025. 6. 16.
 * @작성자     : 현태호
 *
 * 주소 연계 API 호출 클라이언트 ▒
 * - 요청코드(100001)로 행안부 API를 통해 요청하며, 설정된 경로에 ZIP 파일을 저장
 * - 연계 응답에 따라 다운로드 성공, 최신 상태, 자료 없음 등을 구분 처리하며 로그 및 성공 여부를 반환
 *
 * {@code filePath} : 다운로드 저장 경로 (디렉토리 자동 생성)
 * {@code getAddrInfo()} : 특정 기간 주소 다운로드 요청
 *
 * ADSClient 라이브러리 (ADSReceiver, ADSUtils 등)
 */
@Slf4j
public class UpdateClient {

    private final String filePath;

    /**
     * 요청 클라이언트 생성자 (디렉토리 자동 생성 포함)
     * @param filePath 저장 경로
     */
    public UpdateClient(String filePath) {
        this.filePath = filePath;

        // 다운로드 경로 없으면 자동 생성
        try {
            Files.createDirectories(Paths.get(filePath));
        } catch (Exception e) {
            log.error("다운로드 폴더 생성 실패: {}", filePath, e);
            throw new RuntimeException("폴더 생성 실패", e);
        }
    }

    /**
     * @Method 설명 : 주소 연계 요청 실행 (단일 날짜 기준)
     * @param fromDate 시작일 (YYYYMMDD)
     * @param toDate 종료일 (YYYYMMDD)
     * @return true : 데이터 있음 / false : 이전 상태 유지로 응답
     */
    public boolean getAddrInfo(String fromDate, String toDate) {
        try {
            ADSReceiver ads = new ADSReceiver();
            ads.setFilePath(filePath);                         // 다운로드 경로 설정
            ads.setCreateDateDirectory(ADSUtils.YYMMDD);       // 날짜 폴더 생성방식

            // 필수 파라미터 설정
            String app_key = "U01TX0FVVEgyMDI1MDUzMDE1NDMxODExNTgwNzk="; // 발급받은 승인키
            String date_gb = "D";     // D: 일변동
            String retry_in = "Y";    // 재반영 여부 (Y or N)

			// 요청 구분 코드 목록 (확장 가능)
            String[] cntcdClist = { 
            	"100001", 	// 도로명주소 한글
                "100004"	// 상세주소동
            };               

            boolean anyUpdated = false;

            for (String cntc_cd : cntcdClist) {
                log.info("요청 시작 - cntc_cd:{}, date:{}, to:{}", cntc_cd, fromDate, toDate);

                ReceiveDatas res = ads.receiveAddr(app_key, date_gb, cntc_cd, retry_in, fromDate, toDate);

                // 응답 코드 처리
                String resCode = res.getResCode();

                switch (resCode) {
                    case "P0000": {
                        // 정상 응답 - 파일 목록 확인
                        ArrayList<ReceiveData> result = res.getReceiveDatas(ADSUtils.UPDATE_ASC);
                        for (ReceiveData data : result) {
                            log.info("다운로드 완료 : {} (파일명: {})", data.getCntcCode(), data.getFileName());
                        }
                        anyUpdated = true;
                        break;
                    }

                    case "P1000":
                    case "E1001": {
                        // 최신 상태(P1000) or 파일 없음(E1001) → 정상 종료로 처리
                        log.info("최신 상태 또는 자료 없음 (cntc_cd:{}, date:{})", cntc_cd, fromDate);
                        break;
                    }

                    default: {
                        // 그 외 에러는 예외 처리
                        log.error("연계 오류 발생 (code:{}, message:{})", resCode, res.getResMsg());
                        throw new RuntimeException("연계 실패: " + resCode + " - " + res.getResMsg());
                    }
                }
            }

            return anyUpdated;

        } catch (Exception e) {
            Log.error("주소 연계 요청 중 예외 발생 (날짜: {})", fromDate, e);
            throw new RuntimeException("주소 연계 요청 실패", e);
        }
    }
}