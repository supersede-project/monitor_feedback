package ch.uzh.ifi.feedback.library.rest;

import java.util.List;

public interface ISerializationService<T> {
	
	String Serialize(T object);
	T Deserialize(String data);
}
