package com.hadleym.languagemodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.simple.*;

public class App {
	Sentence sent = new Sentence("Lucy is in the sky with diamonds");

	public static void main(String[] args) throws IOException {
		lemmatize(args[0]);
	}

	public static void tokenize(String filename) throws IOException {
		DocumentPreprocessor dp = new DocumentPreprocessor(filename);
		for (List<HasWord> sentence : dp) {
			System.out.println(sentence);
		}

		PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new FileReader(filename), new CoreLabelTokenFactory(), "");
		while (ptbt.hasNext()) {
			CoreLabel label = ptbt.next();
			System.out.println(label);
		}

	}

	public static void lemmatize(String filename) throws IOException {
		Scanner sc = new Scanner(new File(filename));
		while (sc.hasNext()) {
			String line = sc.nextLine();
			ParseDocument pd = new ParseDocument(line);
			Document doc = new Document(pd.line);
			for (Sentence sent : doc.sentences()) {
				System.out.println(sent.parse());
				List<String> words = sent.words();
				for (int i = 0; i < words.size(); i++){
					System.out.println("Word[" + i + "]: " + words.get(i));
				}

				List<String> lemmas = sent.lemmas();
				for (int i = 0; i < lemmas.size(); i++) {
					String w = lemmas.get(i);
					if (Character.isLetter(w.charAt(0)))
						System.out.println("Lemma[" + i + "]: " + lemmas.get(i));
				}
			}
		}

	}
	

}

class ParseDocument{
	public String name; 
	public String line;
	public ParseDocument(String line){
		name = line.substring(0, line.indexOf(":"));
		this.line = line.substring(line.indexOf(":")+1, line.length());
	}
}
