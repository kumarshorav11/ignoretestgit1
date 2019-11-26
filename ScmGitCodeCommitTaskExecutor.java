package com.hcl.adpass.catalogue.executor.latest;

import static com.hcl.adpass.catalogue.config.DataKeyConstants.SCM_BRANCH;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.hcl.adpass.catalogue.cache.URLConstants;
import com.hcl.adpass.catalogue.executor.ADPaaSTaskExecutor;
import com.hcl.adpass.catalogue.executor.models.ADPaaSExecutorInput;
import com.hcl.adpass.catalogue.executor.models.ScmGitCodeCommitRequestModel;
import com.hcl.adpass.catalogue.executor.response.models.ADPaaSExecutorResponse;
import com.hcl.adpass.catalogue.executor.response.models.EmptyResponse;
/**
 * 
 * @author satish-s
 *  The executor that will execute the task to committ code in Git SCM
 */
@Component
public class ScmGitCodeCommitTaskExecutor implements ADPaaSTaskExecutor {

	private static final Logger logger = LoggerFactory.getLogger(ScmGitCodeCommitTaskExecutor.class);

	private RestTemplate restTemplate = new RestTemplate();
	
	@Value("${service.url}")
	private String serviceUrl;


	@Override
	public ScmGitCodeCommitRequestModel preExecute(Map<String,Object> data) {

		logger.debug("Starting extraction for ScmGitCodeCommitRequestModel from data : {}",data);

		ScmGitCodeCommitRequestModel model = new ScmGitCodeCommitRequestModel(
				(String) data.get("gitUrl"),
				(String) data.get(SCM_BRANCH),
				(String) data.get("gitUsername"),
				(String) data.get("gitPassword"),
				"Initial Commit");


		logger.debug("Returning ScmGitCodeCommitRequestModel : {}", model);
		return model;

	}

	@Override
	public ADPaaSExecutorResponse execute(ADPaaSExecutorInput inputData, String tokenHeader) {
		ScmGitCodeCommitRequestModel requestData = (ScmGitCodeCommitRequestModel)inputData;
		logger.info("In execute excuting with data : {} ",requestData);
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("token", tokenHeader);
		HttpEntity<ADPaaSExecutorInput> requestEntity = new HttpEntity<>(requestData, headers);
		String uri=serviceUrl+URLConstants.SCM_GIT_COMMIT_NEW_BRANCH_URL;
		ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);

		logger.info("Response from Task Executor : {} ", response.getBody());
		return new EmptyResponse();
	}

	@Override
	public void postExecute(Object inputData) {
		// TODO Auto-generated method stub

	}

}
