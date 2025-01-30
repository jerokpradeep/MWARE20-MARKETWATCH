package in.codifi.mw.config;

import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import lombok.Getter;
import lombok.Setter;

@Singleton
@Getter
@Setter
public class ApplicationProperties {

	@ConfigProperty(name = "appconfig.app.mw.size")
	String mwSize;
	@ConfigProperty(name = "appconfig.app.mw.flow")
	String mwFlow;
	@ConfigProperty(name = "appconfig.app.mw.exchfull")
	private boolean exchfull;
	
	@ConfigProperty(name = "config.app.local.file.path")
	private String localcontractDir;
	
	@ConfigProperty(name = "config.app.ssh.file.path")
	private String remoteContractDire;
	
	@ConfigProperty(name = "config.app.ssh.username")
	private String sshUserName;

	@ConfigProperty(name = "config.app.ssh.password")
	private String sshPassword;

	@ConfigProperty(name = "config.app.ssh.port")
	private int sshPort;
	
	@ConfigProperty(name = "config.app.ssh.host")
	private String sshHost;
	
	@ConfigProperty(name = "quarkus.datasource.username")
	private String dbUserName;

	@ConfigProperty(name = "quarkus.datasource.password")
	private String dbpassword;

	@ConfigProperty(name = "config.app.db.schema")
	private String dbSchema;

	@ConfigProperty(name = "config.app.db.host")
	private String dbHost;
	
	@ConfigProperty(name = "config.app.local.asmgsm.file.path")
	private String localAsmGsmDir;
	
	@ConfigProperty(name = "config.app.ssh.asmgsm.file.path")
	private String remoteAsmGsmDir;
}
