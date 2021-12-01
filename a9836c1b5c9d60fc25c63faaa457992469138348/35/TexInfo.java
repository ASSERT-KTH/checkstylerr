package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * Created by Moritz on 28.09.2015.
 */
public class TexInfo {

  private static HttpClient client = null;

  private static String makeRequest(String tex, String url) {
    if (client == null) {
      PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
      client = HttpClients.custom()
          .setConnectionManager(cm)
          .build();
    }
    //HttpPost post = new HttpPost("http://localhost/convert");
    HttpPost post = new HttpPost(url);
    try {
      List<NameValuePair> nameValuePairs = new ArrayList<>(1);
      nameValuePairs.add(new BasicNameValuePair("q", tex));
      post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf-8"));
      HttpResponse response = client.execute(post);
      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      String line;
      String result = "";
      while ((line = rd.readLine()) != null) {
        result += line;
      }
      post.releaseConnection();
      return result;
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      post.releaseConnection();
    }
    return "";
  }

  public static Multiset<String> getIdentifiers(String tex, String url) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException, TransformerException {
    final Multiset<String> strings = HashMultiset.create();
    //long t0 = System.nanoTime();
    String json = makeRequest(tex, url);
    if (tex.length() == 0) {
      return strings;
    }
    //System.out.println((System.nanoTime()-t0)/1000000+"ms for "+tex);
    try {
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(json);
      JSONArray identifiers = jsonObject.getJSONArray("identifiers");
      strings.addAll(identifiers);
    } catch (Exception e) {
      System.out.println(tex + " Parsing problem");
      System.out.println("Retrieved: " + json);
      //e.printStackTrace();
    }
    return strings;
  }
}
