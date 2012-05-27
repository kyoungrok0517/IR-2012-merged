package com.joogle.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TermRankingHelper {
	private List<String> prf_docs;
	private List<Map<String, Integer>> prf_doc_vectors = new ArrayList<Map<String, Integer>>();;

	private List<Map<String, Integer>> corpus_vectors;
	private Map<String, Integer> corpus_vector_merged;

	public TermRankingHelper() {

	}

	public TermRankingHelper(List<String> prf_docs,
			List<Map<String, Integer>> corpus_vectors,
			Map<String, Integer> corpus_vector_merged) {

		this.corpus_vectors = corpus_vectors;
		this.corpus_vector_merged = corpus_vector_merged;
		this.prf_docs = prf_docs;

		// dynamically generate term vectors from PRF documents
		for (String doc : prf_docs) {
			Map<String, Integer> doc_vector = getTermVector(doc);
			prf_doc_vectors.add(doc_vector);
		}
	}

	public double getChiSquareWeight(String term) {
		double pr = getRelevantProbability(term);
		double cr = getCollectionProbability(term);
		return Math.pow((pr - cr), 2) / cr;
	}

	public double getRSVWeight(String term) {
		double weight = 0.0;

		for (Map<String, Integer> prf_doc_vector : prf_doc_vectors) {
			if (!prf_doc_vector.containsKey(term)) {
				continue;
			} else {
				int tf = prf_doc_vector.get(term);
				int df = 0;
				for (Map<String, Integer> doc_vector : corpus_vectors) {
					if (doc_vector.containsKey(term)) {
						df++;
					}
				}
				if (tf != 0) {
					weight += ((1 + Math.log(tf)) * Math.log10(corpus_vectors
							.size() / (df + 1)))
							* (getRelevantProbability(term) - getCollectionProbability(term));
				}
			}
		}

		return weight;
	}

	private double getRelevantProbability(String term) {
		return getTermProbability(term, prf_doc_vectors);
	}

	private double getCollectionProbability(String term) {
		return getTermProbability(term, corpus_vectors);
	}

	private double getTermProbability(String term,
			List<Map<String, Integer>> vectors) {
		int doc_length = 0;
		int tf = 0;
		Set<String> vocabulary = new HashSet<String>();
		for (Map<String, Integer> vector : vectors) {
			// sum up document length
			for (String t : vector.keySet()) {
				doc_length += vector.get(t);

				// update vocabulary set (for normalization)
				vocabulary.add(t);
			}

			// calculate tf
			if (vector.containsKey(term)) {
				tf += vector.get(term);
			}
		}
		
		System.out.println("Document lenght: " + doc_length);
		System.out.println(vocabulary.size());

		int B = vocabulary.size();
		return (tf + 1) / (doc_length + B);
	}

	public double getRocchioWeight(String term) {
		double weight = 0.0;

		for (Map<String, Integer> prf_doc_vector : prf_doc_vectors) {
			if (!prf_doc_vector.containsKey(term)) {
				continue;
			} else {
				int tf = prf_doc_vector.get(term);
				int df = 0;
				for (Map<String, Integer> doc_vector : corpus_vectors) {
					if (doc_vector.containsKey(term)) {
						df++;
					}
				}
				if (tf != 0) {
					weight += ((1 + Math.log(tf)) * Math.log10(corpus_vectors
							.size() / (df + 1)));
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
		List<String> terms = Tokenizer.tokenizeWithStemming(document);

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
