package frc.robot;

import java.util.List;
import java.util.function.Supplier;

import edu.wpi.first.math.controller.HolonomicDriveController;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.SwerveControllerCommand;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.button.CommandGenericHID;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Swerve.SwerveSubsystem;
import frc.robot.Commands.Intake_note;
import frc.robot.Commands.Outtake_note;
import frc.robot.Commands.Shoot_note;
import frc.robot.Commands.Unjam_note;
import frc.robot.Commands.*;
import frc.robot.Constants.AutoK;
import frc.robot.Subsystems.Intake;
import frc.robot.Subsystems.Shooter;
import frc.robot.Subsystems.Sled;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Notifier;


public class RobotContainer {
    private final boolean DISABLE_WHEELS = false;
    private boolean m_normalDrive = true;
    private final CommandXboxController m_driverController = new CommandXboxController(0);
    // private final CommandXboxController m_operator = new CommandXboxController(1);
    private final ControllerButtons m_operator = new ControllerButtons(1);
    
    public final SwerveSubsystem m_swerve = new SwerveSubsystem(DISABLE_WHEELS);

    public final Shooter m_shooter = new Shooter();

    public final Intake m_intake = new Intake();
     
    public final Sled m_sled = new Sled();
    

    // public final Notifier m_visionloop = new Notifier(m_vision::run);
    // public final Intake m_intake = new Intake();
    // public final Sled m_sled = new Sled();
    // SendableChooser<Command> m_chooser = new SendableChooser<>();

    public final Intake_note intake = new Intake_note(m_intake, m_sled);
    public final Outtake_note outtake = new Outtake_note(m_intake, m_sled);
    public final Unjam_note unjam = new Unjam_note(m_intake, m_sled);
    public final Shoot_note shoot = new Shoot_note(m_shooter, m_sled);
    // public final Sideload_note sideload_note = new Sideload_note(m_intake, m_sled);
    // public final Ampload_note   ampload_note = new Ampload_note(m_shooter, m_sled);
    // public final Ampfire ampfire = new Ampfire(m_shooter, m_sled);


    Command ShootCmd;

    public RobotContainer(){
      DriverStation.silenceJoystickConnectionWarning(true);
    // ShootCmd = new RunCommand(
    //     () -> m_shooter.shootNote()).withTimeout(0.1)
    //     .andThen(new WaitCommand(1))
    //     .andThen(() -> m_sled.runSled()).withTimeout(0.1)
    //     .andThen(new WaitCommand(1))
    //     .andThen(() -> m_shooter.stop())
    //     .finallyDo(() -> m_sled.stop());
   ShootCmd = new SequentialCommandGroup(

        unjam,
        new InstantCommand( () -> m_shooter.shootNote(), m_shooter),
        new WaitCommand(0.5),
        new InstantCommand( () ->m_sled.runSled(), m_sled),
        new InstantCommand( ()->m_intake.intake(),m_intake),
        new WaitCommand(1),
        new ParallelCommandGroup(
        new InstantCommand(() -> m_shooter.stop(), m_shooter),
        new InstantCommand(() -> m_sled.stop(), m_sled),
        new InstantCommand( ()->m_intake.stop(),m_intake)
        )
   );
  /// swerve
    m_swerve.setDefaultCommand(
      m_swerve.teleopDrive(
        () -> m_driverController.getLeftY(),
        () -> m_driverController.getLeftX(),
        () -> m_driverController.getRightX(),
        () -> m_driverController.getRightTriggerAxis()<=0.5,
        () -> true
      )
     
    );
    ///driving
    m_driverController.y().onTrue(new InstantCommand(() -> m_swerve.resetOdometry(), m_swerve) );
    m_driverController.b().whileTrue(
      m_swerve.evasiveDrive(
        () -> m_driverController.getLeftY(),
        () -> m_driverController.getLeftX(),
        () -> m_driverController.getRightX(),
        () -> true,
        () -> true
      )
      
    );
    m_driverController.x().onTrue(new InstantCommand(()->toggleDriveMode()));
    m_driverController.povDown().onTrue(unjam);
   ///Left bumper intake
    m_driverController.leftBumper().onTrue(intake);
   ///Backtop right bumper
    m_driverController.rightBumper().onTrue(ShootCmd);


    m_operator.arcadeWhiteLeft().onTrue(intake);
    m_operator.arcadeBlackLeft().onTrue(outtake);
    // m_driverController.povUp().onTrue(m_sled.tilt_up());
    // m_driverController.povDown().onTrue(m_sled.tilt_down());
    m_operator.arcadeBlackRight().onTrue(unjam);
    m_operator.arcadeWhiteRight().onTrue(ShootCmd);
 

   
    m_operator.sc4().onTrue(new InstantCommand(()->m_shooter.shootNote_speed(), m_shooter)
    .andThen(new WaitCommand(2))
    .andThen(new InstantCommand(()->m_shooter.zero_speed()))
    );
      
    // Supplier<Double> leftslider = () -> m_operator.getRawAxis(0); 
    // Supplier<Double> rightslider = () -> m_operator.getRawAxis(1);
    
    // m_sled.setDefaultCommand(
    
    //   m_sled.rotateSled(
    //     () -> (-m_operator.getLeftSlider() + 1) / 2 * 45+5
    //   )
        

    // );  

    m_sled.setDefaultCommand(
    
      m_sled.rotateSled_new(
        () -> (-m_operator.getLeftSlider() *10))
        

    );


  

  }
  

