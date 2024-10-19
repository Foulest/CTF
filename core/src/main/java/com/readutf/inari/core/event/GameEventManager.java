package com.readutf.inari.core.event;

import com.readutf.inari.core.event.adapters.*;
import com.readutf.inari.core.game.Game;
import com.readutf.inari.core.game.GameManager;
import com.readutf.inari.core.game.events.GameEvent;
import com.readutf.inari.core.logging.Logger;
import com.readutf.inari.core.logging.LoggerFactory;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

@Getter
public class GameEventManager implements Listener {

    private static final Logger logger = LoggerFactory.getLogger(GameEventManager.class);

    private final JavaPlugin javaPlugin;
    private final Map<UUID, Map<Class<? extends Event>, List<GameEventListener>>> gameIdToEventMethod;
    private final Map<Class<? extends Event>, GameEventAdapter> eventAdapterMap;
    private final List<Class<? extends Event>> activeDebugs;

    public GameEventManager(JavaPlugin javaPlugin, GameManager gameManager) {
        this.javaPlugin = javaPlugin;
        gameIdToEventMethod = new HashMap<>();
        eventAdapterMap = new HashMap<>();
        activeDebugs = new ArrayList<>();

        for (Class<? extends GameEvent> aClass : new Reflections("com.readutf.inari.core").getSubTypesOf(GameEvent.class)) {
            // Check that the class is not interface or abstract
            if (aClass.isInterface() || Modifier.isAbstract(aClass.getModifiers())) {
                continue;
            }

            try {
                registerEventAdapter(aClass, new GameEventEventAdapter(gameManager));
            } catch (RuntimeException ex) {
                logger.warn("Failed to register GameEventEventAdapter for " + aClass.getSimpleName() + ": " + ex.getMessage());
            }
        }

        registerEventAdapter(EntityDamageEvent.class, new EntityDamageAdapter(gameManager));
        registerEventAdapter(AsyncPlayerChatEvent.class, new ChatAdapter(gameManager));
        registerEventAdapter(PlayerMoveEvent.class, new PlayerMoveAdapter(gameManager));
        registerEventAdapter(BlockPlaceEvent.class, new BlockPlaceAdapter(gameManager));
        registerEventAdapter(BlockBreakEvent.class, new BlockBreakAdapter(gameManager));
        registerEventAdapter(EntityPickupItemEvent.class, new EntityPickupItemAdapter(gameManager));

        for (Class<? extends PlayerEvent> aClass : new Reflections("org.bukkit.event").getSubTypesOf(PlayerEvent.class)) {
            // Check that the class is not interface or abstract
            if (eventAdapterMap.containsKey(aClass)) {
                continue;
            }

            if (aClass.isInterface() || Modifier.isAbstract(aClass.getModifiers())) {
                continue;
            }

            try {
                registerEventAdapter(aClass, new PlayerEventAdapter(gameManager));
            } catch (RuntimeException ex) {
                logger.warn("Failed to register PlayerEventAdapter for " + aClass.getSimpleName() + ": " + ex.getMessage());
            }
        }
    }

    private void registerEventAdapter(Class<? extends Event> eventType, GameEventAdapter adapter) {
        eventAdapterMap.put(eventType, adapter);
        Bukkit.getPluginManager().registerEvent(eventType, this, EventPriority.NORMAL, new CustomEventExecutor(), javaPlugin);
    }

