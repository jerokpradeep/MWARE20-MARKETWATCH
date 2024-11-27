package in.codifi.mw.cache;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import in.codifi.mw.entity.logs.AccessLogModel;
import in.codifi.mw.entity.logs.RestAccessLogModel;

@ApplicationScoped
public class AccessLogCache {

	private static AccessLogCache instance = null;

	public static synchronized AccessLogCache getInstance() {
		if (instance == null) {
			instance = new AccessLogCache();
		}
		return instance;
	}

	private List<AccessLogModel> batchAccessModel = new ArrayList<>();
	private List<RestAccessLogModel> batchRestAccessModel = new ArrayList<>();

	public List<AccessLogModel> getBatchAccessModel() {
		return batchAccessModel;
	}

	public void setBatchAccessModel(List<AccessLogModel> batchAccessModel) {
		this.batchAccessModel = batchAccessModel;
	}

	public List<RestAccessLogModel> getBatchRestAccessModel() {
		return batchRestAccessModel;
	}

	public void setBatchRestAccessModel(List<RestAccessLogModel> batchRestAccessModel) {
		this.batchRestAccessModel = batchRestAccessModel;
	}

}
