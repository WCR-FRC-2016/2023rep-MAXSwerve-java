package frc.robot.game;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import org.reflections.Reflections;

import frc.robot.subsystems.LEDController;

public final class GameHandler {
    private static GameHandler instance;

    Hashtable<String, ScreenGame> games = new Hashtable<>();

    private ScreenGame selected_game;

    public static void create() {
        if (instance == null)
            instance = new GameHandler();
    }

    public static void handleGames(LEDController controller) {
        if (instance.selected_game == null)
            return;

        if (instance.selected_game.isFinished())
            instance.selected_game = null;

        if (!instance.selected_game.isInitialized())
            instance.selected_game.init();

        instance.selected_game.update();
        instance.selected_game.draw(controller);
    }
    
    public static void setGame(String identifier) {
        if (instance.selected_game != null)
            instance.selected_game.uninitialize();

        instance.selected_game = instance.games.getOrDefault(identifier, null);
     }

    public static void povUpdate(int controller, int direction) {
        if (instance.selected_game == null)
            return;

        instance.selected_game.povUpdate(controller, direction);
    }

    private GameHandler() {
        var reflections = new Reflections("frc.robot.game");
        var classes = reflections.getSubTypesOf(ScreenGame.class);

        for (var clazz : classes) {
            if (clazz.isInterface())
                continue;

            try {
                var default_constructor = clazz.getConstructor();
                default_constructor.setAccessible(true);

                var handler = (ScreenGame) default_constructor.newInstance();
                var id = handler.getIdentifier();

                if (games.containsKey(id))
                    continue;

                games.put(id, handler);
            } catch(NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ignore) {
                continue;
            }
        }

        // Hardcoded for now
        selected_game = games.get("snake");
    }
}
