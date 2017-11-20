package ru.mipt.java2017.hw3;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleImageSearcher {
  private final static Logger logger = LoggerFactory.getLogger("GoogleSearcher");

  private HttpClient httpClient = HttpClients.createSystem();
//  private final static int CONNECTION_TIMEOUT_MS = 2000;

  private static final String GOOGLE_API_URL = "https://www.googleapis.com/customsearch/v1";
  private final String apiKey;
  private final String searchContext;

  public GoogleImageSearcher(String apiKey, String searchContext) {
    this.apiKey = apiKey;
    this.searchContext = searchContext;

  }

  public String searchImage(String query) throws IOException {
    logger.debug("Attempting search on query \"{}\"", query);
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    URIBuilder uriBuilder = null;
    HttpGet request = null;
    try {
      uriBuilder = new URIBuilder(GOOGLE_API_URL);
      uriBuilder.addParameter("key", apiKey);
      uriBuilder.addParameter("cx", searchContext);
      uriBuilder.addParameter("safe", "high");
      uriBuilder.addParameter("num", "10");
      uriBuilder.addParameter("searchType", "image");
      uriBuilder.addParameter("imgSize", "medium");
      uriBuilder.addParameter("fileType", "jpg");
      uriBuilder.addParameter("q", query);
      request = new HttpGet(uriBuilder.build());
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
//    RequestConfig requestConfig = RequestConfig.custom()
//        .setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
//        .setConnectTimeout(CONNECTION_TIMEOUT_MS)
//        .setSocketTimeout(CONNECTION_TIMEOUT_MS)
//        .build();
//    request.setConfig(requestConfig);
    logger.debug("Search url: {}", request.getURI().toASCIIString());
    HttpResponse response = httpClient.execute(request);
    int code = response.getStatusLine().getStatusCode();
    int codeClass = code / 100;
    if (codeClass == 4 || codeClass == 5) {
      logger.error("Error {}: {}",
          response.getStatusLine().getStatusCode(),
          response.getStatusLine().getReasonPhrase(),
          request.getURI().toASCIIString()
      );
      return null;
    }
    logger.debug("Response recieved!");
    InputStream responseContents = response.getEntity().getContent();
    String jsonString = IOUtils.toString(responseContents, Charset.forName("UTF-8"));
    EntityUtils.consumeQuietly(response.getEntity());
    JSONObject jsonObject = new JSONObject(jsonString);
    JSONArray items = jsonObject.getJSONArray("items");
    for (Object object : items) {
      JSONObject item = (JSONObject) object;
      String link = item.getString("link");
      try {
        logger.debug("Search successful!");
        return new URL(link).toString();
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }
    logger.debug("Search failed!");
    return null;
  }

}
