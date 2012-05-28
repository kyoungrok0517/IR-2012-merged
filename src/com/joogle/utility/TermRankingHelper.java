package com.joogle.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TermRankingHelper {
	private List<Map<String, Integer>> prf_doc_vectors = new ArrayList<Map<String, Integer>>();;
	private List<Map<String, Integer>> corpus_vectors;
	private Map<String, Integer> prf_doc_vectors_merged = new HashMap<String, Integer>();;
	private Map<String, Integer> corpus_vector_merged;

	public TermRankingHelper() {

	}

	public TermRankingHelper(List<String> prf_docs,
			List<Map<String, Integer>> corpus_vectors,
			Map<String, Integer> corpus_vector_merged) {

		this.corpus_vectors = corpus_vectors;
		this.corpus_vector_merged = corpus_vector_merged;

		// dynamically generate term vectors from PRF documents
		for (String doc : prf_docs) {
			Map<String, Integer> doc_vector = getTermVector(doc);
			prf_doc_vectors.add(doc_vector);
		}

		for (Map<String, Integer> vector : prf_doc_vectors) {
			for (String term : vector.keySet()) {
				if (prf_doc_vectors_merged.containsKey(term)) {
					int new_count = prf_doc_vectors_merged.get(term) + vector.get(term);
					prf_doc_vectors_merged.put(term, new_count);
				} else {
					prf_doc_vectors_merged.put(term, vector.get(term));
				}
			}
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
		int doc_length = 0;
		int tf = 0;
		Set<String> vocabulary = new HashSet<String>();
		
		for (String t : prf_doc_vectors_merged.keySet()) {
			int count = prf_doc_vectors_merged.get(t);
			doc_length += count;
			
			if (prf_doc_vectors_merged.containsKey(term)) {
				tf = prf_doc_vectors_merged.get(term);
			}
			
			vocabulary.add(t);
		}

		int B = vocabulary.size();
		return (tf + 1) / (doc_length + B);
	}

	private double getCollectionProbability(String term) {
		int doc_length = 0;
		int tf = 0;
		Set<String> vocabulary = new HashSet<String>();
		
		for (String t : corpus_vector_merged.keySet()) {
			int count = corpus_vector_merged.get(t);
			doc_length += count;
			
			if (corpus_vector_merged.containsKey(term)) {
				tf = corpus_vector_merged.get(term);
			}
			
			vocabulary.add(t);
		}

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
