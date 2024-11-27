package in.codifi.mw.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import in.codifi.mw.cache.AccessLogCache;
import in.codifi.mw.entity.logs.AccessLogModel;
import in.codifi.mw.entity.logs.RestAccessLogModel;
import in.codifi.mw.util.StringUtil;
import io.quarkus.logging.Log;

@ApplicationScoped
public class AccessLogManager {
	@Named("logs")
	@Inject
	DataSource dataSource;

	/**
	 * method to insert access log
	 * 
	 * @author SowmiyaThangaraj
	 * @param accLogModel
	 */
	
	public void insertAccessLog(AccessLogModel accLogModel) {
		long currentTimeMillis = System.currentTimeMillis();
	    
	    // Create a SimpleDateFormat for the "ddMMYYYY" and "HH" formats
	    SimpleDateFormat dayFormat = new SimpleDateFormat("ddMMyyyy");
	    SimpleDateFormat hourFormat = new SimpleDateFormat("HH");

	    // Format the current time into a date string (day and hour)
	    String date = dayFormat.format(new Date(currentTimeMillis));  // Day in ddMMyyyy format
	    String hour = hourFormat.format(new Date(currentTimeMillis));  // Hour in HH format
	    
	    // Construct the table name dynamically using the formatted date and hour
	    String tableName = "tbl_" + date + "_access_log_" + hour;
	    
	    // Set the table name in the AccessLogModel object
	    accLogModel.setTableName(tableName);

	    List<AccessLogModel> cacheAccessLogModels = new ArrayList<>(AccessLogCache.getInstance().getBatchAccessModel());
	    if (cacheAccessLogModels.size() > 0) {
	        if (cacheAccessLogModels.get(0).getTableName().equalsIgnoreCase(tableName)) {
	            AccessLogCache.getInstance().getBatchAccessModel().add(accLogModel);
	        } else {
	            AccessLogCache.getInstance().getBatchAccessModel().clear();
	            AccessLogCache.getInstance().setBatchAccessModel(new ArrayList<>());
	            insertBatchAccessLog(cacheAccessLogModels);
	            AccessLogCache.getInstance().getBatchAccessModel().add(accLogModel);
	        }
	    } else {
	        AccessLogCache.getInstance().getBatchAccessModel().add(accLogModel);
	    }

	    if (AccessLogCache.getInstance().getBatchAccessModel().size() >= 25) {
	        List<AccessLogModel> accessLogModels = new ArrayList<>(AccessLogCache.getInstance().getBatchAccessModel());
	        AccessLogCache.getInstance().getBatchAccessModel().clear();
	        AccessLogCache.getInstance().setBatchAccessModel(new ArrayList<>());
	        insertBatchAccessLog(accessLogModels);
	    }
	}
	/**
	 * Method to insert batch access log
	 * 
	 * @author Dinesh Kumar
	 * @param batchLogs
	 */
	public void insertBatchAccessLog(List<AccessLogModel> batchLogs) {
		
		ExecutorService pool = Executors.newSingleThreadExecutor();
		pool.execute(new Runnable() {
			PreparedStatement statement = null;
			Connection connection = null;
			public void run() {
			    try {
			        connection = dataSource.getConnection();
			        if (batchLogs != null && batchLogs.size() > 0) {
			            String insertQuery = "INSERT INTO " + batchLogs.get(0).getTableName() + " "
			                + " (user_id, ucc, req_id, source, vendor, in_time, out_time, lag_time, module, method, req_body,"
			                + " res_body, device_ip, user_agent, domain, content_type, session, uri,elapsed_time ,batch_id) VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			            statement = connection.prepareStatement(insertQuery);
			            for (AccessLogModel accLogModel : batchLogs) {
			                int paramPos = 1;
			                statement.setString(paramPos++, accLogModel.getUser_id());
			                statement.setString(paramPos++, accLogModel.getUcc());
			                statement.setString(paramPos++, accLogModel.getReq_id());
			                statement.setString(paramPos++, accLogModel.getSource());
			                statement.setString(paramPos++, accLogModel.getVendor());			                
			                statement.setLong(paramPos++, accLogModel.getIn_time());
			                statement.setLong(paramPos++, accLogModel.getOut_time());
			                statement.setLong(paramPos++, accLogModel.getLag_time());
			                statement.setString(paramPos++, accLogModel.getModule());
			                statement.setString(paramPos++, accLogModel.getMethod());
			                statement.setString(paramPos++, accLogModel.getReq_body());
			                String respBody = "";
			                int maxLength = 8192;
			                if (StringUtil.isNotNullOrEmpty(accLogModel.getResBody())
			                    && accLogModel.getResBody().length() > maxLength) {
			                    respBody = accLogModel.getResBody().substring(0, maxLength);
			                } else {
			                    respBody = accLogModel.getResBody();
			                }
			                statement.setString(paramPos++, respBody);
			                statement.setString(paramPos++, accLogModel.getDevice_ip());
			                statement.setString(paramPos++, accLogModel.getUser_agent());
			                statement.setString(paramPos++, accLogModel.getDomain());
			                statement.setString(paramPos++, accLogModel.getContent_type());
			                statement.setString(paramPos++, accLogModel.getSession());
			                statement.setString(paramPos++, accLogModel.getUri());
			                statement.setLong(paramPos++, accLogModel.getElapsed_time());
			                statement.setLong(paramPos++, accLogModel.getBatch_id());
			                statement.addBatch();
			            }
						statement.executeBatch();
					}
					statement.close();
					connection.close();
				} catch (Exception e) {
					Log.error("KB - Auth - insertAccessLog -" + e);
				} finally {
					try {
						if (statement != null) {
							statement.close();
						}
						if (connection != null) {
							connection.close();
						}
					} catch (Exception e) {
						Log.error("KB - Auth - insertAccessLog -" + e);
					}
				}
			}
		});
	}

