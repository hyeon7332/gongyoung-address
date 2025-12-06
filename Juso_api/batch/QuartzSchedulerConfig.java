package com.addressApi.batch;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @FileName  : QuartzSchedulerConfig.java
 * @Project   : Juso_api
 * @Date      : 2025. 6. 16.
 *
 * @프로그램 설명 : Quartz 스케줄러 설정 클래스
 * 매일 지정된 cron 시간에 AddressBatchQuartzJob을 실행하도록 트리거를 설정
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class QuartzSchedulerConfig {

    private final AddressChangeProperties properties;

    /**
     * @Method 설명 : Quartz JobDetail 설정
     * @return JobDetail 객체 (AddressBatchQuartzJob 등록)
     */
    @Bean
    JobDetail addressChangeJobDetail() {
        return JobBuilder.newJob(AddressBatchQuartzJob.class)
                .withIdentity("addressChangeJob")
                .storeDurably()
                .build();
    }

    /**
     * @Method 설명 : Quartz Trigger 설정 (cron 스케줄 기반 실행)
     * @return Trigger 객체
     */
    @Bean
    Trigger addressChangeTrigger() {
        if (!properties.getSchedule().isEnabled()) {
            log.info("addressChangeTrigger 등록되지 않음 (enabled=false)");
            return null;
        }
        log.info("addressChangeTrigger 등록됨 -> cron: {}", properties.getSchedule().getCron());

        return TriggerBuilder.newTrigger()
                .forJob(addressChangeJobDetail())
                .withIdentity("addressChangeTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(properties.getSchedule().getCron()))
                .build();
    }
}