package frc.robot.game;

import frc.robot.subsystems.LEDController;

public abstract class ScreenGame {
    public String getIdentifier() { return "none"; }

    public void update() { }
    public void draw(LEDController controller) { }
    public void povUpdate(int controller, int direction) { }
}