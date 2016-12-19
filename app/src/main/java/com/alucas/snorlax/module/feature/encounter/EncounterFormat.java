package com.alucas.snorlax.module.feature.encounter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.graphics.Typeface;
import android.support.v4.util.Pair;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import POGOProtos.Networking.Requests.RequestTypeOuterClass;

public class EncounterFormat {
	private static final Character SYMBOL_DELIMITER_START = '{';
	private static final Character SYMBOL_DELIMITER_END = '}';

	static Spannable bold(final String text) {
		Spannable spannable = new SpannableString(text);
		spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, spannable.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		return spannable;
	}

	static Spannable format(final String text, final Map<String, Pair<String, Integer>> symbols) {
		final List<ColorPos> colorPositions = new ArrayList<>();
		final String formattedText = replaceSymbol(text, symbols, colorPositions);

		final Spannable spannable = new SpannableString(formattedText);
		for (ColorPos colorPos : colorPositions) {
			spannable.setSpan(new ForegroundColorSpan(colorPos.color), colorPos.start, colorPos.end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		}

		return spannable;
	}

	static String replaceSymbol(final String text, final Map<String, Pair<String, Integer>> symbols, final List<ColorPos> colorPositions) {
		String formattedText = text;
		int indexDelimiterEnd = -1;
		while (true) {
			final int indexDelimiterStart = formattedText.indexOf(SYMBOL_DELIMITER_START, indexDelimiterEnd);
			if (indexDelimiterStart == -1) {
				break;
			}

			indexDelimiterEnd = formattedText.indexOf(SYMBOL_DELIMITER_END, indexDelimiterStart + 1);
			if (indexDelimiterEnd == -1) {
				break;
			}

			final String symbolKey = formattedText.substring(indexDelimiterStart + 1, indexDelimiterEnd);
			final Pair<String, Integer> symbolData = symbols.get(symbolKey);
			if (symbolData == null) {
				continue;
			}

			final String symbolValue = symbolData.first;
			final Integer symbolColor = symbolData.second;

			formattedText = formattedText.replace(SYMBOL_DELIMITER_START + symbolKey + SYMBOL_DELIMITER_END, symbolValue);
			indexDelimiterEnd -= symbolKey.length() - symbolValue.length() + 1;

			colorPositions.add(new ColorPos(symbolColor, indexDelimiterStart, indexDelimiterEnd));
		}

		return formattedText;
	}

	static String formatEncounterType(RequestTypeOuterClass.RequestType encounterType)	{
		switch (encounterType) {
			case ENCOUNTER:
				return "[Wild]";
			case DISK_ENCOUNTER:
				return "[Disk]";
			case INCENSE_ENCOUNTER:
				return "[Incense]";
			default:
				return "[]";
		}
	}

	static class ColorPos {
		final int color;
		final int start;
		final int end;

		ColorPos(final int color, final int start, final int end) {
			this.color = color;
			this.start = start;
			this.end = end;
		}
	}
}
