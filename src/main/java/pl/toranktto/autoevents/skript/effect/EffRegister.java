package pl.toranktto.autoevents.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import pl.toranktto.autoevents.AutoEventManager;
import pl.toranktto.autoevents.AutoEventsPlugin;
import pl.toranktto.autoevents.skript.SkriptAutoEvent;

import java.util.concurrent.TimeUnit;

public final class EffRegister extends Effect {

    static {
        Skript.registerEffect(EffRegister.class,
                "register autoevent %string% with localized name %string% and timeout %-timespan% and command %string%"
        );
    }

    private Expression<String> eventName;
    private Expression<String> eventLocalizedName;
    private Expression<Timespan> timeout;
    private Expression<String> command;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
        eventName = (Expression<String>) vars[0];
        eventLocalizedName = (Expression<String>) vars[1];
        timeout = (Expression<Timespan>) vars[2];
        command = (Expression<String>) vars[3];
        return true;
    }

    @Override
    protected void execute(Event event) {
        AutoEventManager eventManager = AutoEventsPlugin.getInstance().getAutoEventManager();
        String eventName = this.eventName.getSingle(event);
        String eventLocalizedName = this.eventLocalizedName.getSingle(event);
        String command = this.command.getSingle(event);
        Long timeout = TimeUnit.MILLISECONDS.toSeconds(this.timeout.getSingle(event).getMilliSeconds());

        eventManager.register(eventName, SkriptAutoEvent.class, eventName, eventLocalizedName, command, timeout);
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "register autoevent \"" + eventName.getSingle(event) + "\" with localized name \"" + eventLocalizedName.getSingle(event) + "\" and timeout " + timeout.getSingle(event).toString() + " and command \"" + command.getSingle(event) + "\"";
    }
}
