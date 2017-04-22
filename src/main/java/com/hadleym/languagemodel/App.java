package com.hadleym.languagemodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.simple.*;

/**
 * Assignment 4, problem 4
 * Language Model Implementation
 * 4/20/2017
 * 
 * USAGE: % java App input.txt
 * The program will parse the documents contained in input.txt.
 * After the parsing is complete, user is prompted for queries.
 * Ctrl-C ends the program.
 *
 * @author Mark Hadley
 *
 */
public class App {
	public static final double LAMBDA = .5;
	public static final boolean DEBUG = false;
	public static final String FILENAME = "input.txt";

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int collectionTotal = 0;
		List<DocumentInfo> documentInfos = new ArrayList<>();
		List<String> documents = getDocumentsFromFile(FILENAME);
		for (String doc : documents) {
			DocumentInfo di = lemmatize(doc);
			documentInfos.add(di);
			collectionTotal += di.size;
		}
		System.out.println("Document parsing completed.");
		if (DEBUG)
			System.out.println("Collection Total: " + collectionTotal);
		while (true) {
			System.out.print("Enter phrase to query (or <ctl-c> to quit):");
			String input = br.readLine();
			List<String> queryList = parseInput(input);
			List<DocumentRank> documentRanks = new ArrayList<DocumentRank>();
			for (DocumentInfo di : documentInfos) {
				double value = 1;
				for (int i = 0; i < queryList.size(); i++) {
					int collectionFrequency = getCollectionFrequency(documentInfos, queryList.get(i));
					value *= getDocumentValue(di, collectionFrequency, collectionTotal, queryList.get(i), LAMBDA);
				}
				documentRanks.add(new DocumentRank(di.name, value));
			}

			Collections.sort(documentRanks);
			if (DEBUG)
				System.out.println("Sorted ranks");
			for (DocumentRank dr : documentRanks) {
				System.out.println(dr.toString());
			}

		}

	}

	public static List<String> parseInput(String s) {
		String[] splitStrings = s.split("\\s+");
		List<String> lemmas = new ArrayList<>();
		Document doc = new Document(s);
		for (Sentence sent : doc.sentences()) {
			lemmas.addAll(sent.lemmas());
		}
		return lemmas;

	}

	public static double getDocumentValue(DocumentInfo di, int collectionFrequency, int collectionTotal, String query,
			double lambda) {
		double docValue = (double) di.getWordCount(query) / (double) di.size * lambda;
		double collectionValue = ((double) collectionFrequency) / (double) collectionTotal * (1 - lambda);
		return docValue + collectionValue;
	}

	public static int getDocumentTotal(HashMap<String, Integer> map) {
		int total = 0;
		for (String key : map.keySet()) {
			total += map.get(key);
		}
		return total;
	}

	public static List<String> getDocumentsFromFile(String filename) throws IOException {
		List<String> documents = new ArrayList<String>();
		Scanner sc = new Scanner(new File(filename));
		while (sc.hasNext()) {
			documents.add(sc.nextLine());
		}
		return documents;
	}

	public static int getCollectionFrequency(List<DocumentInfo> documentInfos, String word) {
		int count = 0;
		for (DocumentInfo di : documentInfos) {
			count += di.getWordCount(word);
		}
		return count;
	}

	/*
	 * public static void tokenize(String filename) throws IOException {
	 * DocumentPreprocessor dp = new DocumentPreprocessor(filename); for
	 * (List<HasWord> sentence : dp) { System.out.println(sentence); }
	 * 
	 * PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new
	 * FileReader(filename), new CoreLabelTokenFactory(), ""); while
	 * (ptbt.hasNext()) { CoreLabel label = ptbt.next();
	 * System.out.println(label); }
	 * 
	 * }
	 */
	public static DocumentInfo lemmatize(String line) {
		String name;
		HashMap<String, Integer> map = new HashMap<>();
		ParseDocument pd = new ParseDocument(line);
		Document doc = new Document(pd.line);
		name = pd.name;
		for (Sentence sent : doc.sentences()) {
			if (DEBUG)
				System.out.println(sent.parse());
			List<String> words = sent.words();
			for (int i = 0; i < words.size(); i++) {
				if (DEBUG)
					System.out.println("Word[" + i + "]: " + words.get(i));
			}

			List<String> lemmas = sent.lemmas();
			for (int i = 0; i < lemmas.size(); i++) {
				String w = lemmas.get(i);
				if (Character.isLetter(w.charAt(0))) {
					if (DEBUG) {
						System.out.println("Lemma[" + i + "]: " + lemmas.get(i));
					}
					if (map.containsKey(w)) {
						map.put(w, map.get(w) + 1);
					} else {
						map.put(w, 1);
					}
				}
			}
		}
		return new DocumentInfo(map, name);
	}
}

class DocumentInfo {
	public String name;
	public HashMap<String, Integer> map;
	public int size;

	public DocumentInfo(HashMap<String, Integer> m, String n) {
		name = n;
		map = m;
		size = 0;
		for (String key : map.keySet()) {
			size += map.get(key);
		}
	}

	public int getWordCount(String w) {
		if (map.containsKey(w)) {
			return map.get(w);
		} else {
			return 0;
		}
	}
}

class DocumentRank implements Comparable<DocumentRank> {
	public String name;
	public double rank;

	public DocumentRank(String n, double r) {
		name = n;
		rank = r;
	}

	@Override
	public String toString() {
		return String.format("%s: %4f", name, rank);
	}

	@Override
	public int compareTo(DocumentRank o) {
		if (o.rank > rank)
			return 1;
		if (o.rank == rank)
			return 0;
		return -1;
	}
}

class ParseDocument {
	public String name;
	public String line;

	public ParseDocument(String line) {
		name = line.substring(0, line.indexOf(":"));
		this.line = line.substring(line.indexOf(":") + 1, line.length());
	}
}
