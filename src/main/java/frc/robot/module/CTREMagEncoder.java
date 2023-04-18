package frc.robot.module;

import edu.wpi.first.wpilibj.DutyCycle;
import edu.wpi.first.wpilibj.DutyCycleEncoder;

public final class CTREMagEncoder {
    private DutyCycleEncoder encoder;

    public CTREMagEncoder(int dio) {
        encoder = new DutyCycleEncoder(dio);
    }

    public boolean isConnected() { return encoder.isConnected(); }

    public boolean isAbove(double value) { return value > getNormalizedDistanceDegrees(); }
    public boolean isAboveAbs(double value) { return value > Math.abs(getNormalizedDistanceDegrees()); }
    public boolean isBelow(double value) { return value < getNormalizedDistanceDegrees(); }
    public boolean isBelowAbs(double value) { return value < Math.abs(getNormalizedDistanceDegrees()); }
    public boolean isBetween(double low, double high) {
        var degree = getNormalizedDistanceDegrees();

        // Low value is higher than high, flip their use
        if (low > high)
            return (degree >= high) && (degree <= low);
        else 
            return (degree >= low) && (degree <= high);
    }

    public double getDistance() { return encoder.getDistance(); }
    public double getDistanceDegrees() { return getDistance() * 360.0f; }
    public double getNormalizedDistanceDegrees() { 
        int turns = (int) getDistance();
        return getDistanceDegrees() - (turns * 360.0f);
    }
    public int getRevolutions() { return (int) getDistance(); }
}
