/**
 * @FileName  : UpdateScheduler.java
 * @Project   : juso_fetcher
 * @Date      : 2025. 6. 16.
 * @작성자      : 현태호
 * @설명       : 주소 연계 배치 스케줄러
 * 매일 오전 지정된 시간에 도로명주소 변동 자료를 연계API를 통해 다운로드
 * 이전 실행일자를 기준으로 누락된 일수를 계산하여, 최대 10일치까지 자동으로 복구 처리
 * 마지막 성공일자는 로그 파일('logs/last_success_date.txt')에 기록되며, 다운로드 성공 여부에 따라 날짜를 갱신
 * 
 * {@code download-path} : 다운로드 대상 폴더 경로 (.yml)
 * {@code scheduler.cron} : 스케줄 실행 주기 설정 (.yml)
 */
@Component
@Slf4j
public class UpdateScheduler {

    private static final String LAST_DATE_FILE = "logs/last_success_date.txt"; // 마지막 성공일 저장 경로
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${download-path}")
    private String filePath;

    /**
     * @Method 설명 : 매일 지정된 시간에 자동 실행되는 주소 다운로드 배치 작업
     */ 
    @Scheduled(cron = "${schedule.cron}")
    public void downloadDailyUpdate() {
        LocalDate today = LocalDate.now();
        LocalDate lastSuccessDate = readLastSuccessDate();

        long daysGap = ChronoUnit.DAYS.between(lastSuccessDate, today);
        log.info("오늘 : {}, 마지막성공일: {}, 누락일: {}일", today, lastSuccessDate, daysGap);

        if (daysGap > 10) {
            log.error("누릭일 {}일 → 10일 초과로 자동 다운로드 불가. 수동 처리 필요!", daysGap);
            return;
        }

        for (int i = 1; i <= daysGap; i++) {
            LocalDate target = lastSuccessDate.plusDays(i);
            String dateStr = target.format(FORMATTER);

            try {
                // 요청 생성 (성공 여부 반환받음)
                UpdateClient client = new UpdateClient(filePath);
                boolean isUpdated = client.getAddrInfo(dateStr, dateStr); // from = to = 해당 날짜

                if (isUpdated) {
                    log.info("{} 다운로드 성공", dateStr);
                } else {
                    log.info("{} 자료 없음 또는 최신 상태입니다.", dateStr);
                }

                // 성공할 경우 날짜 갱신
                writeLastSuccessDate(target);

            } catch (Exception e) {
                log.error("{} 다운로드 실패", dateStr, e);
                break;	// 실패 시 반복 중단
            }
        }
    }

    /**
     * @Method 설명 : 다운로드 성공(또는 최신 상태)을 경우 파일에 날짜 저장
     * @param target
     */
    private void writeLastSuccessDate(LocalDate date) {
        try {
            Path dir = Paths.get("logs");
            if (!Files.exists(dir)) Files.createDirectories(dir);

            Files.writeString(Paths.get(LAST_DATE_FILE), date.format(FORMATTER),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.error("마지막 성공일 저장 실패", e);
        }
    }

    /**
     * @Method 설명 : 마지막 성공일을 파일에서 읽어 반환 (파일 없으면 어제로 간주)
     * @return
     */
    private LocalDate readLastSuccessDate() {
        try {
        	Path path = Paths.get(LAST_DATE_FILE);
            if (!Files.exists(path)) {
                return LocalDate.now().minusDays(1); // 기본값 : 어제
            }
            String content = Files.readString(path).trim();
            return LocalDate.parse(content, FORMATTER);
        } catch (Exception e) {
            log.warn("마지막 성공일 읽기 실패 - 기본값 사용", e);
            return LocalDate.now().minusDays(1);
        }
    }
}