  // public Command getAutoCommand(){
  //   TrajectoryConfig trajectoryConfig = new TrajectoryConfig(
  //               Constants.AutoK.kMaxSpeedMetersPerSecond,
  //               Constants.AutoK.kMaxAccelerationMetersPerSecondSquared)
  //                       .setKinematics(m_swerve.m_kinematics);
  //   Trajectory trajectory = TrajectoryGenerator.generateTrajectory(
  //               new Pose2d(0, 0, new Rotation2d(0)),
  //               List.of(
  //                       new Translation2d(1, 0),
  //                       new Translation2d(1, -1)),
  //               new Pose2d(2, -1, Rotation2d.fromDegrees(180)),
  //               trajectoryConfig);
  //    PIDController xController = new PIDController(AutoK.kPXController, 0, 0);
  //    PIDController yController = new PIDController(AutoK.kPYController, 0, 0);
  //    ProfiledPIDController thetaController = new ProfiledPIDController(
  //               AutoK.kPThetaController, 0, 0, AutoK.kThetaControllerConstraints);
  //     thetaController.enableContinuousInput(-Math.PI, Math.PI);

  //   // HolonomicDriveController controller = new HolonomicDriveController(xController, yController, thetaController);

  //   SwerveControllerCommand swerveControllerCommand = new SwerveControllerCommand(
  //     trajectory, 
  //     m_swerve::getPose, 
  //     m_swerve.m_kinematics, 
  //     xController,
  //     yController,
  //     thetaController, 
  
  //     m_swerve::setModuleStates, 
  //     m_swerve
  //   );

  //   return new SequentialCommandGroup(
  //               ShootCmd,
  //               new WaitCommand(2),
  //               new InstantCommand(() -> m_swerve.resetOdometry(trajectory.getInitialPose()), m_swerve),
  //               swerveControllerCommand,
  //               new InstantCommand(() -> m_swerve.stop(), m_swerve));
  //   // return new SequentialCommandGroup();

  // }

  //TODO: Fix shoot cmd aspect of auto, otherwise it works

   public Command getAutoCommand1BLUE(){
    return new SequentialCommandGroup(
      new InstantCommand(() -> m_swerve.resetOdometry(new Pose2d(new Translation2d(0.8,6.542), new Rotation2d(-Math.PI*7/6))), m_swerve),
      ShootCmd,
      new WaitCommand(0.5),
      new InstantCommand(() -> m_swerve.drive(1, -1.7, 0, false), m_swerve),
      new WaitCommand(0.9),
      new InstantCommand(() -> m_swerve.drive(0, -0.9, 0, false), m_swerve),
      new WaitCommand(0.5),
      new InstantCommand(() -> m_swerve.drive(1, -1.7, 0, false), m_swerve),
      new WaitCommand(1),
      new InstantCommand(() -> m_swerve.stop(), m_swerve)
    );
  }

  //   public Command getAutoCommand2BLUE(){
  //   return new SequentialCommandGroup(
  //     new InstantCommand(() -> m_swerve.resetOdometry(new Pose2d(new Translation2d(0.8,6.542), new Rotation2d(Math.PI))), m_swerve),
  //     ShootCmd,
  //     new WaitCommand(0.5),
  //     new InstantCommand(() -> m_swerve.drive(1, 0, 0, false ), m_swerve),
  //     new WaitCommand(0.5),
  //     new InstantCommand(() -> m_swerve.drive(1, 1, 0, false), m_swerve),
  //     new WaitCommand(0.5),
  //     new InstantCommand(() -> m_swerve.drive(1.3, 0, 0, false ), m_swerve),
  //     new WaitCommand(1),
  //     new InstantCommand(() -> m_swerve.stop(), m_swerve)
  //   );
  // }

