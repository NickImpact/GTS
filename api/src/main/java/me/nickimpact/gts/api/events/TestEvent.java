package me.nickimpact.gts.api.events;

import com.nickimpact.impactor.api.event.ImpactorEvent;
import com.nickimpact.impactor.api.event.annotations.Param;

public interface TestEvent<T> extends ImpactorEvent.Generic<T> {

    @Param(0)
    T getData();

}
