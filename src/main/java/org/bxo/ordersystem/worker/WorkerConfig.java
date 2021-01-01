package org.bxo.ordersystem.worker;

import org.jobrunr.jobs.mappers.JobMapper;
import org.jobrunr.storage.InMemoryStorageProvider;
import org.jobrunr.storage.StorageProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class WorkerConfig {

    // @Bean
    // MyBean myBean () {
    //     return new MyBean();
    // }

    @Value("${org.jobrunr.background-job-server.worker_count:10}")
    private Long workerCount;

    @Bean
    AsyncTaskExecutor workerTaskExecutor () {
        SimpleAsyncTaskExecutor t = new SimpleAsyncTaskExecutor();
        t.setConcurrencyLimit(100);
        return t;
    }

    // InMemoryStorageProvider to store the job details
    // The`spring-boot-starter-web` provides Jackson as JobMapper
    @Bean
    public StorageProvider storageProvider(JobMapper jobMapper) {
        InMemoryStorageProvider storageProvider = new InMemoryStorageProvider();
        storageProvider.setJobMapper(jobMapper);
        return storageProvider;
    }

}
