package io.intino.examples.federation;

import io.intino.alexandria.logger.Logger;
import io.intino.amidas.authentication.LdapAuthentication;
import io.intino.amidas.box.AmidasBox;
import io.intino.amidas.connectors.ldap.LdapConnector;
import io.intino.amidas.connectors.microsoft.azure.AzureConnector;
import io.intino.amidas.connectors.microsoft.teams.TeamsConnector;
import io.intino.amidas.connectors.rocketchat.RocketChatConnector;
import org.apache.log4j.Level;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import static io.intino.amidas.shared.connectors.ActiveDirectoryConnector.OperationMode.CreateOrUpdateIdentity;
import static io.intino.amidas.shared.connectors.ActiveDirectoryConnector.OperationMode.UpdateIdentity;

public class Main {
	public static void main(String[] args) {
		AmidasBox box = new AmidasBox(args);
		io.intino.alexandria.logger4j.Logger.setLevel(Level.ERROR);
		setup(box);
		box.start();
		Runtime.getRuntime().addShutdownHook(new Thread(box::stop));
	}

	private static void setup(AmidasBox box) {
		try {
			Properties props = new Properties();
			File credentialsFile = new File(box.configuration().get("credentials_file"));
			if (!credentialsFile.exists()) throw new IllegalArgumentException("Credentials file argument not found");
			props.load(new FileInputStream(credentialsFile));
			setupMicrosoftAzure(props, box);
			setupMicrosoftTeams(props, box);
			setupRocketChat(props, box);
			setupLdap(props, box);
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private static void setupRocketChat(Properties configuration, AmidasBox box) {
		if (connectors(box).contains("rocketChat")) box.register(new RocketChatConnector(configuration));
	}

	private static void setupLdap(Properties configuration, AmidasBox box) {
		if (!connectors(box).contains("ldap")) return;
		box.register(new LdapAuthentication(box, configuration));
		box.register(new LdapConnector(configuration, CreateOrUpdateIdentity));
	}

	private static void setupMicrosoftAzure(Properties configuration, AmidasBox box) {
		if (connectors(box).contains("azure")) box.register(new AzureConnector(configuration, UpdateIdentity));
	}

	private static void setupMicrosoftTeams(Properties configuration, AmidasBox box) {
		if (connectors(box).contains("teams")) box.register(new TeamsConnector(configuration));
	}

	private static String connectors(AmidasBox box) {
		return box.configuration().get("connectors");
	}
}