package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.ArmConstants;
import frc.robot.module.ActuatorModule;
import frc.robot.module.CTREMagEncoder;

public class ArmSubsystem extends SubsystemBase {
    private CANSparkMax handLeft;
    private CANSparkMax handRight;
    private CANSparkMax handGrab;

    ActuatorModule lowActuator;
    ActuatorModule highActuator;

    PIDController lowPID;
    PIDController highPID;

    CTREMagEncoder lowEncoder;
    CTREMagEncoder highEncoder;

    DigitalInput outerLimitSwitch;
    DigitalInput innerLimitSwitch;
    DigitalInput pieceSensor;

    int state = -1;
    int goalState = -1;
    int nextGoalState = -1;

    // Warning: Direction inverted from DriveClaw
    int currentPosition = 0;

    public ArmSubsystem() {
        handLeft  = new CANSparkMax(ArmConstants.kHandLeftId,  MotorType.kBrushed);
        handRight = new CANSparkMax(ArmConstants.kHandRightId, MotorType.kBrushed);
        handGrab  = new CANSparkMax(ArmConstants.kHandGrabId,  MotorType.kBrushed);

        lowActuator = new ActuatorModule(ArmConstants.kArmLowId);
        highActuator = new ActuatorModule(ArmConstants.kArmHighId);
    
        lowPID  = new PIDController(ArmConstants.kArmLowP, ArmConstants.kArmLowI, ArmConstants.kArmLowD);
        highPID = new PIDController(ArmConstants.kArmHighP, ArmConstants.kArmHighI, ArmConstants.kArmHighD);

        lowEncoder  = new CTREMagEncoder(ArmConstants.kArmLowEncoderId);
        highEncoder = new CTREMagEncoder(ArmConstants.kArmHighEncoderId);

        outerLimitSwitch = new DigitalInput(0);
        innerLimitSwitch = new DigitalInput(1);
        pieceSensor      = new DigitalInput(4);

        handLeft.restoreFactoryDefaults();
        handRight.restoreFactoryDefaults();
        handGrab.restoreFactoryDefaults();
    
        handLeft.setIdleMode(IdleMode.kBrake);
        handRight.setIdleMode(IdleMode.kBrake);
        handGrab.setIdleMode(IdleMode.kBrake);
    
        handLeft.setSmartCurrentLimit(ArmConstants.kHandLeftCurrentLimit);
        handRight.setSmartCurrentLimit(ArmConstants.kHandRightCurrentLimit);
        handGrab.setSmartCurrentLimit(ArmConstants.kHandGrabCurrentLimit);

        handLeft.burnFlash();
        handRight.burnFlash();
        handGrab.burnFlash();

        lowPID.enableContinuousInput(0.0d, 360.0d);
        highPID.enableContinuousInput(0.0d, 360.0d);
    
        lowPID.setTolerance(1.5d);
        highPID.setTolerance(1.5d);
    }

    @Override
    public void periodic() {
        double low, high;

        switch(goalState) {
            case 1:
                low = -22.2d;
                high = 0.0d;
                break;
            case 2:
                low = 23.0d;
                high = 64.4d;
                break;
            case 3:
                low = -43.5d;
                high = 108.0d;
                break;
            case 4:
                low = -23.0d;
                high = 71.4d;
                break;
            case 5:
            case 6:
                low = 0.0d;
                high = 0.0d;
                break;
            case 7:
                low = -23.0d;
                high = 79.1d;
                break;
            case -1:
            case -2:
            default:
                return;
        }

        turnToAngles(low, high);
        if (lowPID.atSetpoint() && highPID.atSetpoint()) {
            state = goalState;
            goalState = nextGoalState;
            nextGoalState = -1;
        }        
    }

    public void setState(int new_state) {
        goalState = new_state;
        nextGoalState = -1;
    }
    public int getGoalState() { return goalState; }

    public void turnToAngles(double low, double high) {
        double low_move  = lowPID.calculate(getLowerAngle(), low);
        double high_move = highPID.calculate(getUpperAngle(), high);

        low_move = MathUtil.clamp(low_move, -1.0d, 1.0d);
        high_move = MathUtil.clamp(high_move, -1.0d, 1.0d);

        if (!lowPID.atSetpoint())  lowActuator.drive(low_move);
        if (!highPID.atSetpoint()) highActuator.drive(high_move);
    }

    public boolean getOuterLimitSwitchState() { return outerLimitSwitch.get(); }
    public boolean getInnerLimitSwitchState() { return innerLimitSwitch.get(); }

    public void driveClaw(double dir) {
        // TODO: The limit switches are still janky and may occasionally not work
        if (outerLimitSwitch.get())
            if (dir > 0.0d) 
                dir = 0.0d;

        if (innerLimitSwitch.get())
            if (dir < 0.0d)
                dir = 0.0d;

        handGrab.set(dir);
    }

    public boolean hasPiece() { return !pieceSensor.get(); }

    public void driveCollectWheels(double dir) {
        if (dir > 0 && hasPiece()) {
            handRight.set(0.0d);
            handLeft.set(0.0d);
        } else {
            handRight.set(-dir);
            handLeft.set(dir);
        }
    }

    public void drive(double low, double high) {
        goalState = -2;
        lowActuator.drive(low);
        highActuator.drive(high);
    }

    double getUpperAngle() { return highEncoder.getNormalizedDistanceDegrees() - ArmConstants.kArmHighOffset; }
    double getLowerAngle() { return lowEncoder.getNormalizedDistanceDegrees() - ArmConstants.kArmLowOffset; }

    double getRawUpperAngle() { return highEncoder.getDistance(); }
    double getRawLowerAngle() { return lowEncoder.getDistance(); }
}
