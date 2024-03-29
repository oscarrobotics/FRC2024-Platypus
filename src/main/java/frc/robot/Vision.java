package frc.robot;

import org.littletonrobotics.junction.Logger;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;

import static frc.robot.Constants.FieldK.kFieldLayout;
import static frc.robot.Constants.VisionK.*;

import java.util.function.Consumer;

//yoinked
public class Vision {
    public static final record VisionMeasurement (Pose2d measure, double latency, Matrix<N3, N1> stdDevs) {}
    private final Matrix<N3, N1> kDefaultStdDevs = VecBuilder.fill(0.9, 0.9, 0.9);

    private final Consumer<VisionMeasurement> m_visionMeasurementConsumer;

    private final PhotonCamera m_frontTagCam = new PhotonCamera("FrontTag");
    
    private final PhotonPoseEstimator m_frontTagEst = new PhotonPoseEstimator(
        kFieldLayout, PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR, 
        m_frontTagCam, kFrontTagCamLocation);
    
    // private VisionMeasurement m_lefMeasurement;
    // private VisionMeasurement m_Rightmeasurement;


    private final PhotonCamera m_rearTagCam = new PhotonCamera("RearTag");
    private final PhotonPoseEstimator m_rearTagEst = new PhotonPoseEstimator(
        kFieldLayout, PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
        m_rearTagCam, kRearTagCamLocation);

            
    // private final PhotonCamera m_frontObjCam = new PhotonCamera("FrontColor");

    // private final PhotonCamera m_rearObjCam = new PhotonCamera("RearColor");



            





    public Vision(Consumer<VisionMeasurement> visionMeasurementConsumer) {
        m_visionMeasurementConsumer = visionMeasurementConsumer;
        m_frontTagCam.setDriverMode(false);
        // m_rearTagCam.setDriverMode(false);

    }

    public void run() {
        var frontTagUpdate = m_frontTagEst.update();
        if (frontTagUpdate.isPresent()) {
            var est = frontTagUpdate.get();
            m_visionMeasurementConsumer.accept(
                new VisionMeasurement(est.estimatedPose.toPose2d(), est.timestampSeconds, kDefaultStdDevs));
        }

        var rearTagUpdate = m_rearTagEst.update();
        if (rearTagUpdate.isPresent()) {
            var est = rearTagUpdate.get();
            m_visionMeasurementConsumer.accept(
                new VisionMeasurement(est.estimatedPose.toPose2d(), est.timestampSeconds, kDefaultStdDevs));
        }

        // var objCamResult = m_frontObjCam.getLatestResult();
        // if (objCamResult.hasTargets()) {
        //     var noteYaw = objCamResult.getBestTarget().getYaw();
        //     Logger.recordOutput("bestNoteYaw", noteYaw); // TODO: do something more clever with this
        // }

    
    }
    public Command runner(){
        return new RunCommand(()->run());
    }
}
