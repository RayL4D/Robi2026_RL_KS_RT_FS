package core.statemachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;

import core.RobiInterpreter;

/**
 * Centralised manager for all active Robibots.
 *
 * <p>Running on the server side, it acts as the central arbiter:
 * <ul>
 *   <li>Runs a tick loop at a regular interval.</li>
 *   <li>Evaluates every bot's state machine on each tick.</li>
 *   <li>Executes the resulting S-Expressions on the interpreter.</li>
 *   <li>Notifies listeners (e.g. network clients) of the commands to replay.</li>
 * </ul>
 *
 * <p>The manager runs in a dedicated daemon thread.
 */
public class RobibotManager implements Runnable {

    private final Map<String, Robibot> bots;
    private final RobiInterpreter interpreter;
    private final List<BotTickListener> listeners;
    private volatile boolean running;
    private int tickIntervalMs;

    /**
     * Creates a new manager backed by the given interpreter.
     *
     * @param interpreter the Robi interpreter used to execute S-Expressions
     */
    public RobibotManager(RobiInterpreter interpreter) {
        this.bots = new ConcurrentHashMap<>();
        this.interpreter = interpreter;
        this.listeners = new ArrayList<>();
        this.tickIntervalMs = 50; // 20 FPS by default
        this.running = false;
    }

    // ========================================================================
    // Bot management
    // ========================================================================

    /**
     * Registers a bot with this manager.
     *
     * @param bot the Robibot to add
     */
    public void addBot(Robibot bot) {
        bots.put(bot.getElementName(), bot);
    }

    /**
     * Removes and stops the bot identified by the given element name.
     *
     * @param elementName the fully-qualified element name
     */
    public void removeBot(String elementName) {
        Robibot bot = bots.remove(elementName);
        if (bot != null) {
            bot.stop();
        }
    }

    /**
     * Returns the bot registered under the given element name.
     *
     * @param elementName the fully-qualified element name
     * @return the matching Robibot, or {@code null}
     */
    public Robibot getBot(String elementName) {
        return bots.get(elementName);
    }

    /**
     * Returns all registered bots.
     *
     * @return a map of element name to Robibot
     */
    public Map<String, Robibot> getAllBots() {
        return bots;
    }

    /**
     * Stops and removes every registered bot.
     */
    public void clearBots() {
        for (Robibot bot : bots.values()) {
            bot.stop();
        }
        bots.clear();
    }

    // ========================================================================
    // Tick loop
    // ========================================================================

    /**
     * Sets the interval between ticks in milliseconds.
     *
     * @param ms the tick interval
     */
    public void setTickInterval(int ms) {
        this.tickIntervalMs = ms;
    }

    /**
     * Starts all registered bots and launches the tick loop thread
     * if it is not already running.
     */
    public void startAll() {
        for (Robibot bot : bots.values()) {
            bot.start();
            List<String> entryCommands = bot.getStateMachine().start();
            executeCommands(entryCommands, bot);
        }
        if (!running) {
            running = true;
            Thread tickThread = new Thread(this, "RobibotManager-Tick");
            tickThread.setDaemon(true);
            tickThread.start();
        }
    }

    /**
     * Stops the tick loop and all registered bots.
     */
    public void stopAll() {
        running = false;
        for (Robibot bot : bots.values()) {
            bot.stop();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                tick();
                Thread.sleep(tickIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Performs one arbitration tick: evaluates every running bot and
     * executes the resulting commands.
     */
    private void tick() {
        List<String> allCommands = new ArrayList<>();

        for (Robibot bot : bots.values()) {
            if (!bot.isRunning()) {
                continue;
            }

            // The state machine produces the S-Expressions to execute
            List<String> commands = bot.getStateMachine().tick(bot);

            // Replace placeholders with the bot's actual values
            List<String> resolved = resolveCommands(commands, bot);
            allCommands.addAll(resolved);
        }

        if (!allCommands.isEmpty()) {
            executeCommands(allCommands, null);
            notifyListeners(allCommands);
        }
    }

    /**
     * Replaces placeholders in S-Expressions with actual bot values.
     *
     * <p>Supported placeholders:
     * <ul>
     *   <li>{@code {element}} -- element name (e.g. space.r1)</li>
     *   <li>{@code {dx}} -- current horizontal direction</li>
     *   <li>{@code {dy}} -- current vertical direction</li>
     * </ul>
     *
     * @param commands the raw S-Expression list
     * @param bot      the Robibot whose values are substituted
     * @return the resolved commands
     */
    private List<String> resolveCommands(List<String> commands, Robibot bot) {
        List<String> resolved = new ArrayList<>();
        for (String cmd : commands) {
            String r = cmd
                    .replace("{element}", bot.getElementName())
                    .replace("{dx}", String.valueOf(bot.getDx()))
                    .replace("{dy}", String.valueOf(bot.getDy()));
            resolved.add(r);
        }
        return resolved;
    }

    /**
     * Executes S-Expressions on the interpreter, ensuring they run on the
     * Swing Event Dispatch Thread.
     *
     * @param commands the commands to execute
     * @param bot      optional Robibot for placeholder resolution (may be {@code null})
     */
    private void executeCommands(List<String> commands, Robibot bot) {
        if (commands.isEmpty()) {
            return;
        }

        Runnable task = () -> {
            for (String cmd : commands) {
                String resolved = cmd;
                if (bot != null) {
                    resolved = cmd
                            .replace("{element}", bot.getElementName())
                            .replace("{dx}", String.valueOf(bot.getDx()))
                            .replace("{dy}", String.valueOf(bot.getDy()));
                }
                interpreter.oneShot(resolved);
            }
        };

        try {
            if (SwingUtilities.isEventDispatchThread()) {
                task.run();
            } else {
                SwingUtilities.invokeAndWait(task);
            }
        } catch (Exception e) {
            System.err.println("Bot execution error: " + e.getMessage());
        }
    }

    // ========================================================================
    // Listeners (for client synchronisation)
    // ========================================================================

    /**
     * Registers a listener that will be notified after each tick.
     *
     * @param listener the listener to add
     */
    public void addTickListener(BotTickListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a previously registered tick listener.
     *
     * @param listener the listener to remove
     */
    public void removeTickListener(BotTickListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners of the commands produced during a tick.
     *
     * @param commands the commands to broadcast
     */
    private void notifyListeners(List<String> commands) {
        for (BotTickListener listener : listeners) {
            listener.onBotTick(commands);
        }
    }

    /**
     * Callback interface notified after each tick with the commands to execute.
     * Typically used by the server to broadcast commands to connected clients.
     */
    public interface BotTickListener {

        /**
         * Called after a tick with the list of S-Expression commands produced.
         *
         * @param commands the commands generated during the tick
         */
        void onBotTick(List<String> commands);
    }
}
