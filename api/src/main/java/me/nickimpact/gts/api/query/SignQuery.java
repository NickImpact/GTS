package me.nickimpact.gts.api.query;

import com.flowpowered.math.vector.Vector3d;
import com.nickimpact.impactor.api.Impactor;
import com.nickimpact.impactor.api.utilities.Builder;

import java.util.List;

public interface SignQuery<T, P> {

    String TEXT_FORMAT = "{\"text\":\"%s\"}";
    int action = 9;

    List<T> getText();

    Vector3d getSignPosition();

    boolean shouldReopenOnFailure();

    SignSubmission getSubmissionHandler();

    void sendTo(P player);

    @SuppressWarnings("unchecked")
    static <T, P> SignQueryBuilder<T, P> builder() {
        return Impactor.getInstance().getRegistry().createBuilder(SignQueryBuilder.class);
    }

    interface SignQueryBuilder<T, P> extends Builder<SignQuery<T, P>, SignQueryBuilder<T, P>> {

        SignQueryBuilder<T, P> text(List<T> text);

        SignQueryBuilder<T, P> position(Vector3d position);

        SignQueryBuilder<T, P> reopenOnFailure(boolean state);

        SignQueryBuilder<T, P> response(SignSubmission response);

    }

}
