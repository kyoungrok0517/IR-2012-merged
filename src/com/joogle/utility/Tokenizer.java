package com.joogle.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Tokenizer {
//	private static PorterStemmer stemmer = new PorterStemmer();
	private static List<String> stopwords;

	public static List<String> tokenize(String text) {
		// TODO: 여기서 stopword 제거, 소문자화, stemming 등을 모두 처리
		stopwords =  populateStopWords("./rsc/english_stopword_v2.txt");
		String pattern = "(?i)[^A-Z]";
		String strippedString = text.replaceAll(pattern, " ");
		StringTokenizer tokenizer = new StringTokenizer(strippedString,
				" \t\r\n");

		List<String> tokens = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().toLowerCase();
			// stemmer.add(t.toCharArray(), t.length());
			// stemmer.stem();
			// String stemmed = stemmer.toString();

			if (!stopwords.contains(token)) {
				tokens.add(token);
			}
		}

		return tokens;
	}

	private static List<String> populateStopWords(String filename) {
		BufferedReader reader = null;
		List<String> result = new ArrayList<String>();

		try {
			reader = new BufferedReader(new FileReader(filename));

			while (true) {
				String stopword = reader.readLine();

				if (stopword != null) {
					result.add(stopword);
				} else {
					break;
				}
			}
		} catch (IOException e) {

		}

		return result;
	}
}
