/**
 * 
 */
package in.codifi.mw.service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.resteasy.reactive.RestResponse;


import com.jcraft.jsch.JSch;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

import in.codifi.cache.model.ContractMasterModel;
import in.codifi.cache.model.UnderlyingModel;
import in.codifi.mw.config.ApplicationProperties;
import in.codifi.mw.config.HazelcastConfig;
import in.codifi.mw.entity.ContractEntity;

import in.codifi.mw.entity.primary.PnlLotEntity;
import in.codifi.mw.entity.primary.PromptEntity;

import in.codifi.mw.entity.primary.UnderlyingEntity;
import in.codifi.mw.model.PnlLotModel;
import in.codifi.mw.model.PromptModel;
import in.codifi.mw.model.ResponseModel;
import in.codifi.mw.repository.ContractEntityManager;
import in.codifi.mw.repository.ContractRepository;
import in.codifi.mw.repository.PnlLotRepository;
import in.codifi.mw.repository.PromptDao;
import in.codifi.mw.repository.PromptRepository;
import in.codifi.mw.repository.ScripsDao;
import in.codifi.mw.repository.UnderlyingRepository;
import in.codifi.mw.service.spec.ContractServiceSpecs;
import in.codifi.mw.util.AppConstants;
import in.codifi.mw.util.PrepareResponse;
import in.codifi.mw.util.StringUtil;
import io.quarkus.logging.Log;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Vicky
 *
 */
@ApplicationScoped
public class ContractService implements ContractServiceSpecs {

	@Inject
	ContractEntityManager contractEntityManager;
	@Inject
	UnderlyingRepository underlyingRepository;
	@Inject
	PrepareResponse prepareResponse;

	PromptDao promptDao;
	@Inject
	ApplicationProperties props;
	@Inject
	ScripsDao scripsDao;
	@Inject
	ContractRepository contractRepository;
	@Inject
	PromptRepository promptRepository;

	@Inject
	PnlLotRepository pnlLotRepository;
	/**
	 * 
	 */
	public void loadIsinByToken() {
		contractEntityManager.loadIsinByToken();

	}

	/**
	 * 
	 */
	public RestResponse<ResponseModel> loadUnderlyingScrips() {
		try {
			List<UnderlyingEntity> contractList = new ArrayList<>();
			contractList = underlyingRepository.findAll();
			if (contractList.size() > 0)
				HazelcastConfig.getInstance().getContractMaster().clear();
			for (UnderlyingEntity contractEntity : contractList) {
				UnderlyingModel result = new UnderlyingModel();

				result.setExch(contractEntity.getExchange());
				result.setIsin(contractEntity.getIsin());
				result.setLotSize(contractEntity.getLotSize());
				result.setSymbol(contractEntity.getSymbol());
				result.setToken(contractEntity.getToken());
				result.setType(contractEntity.getType());
				result.setUnderlying(contractEntity.getUnderlying());

				String key = contractEntity.getSymbol();
				HazelcastConfig.getInstance().getUnderlyingScript().put(key, result);
			}
			System.out.println("Underlying Scrips Loaded SucessFully");
			System.out.println("Full Size " + HazelcastConfig.getInstance().getUnderlyingScript().size());

		} catch (Exception e) {
			e.printStackTrace();
			return prepareResponse.prepareFailedResponse(AppConstants.UNDERLYING_LOAD_FAILED);
		}
		return prepareResponse.prepareSuccessMessage(AppConstants.UNDERLYING_LOAD_SUCESS);

	}
	

	/**
	 * 
	 */
	public void loadPromptData() {
		promptDao.loadPromptData();
	}

	/**
	 * @param i
	 */
	public RestResponse<ResponseModel> reloadContractMasterFileV1(int daysOffset) {
		boolean status = executeSqlFileFromServerV1(daysOffset);
		if (status) {
			return prepareResponse.prepareSuccessMessage(AppConstants.CONTRACT_LOAD_SUCESS);
		} else {
			return prepareResponse.prepareFailedResponse(AppConstants.CONTRACT_LOAD_FAILED);
		}

	}

