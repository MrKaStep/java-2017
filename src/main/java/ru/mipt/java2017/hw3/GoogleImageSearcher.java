package ru.mipt.java2017.hw3;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleImageSearcher {

  private static final Logger logger = LoggerFactory.getLogger("GoogleSearcher");

  private static final int CONNECTION_TIMEOUT_MS = 3000;

  private HttpClient httpClient = HttpClients.createSystem();

  RequestConfig requestConfig = RequestConfig.custom()
      .setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
      .setConnectTimeout(CONNECTION_TIMEOUT_MS)
      .setSocketTimeout(CONNECTION_TIMEOUT_MS)
      .build();

  private static final String GOOGLE_API_URL = "https://www.googleapis.com/customsearch/v1";
  private final String apiKey;
  private final String searchContext;

  public GoogleImageSearcher(String apiKey, String searchContext) {
    this.apiKey = apiKey;
    this.searchContext = searchContext;

  }

  public String searchImage(String query)  {
    logger.debug("Attempting search on query \"{}\"", query);
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
      logger.warn("Search interrupted!");
      e.printStackTrace();
      return null;
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
      request.setConfig(requestConfig);
    } catch (URISyntaxException e) {
      e.printStackTrace();
      return null;
    }
    logger.debug("Search url: {}", request.getURI().toASCIIString());
    HttpResponse response = null;
    try {
      response = httpClient.execute(request);
    } catch (IOException e) {
      logger.warn("Request execution failed: {}", e.getMessage());
      return null;
    }
    int code = response.getStatusLine().getStatusCode();
    int codeClass = code / 100;
    if (codeClass == 4 || codeClass == 5) {
      logger.warn("Error {}: {}",
          response.getStatusLine().getStatusCode(),
          response.getStatusLine().getReasonPhrase(),
          request.getURI().toASCIIString()
      );
      return null;
    }
    logger.debug("Response recieved!");
    String jsonString = null;
    try {
      InputStream responseContents = response.getEntity().getContent();
      jsonString = IOUtils.toString(responseContents, Charset.forName("UTF-8"));
    } catch (IOException e) {
      logger.warn("Responce parsing failed: {}", e.getMessage());
    }
    EntityUtils.consumeQuietly(response.getEntity());
    JSONArray items = null;
    try {
      JSONObject jsonObject = new JSONObject(jsonString);
      items = jsonObject.getJSONArray("items");
    } catch (JSONException e) {
      logger.warn("Invalid JSON recieved: {}", jsonString);
      return null;
    }
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
