package in.codifi.mw.loader;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import in.codifi.mw.cache.MwCacheController;
import in.codifi.mw.service.ContractService;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
@SuppressWarnings("serial")
public class InitialLoader extends HttpServlet {

	@Inject
	ContractService contractService;
	
	public void init(@Observes StartupEvent ev) throws ServletException {

//		System.out.println(" Market watch data pre-Lodings are started");
//		sevice.loadContractMaster();
//		cacheservice.loadUserMWData();
//		System.out.println(" Market watch data pre-Lodings are ended");
		contractService.loadIsinByToken();
		System.out.println(" Predefined Market watch data pre-Lodings are started");
		MwCacheController.getMasterPredefinedMwList().clear();
		MwCacheController.getMwListUserId().clear();
//		cacheservice.loadPreDefinedMWData();
		System.out.println("Get Mw List UserId clear");
		contractService.loadUnderlyingScrips();
		Log.info("Started to loading contract master");
		contractService.loadContractMaster();
		contractService.loadPromptData();
	}
}
