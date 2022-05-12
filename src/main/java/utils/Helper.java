package utils;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Helper {

  public static URL getResource(String resource) {
    var result = Helper.class.getClassLoader().getResource(resource);
    if (result == null) {
      throw new IllegalStateException(String.format("Could not find '%s' on the classpath", resource));
    }
    return result;
  }

  public static URI getURI(String resource) {
    try {
      return getResource(resource).toURI();
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  public static Path getPath(String resource) {
    return Paths.get(getURI(resource));
  }

} /* ENDCLASS */
