package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.ArmSubsystem;

public class MoveClawCommand extends CommandBase {
    private ArmSubsystem arm;
    private double direction;
    
    public MoveClawCommand(ArmSubsystem arm, double direction) {
        this.arm = arm;
        this.direction = direction;

        addRequirements(arm);
    }

    @Override
    public void execute() {
        // TODO: Logging Stuff

        if (!isFinished())
            arm.driveClaw(direction);
    }
    
    @Override
    public void end(boolean interrupted) {
        arm.driveClaw(0);
    }

    @Override
    public boolean isFinished() { return (direction > 0) ? arm.getOuterLimitSwitchState() : arm.getInnerLimitSwitchState(); }
}
