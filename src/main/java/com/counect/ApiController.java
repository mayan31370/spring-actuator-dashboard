package com.counect;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Created by mayan on 17-8-2.
 */
@RestController
public class ApiController {

  @Value("${actuator-dashboard.hosts}")
  private String hostsInString;

  private List<TreeNode> getApps() throws IOException, ParserConfigurationException, SAXException {
    HttpClient client = HttpClientBuilder.create().build();
    List<TreeNode> result = new ArrayList<>();
    for (String host : StringUtils.split(hostsInString, ",")) {
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
          client.execute(new HttpGet("http://" + host + "/eureka/apps")).getEntity()
              .getContent());
      NodeList nodeList = document.getElementsByTagName("hostName");
      List<String> nodes = new ArrayList<>(nodeList.getLength());
      for (int i = 0; i < nodeList.getLength(); i++) {
        nodes.add(nodeList.item(i).getTextContent());
      }
      result.add(new TreeNode(host, nodes));
    }
    return result;
  }

  @GetMapping("/apps")
  public List<TreeNode> apps() {
    try {
      return getApps();
    } catch (IOException | ParserConfigurationException | SAXException e) {
      return Collections.EMPTY_LIST;
    }
  }

  @GetMapping("/apps/{app}/{point}")
  public String getPoint(@PathVariable("app") String app, @PathVariable("point") String point)
      throws IOException {
    return request(app, point, RequestMethod.GET, null);
  }

  @GetMapping("/apps/{app}/{point}/{detail:.*?}")
  public String getPointWithDetail(@PathVariable("app") String app,
      @PathVariable("point") String point, @PathVariable("detail") String detail)
      throws IOException {
    return request(app, point + "/" + detail, RequestMethod.GET, null);
  }

  @PostMapping("/apps/{app}/pause")
  public String postPause(@PathVariable("app") String app)
      throws IOException {
    return request(app, "pause", RequestMethod.POST, null);
  }

  @PostMapping("/apps/{app}/resume")
  public String postResume(@PathVariable("app") String app)
      throws IOException {
    return request(app, "resume", RequestMethod.POST, null);
  }

  @PostMapping("/apps/{app}/restart")
  public String postRestart(@PathVariable("app") String app)
      throws IOException {
    return request(app, "restart", RequestMethod.POST, null);
  }

  @PostMapping("/apps/{app}/env")
  public String postEnv(@PathVariable("app") String app)
      throws IOException {
    return request(app, "env", RequestMethod.POST, null);
  }

  @PostMapping("/apps/{app}/refresh")
  public String postRefresh(@PathVariable("app") String app)
      throws IOException {
    return request(app, "refresh", RequestMethod.POST, null);
  }

  @PostMapping("/apps/{app}/loggers/{name:.*?}")
  public String postLogger(@PathVariable("app") String app,
      @PathVariable("name") String name, String level)
      throws IOException {
    return request(app, "loggers/" + name, RequestMethod.POST, level);
  }

  private String request(String app, String point, RequestMethod method, String body)
      throws IOException {
    HttpClient client = HttpClientBuilder.create().build();
    HttpRequestBase requestMethod;
    switch (method) {
      case GET:
        requestMethod = new HttpGet("http://" + app + ":8080/" + point);
        break;
      case POST:
        HttpPost httpPost = new HttpPost("http://" + app + ":8080/" + point);
        if (StringUtils.isNotBlank(body)) {
          httpPost.setEntity(new StringEntity(String.format("{\"configuredLevel\":\"%s\"}", body),
              ContentType.APPLICATION_JSON));
        }
        requestMethod = httpPost;
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
