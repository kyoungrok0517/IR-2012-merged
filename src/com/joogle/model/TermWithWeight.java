package com.joogle.model;

public class TermWithWeight implements Comparable<TermWithWeight> {
	public String term;
	public Double freq;

	public TermWithWeight() {

	}

	public TermWithWeight(String term, Double freq) {
		this.term = term;
		this.freq = freq;
	}

	@Override
	public int compareTo(TermWithWeight t) {
		return (int)(Math.signum(t.freq - this.freq));
	}

	public String toString() {
		return term + ":" + freq;
	}
}
