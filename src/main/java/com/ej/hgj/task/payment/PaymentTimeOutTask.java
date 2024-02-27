package com.ej.hgj.task.payment;

import com.ej.hgj.dao.cron.CronDaoMapper;
import com.ej.hgj.entity.cron.Cron;
import com.ej.hgj.task.payment.service.PaymentTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

@Configuration
@EnableScheduling
public class PaymentTimeOutTask implements SchedulingConfigurer {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CronDaoMapper cronDaoMapper;

    @Autowired
    private PaymentTaskService paymentTaskService;

    /**
     * 超时订单定时任务
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addTriggerTask(() -> paymentTaskService.updatePaymentStatus_5(), triggerContext -> {
            Cron cronByType = cronDaoMapper.getByType("6");
            String cron = cronByType.getCron();
            return new CronTrigger(cron).nextExecutionTime(triggerContext);
        });
    }
}
