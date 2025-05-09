package com.ej.hgj.task.carrenew;

import com.ej.hgj.dao.cron.CronDaoMapper;
import com.ej.hgj.entity.cron.Cron;
import com.ej.hgj.task.carpay.service.CarPayTaskService;
import com.ej.hgj.task.carrenew.service.CarRenewTaskService;
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
public class CarRenewFinishTask implements SchedulingConfigurer {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CronDaoMapper cronDaoMapper;

    @Autowired
    private CarRenewTaskService carRenewTaskService;

    /**
     * 新弘月租车续费支付完成后支付中订单定时任务
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addTriggerTask(() -> carRenewTaskService.updateOrderStatus_1(), triggerContext -> {
            Cron cronByType = cronDaoMapper.getByType("13");
            String cron = cronByType.getCron();
            return new CronTrigger(cron).nextExecutionTime(triggerContext);
        });
    }
}
