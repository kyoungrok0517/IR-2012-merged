package com.joogle.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TermRankingHelper {
	private List<String> collection;
	private List<Map<String, Integer>> collection_doc_vectors = new ArrayList<Map<String, Integer>>();;

	private List<String> prf_docs;
	private List<Map<String, Integer>> prf_doc_vectors = new ArrayList<Map<String, Integer>>();;

	private List<String> stopwords;

	public TermRankingHelper() {

	}

	public TermRankingHelper(List<String> prf_docs, List<String> collection,
			List<String> stopwords) {
		this.collection = collection;
		for (String doc : collection) {
			Map<String, Integer> doc_vector = getTermVector(doc);
			collection_doc_vectors.add(doc_vector);
		}

		this.prf_docs = prf_docs;
		for (String doc : prf_docs) {
			Map<String, Integer> doc_vector = getTermVector(doc);
			prf_doc_vectors.add(doc_vector);
		}

		this.stopwords = stopwords;
	}

	public double getRSVWeight(String term) {
		return 0.0;
	}

	public double getRocchioWeight(String term) {
		if (stopwords.contains(term)) {
			return 0.0;
		}

		double weight = 0.0;

		for (Map<String, Integer> prf_doc_vector : prf_doc_vectors) {
			if (!prf_doc_vector.containsKey(term)) {
				continue;
			} else {
				int tf = prf_doc_vector.get(term);
				int df = 0;
				for (Map<String, Integer> doc_vector : collection_doc_vectors) {
					if (doc_vector.containsKey(term)) {
						df++;
					}
				}
				if (tf != 0) {					
					weight += ((1 + Math.log(tf)) * Math.log10(collection.size() / df + 1));
				}
			}
		}

		return weight;
	}

	public static List<String> getUniqueTermVector(String document) {
		Map<String, Integer> vector = getTermVector(document);
		List<String> merged_vector = new ArrayList<String>();

		for (String term : vector.keySet()) {
			merged_vector.add(term);
		}

		return merged_vector;
	}

	public static Map<String, Integer> getTermVector(String document) {
		Map<String, Integer> term_vector = new HashMap<String, Integer>();
		List<String> terms = Tokenizer.tokenize(document);

		for (String token : terms) {
			if (!term_vector.containsKey(token)) {
				term_vector.put(token, 1);
			} else {
				term_vector.put(token, term_vector.get(token) + 1);
			}
		}

		return term_vector;
	}
}
