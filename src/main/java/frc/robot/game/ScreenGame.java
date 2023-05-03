package frc.robot.game;

import frc.robot.subsystems.LEDController;

public abstract class ScreenGame {
    private boolean initialized = false;

    public final boolean isInitialized() { return initialized; }
    public final void uninitialize() { 
        initialized = false;
        destroy();
    }

    public String getIdentifier() { return "none"; }

    public void init() { }
    public void destroy() { }
    public boolean isFinished() { return true; }
    
    public void update() { }
    public void draw(LEDController controller) { }
    public void povUpdate(int controller, int direction) { }

}