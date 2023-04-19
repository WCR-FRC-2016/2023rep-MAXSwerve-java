package frc.robot.module;

import com.revrobotics.AbsoluteEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.SparkMaxAbsoluteEncoder;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.SparkMaxAbsoluteEncoder.Type;

public class ActuatorModule {
    private CANSparkMax max;

    public ActuatorModule(int can_id) {
        max = new CANSparkMax(can_id, MotorType.kBrushed);

        max.setIdleMode(IdleMode.kBrake);
        max.setSmartCurrentLimit(10); // In amps

        max.burnFlash();
    }

    public void drive(double dir) { max.set(-dir); }
}
