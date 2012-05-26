package com.joogle.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLExtractor {
	// Pattern for recognizing a URL, based off RFC 3986
	private static final Pattern urlPattern = Pattern.compile(
			"(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
					+ "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
					+ "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	public static String extractURL(String text) {
		Matcher matcher = urlPattern.matcher(text);

		while (matcher.find()) {
			int matchStart = matcher.start(1);
			int matchEnd = matcher.end();
			
			// now you have the offsets of a URL match
			String url = text.substring(matchStart, matchEnd);
			
			System.out.println(url);
		}
		
		return "";
	}
}
