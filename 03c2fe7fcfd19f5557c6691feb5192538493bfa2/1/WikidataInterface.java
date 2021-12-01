package com.formulasearchengine.mathosphere.mlp.text;

import com.jcabi.log.Logger;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Moritz on 28.09.2015.
 */
public class WikidataInterface {
    private static String makeRequest(String term, String lang) throws URISyntaxException, IOException {
        HttpClient client = new DefaultHttpClient();
        URI uri = new URIBuilder()
                .setScheme("http")
                .setHost("www.wikidata.org")
                .setPath("/w/api.php")
                .setParameter("format", "json")
                .setParameter("action", "wbsearchentities")
                .setParameter("uselang", "en")
                .setParameter("language", lang)
                .setParameter("search", term)
                .build();
        HttpGet get = new HttpGet(uri);
        HttpResponse response = client.execute(get);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;
        String result = "";
        while ((line = rd.readLine()) != null) {
            result += line;
        }
        return result;
    }

    private static String getAlias(String qid, String lang) throws URISyntaxException, IOException {
        HttpClient client = new DefaultHttpClient();
        URI uri = new URIBuilder()
                .setScheme("http")
                .setHost("www.wikidata.org")
                .setPath("/w/api.php")
                .setParameter("format", "json")
                .setParameter("action", "wbgetentities")
                .setParameter("languages", "en")
                .setParameter("ids", qid)
                .setParameter("props", "aliases")
                .build();
        HttpGet get = new HttpGet(uri);
        HttpResponse response = client.execute(get);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;
        String result = "";
        while ((line = rd.readLine()) != null) {
            result += line;
        }
        return result;
    }

    public static List<String> getEntities(String text) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException, TransformerException, URISyntaxException {
        final ArrayList<String> strings = new ArrayList<>();
        String json = makeRequest(text, "en");
        try {
            JSONObject Ojson = (JSONObject) JSONSerializer.toJSON(json);
            if (Ojson.getInt("success") < 1) {
                return strings;
            }
            JSONArray results = Ojson.getJSONArray("search");
            for (Object result : results) {
                final JSONObject sres = (JSONObject) result;
                strings.add(sres.getString("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strings;
    }

    public static List<String> getAliases(String qid) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException, TransformerException, URISyntaxException {
        final ArrayList<String> strings = new ArrayList<>();
        String json = getAlias(qid, "en");
        try {
            JSONObject Ojson = (JSONObject) JSONSerializer.toJSON(json);
            if (Ojson.getInt("success") < 1) {
                return Arrays.asList(new String[] {"fail!"});
            }
            try {
                JSONArray results = ((JSONObject) ((JSONObject) ((JSONObject) Ojson.get("entities")).get(qid)).get("aliases")).getJSONArray("en");
                for (Object result : results) {
                    final JSONObject sres = (JSONObject) result;
                    strings.add((String) sres.get("value"));
                }
            } catch (JSONException e) {
                Logger.error(((JSONObject) ((JSONObject) Ojson.get("entities")).get(qid)).get("aliases"),e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strings;
    }
}