  public Command getAutoCommand22BLUE(){
    return new SequentialCommandGroup(
      new InstantCommand(() -> m_swerve.resetOdometry(new Pose2d(new Translation2d(0.8,6.542), new Rotation2d(Math.PI))), m_swerve),
      ShootCmd,
      new WaitCommand(0.5),
      new ParallelCommandGroup(intake, new InstantCommand(() -> m_swerve.drive(1, 0, 0, false ), m_swerve)),
      new WaitCommand(0.5),
      new InstantCommand(() -> m_swerve.drive(1, 1, 0, false), m_swerve),
      new WaitCommand(0.5),
      new InstantCommand(() -> m_swerve.drive(1.3, 0, 0, false ), m_swerve),
      new WaitCommand(1),
      new InstantCommand(() -> m_swerve.stop(), m_swerve)
    );
  }

   public Command getAutoCommand3BLUE(){
    return new SequentialCommandGroup(
      new InstantCommand(() -> m_swerve.resetOdometry(new Pose2d(new Translation2d(0.8,6.542), new Rotation2d(-Math.PI*5/6))), m_swerve),
      ShootCmd,
      new WaitCommand(0.5),
      new InstantCommand(() -> m_swerve.drive(2, 0, 0, false ), m_swerve),
      new WaitCommand(1),
      new InstantCommand(() -> m_swerve.drive(1.7, 0.9, 0, false), m_swerve),
      new WaitCommand(1),
      // new InstantCommand(() -> m_swerve.drive(1.3, 0, 0, false, true), m_swerve),
      // new WaitCommand(1),
      new InstantCommand(() -> m_swerve.stop(), m_swerve)
    );
  }
  public Command getAutoCommand1RED(){
    return new SequentialCommandGroup(
      new InstantCommand(() -> m_swerve.resetOdometry(new Pose2d(new Translation2d(0.8,6.542), new Rotation2d(Math.PI*7/6))), m_swerve),
      ShootCmd,
      new WaitCommand(0.5),
      new InstantCommand(() -> m_swerve.drive(1, 1.7, 0, false), m_swerve),
      new WaitCommand(0.5),
      new InstantCommand(() -> m_swerve.drive(0, 0.9, 0, false), m_swerve),
      new WaitCommand(0.5),
      new InstantCommand(() -> m_swerve.drive(1, 1.7, 0, false), m_swerve),
      new WaitCommand(1),
      new InstantCommand(() -> m_swerve.stop(), m_swerve)
    );
  }
  

    public Command getAutoCommand2RED(){
    return new SequentialCommandGroup(
      new InstantCommand(() -> m_swerve.resetOdometry(new Pose2d(new Translation2d(0.8,6.542), new Rotation2d(Math.PI))), m_swerve),
      ShootCmd,
      new WaitCommand(0.5),
      new InstantCommand(() -> m_swerve.drive(1, 0, 0, false ), m_swerve),
      new WaitCommand(0.5),
      new InstantCommand(() -> m_swerve.drive(1, 1, 0, false), m_swerve),
      new WaitCommand(0.5),
      new InstantCommand(() -> m_swerve.drive(1.3, 0, 0, false ), m_swerve),
      new WaitCommand(1),
      new InstantCommand(() -> m_swerve.stop(), m_swerve)
    );
  }

   public Command getAutoCommand3RED(){
    return new SequentialCommandGroup(
      new InstantCommand(() -> m_swerve.resetOdometry(new Pose2d(new Translation2d(0.8,6.542), new Rotation2d(Math.PI*5/6))), m_swerve),
      ShootCmd,
      new WaitCommand(0.5),
      new InstantCommand(() -> m_swerve.drive(2, 0, 0, false ), m_swerve),
      new WaitCommand(1),
      new InstantCommand(() -> m_swerve.drive(1.5, -0.9, 0, false), m_swerve),
      new WaitCommand(1),
      // new InstantCommand(() -> m_swerve.drive(1.3, 0, 0, false), m_swerve),
      // new WaitCommand(1),
      new InstantCommand(() -> m_swerve.stop(), m_swerve)
    );
  }


  
  private void toggleDriveMode(){
  
      if (m_normalDrive){
        m_swerve.setDefaultCommand(
          m_swerve.teleop_dir_drive(
            () -> m_driverController.getLeftY(),
            () -> m_driverController.getLeftX(),
            () -> m_driverController.getRightX(),
            () -> m_driverController.getRightY()
          )
        
        );
        System.out.println("directed drive");
        m_normalDrive = false;

      } else {
        m_swerve.setDefaultCommand(
          m_swerve.teleopDrive(
            () -> m_driverController.getLeftY(),
            () -> m_driverController.getLeftX(),
            () -> m_driverController.getRightX(),
            () -> m_driverController.getRightTriggerAxis()<=0.5,
            () -> true
          )
        );
        m_normalDrive = true;
        System.out.println("field drive");
      }
    
  } 
  
  public void teleopInit(){
    // m_swerve.resetOdometry();
    // m_sled.resetSledPivot();
  } 

 

}
