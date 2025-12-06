package com.addressApi.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 상세주소동표시(_dong.txt) 파일 파서
 * 기존 AddressChangeFileReader를 건드리지 않고, 이 클래스를 별도로 추가하여 분리 처리합니다.
 */
@Slf4j
@Component
public class AddressDongDetailFileReader {

    /**
     * @Method 설명 : _dong.txt 파일을 한 줄씩 읽어 AddressDongDetailRow 리스트로 반환
     * @param file 압축 해제된 텍스트 파일 (_dong.txt)
     * @return AddressDongDetailRow 리스트
     * @throws Exception 파일 읽기 실패 시
     */
    public List<AddressDongDetailRow> read(File file) throws Exception {
        List<AddressDongDetailRow> rows = new ArrayList<>();

        if (file == null || !file.exists()) {
            throw new FileNotFoundException("대상 파일이 존재하지 않습니다: " + file);
        }

        // 필요 시 EUC-KR/MS949 유지 (연계 파일 인코딩에 맞춰 조정)
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), Charset.forName("MS949")))) {

            String line;
            int lineNo = 0;

            while ((line = reader.readLine()) != null) {
                lineNo++;
                String[] tokens = line.split("\\|", -1); // 빈 항목 유지

                // ★ 실제 스펙에 맞는 기대 컬럼 수로 변경하세요 (예: 12)
                final int EXPECTED_COLS = 12;
                if (tokens.length < EXPECTED_COLS) {
                    log.warn("{}번째 줄: 필드 수 불일치 ({}개): {}", lineNo, tokens.length, line);
                    continue;
                }

                AddressDongDetailRow row = new AddressDongDetailRow();
                // ★ 인덱스 매핑을 실제 스펙에 맞게 조정하세요.
                row.setSidoCd(nullIfEmpty(tokens[0]));
                row.setSigunguCd(nullIfEmpty(tokens[1]));
                row.setEmdCd(nullIfEmpty(tokens[2]));
                row.setRiCd(nullIfEmpty(tokens[3]));
                row.setAdminType(nullIfEmpty(tokens[4]));
                row.setDongNm(nullIfEmpty(tokens[5]));
                row.setBldMainNo(nullIfEmpty(tokens[6]));
                row.setBldSubNo(nullIfEmpty(tokens[7]));
                row.setDong(nullIfEmpty(tokens[8]));
                row.setHo(nullIfEmpty(tokens[9]));
                row.setFloorNo(nullIfEmpty(tokens[10]));
                row.setNote(nullIfEmpty(tokens[11]));

                if (!validateRequired(row)) {
                    log.warn("{}번째 줄: 필수 필드 누락 SKIP: {}", lineNo, row);
                    continue;
                }

                rows.add(row);
            }
        }

        log.info("AddressDongDetailRow 총 {}건 로딩 완료", rows.size());
        return rows;
    }

    private static boolean validateRequired(AddressDongDetailRow r) {
        // ★ 실제 필수 필드 기준으로 수정하세요.
        // 예시: 시도/시군구/읍면동 + 본번은 필수로 가정
        if (isNullOrEmpty(r.getSidoCd())) { return false; }
        if (isNullOrEmpty(r.getSigunguCd())) { return false; }
        if (isNullOrEmpty(r.getEmdCd())) { return false; }
        if (isNullOrEmpty(r.getBldMainNo())) { return false; }
        return true;
    }

    private static String nullIfEmpty(String token) {
        if (token == null) {
            return null;
        }
        String t = token.trim();
        if (t.isEmpty()) {
            return null;
        }
        return t;
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
