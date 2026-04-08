package core.statemachine;

/**
 * Represents a transition between two states in a Robibot state machine.
 *
 * A transition holds a target state name and a {@link BotCondition} that is
 * evaluated on every tick. When the condition is satisfied the state machine
 * fires the transition.
 */
public class Transition {

    private final String targetStateName;
    private final BotCondition condition;

    /**
     * Creates a new transition to the given target state with the specified condition.
     *
     * @param targetStateName the name of the target state
     * @param condition       the condition that triggers this transition
     */
    public Transition(String targetStateName, BotCondition condition) {
        this.targetStateName = targetStateName;
        this.condition = condition;
    }

    /**
     * Returns the name of the target state.
     *
     * @return the target state name
     */
    public String getTargetStateName() {
        return targetStateName;
    }

    /**
     * Evaluates the transition condition for the given bot.
     *
     * @param bot the Robibot to evaluate against
     * @return {@code true} if the condition is satisfied
     */
    public boolean evaluate(Robibot bot) {
        return condition.test(bot);
    }

    @Override
    public String toString() {
        return "Transition -> " + targetStateName;
    }
}
