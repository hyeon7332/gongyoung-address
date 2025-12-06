package com.addressApi.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @FileName  : AddressChangeFileReader.java
 * @Project   : Juso_api
 * @Date      : 2025. 6. 19.
 *
 * @프로그램 설명 : 주소 변경 텍스트 파일(.txt)을 읽고 AddressChangeRow 객체 리스트로 변환하는 클래스
 * 파일은 '|' 구분자 기반의 24개의 필드로 구성되며, 빈값도 포함
 */

@Slf4j
@Component
public class AddressChangeFileReader {

    /**
     * @Method 설명 : 테스트 파일을 한 줄씩 읽어 AddressChangeRow 리스트로 반환
     * @param file 압축 해제된 텍스트 파일
     * @return AddressChangeRow 리스트
     * @throws IOException 파일 읽기 실패 시
     */
    public List<AddressChangeRow> read(File file) throws IOException {
        List<AddressChangeRow> rows = new ArrayList<>();

        if (file == null || !file.exists()) {
            throw new FileNotFoundException("대상 파일이 존재하지 않습니다: " + file);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("MS949")))) {
            String line;
            int lineNo = 0;

            while ((line = reader.readLine()) != null) {
                lineNo++;
                String[] tokens = line.split("\\|", -1); // 빈 항목 유지

                if (tokens.length < 24) {
                    log.warn("{}번째 줄: 필드 수 불일치 ({}개): {}", lineNo, tokens.length, line);
                    continue;
                }

                if (isNullOrEmpty(tokens[0]) ||  
                    isNullOrEmpty(tokens[1]) || 
                    isNullOrEmpty(tokens[9]) || 
                    isNullOrEmpty(tokens[11]) || 
                    isNullOrEmpty(tokens[12]) ||
                    isNullOrEmpty(tokens[13]))
                {
                    log.warn("필수 필드 누락 SKIP: 도로명관리번호={}, 법정동코드={}, 도로명코드={}, 지하여부={}, 건물상위번호={}, 건물하위번호={}",
                        tokens[0], tokens[1], tokens[9], tokens[11], tokens[12], tokens[13]);
                    continue;	// 이 row는 건너뜀
                }

                AddressChangeRow row = new AddressChangeRow();
                row.setRoadNmCtlNo(tokens[0]);
                row.setLgdongOsdcd(tokens[1]);
                row.setSidoNm(nullIfEmpty(tokens[2]));
                row.setSigunguNm(nullIfEmpty(tokens[3]));
                row.setEpmyndnNm(nullIfEmpty(tokens[4]));

                row.setRiNm(nullIfEmpty(tokens[5]));
                row.setMuntnYn(nullIfEmpty(tokens[6]));            
                row.setUpperHsno(nullIfEmpty(tokens[7]));
                row.setSubHsno(nullIfEmpty(tokens[8]));
                row.setRoadNmCd(nullIfEmpty(tokens[9]));

                row.setRoadNm(nullIfEmpty(tokens[10]));
                row.setUndGrdYn(nullIfEmpty(tokens[11]));
                row.setBldUpperNo(nullIfEmpty(tokens[12]));
                row.setBldSubNo(nullIfEmpty(tokens[13]));
                row.setAddongOsdcd(nullIfEmpty(tokens[14]));

                row.setAddongEpmyndnNm(nullIfEmpty(tokens[15]));
                row.setZipNo(nullIfEmpty(tokens[16]));
                row.setBefRoadNmAddr(nullIfEmpty(tokens[17]));
                row.setApplyBgnDate(nullIfEmpty(tokens[18]));
                row.setCmmBldYn(nullIfEmpty(tokens[19]));
				
                row.setMvRsnCd(nullIfEmpty(tokens[20]));
                row.setInstBldNm(nullIfEmpty(tokens[21]));
                row.setBldNm(nullIfEmpty(tokens[22]));
                row.setNote(nullIfEmpty(tokens[23]));

                rows.add(row);
            }
        }
        log.info("AddressChangeRow 총 {}건 로딩 완료", rows.size());
        return rows;
    }

    private static String nullIfEmpty(String token) {
        return (token == null || token.trim().isEmpty()) ? null : token.trim();
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}