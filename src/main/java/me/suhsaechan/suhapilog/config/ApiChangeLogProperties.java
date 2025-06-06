package me.suhsaechan.suhapilog.config;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiChangeLogProperties {
  private boolean enabled = true;
  private Github github = new Github();
  private Storage storage = new Storage();
  private String defaultAuthor = "";

  @Setter
  @Getter
  public static class Github {
    private String baseUrl;
  }

  @Setter
  @Getter
  public static class Storage {
    private String path = "./github-issues";
  }
}