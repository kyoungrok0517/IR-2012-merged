package com.joogle.utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class BingSearchHelper {
	private static HttpClient client = new DefaultHttpClient();
	private static Gson gson = new Gson();
	private static JsonParser parser = new JsonParser();

	private static String BING_SEARCH_URL = "http://api.bing.com/osjson.aspx";

	public static List<String> getSuggestedQueries(String query) {
		String url = buildSearchURL(query);
		HttpGet method = new HttpGet(url);
		List<String> suggested_queries = new ArrayList<String>();

		try {
			HttpResponse response = client.execute(method);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				int code = response.getStatusLine().getStatusCode();

				if (code >= 300) {
					System.err.println("Failed to fetch suggested terms");
					return suggested_queries;
				} else {
					String string_response = EntityUtils.toString(entity);
					JsonArray json_response = parser.parse(string_response)
							.getAsJsonArray();
					JsonArray expanded_queries = json_response.get(1)
							.getAsJsonArray();
					for (JsonElement query_element : expanded_queries) {
						String exq = query_element.getAsString();
						suggested_queries.add(exq);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Failed to fetch suggested terms");
		}

		return suggested_queries;
	}

	public static List<String> getSuggestedTerms(String query) {
		List<String> suggested_queries = getSuggestedQueries(query);
		List<String> suggested_terms = new ArrayList<String>();

		int original_query_term_count = query.split(" ").length;

		for (String q : suggested_queries) {
			String[] terms_in_suggested_queries = q.split(" ");

			for (int i = original_query_term_count; i < terms_in_suggested_queries.length; i++) {
				suggested_terms.add(terms_in_suggested_queries[i]);
			}
		}

		return suggested_terms;
	}

	private static String buildSearchURL(String query) {
		// build HTTP query string that will be attached to URL
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("query", query));
		String query_string = URLEncodedUtils.format(params, "UTF-8");

		// build URL for question search
		String url = BING_SEARCH_URL + "?" + query_string;

		// return the url
		return url;
	}
}
