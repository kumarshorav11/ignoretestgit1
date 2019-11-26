package com.hcl.adpass.catalogue.executor.latest;
import static com.hcl.adpass.catalogue.config.DataKeyConstants.APPLICATION_PORT;
import static com.hcl.adpass.catalogue.config.DataKeyConstants.ARTIFACT_ID;
import static com.hcl.adpass.catalogue.config.DataKeyConstants.ENTITY_PATH;
import static com.hcl.adpass.catalogue.config.DataKeyConstants.GROUP_ID;
import static com.hcl.adpass.catalogue.config.DataKeyConstants.SERVICE_NAME;

import java.io.File;
import java.util.Map;

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
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import com.hcl.adpass.catalogue.cache.URLConstants;
import com.hcl.adpass.catalogue.config.ApplicationConstants;
import com.hcl.adpass.catalogue.executor.ADPaaSTaskExecutor;
import com.hcl.adpass.catalogue.executor.models.ADPaaSExecutorInput;
import com.hcl.adpass.catalogue.executor.models.SwaggerRestBackendCodeGenRequestModel;
import com.hcl.adpass.catalogue.executor.response.models.ADPaaSExecutorResponse;
import com.hcl.adpass.catalogue.executor.response.models.CodeGenResponse;
import com.hcl.adpass.catalogue.services.FileHelper;
/**
 * 
 * @author satish-s
 *	Task executor to handle the Spring boot based rest services code generation task
 *	The input to this executor is Swagger model
 *	
 */
@Component
public class SpringBootSwaggerBackendCodeGenTaskExecutor implements ADPaaSTaskExecutor {

	private static final Logger logger = LoggerFactory.getLogger(SpringBootSwaggerBackendCodeGenTaskExecutor.class);
	
	private final RestTemplate restTemplate = new RestTemplate();
	
	@Value("${service.url}")
	private String serviceUrl;
	
	@Override
	public SwaggerRestBackendCodeGenRequestModel preExecute(Map<String, Object> data) {
		logger.info("In preExecute",data);


		logger.debug("Starting extraction for Spring Boot Swagger Rest Backend service");

		String packaging =  data.get("packaging").toString();
		if (packaging == null){
			packaging = "jar";
		}
		SwaggerRestBackendCodeGenRequestModel model= new SwaggerRestBackendCodeGenRequestModel(

				
				ObjectUtils.nullSafeToString(data.get(ENTITY_PATH)),
				ObjectUtils.nullSafeToString(data.get("endpointBaseurl")),
				ObjectUtils.nullSafeToString(data.get(SERVICE_NAME)),
				ObjectUtils.nullSafeToString(data.get(GROUP_ID)),
				ObjectUtils.nullSafeToString(data.get(ARTIFACT_ID)),
				packaging,
				ObjectUtils.nullSafeToString(data.get("springBootVersion")),
				ObjectUtils.nullSafeToString(data.get("basePackageStructure")),
				ObjectUtils.nullSafeToString(data.get("mavenProfiles")),
				Boolean.parseBoolean(ObjectUtils.nullSafeToString(data.get("enableEureka"))),
				Boolean.parseBoolean(ObjectUtils.nullSafeToString(data.get("enableDropwizardMetrics"))),
				Boolean.parseBoolean(ObjectUtils.nullSafeToString(data.get("enableSpringSecurity"))),
				Boolean.parseBoolean(ObjectUtils.nullSafeToString(data.get("enableSpringCloudConfig"))),
				Boolean.parseBoolean(ObjectUtils.nullSafeToString(data.get("enableELK"))),
				ObjectUtils.nullSafeToString(data.get("deploymentOptions")),
				ObjectUtils.nullSafeToString(data.get(APPLICATION_PORT)));

		logger.debug("Returning SwaggerRestBackendCodeGenRequestModel : {}", model);
		return model;

	}

	@Override
	public ADPaaSExecutorResponse execute(ADPaaSExecutorInput inputData , String tokenHeader) {
		logger.info("In execute with input : {} ", inputData);
		
		SwaggerRestBackendCodeGenRequestModel codeGenModel = (SwaggerRestBackendCodeGenRequestModel)inputData;
		String codeGenJsonConfigFile = FileHelper.writeInputDataAsJSON(codeGenModel);
		
		File file = new File(codeGenModel.getEntityFilePath());
		File configFile = new File(codeGenJsonConfigFile);
		
			logger.info("codeGenModel.getEntityFilePath() : "+codeGenModel.getEntityFilePath());
			logger.info("codeGenJsonConfigFile : "+codeGenJsonConfigFile);

			if (file.exists()) {
				logger.info("Enity File Path: "+file.getAbsolutePath());
			} else {
				logger.info("Enity not exists: ");
			}

			if (configFile .exists()) {
				logger.info("Config File Path: "+configFile .getAbsolutePath());
			} else {
				logger.info("Config not exists: ");
			} 

		if ((file.exists() && (!file.isDirectory())) &&  (configFile.exists() && (!configFile.isDirectory())) ){
			LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
			map.add(ApplicationConstants.REQUEST_PARAM_SWAGGER_BACKEND_FILE, new FileSystemResource(file));
			map.add(ApplicationConstants.REQUEST_PARAM_BACKEND_CONFIG_FILE, new FileSystemResource(codeGenJsonConfigFile));
			
			HttpHeaders headers = new HttpHeaders();
			headers.add("token", tokenHeader);
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new    HttpEntity<LinkedMultiValueMap<String, Object>>(
			                    map, headers);
			
			String uri=serviceUrl+URLConstants.SPRING_BOOT_SWAGGER_BACKEND_CODE_GEN_URL;
			logger.info("URI .................. : "+ uri);
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity,String.class);
				
			logger.info("Response from swagger REST backend task Executor : {} ", response);
			if(response.getStatusCode().is2xxSuccessful()){
				return new CodeGenResponse();
			}else{
				return null;
			}
			
		}else{
			logger.error("Invalid file info. Please provide correct file information.\n Aboting the execution process...!!");
		}
		logger.debug("Deleting config file and swagger model file ");
			file.delete();
			configFile.delete();
			return null;
	}

	@Override
	public void postExecute(Object inputData) {
		logger.info("In postExecute");
	}


}
