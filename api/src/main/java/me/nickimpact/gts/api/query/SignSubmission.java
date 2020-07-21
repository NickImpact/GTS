package me.nickimpact.gts.api.query;

import java.util.List;

@FunctionalInterface
public interface SignSubmission {

    boolean process(List<String> input);

}
