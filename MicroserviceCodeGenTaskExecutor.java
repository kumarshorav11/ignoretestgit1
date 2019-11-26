package com.hcl.adpass.catalogue.executor.latest;

import static com.hcl.adpass.catalogue.config.DataKeyConstants.ENTITY_PATH;
import static com.hcl.adpass.catalogue.config.DataKeyConstants.MS_NAME;
import static com.hcl.adpass.catalogue.config.DataKeyConstants.PKG_NAME;

import java.io.File;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.hcl.adpass.catalogue.cache.URLConstants;
import com.hcl.adpass.catalogue.config.ApplicationConstants;
import com.hcl.adpass.catalogue.constants.ExecutorConstants;
import com.hcl.adpass.catalogue.executor.ADPaaSTaskExecutor;
import com.hcl.adpass.catalogue.executor.models.ADPaaSExecutorInput;
import com.hcl.adpass.catalogue.executor.models.MicroserviceCodeGenRequestModel;
import com.hcl.adpass.catalogue.executor.models.Tooling;
import com.hcl.adpass.catalogue.executor.response.models.ADPaaSExecutorResponse;
import com.hcl.adpass.catalogue.executor.response.models.CodeGenResponse;
import com.hcl.adpass.catalogue.services.FileHelper;
/**
 * 
 * @author satish-s
 *	Task executor to handle the MicroService code generation task
 */
@Component
public class MicroserviceCodeGenTaskExecutor implements ADPaaSTaskExecutor {

	private static final Logger logger = LoggerFactory.getLogger(MicroserviceCodeGenTaskExecutor.class);
	
	private final RestTemplate restTemplate = new RestTemplate();
	
	@Value("${service.url}")
	private String serviceUrl;
	
	@Override
	public MicroserviceCodeGenRequestModel preExecute(Map<String,Object> data) {
		logger.info("In preExecute");
		logger.debug("Starting extraction for MicroserviceCodeGenRequestModel");
		Tooling tooling = new Tooling((String) data.get("authType"), (String) data.get("buildTool"),
				(String) data.get("devDB"), (String) data.get("prodDB"),
				false,Arrays.asList("gatling"),
				Arrays.asList("en"));

		MicroserviceCodeGenRequestModel model = new MicroserviceCodeGenRequestModel((String) data.get(MS_NAME),
				 (String) data.get(PKG_NAME),
				tooling, (String) data.get(ENTITY_PATH));

		logger.debug("Returning MicroserviceCodeGenRequestModel : {}", model);
		return model;
	}

	@Override
	public ADPaaSExecutorResponse execute(ADPaaSExecutorInput inputData , String tokenHeader) {
		logger.info("In execute with input : {}",inputData);
		MicroserviceCodeGenRequestModel codeGenModel = (MicroserviceCodeGenRequestModel)inputData;
		String codeGenJsonConfigFile = FileHelper.writeInputDataAsJSON(codeGenModel);
		
		File file = new File(codeGenModel.getEntityFilePath());
		File configFile = new File(codeGenJsonConfigFile);
		
		
		if ((file.exists() && (!file.isDirectory())) &&  (configFile.exists() && (!configFile.isDirectory())) ){
			LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
			map.add(ApplicationConstants.REQUEST_PARAM_MODEL_FILE, new FileSystemResource(file));
			map.add(ApplicationConstants.REQUEST_PARAM_CONFIG_FILE,new FileSystemResource(codeGenJsonConfigFile));
			
			HttpHeaders headers = new HttpHeaders();
			headers.add(ExecutorConstants.HEADER_TOKEN, tokenHeader);
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new    HttpEntity<LinkedMultiValueMap<String, Object>>(
			                    map, headers);
			
			String uri=serviceUrl+URLConstants.MS_CODE_GEN_URL;
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity,String.class);
				
			logger.info("Response from microservice Executor : {} ", response);
				CodeGenResponse  result= new CodeGenResponse();
				result.setResult(response.getBody());
				return result;
		}else{
			logger.error("Invalid file info. Please provide correct file information.\n Aboting the execution process...!!");
		}
		logger.debug("Deleting config file and model file ");
			file.delete();
			configFile.delete();
			return null;
	}
	@Override
	public void postExecute(Object inputData) {
		logger.info("In postExecute");
	}


}

