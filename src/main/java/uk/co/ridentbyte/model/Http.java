package uk.co.ridentbyte.model;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
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
                httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
            } else {
                httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
            }
            switch(request.getVerb()) {
                case "POST": {
                    var httpRequestBuilder = HttpRequest.newBuilder().uri(URI.create(parsedUri));
                    if (request.getBody() != null) {
                        httpRequestBuilder = httpRequestBuilder.POST(HttpRequest.BodyPublishers.ofString(request.getBody()));
                    } else {
                        httpRequestBuilder = httpRequestBuilder.POST(HttpRequest.BodyPublishers.noBody());
                    }

                    for (Map.Entry<String, String> header : splitHeaders.entrySet()) {
                        httpRequestBuilder = httpRequestBuilder.header(header.getKey(), header.getValue());
                    }

                    HttpResponse<String> response = httpClient.send(httpRequestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                    var responseHeaders = new HashMap<String, String>();
                    for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
                        responseHeaders.put(header.getKey(), String.join("", header.getValue()));
                    }
                    return new CardinalHttpResponse(response.body(), responseHeaders, response.statusCode());
                }

                case "PUT": {
                    var httpRequestBuilder = HttpRequest.newBuilder().uri(URI.create(parsedUri));
                    if (request.getBody() != null) {
                        httpRequestBuilder = httpRequestBuilder.PUT(HttpRequest.BodyPublishers.ofString(request.getBody()));
                    } else {
                        httpRequestBuilder = httpRequestBuilder.PUT(HttpRequest.BodyPublishers.noBody());
                    }

                    for (Map.Entry<String, String> header : splitHeaders.entrySet()) {
                        httpRequestBuilder = httpRequestBuilder.header(header.getKey(), header.getValue());
                    }

                    HttpResponse<String> response = httpClient.send(httpRequestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                    var responseHeaders = new HashMap<String, String>();
                    for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
                        responseHeaders.put(header.getKey(), String.join("", header.getValue()));
                    }
                    return new CardinalHttpResponse(response.body(), responseHeaders, response.statusCode());
                }

                case "GET": {
                    var httpRequestBuilder = HttpRequest.newBuilder().uri(URI.create(parsedUri)).GET();

                    for (Map.Entry<String, String> header : splitHeaders.entrySet()) {
                        httpRequestBuilder = httpRequestBuilder.header(header.getKey(), header.getValue());
                    }

                    HttpResponse<String> response = httpClient.send(httpRequestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                    var responseHeaders = new HashMap<String, String>();
                    for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
                        responseHeaders.put(header.getKey(), String.join("", header.getValue()));
                    }
                    return new CardinalHttpResponse(response.body(), responseHeaders, response.statusCode());
                }

                case "DELETE": {
                    var httpRequestBuilder = HttpRequest.newBuilder().uri(URI.create(parsedUri)).DELETE();

                    for (Map.Entry<String, String> header : splitHeaders.entrySet()) {
                        httpRequestBuilder = httpRequestBuilder.header(header.getKey(), header.getValue());
                    }

                    HttpResponse<String> response = httpClient.send(httpRequestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                    var responseHeaders = new HashMap<String, String>();
                    for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
                        responseHeaders.put(header.getKey(), String.join("", header.getValue()));
                    }
                    return new CardinalHttpResponse(response.body(), responseHeaders, response.statusCode());
                }

                case "HEAD": {
                    var httpRequestBuilder = HttpRequest.newBuilder().uri(URI.create(parsedUri));
                    if (request.getBody() != null) {
                        httpRequestBuilder = httpRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.ofString(request.getBody()));
                    } else {
                        httpRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
                    }

                    for (Map.Entry<String, String> header : splitHeaders.entrySet()) {
                        httpRequestBuilder = httpRequestBuilder.header(header.getKey(), header.getValue());
                    }

                    HttpResponse<String> response = httpClient.send(httpRequestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                    var responseHeaders = new HashMap<String, String>();
                    for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
                        responseHeaders.put(header.getKey(), String.join("", header.getValue()));
                    }
                    return new CardinalHttpResponse(response.body(), responseHeaders, response.statusCode());
                }

                case "OPTIONS": {
                    var httpRequestBuilder = HttpRequest.newBuilder().uri(URI.create(parsedUri));
                    if (request.getBody() != null) {
                        httpRequestBuilder = httpRequestBuilder.method("OPTIONS", HttpRequest.BodyPublishers.ofString(request.getBody()));
                    } else {
                        httpRequestBuilder.method("OPTIONS", HttpRequest.BodyPublishers.noBody());
                    }

                    for (Map.Entry<String, String> header : splitHeaders.entrySet()) {
                        httpRequestBuilder = httpRequestBuilder.header(header.getKey(), header.getValue());
                    }

                    HttpResponse<String> response = httpClient.send(httpRequestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                    var responseHeaders = new HashMap<String, String>();
                    for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
                        responseHeaders.put(header.getKey(), String.join("", header.getValue()));
                    }
                    return new CardinalHttpResponse(response.body(), responseHeaders, response.statusCode());
                }

                case "TRACE": {
                    var httpRequestBuilder = HttpRequest.newBuilder().uri(URI.create(parsedUri));
                    if (request.getBody() != null) {
                        httpRequestBuilder = httpRequestBuilder.method("TRACE", HttpRequest.BodyPublishers.ofString(request.getBody()));
                    } else {
                        httpRequestBuilder.method("TRACE", HttpRequest.BodyPublishers.noBody());
                    }

                    for (Map.Entry<String, String> header : splitHeaders.entrySet()) {
                        httpRequestBuilder = httpRequestBuilder.header(header.getKey(), header.getValue());
                    }

                    HttpResponse<String> response = httpClient.send(httpRequestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                    var responseHeaders = new HashMap<String, String>();
                    for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
                        responseHeaders.put(header.getKey(), String.join("", header.getValue()));
                    }
                    return new CardinalHttpResponse(response.body(), responseHeaders, response.statusCode());
                }

                case "PATCH": {
                    var httpRequestBuilder = HttpRequest.newBuilder().uri(URI.create(parsedUri));
                    if (request.getBody() != null) {
                        httpRequestBuilder = httpRequestBuilder.method("PATCH", HttpRequest.BodyPublishers.ofString(request.getBody()));
                    } else {
                        httpRequestBuilder.method("PATCH", HttpRequest.BodyPublishers.noBody());
                    }

                    for (Map.Entry<String, String> header : splitHeaders.entrySet()) {
                        httpRequestBuilder = httpRequestBuilder.header(header.getKey(), header.getValue());
                    }

                    HttpResponse<String> response = httpClient.send(httpRequestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                    var responseHeaders = new HashMap<String, String>();
                    for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
                        responseHeaders.put(header.getKey(), String.join("", header.getValue()));
                    }
                    return new CardinalHttpResponse(response.body(), responseHeaders, response.statusCode());
                }

                default:
                    throw new RuntimeException("Invalid HTTP method");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
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
