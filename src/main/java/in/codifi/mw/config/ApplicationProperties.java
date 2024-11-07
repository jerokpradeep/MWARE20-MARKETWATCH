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
}
