package com.github.alura.reminder.config;

import com.github.alura.reminder.scheduler.ReminderJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {
    @Bean
    public JobDetail reminderJobDetail() {
        return JobBuilder.newJob(ReminderJob.class)
                .withIdentity("reminderJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger reminderJobTrigger(JobDetail reminderJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(reminderJobDetail)
                .withIdentity("reminderTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(1)
                        .repeatForever())
                .build();
    }
}
