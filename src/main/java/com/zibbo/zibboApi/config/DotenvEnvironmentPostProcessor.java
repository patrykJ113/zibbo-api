package com.zibbo.zibboApi.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Loads key/value pairs from a {@code .env} file in the working directory so the
 * {@code ${DB_HOST}} style placeholders in application.yaml resolve during local
 * development. Registered via META-INF/spring.factories.
 *
 * <p>The property source is added last, so real environment variables and system
 * properties still win over the file.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private static final String PROPERTY_SOURCE_NAME = "dotenv";
	private static final String FILE_NAME = ".env";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Path file = Path.of(FILE_NAME);
		if (!Files.isRegularFile(file)) {
			return;
		}

		Map<String, Object> values = parse(file);
		if (!values.isEmpty()) {
			environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, values));
		}
	}

	private Map<String, Object> parse(Path file) {
		Map<String, Object> values = new LinkedHashMap<>();

		List<String> lines;
		try {
			lines = Files.readAllLines(file, StandardCharsets.UTF_8);
		}
		catch (IOException ex) {
			return values;
		}

		for (String line : lines) {
			String trimmed = line.trim();
			if (trimmed.isEmpty() || trimmed.startsWith("#")) {
				continue;
			}

			int separator = trimmed.indexOf('=');
			if (separator <= 0) {
				continue;
			}

			String key = trimmed.substring(0, separator).trim();
			String value = unquote(trimmed.substring(separator + 1).trim());
			if (!key.isEmpty()) {
				values.put(key, value);
			}
		}

		return values;
	}

	private String unquote(String value) {
		if (value.length() >= 2
				&& ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")))) {
			return value.substring(1, value.length() - 1);
		}
		return value;
	}

}
