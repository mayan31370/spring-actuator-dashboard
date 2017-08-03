package com.counect;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by mayan on 17-8-2.
 */
@RestController
public class ApiController {

  @Value("${actuator-dashboard.apps}")
  private String APPS_IN_STRING;
  @Value("${actuator-dashboard.actuator.username}")
  private String USERNAME;
  @Value("${actuator-dashboard.actuator.password}")
  private String PASSWORD;

  private List<TreeNode> getApps() {
    List<TreeNode> result = new ArrayList<>();
    for (String group : StringUtils.split(APPS_IN_STRING, "|")) {
      String groupName = StringUtils.substringBefore(group, ":");
      List<String> apps = Arrays
          .asList(StringUtils.split(StringUtils.substringAfter(group, ":"), ","));
      result.add(new TreeNode(groupName, apps));
    }
    return result;
  }

  @GetMapping("/apps")
  public List<TreeNode> apps() {
    return getApps();
  }

  @GetMapping("/apps/{app}/{point}")
  public String pointString(@PathVariable("app") String app, @PathVariable("point") String point)
      throws IOException {
    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(new AuthScope(app, 8080),
        new UsernamePasswordCredentials(USERNAME, PASSWORD));
    HttpClient client = HttpClientBuilder.create()
        .setDefaultCredentialsProvider(credentialsProvider).build();
    HttpGet method = new HttpGet("http://" + app + ":8080/" + point);
    InputStream stream = client.execute(method).getEntity().getContent();
    return IOUtils.toString(stream, "utf8");
  }

  class TreeNode {

    public String text;
    public String iconCls = "icon-none";
    public String state = "open";
    public List<TreeNode> children;

    public TreeNode(String text, List<String> children) {
      this.text = text;
      this.children = children == null ? null
          : children.stream().map(name -> new TreeNode(name, null)).collect(Collectors.toList());
    }
  }
}