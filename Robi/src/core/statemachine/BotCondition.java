package core.statemachine;

/**
 * Functional interface representing a transition condition for a Robibot.
 * Evaluated at each tick to determine whether a transition should fire.
 */
@FunctionalInterface
public interface BotCondition {

    /**
     * Tests whether the condition is satisfied for the given bot.
     *
     * @param bot the Robibot to evaluate
     * @return {@code true} if the condition is met, {@code false} otherwise
     */
    boolean test(Robibot bot);
}
