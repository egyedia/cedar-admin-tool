package org.metadatacenter.cedar.admintool.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.config.environment.CedarEnvironmentVariable;
import org.metadatacenter.config.environment.CedarEnvironmentVariableProvider;
import org.metadatacenter.model.SystemComponent;
import org.metadatacenter.util.test.TestUtil;

import java.util.HashMap;
import java.util.Map;

public class CedarConfigAdminToolTest {

  @Before
  public void setEnvironment() {
    Map<String, String> env = new HashMap<>();

    env.put(CedarEnvironmentVariable.CEDAR_HOME.getName(), "/home/cedar/");

    env.put(CedarEnvironmentVariable.CEDAR_HOST.getName(), "metadatacenter.orgx");

    env.put(CedarEnvironmentVariable.CEDAR_ADMIN_USER_PASSWORD.getName(), "adminPassword");
    env.put(CedarEnvironmentVariable.CEDAR_ADMIN_USER_API_KEY.getName(), "1234");

    env.put(CedarEnvironmentVariable.CEDAR_NEO4J_USER_NAME.getName(), "name");
    env.put(CedarEnvironmentVariable.CEDAR_NEO4J_USER_PASSWORD.getName(), "password");

    env.put(CedarEnvironmentVariable.CEDAR_KEYCLOAK_CLIENT_ID.getName(), "cedar-angular-app");

    env.put(CedarEnvironmentVariable.CEDAR_MONGO_APP_USER_NAME.getName(), "cedarUser");
    env.put(CedarEnvironmentVariable.CEDAR_MONGO_APP_USER_PASSWORD.getName(), "password");
    env.put(CedarEnvironmentVariable.CEDAR_MONGO_HOST.getName(), "localhost");
    env.put(CedarEnvironmentVariable.CEDAR_MONGO_PORT.getName(), "27017");

    env.put(CedarEnvironmentVariable.CEDAR_SALT_API_KEY.getName(), "saltme");

    env.put(CedarEnvironmentVariable.CEDAR_LD_USER_BASE.getName(), "https://metadatacenter.org/users/");

    env.put(CedarEnvironmentVariable.CEDAR_EVERYBODY_GROUP_NAME.getName(), "Everybody");

    TestUtil.setEnv(env);
  }

  @Test
  public void testGetInstance() throws Exception {
    SystemComponent systemComponent = SystemComponent.ADMIN_TOOL;
    Map<String, String> environment = CedarEnvironmentVariableProvider.getFor(systemComponent);
    CedarConfig instance = CedarConfig.getInstance(environment);
    Assert.assertNotNull(instance);
  }

}