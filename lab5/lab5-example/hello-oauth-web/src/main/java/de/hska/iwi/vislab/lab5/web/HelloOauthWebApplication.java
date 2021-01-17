package de.hska.iwi.vislab.lab5.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@SpringBootApplication
@EnableOAuth2Client
@Controller
public class HelloOauthWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelloOauthWebApplication.class, args);
	}

	@Value("${oauth.resource:http://localhost:8080}")
	private String baseUrl;

	@Value("${oauth.authorize:http://localhost:8080/oauth/authorize}")
	private String authorizeUrl;

	@Value("${oauth.token:http://localhost:8080/oauth/token}")
	private String tokenUrl;

	@Autowired
	private OAuth2RestOperations restTemplate;

	@RequestMapping("/")
	public String home() {
		return "greet";
	}

	@RequestMapping("/next")
	public String next(Model model) {
		String nextFibonacci = restTemplate.postForObject(baseUrl + "/fibonacci", null, String.class);
		model.addAttribute("fibonacci", nextFibonacci);
		// name of the template to return
		return "greet";
	}

	@RequestMapping(value = "/restart")
	public String delete(Model model) {
		restTemplate.delete(baseUrl + "/fibonacci");
		model.addAttribute("fibonacci", "restarted the fibonacci count");
		// name of the template to return
		return "greet";
	}

	@RequestMapping(value = "/specific", produces = MediaType.APPLICATION_JSON_VALUE)
	public String specific(@RequestParam int index, Model model) {
		String specific = restTemplate.getForObject(baseUrl + "/fibonacci/" + index, String.class);
		model.addAttribute("fibonacci", specific);
		// name of the template to return
		return "greet";
	}

	
	@Bean
	public OAuth2RestOperations restTemplate(OAuth2ClientContext oauth2ClientContext) {
		return new OAuth2RestTemplate(resource(), oauth2ClientContext);
	}

//	@Bean
//	protected OAuth2ProtectedResourceDetails resource() {
//		AuthorizationCodeResourceDetails resource = new AuthorizationCodeResourceDetails();
//		resource.setAccessTokenUri(tokenUrl);
//		resource.setUserAuthorizationUri(authorizeUrl);
//		resource.setClientId("my-trusted-client");
//		resource.setClientSecret("secret");
//		return resource;
//	}

	@Bean
	protected OAuth2ProtectedResourceDetails resource() {
		ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
		resource.setAccessTokenUri(tokenUrl);
		resource.setClientId("my-client-with-secret");
		resource.setClientSecret("secret");
		return resource;
	}

}
