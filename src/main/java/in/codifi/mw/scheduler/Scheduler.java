/**
 * 
 */
package in.codifi.mw.scheduler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;

import in.codifi.mw.service.ContractService;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;

/**
 * @author Vicky
 *
 */
@ApplicationScoped
public class Scheduler {

	@Inject
	ContractService contractService;
	
	/**
	 * Scheduler to Load contract at morning 7:00 AM (UTC 1:30 AM)
	 * 
	 * @author DINESH KUMAR
	 *
	 * @param execution
	 * @throws ServletException
	 */
//	@Scheduled(cron = "0 0 2 * * ?")
//	@Scheduled(cron = "0 40 6 * * ?")
//	public void loadContractMaster(ScheduledExecution execution) throws ServletException {
//		Log.info("Scheduler started to Load Contract Master");
////		contractService.reloadContractMasterFile();
//		contractService.reloadContractMasterFileV1(0);
//		contractService.reloadAsmGsmFile(0);
//		contractService.loadPnlLotSize();
//		Log.info("Scheduler completed to Load Contract Master");
//	}
}