    public void scanForListeners(Game game, @NotNull Object object) {
        Class<?> clazz = object.getClass();

        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(GameEventHandler.class)) {
                Class<?>[] parameters = method.getParameterTypes();

                if (parameters.length != 1) {
                    logger.warn("Method " + method.getName() + " in class " + clazz.getName() + " must have only one parameter of type Event");
                    continue;
                }

                if (!Event.class.isAssignableFrom(parameters[0])) {
                    logger.warn("Method " + method.getName() + " in class " + clazz.getName() + " must have only one parameter of type Event");
                    continue;
                }

                Class<? extends Event> eventClass = parameters[0].asSubclass(Event.class);

                if (findEventAdapter(eventClass) == null) {
                    logger.warn("No GameEventAdapter found for event " + parameters[0].getSimpleName());
                    continue;
                }

                GameEventHandler annotation = method.getAnnotation(GameEventHandler.class);

                Map<Class<? extends Event>, List<GameEventListener>> eventMethodMap = gameIdToEventMethod.getOrDefault(game.getGameId(), new HashMap<>());
                List<GameEventListener> listeners = eventMethodMap.getOrDefault(eventClass, new ArrayList<>());

                listeners.add(new GameEventListener(object, method, annotation));
                eventMethodMap.put(eventClass, listeners);
                gameIdToEventMethod.put(game.getGameId(), eventMethodMap);
            }
        }
    }

    public void unregisterListeners(@NotNull Game game, Object object) {
        Map<Class<? extends Event>, List<GameEventListener>> gameListeners = new HashMap<>(gameIdToEventMethod.getOrDefault(game.getGameId(), new HashMap<>()));
        gameListeners.forEach((aClass, gameEventListeners) -> gameEventListeners.removeIf(eventListener -> eventListener.getOwner() == object));
    }

    public void unregisterGame(@NotNull Game game) {
        gameIdToEventMethod.remove(game.getGameId());
    }

    private class CustomEventExecutor implements EventExecutor {

        @Override
        public void execute(@NotNull Listener listener, @NotNull Event event) throws EventException {
            boolean debugEvent = activeDebugs.contains(event.getClass());

            if (debugEvent) {
                logger.debug("Received event " + event.getClass().getSimpleName());
            }

            GameEventAdapter gameEventAdapter = findEventAdapter(event.getClass());

            if (gameEventAdapter == null) {
                if (debugEvent) {
                    logger.debug("Failed to find GameEventAdapter for event " + event.getClass().getSimpleName());
                }
                return;
            }

            GameAdapterResult result = gameEventAdapter.getGame(event);

            if (!result.isSuccessful()) {
                if (debugEvent) {
                    logger.debug("Failed to find game for event " + event.getClass().getSimpleName() + " because " + result.getFailReason());
                }
                return;
            }

            Game game = result.getGame();

            if (game == null) {
                logger.warn("Successful GameAdapterResult should not return null");
                return;
            }

            List<GameEventListener> gameListeners = findEventListeners(game, event.getClass());

            if (debugEvent) {
                logger.debug("Found " + gameListeners.size() + " listeners for event " + event.getClass().getSimpleName());
            }

            gameListeners.stream()
                    .filter(gameEventListener -> !gameEventListener.getGameEventHandler().ignoreCancelled())
                    .sorted(Comparator.<GameEventListener>comparingInt(value -> value.getGameEventHandler().priority()).reversed())
                    .forEach(eventListener -> {
                        try {
                            eventListener.getMethod().invoke(eventListener.getOwner(), event);
                        } catch (IllegalAccessException | InvocationTargetException ex) {
                            ex.printStackTrace();
                        }
                    });
        }

        private List<GameEventListener> findEventListeners(@NotNull Game game, Class<? extends Event> eventClass) {
            Map<Class<? extends Event>, List<GameEventListener>> listeners = gameIdToEventMethod.getOrDefault(game.getGameId(), new HashMap<>());

            if (listeners.containsKey(eventClass)) {
                return listeners.get(eventClass);
            }

            for (Map.Entry<Class<? extends Event>, List<GameEventListener>> entry : listeners.entrySet()) {
                if (entry.getKey().isAssignableFrom(eventClass)) {
                    return entry.getValue();
                }
            }
            return new ArrayList<>();
        }
    }

    private @Nullable GameEventAdapter findEventAdapter(Class<? extends Event> eventClass) {
        GameEventAdapter gameEventAdapter = eventAdapterMap.get(eventClass);

        if (gameEventAdapter != null) {
            return gameEventAdapter;
        }

        for (Map.Entry<Class<? extends Event>, GameEventAdapter> entry : eventAdapterMap.entrySet()) {
            if (entry.getKey().isAssignableFrom(eventClass)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
