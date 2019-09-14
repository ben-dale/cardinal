package uk.co.ridentbyte.model;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Http {

    private CardinalRequest request;

    public Http(CardinalRequest request) {
        this.request = request;
    }

    public CardinalHttpResponse send() {
        String parsedUri = parseUri(request.getUri());
        Map<String, String> splitHeaders = request.getHeaders().stream().map((h) -> {
           String[] splitHeader = h.split(":");
           if (splitHeader.length == 2) {
               return new AbstractMap.SimpleEntry<>(splitHeader[0], splitHeader[1]);
           } else {
               return new AbstractMap.SimpleEntry<>(splitHeader[0], "");
           }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        try {
            HttpClient httpClient = null;
            if (request.shouldFollowRedirects()) {
                httpClient = HttpClientBuilder.create().build();
            } else {
                httpClient = HttpClientBuilder.create().disableRedirectHandling().build();
            }
            switch(request.getVerb()) {
                case "POST": {
                    HttpPost httpPost = new HttpPost(parsedUri);
                    splitHeaders.forEach((k, v) -> httpPost.setHeader(k, v));
                    if (request.getBody() != null) {
                        httpPost.setEntity(new StringEntity(request.getBody()));
                    }

                    HttpResponse res = httpClient.execute(httpPost);
                    Map<String, String> headers = new HashMap<>();
                    for (Header h : res.getAllHeaders()) {
                        headers.put(h.getName(), h.getValue());
                    }
                    String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity()) : "";
                    int code = res.getStatusLine().getStatusCode();

                    return new CardinalHttpResponse(body, headers, code);
                }
                case "PUT": {
                    HttpPut httpPut = new HttpPut(parsedUri);
                    splitHeaders.forEach((k, v) -> httpPut.setHeader(k, v));
                    if (request.getBody() != null) {
                        httpPut.setEntity(new StringEntity(request.getBody()));
                    }

                    HttpResponse res = httpClient.execute(httpPut);
                    Map<String, String> headers = new HashMap<>();
                    for (Header h : res.getAllHeaders()) {
                        headers.put(h.getName(), h.getValue());
                    }
                    String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity()) : "";
                    int code = res.getStatusLine().getStatusCode();

                    return new CardinalHttpResponse(body, headers, code);
                }

                case "GET": {
                    HttpGet httpGet = new HttpGet(parsedUri);
                    splitHeaders.forEach((k, v) -> httpGet.setHeader(k, v));

                    HttpResponse res = httpClient.execute(httpGet);
                    Map<String, String> headers = new HashMap<>();
                    for (Header h : res.getAllHeaders()) {
                        headers.put(h.getName(), h.getValue());
                    }
                    String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity()) : "";
                    int code = res.getStatusLine().getStatusCode();

                    return new CardinalHttpResponse(body, headers, code);
                }

                case "DELETE": {
                    HttpDelete httpDelete = new HttpDelete(parsedUri);
                    splitHeaders.forEach((k, v) -> httpDelete.setHeader(k, v));

                    HttpResponse res = httpClient.execute(httpDelete);
                    Map<String, String> headers = new HashMap<>();
                    for (Header h : res.getAllHeaders()) {
                        headers.put(h.getName(), h.getValue());
                    }
                    String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity()) : "";
                    int code = res.getStatusLine().getStatusCode();

                    return new CardinalHttpResponse(body, headers, code);
                }

                case "HEAD": {
                    HttpHead httpHead = new HttpHead(parsedUri);
                    splitHeaders.forEach((k, v) -> httpHead.setHeader(k, v));

                    HttpResponse res = httpClient.execute(httpHead);
                    Map<String, String> headers = new HashMap<>();
                    for (Header h : res.getAllHeaders()) {
                        headers.put(h.getName(), h.getValue());
                    }
                    String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity()) : "";
                    int code = res.getStatusLine().getStatusCode();

                    return new CardinalHttpResponse(body, headers, code);
                }

                case "OPTIONS": {
                    HttpOptions httpOptions = new HttpOptions(parsedUri);
                    splitHeaders.forEach((k, v) -> httpOptions.setHeader(k, v));

                    HttpResponse res = httpClient.execute(httpOptions);
                    Map<String, String> headers = new HashMap<>();
                    for (Header h : res.getAllHeaders()) {
                        headers.put(h.getName(), h.getValue());
                    }
                    String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity()) : "";
                    int code = res.getStatusLine().getStatusCode();

                    return new CardinalHttpResponse(body, headers, code);
                }

                case "TRACE": {
                    HttpTrace httpTrace = new HttpTrace(parsedUri);
                    splitHeaders.forEach((k, v) -> httpTrace.setHeader(k, v));

                    HttpResponse res = httpClient.execute(httpTrace);
                    Map<String, String> headers = new HashMap<>();
                    for (Header h : res.getAllHeaders()) {
                        headers.put(h.getName(), h.getValue());
                    }
                    String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity()) : "";
                    int code = res.getStatusLine().getStatusCode();

                    return new CardinalHttpResponse(body, headers, code);
                }

                case "PATCH": {
                    HttpPatch httpPatch = new HttpPatch(parsedUri);
                    splitHeaders.forEach((k, v) -> httpPatch.setHeader(k, v));
                    if (request.getBody() != null) {
                        httpPatch.setEntity(new StringEntity(request.getBody()));
                    }

                    HttpResponse res = httpClient.execute(httpPatch);
                    Map<String, String> headers = new HashMap<>();
                    for (Header h : res.getAllHeaders()) {
                        headers.put(h.getName(), h.getValue());
                    }
                    String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity()) : "";
                    int code = res.getStatusLine().getStatusCode();

                    return new CardinalHttpResponse(body, headers, code);
                }

                default:
                    throw new RuntimeException("Invalid HTTP method");
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid HTTP method");
        }

    }

    private String parseUri(String rawUri) {
        String validUri = "";
        try {
            new URL(rawUri);
            validUri = rawUri;
        } catch (MalformedURLException e) {
            validUri =  "http://" + rawUri;
        }

        try {
            String decodedUri = URLDecoder.decode(validUri, "UTF-8");
            URL url = new URL(decodedUri);
            URI uri = new URI(
                    url.getProtocol(),
                    url.getUserInfo(),
                    url.getHost(),
                    url.getPort(),
                    url.getPath(),
                    url.getQuery(),
                    url.getRef()
            );
            return uri.toASCIIString();
        } catch (UnsupportedEncodingException | MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
            return "";
        }
    }

}
