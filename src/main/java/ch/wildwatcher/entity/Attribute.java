package ch.wildwatcher.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = { "key" })
public class Attribute {
	private String key;
	private String value;
}
