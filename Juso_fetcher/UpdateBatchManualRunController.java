package com.jusoFetcher;

import com.juso.fetcher.batch.UpdateScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

/**
 * 연계배치 수동 실행을 위한 컨트롤러 클래스입니다.
 * 
 * 개발 또는 운영 환경에서 연계 주소 배치(UpdateScheduler)를 수동으로 실행하고자 할 때
 * 사용되는 엔드포인트입니다. 이 컨트롤러는 배치 로직이 담긴 UpdateScheduler를 직접 호출하며,
 * 마지막 성공일 파일(last_success_date.txt)에 기록된 날짜 기준으로 
 * 현재 날짜까지 필요한 날짜 범위의 데이터를 다운로드 시도합니다.
 * 
 * 참고: 특정 날짜를 지정하여 받는 기능은 제공하지 않으며, 필요한 경우 last_success_date.txt 파일을
 * 수동으로 수정 후 해당 API를 호출하십시오.
 * 
 */
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
@Slf4j
public class UpdateBatchManualRunController {

    private final UpdateScheduler updateScheduler;

    @PostMapping("/run-update")
    public ResponseEntity<String> runUpdateBatchManually() {
        log.info("연계 주소 배치 수동 실행 시작");
        updateScheduler.downloadDailyUpdate();
        log.info("연계 주소 배치 수동 실행 완료");
        return new ResponseEntity<>("연계 주소 배치 수동 실행 완료", HttpStatus.OK);
    }
}