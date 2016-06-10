package org.metadatacenter.admin.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.codec.net.URLCodec;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.metadatacenter.config.CedarConfig;
import org.metadatacenter.constant.HttpConnectionConstants;
import org.metadatacenter.constant.HttpConstants;
import org.metadatacenter.model.CedarNodeType;
import org.metadatacenter.model.folderserver.CedarFSFolder;
import org.metadatacenter.model.folderserver.CedarFSNode;
import org.metadatacenter.server.neo4j.Neo4JUserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ExportResources extends AbstractNeo4JWritingTask {

  public static final String FOLDER_INFO = "folder.info.json";
  public static final String DEFAULT_SORT = "name";

  private CedarConfig cedarConfig;
  private Neo4JUserSession adminNeo4JSession;
  private ObjectMapper prettyMapper;
  private List<CedarNodeType> nodeTypeList;
  private List<String> sortList;
  private String authString;
  private Logger logger = LoggerFactory.getLogger(ExportResources.class);

  public ExportResources() {
    description.add("Exports folders, resources, users into a directory structure");
    description.add("The export is executed using the cedar-admin user");
    description.add("The export target is the $CEDAR_HOME/export folder");
  }

  @Override
  public void init(CedarConfig cedarConfig) {
    this.cedarConfig = cedarConfig;
  }

  @Override
  public int execute() {
    String exportDir = cedarConfig.getImportExportConfig().getExportDir();
    System.out.println("Export dir:=>" + exportDir + "<=");

    prettyMapper = new ObjectMapper();
    prettyMapper.enable(SerializationFeature.INDENT_OUTPUT);

    nodeTypeList = new ArrayList<>();
    nodeTypeList.add(CedarNodeType.FOLDER);
    nodeTypeList.add(CedarNodeType.FIELD);
    nodeTypeList.add(CedarNodeType.ELEMENT);
    nodeTypeList.add(CedarNodeType.TEMPLATE);
    nodeTypeList.add(CedarNodeType.INSTANCE);

    sortList = new ArrayList<>();
    sortList.add(DEFAULT_SORT);

    adminNeo4JSession = buildCedarAdminNeo4JSession(cedarConfig, false);

    String apiKey = adminUser.getFirstActiveApiKey();
    authString = HttpConstants.HTTP_AUTH_HEADER_APIKEY_PREFIX + apiKey;

    String rootPath = adminNeo4JSession.getRootPath();
    CedarFSFolder rootFolder = adminNeo4JSession.findFolderByPath(rootPath);

    Path exportPath = Paths.get(exportDir).resolve("resources");
    serializeAndWalkFolder(exportPath, rootFolder);
    return 0;
  }

  private void serializeAndWalkFolder(Path path, CedarFSNode node) {
    if (node instanceof CedarFSFolder) {
      CedarFSFolder folder = (CedarFSFolder) node;
      String id = folder.getId();
      String uuid = adminNeo4JSession.getFolderUUID(id);
      Path createdFolder = createFolder(path, uuid);
      createFolderDescriptor(createdFolder, folder);
      List<CedarFSNode> folderContents = adminNeo4JSession.findFolderContents(id, nodeTypeList, Integer.MAX_VALUE, 0,
          sortList);
      for (CedarFSNode child : folderContents) {
        serializeAndWalkFolder(createdFolder, child);
      }
    } else {
      serializeResource(path, node);
    }
  }

  private Path createFolder(Path path, String name) {
    Path newFolder = path.resolve(name);
    newFolder.toFile().mkdirs();
    return newFolder;
  }

  private void createFolderDescriptor(Path path, CedarFSFolder folder) {
    Path folderInfo = path.resolve(FOLDER_INFO);
    try {
      Files.write(folderInfo, prettyMapper.writeValueAsString(folder).getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void serializeResource(Path path, CedarFSNode node) {
    String id = node.getId();
    CedarNodeType nodeType = node.getType();
    String uuid = adminNeo4JSession.getResourceUUID(id, nodeType);
    String infoName = uuid + ".info.json";
    Path createdInfoFile = path.resolve(infoName);
    try {
      Files.write(createdInfoFile, prettyMapper.writeValueAsString(node).getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
    String content = getTemplateServerContent(id, nodeType);
    if (content != null) {
      String name = uuid + ".content.json";
      Path createdFile = path.resolve(name);
      try {
        JsonNode jsonNode = prettyMapper.readTree(content);
        Files.write(createdFile, prettyMapper.writeValueAsString(jsonNode).getBytes(StandardCharsets.UTF_8));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private String getTemplateServerContent(String id, CedarNodeType nodeType) {
    try {
      String url = cedarConfig.getServers().getTemplate().getBase() + nodeType.getPrefix() + "/" + new URLCodec()
          .encode(id);
      System.out.println(url);
      Request proxyRequest = Request.Get(url)
          .connectTimeout(HttpConnectionConstants.CONNECTION_TIMEOUT)
          .socketTimeout(HttpConnectionConstants.SOCKET_TIMEOUT);
      proxyRequest.addHeader(HttpHeaders.AUTHORIZATION, authString);
      HttpResponse proxyResponse = proxyRequest.execute().returnResponse();
      HttpEntity entity = proxyResponse.getEntity();
      if (entity != null) {
        return EntityUtils.toString(entity);
      }
    } catch (Exception e) {
      play.Logger.error("Error while reading " + nodeType.getValue(), e);
    }
    return null;
  }


}