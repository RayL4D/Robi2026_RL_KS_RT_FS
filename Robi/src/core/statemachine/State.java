package core.statemachine;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a state in a Robibot state machine.
 *
 * Each state holds:
 * <ul>
 *   <li>entry actions: S-Expressions executed when entering the state</li>
 *   <li>do actions: S-Expressions executed on every tick</li>
 *   <li>exit actions: S-Expressions executed when leaving the state</li>
 *   <li>transitions: outgoing transitions evaluated on every tick</li>
 * </ul>
 */
public class State {

    private final String name;
    private final List<String> entryActions;
    private final List<String> doActions;
    private final List<String> exitActions;
    private final List<Transition> transitions;

    /**
     * Creates a new state with the given name.
     *
     * @param name the unique name of this state
     */
    public State(String name) {
        this.name = name;
        this.entryActions = new ArrayList<>();
        this.doActions = new ArrayList<>();
        this.exitActions = new ArrayList<>();
        this.transitions = new ArrayList<>();
    }

    /**
     * Returns the name of this state.
     *
     * @return the state name
     */
    public String getName() {
        return name;
    }

    /**
     * Adds an entry action S-Expression to this state.
     *
     * @param sExpression the S-Expression to execute on entry
     */
    public void addEntryAction(String sExpression) {
        entryActions.add(sExpression);
    }

    /**
     * Adds a do-action S-Expression to this state.
     *
     * @param sExpression the S-Expression to execute on each tick
     */
    public void addDoAction(String sExpression) {
        doActions.add(sExpression);
    }

    /**
     * Adds an exit action S-Expression to this state.
     *
     * @param sExpression the S-Expression to execute on exit
     */
    public void addExitAction(String sExpression) {
        exitActions.add(sExpression);
    }

    /**
     * Adds an outgoing transition to this state.
     *
     * @param transition the transition to add
     */
    public void addTransition(Transition transition) {
        transitions.add(transition);
    }

    /**
     * Returns the list of entry action S-Expressions.
     *
     * @return the entry actions
     */
    public List<String> getEntryActions() {
        return entryActions;
    }

    /**
     * Returns the list of do-action S-Expressions.
     *
     * @return the do actions
     */
    public List<String> getDoActions() {
        return doActions;
    }

    /**
     * Returns the list of exit action S-Expressions.
     *
     * @return the exit actions
     */
    public List<String> getExitActions() {
        return exitActions;
    }

    /**
     * Returns the list of outgoing transitions.
     *
     * @return the transitions
     */
    public List<Transition> getTransitions() {
        return transitions;
    }

    @Override
    public String toString() {
        return "State[" + name + "]";
    }
}
