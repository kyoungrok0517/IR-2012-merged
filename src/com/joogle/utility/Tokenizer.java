package com.joogle.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Tokenizer {
	private static PorterStemmer stemmer = new PorterStemmer();

	public static List<String> tokenize(String text) {
		// TODO: 여기서 stopword 제거, 소문자화, stemming 등을 모두 처리
		String pattern = "(?i)[^A-Z]";
		String strippedString = text.replaceAll(pattern, " ");
		StringTokenizer tokenizer = new StringTokenizer(strippedString, " \t\r\n");

		List<String> tokens = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			String t = tokenizer.nextToken().toLowerCase();
//			stemmer.add(t.toCharArray(), t.length());
//			stemmer.stem();
//			String stemmed = stemmer.toString();

			tokens.add(t);
		}
		
		return tokens;
	}
}
