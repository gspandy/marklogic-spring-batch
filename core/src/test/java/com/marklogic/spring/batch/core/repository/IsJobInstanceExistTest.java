package com.marklogic.spring.batch.core.repository;

import com.marklogic.spring.batch.AbstractSpringBatchCoreTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;

public class IsJobInstanceExistTest extends AbstractSpringBatchCoreTest {

    private final String jobName = "job";
    private JobParameters params;
    private JobRepository jobRepository;

    @Before
    public void JobExecution() throws JobExecutionAlreadyRunningException, JobRestartException,
        JobInstanceAlreadyCompleteException, Exception {
        jobRepository = getMarklogicBatchConfigurer().getJobRepository();
        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addLong("count", 123L);
        jobRepository.createJobExecution(jobName, builder.toJobParameters());
        params = builder.toJobParameters();
    }

    @Test
    public void verifyJobInstanceExistsTest() {
        assertTrue(jobRepository.isJobInstanceExists(jobName, params));
    }

    @Test
    public void verifyJobInstanceDoesNotExistWithJobNameTest() {
        assertFalse(jobRepository.isJobInstanceExists(jobName + "-test", params));
    }

    @Test
    public void verifyJobInstanceDoesNotExistWithJobParametersTest() {
        JobParametersBuilder builder = new JobParametersBuilder(params);
        builder.addLong("second", 100L, true);
        assertFalse(jobRepository.isJobInstanceExists(jobName, builder.toJobParameters()));
    }

}
