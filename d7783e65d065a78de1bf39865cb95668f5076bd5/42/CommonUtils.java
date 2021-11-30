package com.asynch.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommonUtils {

	public static List<String> getLinks(final String linkFile) throws IOException {
		try (Stream<String> stream = Files.lines(Paths.get(linkFile))) {
			return stream.collect(Collectors.toList());
		} catch (IOException e) {
			throw e;
		}
	}

	public static boolean isNotBlank(String value) {
		return value != null && !value.equals("");
	}

}
