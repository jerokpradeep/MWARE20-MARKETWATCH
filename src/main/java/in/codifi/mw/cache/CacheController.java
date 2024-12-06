/**
 * 
 */
package in.codifi.mw.cache;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.RestResponse;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import in.codifi.mw.model.GenericResponse;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.util.AppConstants;
import in.codifi.mw.util.PrepareResponse;

/**
 * @author Vicky
 *
 */
@Path("/cache")
public class CacheController {
	@Autowired
	PrepareResponse prepareResponse;
	
	/**
	 * Method to get Cache data
	 * @param name
	 * @param key
	 * @return
	 */
	@Path("/getCacheData")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public RestResponse<ResponseModel> getMWCacheKeysByName(@QueryParam("userId") String pUserId) {
		List<JSONObject> res = MwCacheController.getMwListUserId().get(pUserId);
		return prepareResponse.prepareSuccessResponseObject(res);
	}
	
	/**
	 * Method to get Cache data
	 * @param name
	 * @param key
	 * @return
	 */
	@Path("/remove")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public RestResponse<ResponseModel> removeMWCacheKeysByName(@QueryParam("userId") String pUserId) {
		MwCacheController.getMwListUserId().remove(pUserId);
		return prepareResponse.prepareMWSuccessResponseObject(AppConstants.SUCCESS_STATUS);
	}
}
