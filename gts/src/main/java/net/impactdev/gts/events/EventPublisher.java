package net.impactdev.gts.events;

import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.events.ImpactorEvent;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;
import net.kyori.event.Cancellable;
import net.kyori.event.PostResult;

import java.util.concurrent.atomic.AtomicInteger;

public final class EventPublisher {

    public static boolean post(ImpactorEvent event) {
        try {
            PostResult result = Impactor.instance().events().post(event);
            result.raise();

            return !result.wasSuccessful() && !cancelled(event);
        } catch (PostResult.CompositeException e) {
            PrettyPrinter printer = new PrettyPrinter(80);
            printer.title("Encountered an exception while handling an event!");
            printer.add("It seems an exception was encountered while processing an event within")
                    .add("the GTS pipeline. This does not necessarily mean GTS is at fault, and")
                    .add("depending on the exception, might mean another plugin is causing the")
                    .add("problem. Please check the following traces for the possible cause of")
                    .add("failure.")
                    .hr('-')
                    .add("Root Exception").center()
                    .add(e)
                    .newline()
                    .add("Tracked Exceptions:")
                    .consume(p -> {
                        AtomicInteger index = new AtomicInteger(1);
                        e.result().exceptions().values().forEach(exception -> {
                            p.add("Exception %d: " + index.getAndIncrement());
                            p.add(exception);
                            p.newline();
                        });
                    });

            printer.log(GTSPlugin.instance().logger(), PrettyPrinter.Level.ERROR);

            return false;
        }
    }

    public static boolean cancelled(ImpactorEvent event) {
        if(event instanceof Cancellable) {
            return ((Cancellable) event).cancelled();
        }

        return false;
    }

}
