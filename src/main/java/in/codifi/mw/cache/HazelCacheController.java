package in.codifi.mw.cache;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.ConfigProvider;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import in.codifi.cache.model.ContractMasterModel;
import in.codifi.cache.model.UnderlyingModel;
import in.codifi.mw.model.PreferenceModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApplicationScoped
public class HazelCacheController {

	public static HazelCacheController HazelCacheController = null;
	private HazelcastInstance hz = null;

	public static HazelCacheController getInstance() {
		if (HazelCacheController == null) {
			HazelCacheController = new HazelCacheController();

		}
		return HazelCacheController;
	}

	public HazelcastInstance getHz() {
		if (hz == null) {
			ClientConfig clientConfig = new ClientConfig();
			clientConfig.setClusterName(ConfigProvider.getConfig().getValue("config.app.hazel.cluster", String.class));
			List<String> hazelAddress = List
					.of(ConfigProvider.getConfig().getValue("config.app.hazel.address", String.class).split(","));
			hazelAddress.stream().forEach(address -> {
				clientConfig.getNetworkConfig().addAddress(address);
			});

			hz = HazelcastClient.newHazelcastClient(clientConfig);
		}
		return hz;
	}

	private Map<String, ContractMasterModel> contractMaster = getHz().getMap("contractMaster");
	private Map<String, List<PreferenceModel>> perference = getHz().getMap("perference");
	private Map<String, UnderlyingModel> underlyingScript = getHz().getMap("underlyingScript");

	public Map<String, ContractMasterModel> getContractMaster() {
		return contractMaster;
	}

	public void setContractMaster(Map<String, ContractMasterModel> contractMaster) {
		this.contractMaster = contractMaster;
	}

	public Map<String, List<PreferenceModel>> getPerference() {
		return perference;
	}

	public void setPerference(Map<String, List<PreferenceModel>> perference) {
		this.perference = perference;
	}

	/**
	 * @return the hazelCacheController
	 */
	public static HazelCacheController getHazelCacheController() {
		return HazelCacheController;
	}

	/**
	 * @param hazelCacheController the hazelCacheController to set
	 */
	public static void setHazelCacheController(HazelCacheController hazelCacheController) {
		HazelCacheController = hazelCacheController;
	}

}
