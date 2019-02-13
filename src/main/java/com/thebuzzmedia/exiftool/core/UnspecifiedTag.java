package com.thebuzzmedia.exiftool.core;

import com.thebuzzmedia.exiftool.Tag;

import java.util.Objects;
import java.util.regex.Pattern;

import static com.thebuzzmedia.exiftool.Constants.SEPARATOR;

/**
 * Class that can represent any tag, not just the standard tags.
 * <p>
 * Since the type of value is unknown, typed parsing is not possible,
 * and the tag does not know whether multiple values are expected or not.
 * Consequently, when parsing, it returns a String[] regardless of whether
 * the tag will ever have multiple values. Further parsing is then up to the caller.
 *
 * @author David Edwards (david@more.fool.me.uk)
 */

public class UnspecifiedTag implements Tag {

	private String name;

	public UnspecifiedTag(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDisplayName() {
		return name;
	}

	@Override
	public <T> T parse(String value) {
		// Clearly this won't work if caller is expecting anything other than
		// a string array. This is due to the way the {@link Tag} interface is designed.
		return (T) value.split(Pattern.quote(SEPARATOR));
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UnspecifiedTag that = (UnspecifiedTag) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
