package com.joogle.model;

public class YahooQuestion {
	public String Id;
	public String Subject;
	public String Content;
	public String Link;
	public String Date;
	
	public int NumAnswers;
	public int NumComments;
	public String ChosenAnswer;
	public String ChosenAnswererId;
	
	public YahooQuestion() {
		
	}
	
	public String toString() {
		return "Id: " + Id + ", Subject: " + Subject;
	}
}
