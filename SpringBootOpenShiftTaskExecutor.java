package com.hcl.adpass.catalogue.executor.latest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import com.hcl.adpass.catalogue.cache.URLConstants;
import com.hcl.adpass.catalogue.executor.ADPaaSTaskExecutor;
import com.hcl.adpass.catalogue.executor.models.ADPaaSExecutorInput;
import com.hcl.adpass.catalogue.executor.models.AppPort;
import com.hcl.adpass.catalogue.executor.models.OpenShiftAppParameters;
import com.hcl.adpass.catalogue.executor.models.SpringBootOpenShiftCodeGenRequestModel;
import com.hcl.adpass.catalogue.executor.response.models.ADPaaSExecutorResponse;
import com.hcl.adpass.catalogue.executor.response.models.CodeGenResponse;

/**
 * 
 * @author desai.v
 *<p>
 *         Task executor to handle the Spring Boot code generation API exposed
 *         by ADPaaS
 * </p>
 * 
 */
@Component
public class SpringBootOpenShiftTaskExecutor implements ADPaaSTaskExecutor {

	private static final Logger logger = LoggerFactory.getLogger(SpringBootOpenShiftTaskExecutor.class);

	private RestTemplate restTemplate = new RestTemplate();

	@Value("${service.url}")
	private String serviceUrl;

	@Override
	public OpenShiftAppParameters preExecute(Map<String, Object> data) {
		logger.debug("In pre Execute");

		logger.debug("Starting extraction for Spring Boot OpenShift Swagger Rest service");
        List<AppPort> appList = new ArrayList<AppPort>();
		AppPort app = new AppPort();
		app.setPortName("web");
		app.setPortNumber(Integer.parseInt(ObjectUtils.nullSafeToString(data.get("applicationPort"))));
		app.setTargetPort(Integer.parseInt(ObjectUtils.nullSafeToString(data.get("applicationPort"))));
		app.setProtocol("TCP");
		appList.add(app);
		
		OpenShiftAppParameters value = new OpenShiftAppParameters();
		//int instance = Integer.parseInt(ObjectUtils.nullSafeToString(data.get("pcfInstance")));
		SpringBootOpenShiftCodeGenRequestModel model= new SpringBootOpenShiftCodeGenRequestModel();
		model.setApplicationName(ObjectUtils.nullSafeToString(data.get("ms_name")));
		model.setApplicationImage(ObjectUtils.nullSafeToString(data.get("docker-image")));
		model.setApplicationReplicas(1);
		model.setDbRequired(false);
		model.setAppPorts(appList);
		
		value.setOpenShiftParams(model);
		//model.setPcfInstance(Integer.parseInt(ObjectUtils.nullSafeToString(data.get("app-instance"))));
		//model.setAppcodegen("spring-boot");
		logger.debug("Returning OpenShiftAppParameters : {}", value);
		return value;
	

	}

	@Override
	public ADPaaSExecutorResponse execute(ADPaaSExecutorInput inputData, String tokenHeader) {
		logger.info("In execute : {}",inputData);
		
		OpenShiftAppParameters codeGenModel = (OpenShiftAppParameters) inputData;
		HttpHeaders headers = new HttpHeaders();
		headers.add("token", tokenHeader);
		headers.add("Content-Type", "application/json");
		HttpEntity<ADPaaSExecutorInput> requestEntity = new HttpEntity<>(codeGenModel, headers);

		String uri = serviceUrl + URLConstants.SPRING_BOOT_OPENSHIFT_URL;
		ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
		logger.info("Response from spring-boot Executor : {} ", response);
		CodeGenResponse result = new CodeGenResponse();
		if (response.getStatusCode().is2xxSuccessful()){
			result.setResult(response.getBody());
			return result;
		}else{
			return null;
		}

	}

	@Override
	public void postExecute(Object inputData) {
		// TODO Auto-generated method stub

	}

}