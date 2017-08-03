package com.counect;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Created by mayan on 17-8-2.
 */
@Controller
public class PagesController {

  @GetMapping("/")
  public String index(Model model) {
    return "index";
  }

  @GetMapping("/apps/{appId}")
  public String app(@PathVariable("appId") String appId, Model model) {
    model.addAttribute("appId", appId);
    return "app";
  }
}