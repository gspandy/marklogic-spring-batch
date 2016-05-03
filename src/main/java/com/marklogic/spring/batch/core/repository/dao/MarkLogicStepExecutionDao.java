package com.marklogic.spring.batch.core.repository.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.StepExecutionDao;
import org.springframework.util.Assert;

import com.marklogic.client.DatabaseClient;
import com.marklogic.spring.batch.core.AdaptedStepExecution;

public class MarkLogicStepExecutionDao extends AbstractMarkLogicBatchMetadataDao implements StepExecutionDao {
	
	private static final Log logger = LogFactory.getLog(MarkLogicJobInstanceDao.class);
	
	private JobExecutionDao jobExecutionDao;
	
	public MarkLogicStepExecutionDao(DatabaseClient databaseClient) {
		this.databaseClient = databaseClient;
	}

	@Override
	public void saveStepExecution(StepExecution stepExecution) {
		Assert.isTrue(stepExecution.getId() == null);
		Assert.isTrue(stepExecution.getVersion() == null);
		
		Assert.notNull(stepExecution.getJobExecutionId(), "JobExecution must be saved already.");
		JobExecution jobExecution = jobExecutionDao.getJobExecution(stepExecution.getJobExecutionId());
		Assert.notNull(jobExecution, "JobExecution must be saved already.");
		
		validateStepExecution(stepExecution);
		
		stepExecution.setId(incrementer.nextLongValue());
		stepExecution.incrementVersion();
		
		List<StepExecution> stepExecutions = new ArrayList<StepExecution>();
		stepExecutions.add(stepExecution);
		jobExecution.addStepExecutions(stepExecutions);
		jobExecutionDao.updateJobExecution(jobExecution);
		logger.info("insert step execution: " + stepExecution.getId() + ",jobExecution:" + jobExecution.getId());
    	return;

	}

	@Override
	public void saveStepExecutions(Collection<StepExecution> stepExecutions) {
		Assert.notNull(stepExecutions, "Attempt to save a null collection of step executions");

        if (!stepExecutions.isEmpty()) {
        	
        	Long jobExecutionId = stepExecutions.iterator().next().getJobExecutionId();
        	Assert.notNull(jobExecutionId, "JobExecution must be saved already.");
    		JobExecution jobExecution = jobExecutionDao.getJobExecution(jobExecutionId);
    		Assert.notNull(jobExecution, "JobExecution must be saved already.");
        	
        	List<StepExecution> stepExecutionList = new ArrayList<StepExecution>();
        	
        	for (StepExecution stepExecution : stepExecutions) {
        		Assert.isTrue(stepExecution.getId() == null);
        		Assert.isTrue(stepExecution.getVersion() == null);
        		validateStepExecution(stepExecution);
        		
        		stepExecution.setId(incrementer.nextLongValue());
        		stepExecution.incrementVersion();
        		stepExecutionList.add(stepExecution);
        	}
        	
        	jobExecution.addStepExecutions(stepExecutionList);
        	jobExecutionDao.updateJobExecution(jobExecution);
        }	
	}

	@Override
	public void updateStepExecution(StepExecution stepExecution) {
		validateStepExecution(stepExecution);
		Assert.notNull(stepExecution.getId(), "StepExecution Id cannot be null. StepExecution must saved"
				+ " before it can be updated.");
		
		
		Assert.notNull(stepExecution.getJobExecutionId(), "JobExecution must be saved already.");
		JobExecution jobExecution = jobExecutionDao.getJobExecution(stepExecution.getJobExecutionId());
		Assert.notNull(jobExecution, "JobExecution must be saved already.");
		
		validateStepExecution(stepExecution);

		Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
		synchronized (stepExecution) {
			for (StepExecution se : stepExecutions) {
				if (se.getId().equals(stepExecution.getId())) {	
					se.incrementVersion();
					se.setLastUpdated(stepExecution.getLastUpdated());
					se.setStatus(stepExecution.getStatus());
					
					stepExecution.incrementVersion();
				}
			}
			
			jobExecutionDao.updateJobExecution(jobExecution);
			logger.info("update step execution: " + stepExecution.getId() + ",jobExecution:" + jobExecution.getId());
		}
    	return;
	}

	@Override
	public StepExecution getStepExecution(JobExecution jobExecution, Long stepExecutionId) {
		JobExecution je = jobExecutionDao.getJobExecution(jobExecution.getId());
		if (je == null) {
			return null;
		}
		List<StepExecution> executions = new ArrayList<StepExecution>(je.getStepExecutions());
		
		if (executions.isEmpty()) {
			return null;
		}
		
		StepExecution execution = null;
		for (StepExecution se : executions) {
			if (se.getId().equals(stepExecutionId)) {
				execution = se;
			}
		}

		if (execution == null) {
			return null;
		} else {
			return execution;
		}
	}

	@Override
	public void addStepExecutions(JobExecution jobExecution) {
		Collection<StepExecution> stepExecutions = jobExecutionDao.getJobExecution(jobExecution.getId()).getStepExecutions();
		List<StepExecution> stepExecutionList = new ArrayList<StepExecution>(stepExecutions);
		jobExecution.addStepExecutions(stepExecutionList);

	}
	
	private void validateStepExecution(StepExecution stepExecution) {
		Assert.notNull(stepExecution);
		Assert.notNull(stepExecution.getStepName(), "StepExecution step name cannot be null.");
		Assert.notNull(stepExecution.getStartTime(), "StepExecution start time cannot be null.");
		Assert.notNull(stepExecution.getStatus(), "StepExecution status cannot be null.");
	}

	protected JAXBContext jaxbContext() {
		JAXBContext jaxbContext = null;
		try {
            jaxbContext = JAXBContext.newInstance(AdaptedStepExecution.class);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
		return jaxbContext;
	}

	public JobExecutionDao getJobExecutionDao() {
		return jobExecutionDao;
	}

	public void setJobExecutionDao(JobExecutionDao jobExecutionDao) {
		this.jobExecutionDao = jobExecutionDao;
	}
}