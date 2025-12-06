package com.addressApi.batch;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Component;

import com.addressApi.mapper.AddressChangeMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
/**
 * @FileName : AddressChangeWriter.java
 * @Project  : juso_api
 * @Date     : 2025. 6. 19.
 *
 * @프로그램 설명 : TEMP 테이블에 주소 변경 데이터를 누적 INSERT 처리하는 Writer 클래스
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class AddressChangeWriter {

    private final SqlSessionFactory sqlSessionFactory;

    /**
     * @Method 설명 : TEMP 테이블에 주소 변경 데이터를 누적 INSERT
     * @param rows 파싱된 주소 변경 데이터 리스트
     */
    public void write(List<AddressChangeRow> rows) {
        Log.info("AddressChangeWriter 시작 - 총 {}건", rows.size());

        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, false); // 수동 커밋

        try {
            AddressChangeMapper mapper = session.getMapper(AddressChangeMapper.class);
            String stdDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            for (AddressChangeRow row : rows) {
                row.setStdDate(stdDate); // yyyyMMdd 문자열 세팅
                mapper.insertTempAddress(row);
            }

            session.commit();
            Log.info("AddressChangeWriter 커밋 완료 - 총 {}건", rows.size());

            // 프로시저 호출 (SP_ROAD_NM_ADDR_CHG_RFLCT)
            try {
                mapper.callRoadNameAddrChangeReflectProcedure();
                Log.info("도로명주소 변경분 반영 프로시저 호출 완료");
            } catch (Exception e) {
                Log.error("도로명주소 변경분 반영 프로시저 호출 실패", e.getMessage(), e);
            }

        } catch (Exception e) {
            session.rollback();
            Log.error("AddressChangeWriter 트랜잭션 롤백 발생 - 에러: {}", e.getMessage(), e);
            
        } finally {
            session.close();
        }
    }
}