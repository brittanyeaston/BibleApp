package edu.southern.resources;

public class SearchVerse {
	String reference;
	String text;

	public String getReference() {
		return reference;
	}

	public String getText() {
		return text;
	}
	
	SearchVerse(String reference, String text){
		this.reference = reference;
		this.text = text;
	}
}