	/**
	 * Method can be used for Update contract master
	 * 
	 * Desc : GET SQL file from server and update the same in DB
	 * 
	 * @author Nesan created on 22-03-23
	 * 
	 * @return
	 */
	public Boolean executeSqlFileFromServerV1(int daysOffset) {
		boolean status = false;
		try {
			String localFilePath = props.getLocalcontractDir(); 
			File localFileDir = new File(props.getLocalcontractDir()); 
			if (!localFileDir.exists()) {
				localFileDir.mkdirs();
			}

//			Date today = new Date();
//			String date = new SimpleDateFormat("ddMMYY").format(today);
			String date = getFormattedDateWithOffset(daysOffset);
			if (StringUtil.isNullOrEmpty(date)) {
				Log.error("Failed to load contract file from server due to date is empty");
				return status;
			}
			String fileName = AppConstants.CONTRACT_FILE_NMAE + date + AppConstants.SQL;

			String remoteDir = (props.getRemoteContractDire() + fileName).trim();
			Log.info("Contract file  - " + remoteDir);
			boolean isFileMoved = getsqlFileFromServer(localFilePath.toString(), remoteDir);
			if (isFileMoved) {
				boolean isInserted = executeSqlFile(localFilePath, fileName, date);
				deleteExpiredContract();
				loadContractIntoCache();
			} else {
				deleteExpiredContract();
				Log.error("Failed to get contract file from server");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		return status;
	}

	public static String getFormattedDateWithOffset(int daysOffset) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMYY");
		return LocalDate.now().minusDays(daysOffset).format(formatter);
	}

	/**
	 * Method to move file from server to local
	 * 
	 * @author Nesan
	 * 
	 * @param localFilePath
	 * @param remotefilePath
	 * @return
	 */
	private boolean getsqlFileFromServer(String localFilePath, String remotefilePath) {
		boolean status = false;

		Session session = null;
		ChannelSftp channelSftp = null;
		try {
			JSch jsch = new JSch();

			session = jsch.getSession(props.getSshUserName(), props.getSshHost(), props.getSshPort());
//			session.setPortForwardingL(forwardPort, localHost, localPort);

			session.setPassword(props.getSshPassword());
			session.setConfig(AppConstants.STRICTHOSTKEYCHECKING, AppConstants.NO);
			session.connect();
			/* File movement from server to local */
			Channel sftp = session.openChannel(AppConstants.SFTP);
			// 5 seconds timeout
			sftp.connect(5000);
			channelSftp = (ChannelSftp) sftp;
			/* transfer file from remote server to local */
			channelSftp.stat(remotefilePath);
			channelSftp.get(remotefilePath, localFilePath);
			channelSftp.exit();
			status = true;
			Log.info("File downloaded");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (session != null)
				session.disconnect();
		}
		return status;
	}

	/**
	 * Method to execute sql file
	 * 
	 * @author Nesan
	 * 
	 * @param string
	 */
	private boolean executeSqlFile(String localFilePath, String fileName, String date) {
		boolean status = false;
		File directory = new File(localFilePath + fileName);
		int size = localFilePath.lastIndexOf("/");
		String slash = "//";
		if (size > 0) {
			slash = "/";
		}
		try {

			if (directory.isFile()) {
				/* This one can be finalized */
//				String tCommand = "mysql -u " + props.getDbUserName() + " -p" + props.getDbpassword() + " "
//						+ props.getDbSchema();

				String tCommand = "mysql -u " + props.getDbUserName() + " -p" + props.getDbpassword() + " -h "
						+ props.getDbHost() + " " + props.getDbSchema();
				Log.info("Stated to import contract");
				String sqlQueries = new String(Files.readAllBytes(Paths.get(directory.toURI())));
				Process tProcess = Runtime.getRuntime().exec(tCommand); // 20
				OutputStream tOutputStream = tProcess.getOutputStream();
				Writer w = new OutputStreamWriter(tOutputStream);
				w.write(sqlQueries);
				w.flush();

				int exitCode = tProcess.waitFor(); // Wait for the process to finish
				if (exitCode == 0) {
					Log.info("Import completed successfully.");
					status = true;
				} else {
					Log.error("Error during import. Exit code: " + exitCode);
					status = false;
				}

				status = true;
				File completed = new File(localFilePath + "completed");
				if (!completed.exists()) {
					completed.mkdirs();
				}
				if (directory.renameTo(new File(completed.toString() + slash + date + ".sql"))) {
					directory.delete();
					Log.info("File Moved Successfully");
				}
			} else {
				/* sent mail */
				File completed = new File(localFilePath + "failed");
				if (!completed.exists()) {
					completed.mkdirs();
				}
				if (directory.renameTo(new File(completed.toString() + slash + date + ".sql"))) {
					directory.delete();
					Log.info("Contract master update is failed");
				}
			}
		} catch (Exception e) {
			/* sent mail */
			File completed = new File(localFilePath + "failed");
			if (!completed.exists()) {
				completed.mkdirs();
			}
			if (directory.renameTo(new File(completed.toString() + slash + date))) {
				directory.delete();
				Log.info("Contract master update is failed");
			}
			e.printStackTrace();
		}
		return status;
	}

	/**
	 * Delete Expired contract manually
	 * 
	 * @author Dinesh Kumar
	 *
	 * @return
	 */
	@Override
	public RestResponse<ResponseModel> deleteExpiredContract() {

		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String todayDate = format.format(date);
		int deleteCount = contractEntityManager.deleteExpiredContract(todayDate);
		if (deleteCount >= 0) {
			loadContractIntoCache();
			return prepareResponse.prepareSuccessMessage(deleteCount + "-" + AppConstants.RECORD_DELETED);
		}
		return prepareResponse.prepareFailedResponse(AppConstants.DELETE_FAILED);
	}

	/**
	 * Method to clear cache and load latest data into cache
	 * 
	 * @author DINESH KUMAR
	 *
	 */
	public void loadContractIntoCache() {
		Log.info("Started to clear cache");
		HazelcastConfig.getInstance().getFetchDataFromCache().clear();
		HazelcastConfig.getInstance().getDistinctSymbols().clear();
		HazelcastConfig.getInstance().getLoadedSearchData().clear();
		HazelcastConfig.getInstance().getFetchDataFromCache().clear();
		Log.info("Cache cleared and started to load new data");
		HazelcastConfig.getInstance().getFetchDataFromCache().put(AppConstants.FETCH_DATA_FROM_CACHE, true);
		scripsDao.loadDistintValue(2);
		scripsDao.loadDistintValue(3);
		loadContractMaster();
		loadIsin();
		loadIsinByToken();
//		loadPromptData();
		Log.info("Cache loaded sucessfully");
	}

	/**
	 * Method To load contract master into cache
	 * 
	 * @author Dinesh Kumar
	 *
	 * @return
	 */
	@Override
	public RestResponse<ResponseModel> loadContractMaster() {
		try {
			List<ContractEntity> contractList = new ArrayList<>();
			contractList = contractRepository.findAll();
			if (contractList.size() > 0)
				HazelcastConfig.getInstance().getContractMaster().clear();
//			if (contractList.size() > 0)
//				HazelcastConfig.getInstance().getContractMaster().clear();
			for (ContractEntity contractEntity : contractList) {
				ContractMasterModel result = new ContractMasterModel();

				result.setExch(contractEntity.getExch());
				result.setSegment(contractEntity.getSegment());
				result.setSymbol(contractEntity.getSymbol());
				result.setIsin(contractEntity.getIsin());
				result.setFormattedInsName(contractEntity.getFormattedInsName());
				result.setToken(contractEntity.getToken());
				result.setTradingSymbol(contractEntity.getTradingSymbol());
				result.setGroupName(contractEntity.getGroupName());
				result.setInsType(contractEntity.getInsType());
				result.setOptionType(contractEntity.getOptionType());
				result.setStrikePrice(contractEntity.getStrikePrice());
				result.setExpiry(contractEntity.getExpiryDate());
				result.setLotSize(contractEntity.getLotSize());
				result.setTickSize(contractEntity.getTickSize());
				result.setPdc(contractEntity.getPdc());
				result.setWeekTag(contractEntity.getWeekTag());
				result.setFreezQty(contractEntity.getFreezeQty());
				result.setAlterToken(contractEntity.getAlterToken());
				result.setCompanyName(contractEntity.getCompanyName());
				String key = contractEntity.getExch() + "_" + contractEntity.getToken();

				HazelcastConfig.getInstance().getContractMaster().remove(key);
				HazelcastConfig.getInstance().getContractMaster().put(key, result);

				String token = contractEntity.getToken();
				String exch = contractEntity.getExch().toUpperCase();
				String tradingSymbol = contractEntity.getTradingSymbol();
				if (StringUtil.isNotNullOrEmpty(tradingSymbol)) {
					HazelcastConfig.getInstance().getTradingSymbolTokenMapKB().put(tradingSymbol.toUpperCase(),
							token + "_" + exch);
				}
			}
			System.out.println("Loaded SucessFully");
			System.out.println("Full Size " + HazelcastConfig.getInstance().getContractMaster().size());

			System.out.println("Trading symbol token map TR Loaded SucessFully");
			System.out.println("Trading symbol token map TR Full Size "
					+ HazelcastConfig.getInstance().getTradingSymbolTokenMapKB().size());
		} catch (Exception e) {
			e.printStackTrace();
			return prepareResponse.prepareFailedResponse(AppConstants.CONTRACT_LOAD_FAILED);
		}
		return prepareResponse.prepareSuccessMessage(AppConstants.CONTRACT_LOAD_SUCESS);
	}

	/**
	 * 
	 * Method to load isin token into cache for holdings
	 * 
	 * @author Dinesh Kumar
	 *
	 */
	public void loadIsin() {
		contractEntityManager.loadIsin();
	}

	/**
	 * Method to get ASM/GSM file from server
	 * 
	 * @author Dinesh Kumar
	 *
	 * @return
	 */
	@Override
	public RestResponse<ResponseModel> reloadAsmGsmFile(int daysOffset) {

		boolean status = executeAsmGsmSqlFileFromServer(daysOffset);
		if (status) {
			return prepareResponse.prepareSuccessMessage(AppConstants.ASM_GSM_LOAD_SUCESS);
		} else {
			return prepareResponse.prepareFailedResponse(AppConstants.ASM_GSM_LOAD_FAILED);
		}
	}

	/**
	 * Method to get ASM and GSM dump file from server
	 * 
	 * @author Dinsh Kumar
	 * @return
	 */
	public Boolean executeAsmGsmSqlFileFromServer(int daysOffset) {
		boolean status = false;
		try {
			String localFilePath = props.getLocalAsmGsmDir();
			File localFileDir = new File(props.getLocalAsmGsmDir());
			if (!localFileDir.exists()) {
				localFileDir.mkdirs();
			}

//			Date today = new Date();
//			String date = new SimpleDateFormat("ddMMYY").format(today);
			String date = getFormattedDateWithOffset(daysOffset);
			if (StringUtil.isNullOrEmpty(date)) {
				Log.error("Failed to load ASM/GSM file from server due to date is empty");
				return status;
			}

			String fileName = AppConstants.ASMGSM_FILE_NMAE + date + AppConstants.SQL;
			Log.info("ASM GSM fileName - " + fileName);
			String remoteDir = props.getRemoteAsmGsmDir() + fileName;
			boolean isFileMoved = getsqlFileFromServer(localFilePath.toString(), remoteDir);
			if (isFileMoved) {
				boolean isInserted = executeSqlFile(localFilePath, fileName, date);
				loadPromptDataInThread();
				Log.info("ASM GSM Loaded sucessfully - " + fileName);
			} else {
				Log.error("Failed to get ASM/GSM file from server");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		return status;
	}

	/**
	 * 
	 * Method to load prompt
	 * 
	 * @author Dinesh Kumar
	 *
	 */
	public void loadPromptDataInThread() {
		try {
//			List<PromptModel> promptModels = promptDao.getPromptData();
			List<PromptEntity> promptEntities = promptRepository.findAll();

			ExecutorService pool = Executors.newSingleThreadExecutor();
			pool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						if (!promptEntities.isEmpty()) {
							List<PromptModel> promptModels = preparePromptModel(promptEntities);
							List<PromptModel> response = new ArrayList<PromptModel>();
							HazelcastConfig.getInstance().getPromptMasterv1().clear();
							for (PromptModel promptModel : promptModels) {
								String key = (promptModel.getIsin() + "_" + promptModel.getExch()).toUpperCase();
								response = HazelcastConfig.getInstance().getPromptMasterv1().get(key);
								if (response != null && response.size() > 0) {
									response = HazelcastConfig.getInstance().getPromptMasterv1().get(key);
									response.add(promptModel);
									HazelcastConfig.getInstance().getPromptMasterv1().put(key, response);
								} else {
									response = new ArrayList<>();
									response.add(promptModel);
									HazelcastConfig.getInstance().getPromptMasterv1().put(key, response);
								}
							}
						} else {
							Log.error("Prompt data is empty in DB");
						}

					} catch (Exception e) {
						e.printStackTrace();
						Log.error(e.getMessage());
					} finally {
						pool.shutdown();
					}
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
	}

	/**
	 * Helper method to prepare prompt model from entity
	 * 
	 * @author Dinesh Kumar
	 * @param promptEntities
	 * @return
	 */
	private List<PromptModel> preparePromptModel(List<PromptEntity> promptEntities) {
		List<PromptModel> promptModels = new ArrayList<PromptModel>();
		try {
			for (PromptEntity entity : promptEntities) {
				PromptModel result = new PromptModel();
				result.setIsin(entity.getIsin());
				result.setExch(entity.getExch());
				result.setCompany_name(entity.getCompany_name());
				result.setMsg(entity.getMsg());
				result.setType(entity.getType());
				result.setSeverity(entity.getSeverity());
				result.setPrompt(entity.getPrompt());
				promptModels.add(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.error(e.getMessage());
		}
		return promptModels;
	}

	/**
	 * 
	 */
	@Override
	public RestResponse<ResponseModel> loadPnlLotSize() {
		try {
			List<PnlLotEntity> pnlLotEntities = new ArrayList<>();
			List<PnlLotModel> pnlLotModels = new ArrayList<>();
			pnlLotEntities = pnlLotRepository.findAll();
			if (pnlLotEntities.size() > 0)
				HazelcastConfig.getInstance().getPnlLot().clear();
			for (PnlLotEntity pnlLotEntity : pnlLotEntities) {
				PnlLotModel result = new PnlLotModel();
				result.setExch(pnlLotEntity.getExch());
				result.setSymbol(pnlLotEntity.getSymbol());
				result.setToken(pnlLotEntity.getToken());
				result.setTradingSymbol(pnlLotEntity.getTradingSymbol());
				result.setExpiry(pnlLotEntity.getExpiryDate());
				result.setLotSize(pnlLotEntity.getLotSize());
				pnlLotModels.add(result);
			}
			HazelcastConfig.getInstance().getPnlLot().put(AppConstants.PNL_LOT, pnlLotModels);
			System.out.println("Pnl Lot loaded sucessFully");
		} catch (Exception e) {
			e.printStackTrace();
			return prepareResponse.prepareFailedResponse(AppConstants.CONTRACT_LOAD_FAILED);
		}
		return prepareResponse.prepareSuccessMessage(AppConstants.CONTRACT_LOAD_SUCESS);
		
	}
}
