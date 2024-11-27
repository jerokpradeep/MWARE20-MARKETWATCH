package in.codifi.mw.filter;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.eclipse.microprofile.jwt.JsonWebToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.codifi.mw.entity.logs.AccessLogModel;
import in.codifi.mw.repository.AccessLogManager;
import in.codifi.mw.util.AppConstants;
import io.quarkus.arc.Priority;

/**
 * @author Vicky
 *
 */
@Provider
@Priority(Priorities.USER)

@ApplicationScoped
public class AccessLogFilter implements ContainerRequestFilter, ContainerResponseFilter  {
	ObjectMapper objectMapper = null;

	@Inject
	io.vertx.core.http.HttpServerRequest req;

	@Inject
	AccessLogManager accessLogManager;

	@Inject
	JsonWebToken idToken;

	@Context
	HttpServletRequest request;

	/**
	 * Method to capture and single save request and response
	 * 
	 * @param requestContext
	 * @param responseContext
	 */

public void caputureInSingleShot(ContainerRequestContext requestContext,
			ContainerResponseContext responseContext) {

	String uId = "";
	String clientId = "";

	if (this.idToken != null) {
	    if (this.idToken.containsClaim("preferred_username")) {
	        uId = this.idToken.getClaim("preferred_username").toString();
	    }
	    if (this.idToken.containsClaim("ucc")) {
	        clientId = this.idToken.getClaim("ucc").toString();
	    }
	}

	String userId = uId;
	String ucc = clientId;
		

		ExecutorService pool = Executors.newSingleThreadExecutor();
		pool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					objectMapper = new ObjectMapper();
					AccessLogModel accLogModel = new AccessLogModel();
					UriInfo uriInfo = requestContext.getUriInfo();
					if (!uriInfo.getPath().toString().equalsIgnoreCase("/marketWatch/getAllMwScrips")
							&& !uriInfo.getPath().toString().equalsIgnoreCase("/marketWatch/getAllMwScrips/mob")) {
						
						MultivaluedMap<String, String> headers = requestContext.getHeaders();
						accLogModel.setContent_type(headers.getFirst(AppConstants.CONTENT_TYPE));
						accLogModel.setDevice_ip(headers.getFirst("X-Forwarded-For"));
						accLogModel.setDomain(headers.getFirst("Host"));
						
					    Timestamp inTime = (Timestamp) requestContext.getProperty("inTime");
//					    System.out.println(inTime);
			            Timestamp outTime = new Timestamp(System.currentTimeMillis());
			            
			            if (inTime != null) {
			                accLogModel.setIn_time(convertTimestampToMillisWithinHour(inTime));
			            }
			            accLogModel.setOut_time(convertTimestampToMillisWithinHour(outTime));
			            
			            // Calculate lag_time with null check
			            int lagTime = 0;
			            if (inTime != null) {
			                lagTime = (int)(outTime.getTime() - inTime.getTime());
			            }
			            accLogModel.setLag_time(lagTime);
			            
			            // Calculate elapsed_time (out_time_ms - in_time_ms)
			            long elapsedTime = accLogModel.getOut_time() - accLogModel.getIn_time();
			            accLogModel.setElapsed_time((int) elapsedTime);
			            
						accLogModel.setMethod(requestContext.getMethod());
						accLogModel.setModule(AppConstants.c);
						accLogModel.setReq_body(objectMapper.writeValueAsString(requestContext.getProperty("reqBody")));
						Object reponseObj = responseContext.getEntity();
						accLogModel.setRes_body(objectMapper.writeValueAsString(reponseObj));
						accLogModel.setSource("");
						accLogModel.setUri(uriInfo.getPath().toString());
						accLogModel.setUser_agent(headers.getFirst("User-Agent"));
						accLogModel.setUser_id(userId);
						accLogModel.setUcc(ucc);
						accLogModel.setVendor("KB");
						accLogModel.setBatch_id(0);
						accLogModel.setSession(headers.getFirst(AppConstants.AUTHORIZATION));
						accLogModel.setReq_id(requestContext.getProperty("threadId") != null
								? requestContext.getProperty("threadId").toString()
								: "singlecapture");
						accessLogManager.insertAccessLog(accLogModel);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					pool.shutdown();
				}
			}
		});
	}

private int convertTimestampToMillisWithinHour(Timestamp timestamp) {
    if (timestamp == null) {
        return 0;
    }
   
    LocalDateTime dateTime = timestamp.toLocalDateTime();
    int minutes = dateTime.getMinute();
    int seconds = dateTime.getSecond();
    int milliseconds = dateTime.getNano() / 1_000_000; 
    return ((minutes * 60* 1000) +( seconds * 1000) + milliseconds);
}


public void filter(ContainerRequestContext requestContext) throws IOException {
		try {
			requestContext.setProperty("inTime", new Timestamp(System.currentTimeMillis()));
			byte[] body = requestContext.getEntityStream().readAllBytes();

			InputStream stream = new ByteArrayInputStream(body);
			requestContext.setEntityStream(stream);
			String formedReq = new String(body);
			requestContext.setProperty("reqBody", formedReq);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		caputureInSingleShot(requestContext, responseContext);

	}

}
