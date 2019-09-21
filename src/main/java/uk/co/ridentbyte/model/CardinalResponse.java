package uk.co.ridentbyte.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CardinalResponse {

    private CardinalHttpResponse raw;
    private long time;

    public CardinalResponse(CardinalHttpResponse raw, long time) {
        this.raw = raw;
        this.time = time;
    }

    public String formattedBody() {
        String contentType = raw.getHeaders().get("Content-Type");
        if (contentType != null && contentType.contains("application/json")) {
            try {
                JsonParser parser = new JsonParser();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonElement el = parser.parse(this.raw.getBody());
                return gson.toJson(el);
            } catch (JsonSyntaxException jse) {
                return this.raw.getBody();
            }
        } else if (contentType != null && contentType.contains("application/xml")) {
            String body = this.raw.getBody();
            if (body.startsWith("\u00EF\u00BB\u00BF")) {
                body = body.replaceFirst("\u00EF\u00BB\u00BF", "");
            }

            try {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8))));
                document.normalize();
                XPath xPath = XPathFactory.newInstance().newXPath();
                NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']", document, XPathConstants.NODESET);
                for (int i = 0; i < nodeList.getLength(); ++i) {
                    Node node = nodeList.item(i);
                    node.getParentNode().removeChild(node);
                }

                // Setup pretty print options
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                transformerFactory.setAttribute("indent-number", 2);
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");

                StringWriter stringWriter = new StringWriter();
                transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
                return stringWriter.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return this.raw.getBody();
            }
        } else {
            return this.raw.getBody();
        }
    }

    public String toCSV() {
        String statusCodeString = "\"" + this.raw.getStatusCode() + "\"";
        String bodyString = "\"" + this.formattedBody().replace("\"", "\"\"") + "\"";
        String timeString = "\"" + this.time + "\"";
        StringBuilder headerStringBuilder = new StringBuilder();
        for (Map.Entry<String, String> header : this.raw.getHeaders().entrySet()) {
            headerStringBuilder.append(header.getKey());
            headerStringBuilder.append(":");
            headerStringBuilder.append(header.getValue());
            headerStringBuilder.append("\n");
        }
        String headersString = "\"" + headerStringBuilder.toString().replace("\"", "\"\"") + "\"";
        return statusCodeString + "," + headersString + "," + bodyString + "," + timeString;
    }

    public long getTime() {
        return this.time;
    }

    public int getStatusCode() {
        return this.raw.getStatusCode();
    }

    public Map<String, String> getHeaders() {
        return this.raw.getHeaders();
    }

    public static CardinalResponse blank() {
        return new CardinalResponse(new CardinalHttpResponse("", new HashMap<>(), 0), 0L) {
            @Override
            public String toCSV() {
                return ",,,";
            }

            @Override
            public String formattedBody() {
                return "";
            }
        };
    }

    public static String csvHeaders() {
        return "response code,response headers,response body,response time (ms)";
    }

    @Override
    public String toString() {
        return "CardinalResponse{" +
                "raw=" + raw +
                ", time=" + time +
                '}';
    }
}