	/**
	 * Method to insert rest access log
	 * 
	 * @author Dinesh Kumar
	 * @param accLogModel
	 */
//	public void insertRestAccessLog(RestAccessLogModel accLogModel) {
//
//		Date inTimeDate = new Date();
//		String date = new SimpleDateFormat("ddMMYYYY").format(inTimeDate);
//		String tableName = "tbl_" + date + "_rest_access_log";
//		AccessLogCache.getInstance().getBatchRestAccessModel().add(accLogModel);
//		if (AccessLogCache.getInstance().getBatchRestAccessModel().size() >= 25) {
//			List<RestAccessLogModel> accessLogModels = new ArrayList<>(
//					AccessLogCache.getInstance().getBatchRestAccessModel());
//			AccessLogCache.getInstance().getBatchRestAccessModel().clear();
//			AccessLogCache.getInstance().setBatchRestAccessModel(new ArrayList<>());
//			insertBatchRestAccessLog(tableName, accessLogModels);
//		}
//	}

	/**
	 * Method to insert batch Rest Access Log
	 * 
	 * @author Dinesh kumar
	 * @param tableName
	 * @param batchLogs
	 */
	public void insertBatchRestAccessLog(String tableName, List<RestAccessLogModel> logsData) {

		ExecutorService pool = Executors.newSingleThreadExecutor();
		pool.execute(new Runnable() {
			@Override
			public void run() {
				List<RestAccessLogModel> batchLogs = new ArrayList<>();
				batchLogs = logsData;
				PreparedStatement statement = null;
				Connection connection = null;
				try {
					connection = dataSource.getConnection();
					if (batchLogs != null && batchLogs.size() > 0) {

						String insertQuery = "INSERT INTO " + tableName
								+ "(user_id, url, in_time, out_time, total_time, module,"
								+ " method, req_body, res_body,vendor) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

						statement = connection.prepareStatement(insertQuery);
						for (RestAccessLogModel accLogModel : batchLogs) {
							int paramPos = 1;
							statement.setString(paramPos++, accLogModel.getUser_id());
							statement.setString(paramPos++, accLogModel.getUri());
							statement.setLong(paramPos++, accLogModel.getIn_time());
							statement.setLong(paramPos++, accLogModel.getOut_time());
							statement.setLong(paramPos++, accLogModel.getLag_time());
							statement.setString(paramPos++, accLogModel.getModule());
							statement.setString(paramPos++, accLogModel.getMethod());
							statement.setString(paramPos++, accLogModel.getReq_body());
							String respBody = "";
							int maxLength = 8192;
							if (StringUtil.isNotNullOrEmpty(accLogModel.getResBody())
									&& accLogModel.getResBody().length() > maxLength) {
								respBody = accLogModel.getResBody().substring(0, maxLength);
							} else {
								respBody = accLogModel.getResBody();
							}
							statement.setString(paramPos++, respBody);
							statement.setString(paramPos++, "KB");
							statement.addBatch();
						}
						statement.executeBatch();
					} else {
						System.out.println("0");
					}

					statement.close();
					connection.close();
				} catch (Exception e) {
					Log.error("KB - Auth - insertRestAccessLog -" + e);
				} finally {
					try {
						if (statement != null) {
							statement.close();
						}
						if (connection != null) {
							connection.close();
						}
					} catch (Exception e) {
						Log.error("KB - Auth - insertRestAccessLog -" + e);
					}
				}
			}
		});
	}

}
