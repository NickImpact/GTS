package me.nickimpact.gts.common.utils.gson;

import com.google.gson.JsonElement;

public interface JElement<T extends JsonElement> {

	T toJson();

}
