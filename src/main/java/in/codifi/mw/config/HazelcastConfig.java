package in.codifi.mw.config;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.ConfigProvider;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import in.codifi.cache.model.ContractMasterModel;
import in.codifi.mw.model.PnlLotModel;
import in.codifi.mw.model.VendorDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApplicationScoped
public class HazelcastConfig {

	public static HazelcastConfig HazelcastConfig = null;
	private HazelcastInstance hz = null;

	public static HazelcastConfig getInstance() {
		if (HazelcastConfig == null) {
			HazelcastConfig = new HazelcastConfig();

		}
		return HazelcastConfig;
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
	private Map<String, String> isinKB = getHz().getMap("isinKB");
	private Map<String, List<PnlLotModel>> pnlLot = getHz().getMap("pnlLot");
	private Map<String, String> isinByTokenKB = getHz().getMap("isinByTokenKB");
	private Map<String, String> tradingSymbolTokenMapKB = getHz().getMap("tradingSymbolTokenMapKB");
	private Map<String, VendorDTO> ssoSessionMapping = getHz().getMap("ssoSessionMapping");
}