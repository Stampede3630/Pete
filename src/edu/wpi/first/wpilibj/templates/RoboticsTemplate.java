/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Preferences;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class RoboticsTemplate extends IterativeRobot {

    Deck deck;
    Shooter shooter;
    Launcher launcher;
    DriveTrain driveTrain;
    Winch winch;
    DriverStationLCD driverStation;
    Joystick leftStick;
    Joystick rightStick;
    Joystick gamepad;
    
    // autonomous handler variables
    Timer autoTimer;
    int autoState;
    int autoShots;
    int shotCounter;
    double autoShotAngle, autoShotRPM, autoShotDelay;
    double autoBackHeading, autoBackSpeed, 
            autoBackTime1, autoBackAngle1, 
            autoBackTime2, autoBackAngle2, 
            autoBackRotation;
    boolean shotFired;
    
    Preferences autoPrefs;

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code 
     */
    public void robotInit() {
       // create all objects
        shooter = new Shooter();
        driveTrain = new DriveTrain();
        launcher = new Launcher();
        deck = new Deck();
        winch = new Winch();
        
        if(true){ 
           autoPrefs = Preferences.getInstance();
           
           autoShotRPM = autoPrefs.getDouble("autoShotRPM", 1600);
           autoShots = autoPrefs.getInt("autoShots", 3); 
           autoShotAngle = autoPrefs.getDouble("autoAngle", -4); 
           autoShotDelay = autoPrefs.getDouble("autoShotDelay", 2); 

           autoBackHeading = autoPrefs.getDouble("autoBackHeading", -10); // heading when shooting
           autoBackSpeed = autoPrefs.getDouble("autoBackSpeed", -0.3); // power
           autoBackTime1 = autoPrefs.getDouble("autoBackTime1", 2);   // seconds
           autoBackTime2 = autoPrefs.getDouble("autoBackTime2", 0);   // seconds
           autoBackAngle1 = autoPrefs.getDouble("autoBackAngle1", 155); // degrees
           autoBackAngle2 = autoPrefs.getDouble("autoBackAngle2", 175); // degrees
           autoBackRotation = autoPrefs.getDouble("autoBackRotation", 0.15); // power

           winch.winchInSpeed = autoPrefs.getDouble("winchInSpeed", -0.3); // power
           winch.winchOutSpeed = autoPrefs.getDouble("winchOutSpeed", 0.7); // power
       }
   
        // init all objects
        shooter.init();
        driveTrain.init();
        launcher.init();
        deck.init();
        winch.init();

        // create UI elements
        leftStick = new Joystick(1);
        rightStick = new Joystick(2);
        gamepad = new Joystick(3);

        //intialize deck controls 
        deck.joystick = gamepad;
        deck.raiseDeckButton = 6; 
        deck.lowerDeckButton = 8;
        deck.shootingPositionButton = 4;
        deck.deckBottomButton = 2; 
        
        //intilize shooter controls  
        shooter.joystick = gamepad;
        shooter.increaseRPMButton = 3; 
        shooter.decreaseRPMButton = 1;
        shooter.shootingRPMButton = 4; 
        shooter.offButton = 2; 
        
        //intialize drive train controls
        driveTrain.rightStick = rightStick;
        driveTrain.leftStick = leftStick;
        driveTrain.gamepad = gamepad; 
        driveTrain.approachButton = 4;
        driveTrain.gyroLockButton = 1;
        driveTrain.shooterRotationButton = 11; 
        
        //intialize winch controls 
       //** winch.gamepad = leftStick;

        winch.leftWinchOutButton = 6;
        winch.leftWinchInButton = 7;
        winch.rightWinchOutButton = 11;
        winch.rightWinchInButton = 10;
        winch.winchActivateButton1 = 8;
        winch.winchActivateButton2 = 9;  

        //intialize launcher controls 
        launcher.joystick = gamepad;
        launcher.fireButton = 10;
        launcher.safetyButton = 10;
        launcher.reTapButton = 5; 
        launcher.rePushButton = 7;
        launcher.reShootButton = 9;

        autoTimer = new Timer();
    }

    public void disabledInit() {
        if(false){ 
           autoPrefs = Preferences.getInstance();
           
           autoShotRPM = autoPrefs.getDouble("autoShotRPM", 1200);
           autoShots = autoPrefs.getInt("autoShots", 3); 
           autoShotAngle = autoPrefs.getDouble("autoAngle", 40); 
           autoShotDelay = autoPrefs.getDouble("autoShotDelay", 2); 

           autoBackHeading = autoPrefs.getDouble("autoBackHeading", 0); // heading when shooting
           autoBackSpeed = autoPrefs.getDouble("autoBackSpeed", -0.4); // power
           autoBackAngle1 = autoPrefs.getDouble("autoBackAngle1", 145); // degrees
           autoBackRotation = autoPrefs.getDouble("autoBackRotation", 0.3); // power

           winch.winchInSpeed = autoPrefs.getDouble("winchInSpeed", -0.3); // power
           winch.winchOutSpeed = autoPrefs.getDouble("winchOutSpeed", 0.7); // power
       }
        //deck.moveToTop();
    }

    // called periodically during disabled
    public void disabledPeriodic() {
       deck.handler();
   }

    public void teleopInit() {
        driveTrain.teleopInit();
        launcher.teleopInit();
        deck.teleopInit();
        winch.teleopInit();
        shooter.setTargetRPM(0); 
    }

    public void teleopPeriodic() {
        driveTrain.handler();
        deck.handler();
        shooter.handler();
        launcher.handler();
        winch.handler();

        displayHandler();

        driveTrain.ui();
        deck.ui();
        shooter.ui();
        launcher.ui();
        winch.ui(); 
        
        // automated shooter/deck action
        if(launcher.emptyFlag){
            // empty - stop/lower
            shooter.setTargetRPM(0);
            deck.moveToBottom();
            launcher.emptyFlag = false;
        }
        if(launcher.fullFlag){
            // full - start/raise
            shooter.setTargetRPM(1600);
            deck.moveToAngle(35);
            launcher.fullFlag = false;
        }
        if(rightStick.getRawButton(11)){
            shooter.setTargetRPM(0);
        }
        
        // make sure fresbee detector is only active when at dock
        if(driveTrain.obstacleDistance < 20){
            // near the wall
            launcher.fresbeeDetectorActive = true;
        }
        else {
            // free - avoid detection of tower!
            launcher.fresbeeDetectorActive = false;
        }
    }

    public void autonomousInit() {
        driveTrain.drive.setSafetyEnabled(false);


      //  launcher.autonomousInit();
        autoTimer.start();
        autoState = 0;
        shotCounter = autoShots;
        // deck.autonomousInit();
    }

    public void autonomousPeriodic() {
        driveTrain.handler();
        deck.handler();
        shooter.handler();
        launcher.handler();
        winch.handler();
        displayHandler();

        //handle sequencing 
        double t;
        t = autoTimer.get();

        switch (autoState) {
            case 0: // raise deck, spin up
                deck.moveToTop();
                shooter.setTargetRPM(autoShotRPM);
                autoState++;
                break;

            case 1: // check if deck up, go to target angle
                if (deck.isAtTop()) {
                    deck.moveToAngle(autoShotAngle);
                    autoState++;
                }
                break;

            case 2: // wait until deck at level
                if (deck.isAtTargetAngle()) {
                    autoState++;
                }
                break;

            case 3: // make sure some min. time has transpired
                if (t >= 1) {
                    autoState++;
                    autoTimer.reset();
                    shotFired = false;
                }
                break;

            case 4: // shoot every 2 seconds, 3 times
                if (shotCounter > 0) {
                    if(!shotFired){
                        launcher.fire();
                        shotFired = true;
                    }
                    if (t >= autoShotDelay) {
                        autoTimer.reset();
                        shotCounter--;
                        shotFired = false;
                    }
                } else {
                    autoState++;
                    shooter.setTargetRPM(0);
                    deck.moveToBottom();
                }
                break;

            case 5: // back out
                // drive backwards slowly
                // tell the robot which way it's heading (0 being the field axis away from driver station)
                //(so we back out straight towards the driver station!)
                driveTrain.gyroSet(autoBackHeading);  
               
                autoTimer.reset();
                autoState++;
                break;

            case 6:
                // back out and turn for autoBackTime1 seconds
                if (t < autoBackTime1) {
                    // yes - keep calling this so gyro angle is calculated properly!
                    
                    if(driveTrain.gyroAngle < autoBackAngle1) {
                        driveTrain.drive(0, autoBackSpeed, autoBackRotation);
                    }
                    else {
                        driveTrain.drive(0, autoBackSpeed, 0);
                    }
                }
                else {
                    autoTimer.reset();
                    autoState++;
                }
                break;
                
            case 7:
                // keep driving with sonar obstacle avoidance
                if (t < autoBackTime2) {
                    // yes - keep calling this so gyro angle is calculated properly!
                    
                    // turn around while backing out
                    double slide = 0;
                    double speed = -autoBackSpeed;
                    
                    if(driveTrain.obstacleDistance > 40){
                        // keep going straight
                        slide = 0;
                    }
                    else if(driveTrain.obstacleDistance  > 24){
                        // slide over gently
                        slide = (driveTrain.obstacleDistance > 0)?(-0.2):0.2;
                    }
                    if(driveTrain.obstacleDistance  > 12){
                        // slide over aggressively
                        slide = (driveTrain.obstacleDistance > 0)?(-0.4):0.4;
                    }
                    else {
                        // within a foot - stop
                        speed = 0;
                    }
                    // reset gyro (to keep driving straight!)
                    driveTrain.gyroSet(0); 
                    driveTrain.drive(slide, speed, 0);
                }
                else {
                    // stop
                    driveTrain.drive(0, 0, 0);
                    autoState++;
                }
                break;
                
            default:
                break;
        
        }
       
        
    }

    public void displayHandler() {
        SmartDashboard.putNumber("ActualRPM", shooter.shooterRPM);
        SmartDashboard.putNumber("ActualPower", shooter.actualPower);
        SmartDashboard.putNumber("Angle Encoder(Degrees", deck.deckAngle);
        
        SmartDashboard.putNumber("RawEncoder", deck.angleEncoder.getRaw());
        
        
        SmartDashboard.putNumber("Sonar Distance", driveTrain.sonarDistance);
        SmartDashboard.putNumber("Odometer", driveTrain.distanceCounter.get());
        SmartDashboard.putNumber("RawEncoder", deck.angleEncoder.getDistance());
        SmartDashboard.putNumber("TargetRPM", shooter.targetRPM);       
        SmartDashboard.putNumber("Period", shooter.shooterCounter.getPeriod());       
        SmartDashboard.putNumber("State", autoState);       
        SmartDashboard.putNumber("AutoTimer", autoTimer.get());  
        SmartDashboard.putNumber("Gyro Angle", driveTrain.gyroAngle);
        SmartDashboard.putNumber("LeftSonar", driveTrain.leftSonar.getRangeInches());
        SmartDashboard.putNumber("RightSonar", driveTrain.rightSonar.getRangeInches()); 
        SmartDashboard.putBoolean("GamePadButton 11", gamepad.getRawButton(11));
        SmartDashboard.putBoolean("GamePadButton 12", gamepad.getRawButton(12)); 
        
    }
}
