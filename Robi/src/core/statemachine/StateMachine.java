package core.statemachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Finite state machine driving the behaviour of a Robibot.
 *
 * <p>On each tick the machine:
 * <ol>
 *   <li>Evaluates the transitions of the current state.</li>
 *   <li>If a transition fires: executes exit actions, changes state, executes entry actions.</li>
 *   <li>Executes the do-actions of the (possibly new) current state.</li>
 * </ol>
 */
public class StateMachine {

    private final Map<String, State> states;
    private State currentState;
    private String initialStateName;

    /**
     * Creates a new empty state machine.
     */
    public StateMachine() {
        this.states = new HashMap<>();
    }

    /**
     * Adds a state to this machine.
     *
     * @param state the state to add
     */
    public void addState(State state) {
        states.put(state.getName(), state);
    }

    /**
     * Returns the state with the given name, or {@code null} if not found.
     *
     * @param name the state name
     * @return the matching state or {@code null}
     */
    public State getState(String name) {
        return states.get(name);
    }

    /**
     * Sets the initial state of the machine.
     *
     * @param stateName the name of the initial state
     */
    public void setInitialState(String stateName) {
        this.initialStateName = stateName;
        this.currentState = states.get(stateName);
    }

    /**
     * Returns the current state of the machine.
     *
     * @return the current state
     */
    public State getCurrentState() {
        return currentState;
    }

    /**
     * Starts the machine by entering the initial state.
     *
     * @return the entry S-Expressions of the initial state
     */
    public List<String> start() {
        currentState = states.get(initialStateName);
        if (currentState != null) {
            return currentState.getEntryActions();
        }
        return List.of();
    }

    /**
     * Performs a single tick of the state machine.
     * Returns the list of S-Expressions to execute (exit + entry + do).
     *
     * @param bot the Robibot context used for condition evaluation
     * @return the S-Expressions produced during this tick
     */
    public List<String> tick(Robibot bot) {
        if (currentState == null) {
            return List.of();
        }

        List<String> commands = new ArrayList<>();

        // 1. Evaluate transitions
        for (Transition t : currentState.getTransitions()) {
            if (t.evaluate(bot)) {
                // Exit current state
                commands.addAll(currentState.getExitActions());

                // Change state
                State nextState = states.get(t.getTargetStateName());
                if (nextState != null) {
                    currentState = nextState;
                    // Enter new state
                    commands.addAll(currentState.getEntryActions());
                }
                break; // Only one transition per tick
            }
        }

        // 2. Execute do-actions of the current state
        commands.addAll(currentState.getDoActions());

        return commands;
    }

    /**
     * Returns all states registered in this machine.
     *
     * @return an unmodifiable view is not enforced; the internal map of states
     */
    public Map<String, State> getStates() {
        return states;
    }
}
