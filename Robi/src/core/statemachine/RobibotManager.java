package core.statemachine;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;

import core.RobiInterpreter;
import graphicLayer.GBounded;
import graphicLayer.GElement;
import graphicLayer.GImage;
import graphicLayer.GSpace;

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

        // Detect collisions: bot vs other bots AND bot vs all non-bot elements
        List<Robibot> botList = new ArrayList<>(bots.values());

        // Bot vs bot collisions
        // After inversion, translate 2x to cancel the pending do-action move + separate
        for (int i = 0; i < botList.size(); i++) {
            for (int j = i + 1; j < botList.size(); j++) {
                Robibot a = botList.get(i);
                Robibot b = botList.get(j);
                if (a.isRunning() && b.isRunning() && a.isCollidingWith(b)) {
                    a.setDirection(-a.getDx(), -a.getDy());
                    b.setDirection(-b.getDx(), -b.getDy());
                    allCommands.add("(" + a.getElementName() + " translate "
                            + (a.getDx() * 2) + " " + (a.getDy() * 2) + ")");
                    allCommands.add("(" + b.getElementName() + " translate "
                            + (b.getDx() * 2) + " " + (b.getDy() * 2) + ")");
                }
            }
        }

        // Bot vs non-bot element collisions (GBounded + GImage)
        for (Robibot bot : botList) {
            if (!bot.isRunning()) {
                continue;
            }
            GSpace sp = bot.getSpace();
            if (sp == null) {
                continue;
            }
            GElement[] elements = sp.getRawContents();
            for (GElement elem : elements) {
                // Skip the bot's own element
                if (elem == bot.getElement()) {
                    continue;
                }
                // Skip elements that are themselves bots (handled above)
                boolean isBotElem = false;
                for (Robibot other : botList) {
                    if (other.getElement() == elem) {
                        isBotElem = true;
                        break;
                    }
                }
                if (isBotElem) {
                    continue;
                }
                // Get position and size of the target element
                Point p2;
                int w2;
                int h2;
                if (elem instanceof GBounded) {
                    GBounded bounded = (GBounded) elem;
                    p2 = bounded.getPosition();
                    w2 = bounded.getWidth();
                    h2 = bounded.getHeight();
                } else if (elem instanceof GImage) {
                    GImage img = (GImage) elem;
                    p2 = img.getPosition();
                    w2 = img.getRawImage().getWidth(null);
                    h2 = img.getRawImage().getHeight(null);
                } else {
                    continue;
                }
                // Get bot element bounds
                Point p1;
                int w1;
                int h1;
                if (bot.getElement() instanceof GBounded) {
                    p1 = ((GBounded) bot.getElement()).getPosition();
                    w1 = ((GBounded) bot.getElement()).getWidth();
                    h1 = ((GBounded) bot.getElement()).getHeight();
                } else if (bot.getElement() instanceof GImage) {
                    GImage botImg = (GImage) bot.getElement();
                    p1 = botImg.getPosition();
                    w1 = botImg.getRawImage().getWidth(null);
                    h1 = botImg.getRawImage().getHeight(null);
                } else {
                    continue;
                }
                boolean collides = p1.x < p2.x + w2 && p1.x + w1 > p2.x
                        && p1.y < p2.y + h2 && p1.y + h1 > p2.y;
                if (collides) {
                    bot.setDirection(-bot.getDx(), -bot.getDy());
                    allCommands.add("(" + bot.getElementName() + " translate "
                            + (bot.getDx() * 2) + " " + (bot.getDy() * 2) + ")");
                    break;
                }
            }
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
     *   <li>{@code {randomColor}} -- a random colour name from the palette</li>
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
            // Resolve {randomColor} with a fresh random pick each time
            if (r.contains("{randomColor}")) {
                r = r.replace("{randomColor}", randomColor());
            }
            resolved.add(r);
        }
        return resolved;
    }

    /** Palette de couleurs pour les bots. */
    private static final String[] BOT_COLORS = {
        "red", "blue", "green", "cyan", "magenta", "orange", "yellow", "pink"
    };

    /**
     * Returns a random colour name from the bot palette.
     *
     * @return a colour name string
     */
    private static String randomColor() {
        return BOT_COLORS[(int) (Math.random() * BOT_COLORS.length)];
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
