package kr.suhsaechan.suhapilog.config;

public class ApiChangeLogProperties {
  private boolean enabled = true;
  private Github github = new Github();
  private Storage storage = new Storage();
  private String defaultAuthor = "";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Github getGithub() {
    return github;
  }

  public void setGithub(Github github) {
    this.github = github;
  }

  public Storage getStorage() {
    return storage;
  }

  public void setStorage(Storage storage) {
    this.storage = storage;
  }

  public String getDefaultAuthor() {
    return defaultAuthor;
  }

  public void setDefaultAuthor(String defaultAuthor) {
    this.defaultAuthor = defaultAuthor;
  }

  public static class Github {
    private String baseUrl;

    public String getBaseUrl() {
      return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
    }
  }

  public static class Storage {
    private String path = "./github-issues";

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }
  }
}