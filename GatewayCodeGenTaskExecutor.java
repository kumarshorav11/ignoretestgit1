package com.hcl.adpass.catalogue.executor.latest;

import static com.hcl.adpass.catalogue.config.DataKeyConstants.APP_NAME_SUFFIX_UI;
import static com.hcl.adpass.catalogue.config.DataKeyConstants.ENTITY_PATH;
import static com.hcl.adpass.catalogue.config.DataKeyConstants.MS_NAME;
import static com.hcl.adpass.catalogue.config.DataKeyConstants.PKG_NAME;

import java.util.Arrays;
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
import com.hcl.adpass.catalogue.executor.models.GatewayCodeGenRequestModel;
import com.hcl.adpass.catalogue.executor.models.Tooling;
import com.hcl.adpass.catalogue.executor.response.models.ADPaaSExecutorResponse;
import com.hcl.adpass.catalogue.executor.response.models.CodeGenResponse;

/**
 * 
 * @author satish-s Task executor to handle the Gateway code generation task
 */
@Component
public class GatewayCodeGenTaskExecutor implements ADPaaSTaskExecutor {

	private static final Logger logger = LoggerFactory.getLogger(GatewayCodeGenTaskExecutor.class);

	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${service.url}")
	private String serviceUrl;

	@Override
	public GatewayCodeGenRequestModel preExecute(Map<String, Object> data) {
		logger.info("In preExecute");

		logger.debug("Starting extraction for GatewayCodeGenRequestModel");
		
		Tooling tooling = new Tooling((String) data.get("authType"), (String) data.get("buildTool"),
				(String) data.get("devDB"), (String) data.get("prodDB"), 
				false,Arrays.asList("gatling"), 
				Arrays.asList("en"));
		
		GatewayCodeGenRequestModel model = new GatewayCodeGenRequestModel(ObjectUtils.nullSafeToString(data.get(MS_NAME)).trim().concat(APP_NAME_SUFFIX_UI),
				 (String) data.get(PKG_NAME),
				 tooling,
				 Arrays.asList((String) data.get(MS_NAME)),
				 (String) data.get(ENTITY_PATH));
		
		logger.debug("Returning GatewayCodeGenRequestModel : {}", model);
		return model;
	}

	@Override
	public ADPaaSExecutorResponse execute(ADPaaSExecutorInput inputData, String tokenHeader) {
		logger.info("In execute");
		GatewayCodeGenRequestModel codeGenModel = (GatewayCodeGenRequestModel) inputData;

		codeGenModel.setName(codeGenModel.getName());
		HttpHeaders headers = new HttpHeaders();
		headers.add("token", tokenHeader);
		HttpEntity<ADPaaSExecutorInput> requestEntity = new HttpEntity<>(codeGenModel, headers);

		String uri = serviceUrl + URLConstants.UI_APP_CODE_GEN_URL;
		ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);

		logger.info("Response from gateway Executor : {} ", response);
		CodeGenResponse result = new CodeGenResponse();
		result.setResult(response.getBody());
		return result;
	}

	@Override
	public void postExecute(Object inputData) {
		logger.info("In postExecute");
	}

}
