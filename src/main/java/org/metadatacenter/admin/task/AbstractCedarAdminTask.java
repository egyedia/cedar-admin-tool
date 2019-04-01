package org.metadatacenter.admin.task;

import org.metadatacenter.admin.util.AdminOutput;
import org.metadatacenter.bridge.CedarDataServices;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.model.CedarNodeType;
import org.metadatacenter.server.jsonld.LinkedDataUtil;
import org.metadatacenter.server.service.UserService;

import java.io.Console;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractCedarAdminTask implements ICedarAdminTask {

  protected List<String> arguments;
  protected final List<String> description = new ArrayList<>();
  protected AdminOutput out;
  protected CedarConfig cedarConfig;
  protected LinkedDataUtil linkedDataUtil;
  public static final String CONFIRM = "yes";

  protected String mongoDatabaseName;
  protected String templateElementsCollectionName;
  protected String templateFieldCollectionName;
  protected String templateInstancesCollectionName;
  protected String templatesCollectionName;
  protected String usersCollectionName;

  @Override
  public void setArguments(String[] args) {
    arguments = new ArrayList<>();
    arguments.addAll(Arrays.asList(args));
  }

  @Override
  public void injectConfig(CedarConfig cedarConfig) {
    this.cedarConfig = cedarConfig;
    this.linkedDataUtil = cedarConfig.getLinkedDataUtil();
  }

  @Override
  public List<String> getDescription() {
    return description;
  }

  public List<String> getArguments() {
    return arguments;
  }

  @Override
  public void setOutput(AdminOutput out) {
    this.out = out;
  }

  protected UserService getNeoUserService() {
    return CedarDataServices.getNeoUserService();
  }

  protected UserService getMongoUserService() {
    return CedarDataServices.getMongoUserService();
  }

  protected boolean getConfirmInput(String message) {
    Console c = System.console();
    if (c == null) {
      return false;
    }
    out.warn("You need to confirm your intent by entering '" + CONFIRM + "'!");
    out.warn(message);
    String yes = c.readLine("Do you really want to perform the operation: ");
    boolean proceed = CONFIRM.equals(yes);
    if (!proceed) {
      out.error("You chose not to continue with the process! Process finished.");
    }
    return proceed;
  }

  protected void initMongoCollectionNames() {
    mongoDatabaseName = cedarConfig.getArtifactServerConfig().getDatabaseName();
    templateFieldCollectionName = cedarConfig.getArtifactServerConfig().getCollections().get(CedarNodeType.FIELD
        .getValue());
    templateElementsCollectionName = cedarConfig.getArtifactServerConfig().getCollections().get(CedarNodeType.ELEMENT
        .getValue
            ());
    templatesCollectionName = cedarConfig.getArtifactServerConfig().getCollections().get(CedarNodeType.TEMPLATE
        .getValue());
    templateInstancesCollectionName = cedarConfig.getArtifactServerConfig().getCollections().get(CedarNodeType.INSTANCE
        .getValue());
    usersCollectionName = cedarConfig.getUserServerConfig().getCollections().get(CedarNodeType.USER.getValue());
  }

}
