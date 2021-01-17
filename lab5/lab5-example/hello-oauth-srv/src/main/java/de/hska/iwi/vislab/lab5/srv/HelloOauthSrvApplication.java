package de.hska.iwi.vislab.lab5.srv;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.test.OAuth2ContextConfiguration;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableResourceServer
@RestController
public class HelloOauthSrvApplication{

	public static void main(String[] args) {
		SpringApplication.run(HelloOauthSrvApplication.class, args);
	}

	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public String hello() {
		return "Hello Oauth!";
	}

	// here is for fibonacci

    private int fibonacciIndex = 0;

    private void setFibonacciIndex(int i){
         this.fibonacciIndex = i;
    }

    private int getFibonacciIndex(){
         return this.fibonacciIndex;
    }

    private int getFibonacci() {
        int prevSum = 1;
        if (getFibonacciIndex() == 0) {
            setFibonacciIndex(1);
            return 0;
        } else if (getFibonacciIndex() == 1) {
            setFibonacciIndex(2);
            return 1;
        } else if (getFibonacciIndex() == 2) {
            setFibonacciIndex(3);
            return 1;
        } else {
            int sum = prevSum;
            for (int i = 3; i <= getFibonacciIndex(); i++) {
                int oldSum = sum;
                sum = sum + prevSum;
                prevSum = oldSum;
            }
            setFibonacciIndex(getFibonacciIndex()+1);
            return sum;
        }
    }

    /**
     * Method handling HTTP GET requests.
     *
     * @return String that will be returned as a JSON response.
     */
    @RequestMapping(value = "/fibonacci", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
    public String getIt() {
        return Integer.toString(getFibonacci());
    }

    @RequestMapping(value = "/fibonacci", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> restartFibonacci() {
        setFibonacciIndex(0);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
    * Method handling HTTP PUT request. Updates current fibonacci number.
    */
    @RequestMapping(value = "/fibonacci/{index}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> specificFibonacci(@PathVariable int index) {
		setFibonacciIndex(index);
        return new ResponseEntity<>(getFibonacci(), HttpStatus.OK);
	}


	@Configuration
	@EnableAuthorizationServer
	protected static class OAuth2Config extends AuthorizationServerConfigurerAdapter {

		@Autowired
		private AuthenticationManager authenticationManager;

		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
			endpoints.authenticationManager(authenticationManager);
		}

		@Override
		public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
			security.checkTokenAccess("isAuthenticated()");
		}

		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
			// @formatter:off
			clients.inMemory()
				.withClient("my-trusted-client")
					.authorizedGrantTypes("password", "authorization_code", "refresh_token", "implicit")
					.authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT")
					.scopes("read", "write", "trust")
					.resourceIds("oauth2-resource")
					.accessTokenValiditySeconds(600)
			.and()
				.withClient("my-client-with-registered-redirect")
					.authorizedGrantTypes("authorization_code")
					.authorities("ROLE_CLIENT")
					.scopes("read", "trust")
					.resourceIds("oauth2-resource")
					.redirectUris("http://anywhere?key=value")
			.and()
				.withClient("my-client-with-secret")
					.authorizedGrantTypes("client_credentials", "password")
					.authorities("ROLE_CLIENT")
					.scopes("read")
					.resourceIds("oauth2-resource")
					.secret("secret");
			// @formatter:on
		}

	}
}
