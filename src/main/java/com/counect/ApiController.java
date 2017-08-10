package com.counect;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by mayan on 17-8-2.
 */
@RestController
public class ApiController {

  @Value("${actuator-dashboard.apps}")
  private String appsInString;
  @Value("${actuator-dashboard.actuator.username}")
  private String username;
  @Value("${actuator-dashboard.actuator.password}")
  private String password;

  private List<TreeNode> getApps() {
    List<TreeNode> result = new ArrayList<>();
    for (String group : StringUtils.split(appsInString, "|")) {
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
  public String getPoint(@PathVariable("app") String app, @PathVariable("point") String point)
      throws IOException {
    return request(app, point, RequestMethod.GET);
  }


  @GetMapping("/apps/{app}/{point}/{detail}")
  public String getPointWithDetail(@PathVariable("app") String app,
      @PathVariable("point") String point, @PathVariable("detail") String detail)
      throws IOException {
    return request(app, point + "/" + detail, RequestMethod.GET);
  }

  @PostMapping("/apps/{app}/{point}")
  public boolean postPoint(@PathVariable("app") String app, @PathVariable("point") String point)
      throws IOException {
    return BooleanUtils.toBoolean(request(app, point, RequestMethod.POST));
  }

  @PostMapping("/apps/{app}/{point}/{detail}")
  public boolean postPointWithDetail(@PathVariable("app") String app,
      @PathVariable("point") String point, @PathVariable("detail") String detail)
      throws IOException {
    return BooleanUtils.toBoolean(request(app, point + "/" + detail, RequestMethod.POST));
  }

  private String request(String app, String point, RequestMethod method) throws IOException {
    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(new AuthScope(app, 8080),
        new UsernamePasswordCredentials(username, password));
    HttpClient client = HttpClientBuilder.create()
        .setDefaultCredentialsProvider(credentialsProvider).build();
    HttpRequestBase requestMethod;
    switch (method) {
      case GET:
        requestMethod = new HttpGet("http://" + app + ":8080/" + point);
        break;
      case POST:
        requestMethod = new HttpPost("http://" + app + ":8080/" + point);
        break;
      default:
        throw new RuntimeException("Not supported.");
    }
    InputStream stream = client.execute(requestMethod).getEntity().getContent();
    return IOUtils.toString(stream, "utf8");
  }

  class TreeNode {

    private String text;
    private String iconCls = "icon-none";
    private String state = "open";
    private List<TreeNode> children;

    public TreeNode(String text, List<String> children) {
      this.text = text;
      this.children = children == null ? null
          : children.stream().map(name -> new TreeNode(name, null)).collect(Collectors.toList());
    }

    public String getText() {
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }

    public String getIconCls() {
      return iconCls;
    }

    public void setIconCls(String iconCls) {
      this.iconCls = iconCls;
    }

    public String getState() {
      return state;
    }

    public void setState(String state) {
      this.state = state;
    }

    public List<TreeNode> getChildren() {
      return children;
    }

    public void setChildren(List<TreeNode> children) {
      this.children = children;
    }
  }
}
