// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.XboxController.Button;
import frc.robot.Constants.AutoConstants;
import frc.robot.Constants.DriveConstants;
import frc.robot.Constants.OIConstants;
import frc.robot.subsystems.ArmSubsystem;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.LEDController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SwerveControllerCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.POVButton;
import java.util.List;

/*
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
    // The robot's subsystems
    private final DriveSubsystem m_drive = new DriveSubsystem();
    private final ArmSubsystem m_arm = new ArmSubsystem();
    private final LEDController m_leds = new LEDController();

    // The driver's controller
    XboxController m_driverController = new XboxController(OIConstants.kDriverControllerPort);
    XboxController m_manipController = new XboxController(OIConstants.kManipControllerPort); // :Trollface:

    private boolean m_relative = true;
    private boolean m_rate_limit = true;

    /**
     * The container for the robot. Contains subsystems, OI devices, and commands.
     */
    public RobotContainer() {
    // Configure the button bindings
    configureButtonBindings();

    // Configure default commands
    m_drive.setDefaultCommand(
        // The left stick controls translation of the robot.
        // Turning is controlled by the X axis of the right stick.
        new RunCommand(
            () -> m_drive.drive(
                -MathUtil.applyDeadband(m_driverController.getLeftY(), OIConstants.kDriveDeadband),
                -MathUtil.applyDeadband(m_driverController.getLeftX(), OIConstants.kDriveDeadband),
                -MathUtil.applyDeadband(m_driverController.getRightX(), OIConstants.kDriveDeadband),
                m_relative, m_rate_limit),
            m_drive));

    m_arm.setDefaultCommand(new RunCommand(() -> {
        if (m_arm.getGoalState() == -2) {
            m_arm.drive(
                MathUtil.applyDeadband(m_manipController.getRightY(), OIConstants.kDriveDeadband),
                MathUtil.applyDeadband(-m_manipController.getLeftY(), OIConstants.kDriveDeadband));

            var spit = m_manipController.getRightTriggerAxis() > 0.5d ? 1.0d : 0.0d;
            var suck = m_manipController.getLeftTriggerAxis() > 0.5d ? 1.0d : 0.0d;

            m_arm.driveCollectWheels(suck - spit);
        }
        }, m_arm));
    }

    /**
     * Use this method to define your button->command mappings. Buttons can be
     * created by
     * instantiating a {@link edu.wpi.first.wpilibj.GenericHID} or one of its
     * subclasses ({@link
     * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then calling
     * passing it to a
     * {@link JoystickButton}.
     */
    private void configureButtonBindings() {
    new JoystickButton(m_driverController, Button.kX.value)
        .whileTrue(new RunCommand(
            () -> m_drive.setX(),
            m_drive));

    new JoystickButton(m_driverController, Button.kY.value)
        .whileTrue(new InstantCommand(
            () -> {
                m_relative = !m_relative;
                m_leds.setState(m_relative ? 4 : 5);
            },
            m_drive));

    new JoystickButton(m_driverController, Button.kStart.value)
        .whileTrue(new InstantCommand(
            () -> m_drive.swapSpeed(),
            m_drive));

    new JoystickButton(m_driverController, Button.kBack.value)
        .whileTrue(new InstantCommand(
            () -> m_drive.zeroHeading(),
            m_drive));

    // LED State Change Commands
    new POVButton(m_manipController, 0)
        .onTrue(new InstantCommand(
            () -> m_leds.setState(1),
            m_leds));

    new POVButton(m_manipController, 180)
        .onTrue(new InstantCommand(
            () -> m_leds.setState(2),
            m_leds));

    new POVButton(m_manipController, 90)
        .onTrue(new InstantCommand(
            () -> m_leds.setState(0),
            m_leds));

    new POVButton(m_manipController, 270)
        .onTrue(new InstantCommand(
            () -> m_leds.setState(8),
            m_leds));
    }

    /**
     * Use this to pass the autonomous command to the main {@link Robot} class.
     *
     * @return the command to run in autonomous
     */
    public Command getAutonomousCommand() {
    // Create config for trajectory
    TrajectoryConfig config = new TrajectoryConfig(
        AutoConstants.kMaxSpeedMetersPerSecond,
        AutoConstants.kMaxAccelerationMetersPerSecondSquared)
        // Add kinematics to ensure max speed is actually obeyed
        .setKinematics(DriveConstants.kDriveKinematics);

    // An example trajectory to follow. All units in meters.
    Trajectory exampleTrajectory = TrajectoryGenerator.generateTrajectory(
        // Start at the origin facing the +X direction
        new Pose2d(0, 0, new Rotation2d(0)),
        // Pass through these two interior waypoints, making an 's' curve path
        List.of(new Translation2d(1, 1), new Translation2d(2, -1)),
        // End 3 meters straight ahead of where we started, facing forward
        new Pose2d(3, 0, new Rotation2d(0)),
        config);

    var thetaController = new ProfiledPIDController(
        AutoConstants.kPThetaController, 0, 0, AutoConstants.kThetaControllerConstraints);
    thetaController.enableContinuousInput(-Math.PI, Math.PI);

    SwerveControllerCommand swerveControllerCommand = new SwerveControllerCommand(
        exampleTrajectory,
        m_drive::getPose, // Functional interface to feed supplier
        DriveConstants.kDriveKinematics,

        // Position controllers
        new PIDController(AutoConstants.kPXController, 0, 0),
        new PIDController(AutoConstants.kPYController, 0, 0),
        thetaController,
        m_drive::setModuleStates,
        m_drive);

    // Reset odometry to the starting pose of the trajectory.
    m_drive.resetOdometry(exampleTrajectory.getInitialPose());

    // Run path following command, then stop at the end.
    return swerveControllerCommand.andThen(() -> m_drive.drive(0, 0, 0, false, false));
    }
}
