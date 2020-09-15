package me.nickimpact.gts.common.messaging.interpreters;

import me.nickimpact.gts.common.plugin.GTSPlugin;

public interface Interpreter {

    void register(GTSPlugin plugin);

    void getDecoders(GTSPlugin plugin);

    void getInterpreters(GTSPlugin plugin);

}
