package com.addressApi.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Component
public class ZipExtractor {

    /**
     * ZIP 선택(파일명에 nameContains 포함) → ZIP 내부에서 entrySuffix로 끝나는 첫 파일만 추출
     * 예)
     *  - JUSUKR: nameContains = "JUSUKR", entrySuffix = "_mst.txt"
     *  - JUSDG:  nameContains = "JUSDG", entrySuffix = "_dong.txt"
     *
     * @param zipDir        ZIP 파일들이 위치한 날짜 폴더 (예: D:/just-download/250901)
     * @param extractRoot   압축해제 루트 (예: D:/just-extracted)
     * @param nameContains  ZIP 파일명 필터(대소문자 무시)
     * @param entrySuffix   ZIP 내부 엔트리 접미사(대소문자 무시)
     * @return              추출된 파일 객체
     */
    public File extractTextFile(String zipDir, String extractRoot, String nameContains, String entrySuffix) throws IOException {
        if (nameContains == null || nameContains.isEmpty()) {
            throw new IllegalArgumentException("nameContains는 필수입니다.");
        }
        if (entrySuffix == null || entrySuffix.isEmpty()) {
            throw new IllegalArgumentException("entrySuffix는 필수입니다.");
        }

        File zipDirectory = new File(zipDir);
        if (!zipDirectory.exists() || !zipDirectory.isDirectory()) {
            throw new IllegalArgumentException("ZIP 디렉터리가 존재하지 않습니다: " + zipDir);
        }

        // 날짜 폴더명 추출 (예: 250901)
        String[] parts = zipDir.replace("\\", "/").split("/");
        String datePart = parts[parts.length - 1];

        // 압축 해제 루트/날짜 하위 폴더 생성
        File targetDir = new File(extractRoot, datePart);
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IOException("압축 해제 대상 디렉토리 생성 실패: " + targetDir.getAbsolutePath());
        }

        final String needle = nameContains.toLowerCase();
        File[] zipFiles = zipDirectory.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            if (!lower.endsWith(".zip")) {
                return false;
            }
            return lower.contains(needle);
        });

        if (zipFiles == null || zipFiles.length == 0) {
            throw new FileNotFoundException("조건(nameContains=" + nameContains + ")에 맞는 .zip 파일이 없습니다: " + zipDir);
        }

        // 여러 개면 첫 번째만 사용 (운영 정책 상 날짜/종류별 1개 가정)
        File zipFile = zipFiles[0];
        log.info("[{}] 압축 해제 대상 ZIP: {}", nameContains, zipFile.getAbsolutePath());

        return extractFirstEntryBySuffix(zipFile, targetDir, entrySuffix);
    }

    /**
     * 단일 ZIP에서 entrySuffix로 끝나는 첫 파일만 추출
     */
    private File extractFirstEntryBySuffix(File zipFile, File targetDir, String entrySuffix) throws IOException {
        final String suffixLower = entrySuffix.toLowerCase();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry = zis.getNextEntry();

            while (entry != null) {
                String fileName = entry.getName();
                if (fileName != null && fileName.toLowerCase().endsWith(suffixLower)) {
                    File extractedFile = new File(targetDir, fileName);

                    // ZIP 내부에 폴더 경로가 포함될 수 있으므로 상위 폴더 생성
                    File parent = extractedFile.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw new IOException("하위 디렉토리 생성 실패: " + parent.getAbsolutePath());
                    }

                    try (FileOutputStream fos = new FileOutputStream(extractedFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }

                    log.info("압축 해제 완료: {}", extractedFile.getAbsolutePath());
                    return extractedFile;
                }
                entry = zis.getNextEntry();
            }
        }

        throw new FileNotFoundException("ZIP 내부에 '" + entrySuffix + "' 파일이 존재하지 않습니다. (" + zipFile.getName() + ")");
    }
}
