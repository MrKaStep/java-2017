package ru.mipt.java2017.hw3;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YandexDiskFileRetriever {
  private static final Logger logger = LoggerFactory.getLogger("YDiskRetriever");

  private HttpClient httpClient = HttpClients.createSystem();
  private CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

  private static final String DISK_URL = "https://webdav.yandex.ru";

  public YandexDiskFileRetriever(String login, String password) {
    Credentials credentials = new UsernamePasswordCredentials(login, password);
    credentialsProvider.setCredentials(AuthScope.ANY, credentials);
  }

  public InputStream getFile(String path) throws IOException {
    HttpGet request = null;
    try {
      URIBuilder builder= new URIBuilder(DISK_URL);
      builder.setPath("/" + path);
      request = new HttpGet(builder.build());
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
    HttpClientContext context = HttpClientContext.create();
    context.setCredentialsProvider(credentialsProvider);
    HttpResponse response = httpClient.execute(request, context);
    int code = response.getStatusLine().getStatusCode();
    int codeClass = code / 100;
    if (codeClass == 4 || codeClass == 5) {
      logger.error("Error {}: {}",
          response.getStatusLine().getStatusCode(),
          response.getStatusLine().getReasonPhrase()
      );
      return null;
    }
    return response.getEntity().getContent();
  }
}
