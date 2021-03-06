package com.untzuntz.ustackserverapi.auth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.untzuntz.ustack.data.AccessToken;
import com.untzuntz.ustack.data.AccessToken.AccessTokenDetails;
import com.untzuntz.ustack.data.UserAccount;
import com.untzuntz.ustackserverapi.APIException;
import com.untzuntz.ustackserverapi.CallParameters;
import com.untzuntz.ustackserverapi.MethodDefinition;
import com.untzuntz.ustackserverapi.params.ParamNames;
import com.untzuntz.ustackserverapi.params.types.ParameterDefinitionInt;

/**
 * Authentication method for an access token (temporary access value)
 * 
 * @author jdanner
 *
 */
public class AccessTokenAuth implements AuthenticationInt<UserAccount> {

    static Logger           		logger               	= Logger.getLogger(AccessTokenAuth.class);
 
    public static final String CookieTokenName = "UNTZ";
    
    public String getAuthenticationDescription() {
    	return "Access token in request parameters required.";
    }

    public boolean isBasicAuth() {
    	return false;
    }

    /**
     * 
     */
    public List<ParameterDefinitionInt<?>> getAuthenticationParameters() {
    	
    	List<ParameterDefinitionInt<?>> ret = new ArrayList<ParameterDefinitionInt<?>>();
    	
    	ret.add(ParamNames.token);
    	
    	return ret;
    }
    
	@Override
	public UserAccount authenticate(MethodDefinition method, HttpRequest req, CallParameters params) throws APIException {

		//AuthTypes.ClientKey.authenticate(method, req, params);
		AccessTokenDetails details = AccessToken.decode( params.get(ParamNames.token) );
		
		if (details == null)
		{
			logger.warn(String.format("Invalid Token => '%s'", params.get(ParamNames.token)));
			throw new APIAuthenticationException("Invalid Token");
		}
		if (details.expirationAge < System.currentTimeMillis())
		{
			logger.info(String.format("%s/%s | Access Token expired at '%s'", details.userName, details.clientId, new SimpleDateFormat("MM/dd/yyyy h:mm:ss a").format(new Date(details.expirationAge))));
			throw new APIAuthenticationException("Token has expired");
		}

//		logger.info(String.format("%s -> Expiration %d -> Now: %d", details.userName, details.expirationAge, System.currentTimeMillis()));
		
		params.setParameterValue(ParamNames.client_id.getName(), details.clientId);
		
		return UserAccount.getUser(details.userName);
	}

}
