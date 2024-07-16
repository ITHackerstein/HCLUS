package com.davidecarella.hclus.client;

import java.io.*;
import java.util.*;

/**
 * Classe di utilità fornita dal docente per la gestione dell'input da tastiera.
 */
public class Keyboard {
	/**
	 * NOTA: Viene aggiunto solo per evitare il warning durante la generazione del JavaDoc.
	 */
	private Keyboard() {}

	/**
	 * Flag che indica se bisogna stampare gli errori in output.
	 */
	private static boolean printErrors = true;
	/**
	 * Numero degli errori riscontrati fino ad ora
	 */
	private static int errorCount = 0;

	/**
	 * Restituisce il numero di errori riscontrati.
	 *
	 * @return il numero di errori riscontrati
	 */
	public static int getErrorCount() {
		return errorCount;
	}

	/**
	 * Reimposta il numero degli errori incontrati.
	 */
	public static void resetErrorCount() {
		errorCount = 0;
	}

	/**
	 * Restituisce un flag che indica se vengono stampati gli errori in output.
	 *
	 * @return un flag che se è {@code true} allora indica che gli errori vengono stampati in output, {@code falso}
	 *         altrimenti
	 */
	public static boolean getPrintErrors() {
		return printErrors;
	}

	/**
	 * Se {@code flag} è vero allora gli errori verrano stampati in output, altrimenti no.
	 *
	 * @param flag un flag che se è {@code true} allora indica che gli errori vengono stampati in output, {@code falso}
	 *             altrimenti
	 */
	public static void setPrintErrors(boolean flag) {
		printErrors = flag;
	}

	/**
	 * Stampa un errore (se {@link Keyboard#printErrors printErrors} è impostato) e incrementa il numero di errori
	 * riscontrati
	 *
	 * @param str il messaggio d'errore
	 */
	private static void error(String str) {
		errorCount++;
		if (printErrors)
			System.out.println(str);
	}

	/**
	 * Il token che si sta leggendo ora
	 */
	private static String currentToken = null;

	/**
	 * Tokenizer per la lettura di token dalla tastiera
	 */
	private static StringTokenizer reader;

	/**
	 * Lo stream di input da tastiera
	 */
	private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	/**
	 * Restituisce il prossimo token.
	 *
	 * @return il prossimo token
	 */
	private static String getNextToken() {
		return getNextToken(true);
	}

	/**
	 * Restituisce il prossimo token (se {@code skip} è {@code true} allora salta quello attuale).
	 *
	 * @param skip indica se saltare il token
	 * @return il prossimo token
	 */
	private static String getNextToken(boolean skip) {
		String token;

		if (currentToken == null)
			token = getNextInputToken(skip);
		else {
			token = currentToken;
			currentToken = null;
		}

		return token;
	}

	/**
	 * Restituisce il prossimo token (se {@code skip} è {@code true} allora salta quello attuale).
	 *
	 * @param skip indica se saltare il token
	 * @return il prossimo token
	 */
	private static String getNextInputToken(boolean skip) {
		final String delimiters = " \t\n\r\f";
		String token = null;

		try {
			if (reader == null)
				reader = new StringTokenizer(in.readLine(), delimiters, true);

			while (token == null || ((delimiters.indexOf(token) >= 0) && skip)) {
				while (!reader.hasMoreTokens())
					reader = new StringTokenizer(in.readLine(), delimiters,
							true);

				token = reader.nextToken();
			}
		} catch (Exception exception) {
			token = null;
		}

		return token;
	}

	/**
	 * Restituisce {@code true} se si è raggiunta la fine della linea, {@code false} altrimenti.
	 *
	 * @return {@code true} se si è raggiunta la fine della linea, {@code false} altrimenti.
	 */
	public static boolean endOfLine() {
		return !reader.hasMoreTokens();
	}

	/**
	 * Legge una stringa da tastiera.
	 *
	 * @return la stringa letta da tastiera
	 */
	public static String readString() {
		String str;

		try {
			str = getNextToken(false);
			while (!endOfLine()) {
				str = str + getNextToken(false);
			}
		} catch (Exception exception) {
			error("Error reading String data, null value returned.");
			str = null;
		}
		return str;
	}

	/**
	 * Legge una parola da tastiera (si ferma quando incontra uno spazio).
	 *
	 * @return la parola letta da tastiera
	 */
	public static String readWord() {
		String token;
		try {
			token = getNextToken();
		} catch (Exception exception) {
			error("Error reading String data, null value returned.");
			token = null;
		}
		return token;
	}

	/**
	 * Legge un valore booleano da tastiera.
	 *
	 * @return il valore booleano letto da tastiera
	 */
	public static boolean readBoolean() {
		String token = getNextToken();
		boolean bool;
		try {
			if (token.toLowerCase().equals("true"))
				bool = true;
			else if (token.toLowerCase().equals("false"))
				bool = false;
			else {
				error("Error reading boolean data, false value returned.");
				bool = false;
			}
		} catch (Exception exception) {
			error("Error reading boolean data, false value returned.");
			bool = false;
		}
		return bool;
	}

	/**
	 * Legge un carattere da tastiera.
	 *
	 * @return il carattere letto da tastiera
	 */
	public static char readChar() {
		String token = getNextToken(false);
		char value;
		try {
			if (token.length() > 1) {
				currentToken = token.substring(1, token.length());
			} else
				currentToken = null;
			value = token.charAt(0);
		} catch (Exception exception) {
			error("Error reading char data, MIN_VALUE value returned.");
			value = Character.MIN_VALUE;
		}

		return value;
	}

	/**
	 * Legge un intero ({@code int}) da tastiera.
	 *
	 * @return l'intero letto da tastiera
	 */
	public static int readInt() {
		String token = getNextToken();
		int value;
		try {
			value = Integer.parseInt(token);
		} catch (Exception exception) {
			error("Error reading int data, MIN_VALUE value returned.");
			value = Integer.MIN_VALUE;
		}
		return value;
	}

	/**
	 * Legge un intero ({@code long}) da tastiera.
	 *
	 * @return l'intero letto da tastiera
	 */
	public static long readLong() {
		String token = getNextToken();
		long value;
		try {
			value = Long.parseLong(token);
		} catch (Exception exception) {
			error("Error reading long data, MIN_VALUE value returned.");
			value = Long.MIN_VALUE;
		}
		return value;
	}

	/**
	 * Legge un reale ({@code float}) da tastiera.
	 *
	 * @return il reale letto da tastiera
	 */
	public static float readFloat() {
		String token = getNextToken();
		float value;
		try {
			value = Float.parseFloat(token);
		} catch (Exception exception) {
			error("Error reading float data, NaN value returned.");
			value = Float.NaN;
		}
		return value;
	}

	/**
	 * Legge un reale ({@code double}) da tastiera.
	 *
	 * @return il reale letto da tastiera
	 */
	public static double readDouble() {
		String token = getNextToken();
		double value;
		try {
			value = Double.parseDouble(token);
		} catch (Exception exception) {
			error("Error reading double data, NaN value returned.");
			value = Double.NaN;
		}
		return value;
	}
}
