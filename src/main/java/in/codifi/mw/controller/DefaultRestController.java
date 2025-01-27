
package in.codifi.mw.controller;


import java.util.Set;

import javax.inject.Inject;

import org.eclipse.microprofile.jwt.JsonWebToken;

import in.codifi.mw.model.ClinetInfoModel;
import io.quarkus.logging.Log;


/**
 * @author Vicky
 *
 */
public class DefaultRestController {

	private static final String USER_ID_KEY = "preferred_username";
	private static final String UCC = "ucc";
	/**
	 *      * Injection point for the ID Token issued by the OpenID Connect Provider
	 *     
	 */

	@Inject
	JsonWebToken idToken;
	@Inject
	JsonWebToken accessToken;

	public String getUserId() {
		return this.idToken.getClaim(USER_ID_KEY).toString().toUpperCase();
	}

	/**
	 * 
	 * Method to get client details
	 * 
	 * @author Dinesh Kumar
	 *
	 * @return
	 */
	public ClinetInfoModel clientInfo() {
		ClinetInfoModel model = new ClinetInfoModel();
//		Set<String> claims = this.idToken.getClaimNames();
//
//		// Print all keys
//		for (String key : claims) {
//		    System.out.println("Key: " + key);
//		}
//		Log.info("--------> "+this.idToken.getClaim(USER_ID_KEY).toString());
//		Log.info("--------------> "+this.idToken.getClaim(USER_ID_KEY).toString().toUpperCase());
		model.setUserId(this.idToken.getClaim(USER_ID_KEY).toString().toUpperCase());
		if (this.idToken.containsClaim(UCC)) {
			model.setUcc(this.idToken.getClaim(UCC).toString());
		}
		return model;
	}
	
	public String getAcToken() {
		return this.accessToken.getRawToken();
	}
}
