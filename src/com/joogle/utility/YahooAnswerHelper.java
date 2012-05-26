package com.joogle.utility;
import java.io.IOException;
import java.util.ArrayList;
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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.joogle.model.YahooAnswer;
import com.joogle.model.YahooQuestion;

public class YahooAnswerHelper {
	private static HttpClient client = new DefaultHttpClient();
	private static Gson gson = new Gson();
	private static JsonParser parser = new JsonParser();

	private static String API_SECRET = "e23c1b9928c9eaab910e58159db865c8a257201b";
	private static String QUESTION_SEARCH_URL = "http://answers.yahooapis.com/AnswersService/V1/questionSearch";
	private static String QUESTION_DETAIL_URL = "http://answers.yahooapis.com/AnswersService/V1/getQuestion";

	public static List<YahooAnswer> getAnswers(YahooQuestion question) {
		String question_id = question.Id;
		return getAnswers(question_id);
	}

	public static List<YahooAnswer> getAnswers(String question_id) {
		String url = buildQuestionDetailURL(question_id);
		HttpGet method = new HttpGet(url);
		List<YahooAnswer> answers = new ArrayList<YahooAnswer>();

		try {
			HttpResponse response = client.execute(method);
			HttpEntity entity = response.getEntity();

			if (entity != null) { // we have the response
				int code = response.getStatusLine().getStatusCode();

				if (code >= 300) { // the server responded with error
					System.err.println("Failed");
				} else {
					String string_response = EntityUtils.toString(entity);
					JsonObject json_response = parser.parse(string_response)
							.getAsJsonObject();
					JsonObject json_all = json_response.get("all")
							.getAsJsonObject();
					JsonArray json_answers = json_all.get("answers")
							.getAsJsonArray();

					for (JsonElement item : json_answers) {
						YahooAnswer answer = gson.fromJson(item,
								YahooAnswer.class);
						answers.add(answer);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return answers;
	}

	public static List<YahooQuestion> searchQuestions(String query) {
		String url = buildQuestionSearchURL(query);
		HttpGet method = new HttpGet(url);
		List<YahooQuestion> questions = new ArrayList<YahooQuestion>();

		try {
			HttpResponse response = client.execute(method);
			HttpEntity entity = response.getEntity();

			if (entity != null) { // we have the response
				int code = response.getStatusLine().getStatusCode();

				if (code >= 300) { // the server responded with error
					System.err.println("Failed");
				} else {
					String string_response = EntityUtils.toString(entity);
					JsonObject json_response = parser.parse(string_response)
							.getAsJsonObject();
					JsonObject json_all = json_response.get("all")
							.getAsJsonObject();
					JsonArray json_questions = json_all.get("questions")
							.getAsJsonArray();

					for (JsonElement item : json_questions) {
						YahooQuestion question = gson.fromJson(item,
								YahooQuestion.class);
						questions.add(question);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return questions;
	}

	private static String buildQuestionSearchURL(String query) {
		// build HTTP query string that will be attached to URL
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("appid", API_SECRET));
		params.add(new BasicNameValuePair("query", query));
		params.add(new BasicNameValuePair("output", "json"));
		params.add(new BasicNameValuePair("results", "10"));
		String query_string = URLEncodedUtils.format(params, "UTF-8");

		// build URL for question search
		String url = QUESTION_SEARCH_URL + "?" + query_string;

		// return the url
		return url;
	}

	private static String buildQuestionDetailURL(String question_id) {
		// build HTTP query string that will be attached to URL
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("appid", API_SECRET));
		params.add(new BasicNameValuePair("question_id", question_id));
		params.add(new BasicNameValuePair("output", "json"));
		String query_string = URLEncodedUtils.format(params, "UTF-8");

		// build URL for question search
		String url = QUESTION_DETAIL_URL + "?" + query_string;

		// return the url
		return url;
	}
}
