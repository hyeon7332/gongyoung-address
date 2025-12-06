package com.addressApi.batch;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @FileName  : AddressChangeRow.java
 * @Project   : Juso_api
 * @Date      : 2025. 6. 16.
 *
 * @프로그램 설명 : 변동자료 DTO
 */
@Getter @Setter
@ToString
public class AddressChangeRow {
    private String roadNmCt1No;     // 도로명관리번호 PK
    private String lgdongOsdcd;     // 법정동코드
    private String sidoNm;          // 시도명
    private String sigunguNm;       // 시군구명
    private String epmyndnNm;		// 읍면동명

    private String riNm;            // 리명
    private String muntnYn;         // 산 여부 (0:대지, 1:산)
    private String upperHsno;      	// 번지 (상위번지)
    private String subHsno;        	// 호 (하위번지)
    private String roadNmCd;        // 도로명코드

    private String roadNm;          // 도로명
    private String undGrdYn;       	// 지하여부
    private String bldUpperNo;      // 건물본번
    private String bldSubNo;        // 건물부번
    private String addongOsdcd;     // 행정동코드

    private String addongEpmyndnNm; // 행정동명
    private String zipNo;           // 우편번호
    private String befRoadNmAddr;   // 이전도로명주소
    private String applyBgnDate;    // 효력발생일
    private String cmmBldYn;        // 공동주택여부

    private String mvRsnCd;         // 이동사유코드 (31:신규, 34:수정, 63:폐지)
    private String instBldNm;       // 등록건물명
    private String bldNm;           // 건물명
    private String note;            // 비고
    
    private String stdDate;         // 기준일자
}