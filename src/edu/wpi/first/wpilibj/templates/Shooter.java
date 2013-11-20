/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;

/**
 *
 * @author Robotics
 */
public class Shooter {

    DigitalInput shooterWheelSensor;
    Counter shooterCounter;
    double targetRPM;
    double predictedPower;
    double actualPower;
    boolean lastIncreaseButton;
    boolean lastDecreaseButton;
    double shooterRPM;
    double RPMError;
    Timer shooterTimer;
    Joystick joystick;
    Talon shooterTalon;
    int increaseRPMButton,
            decreaseRPMButton,
            offButton, 
            shootingRPMButton, 
            towerShootingRPMButton; 
   
    public void init() {
        shooterWheelSensor = new DigitalInput(5);
        shooterCounter = new Counter(shooterWheelSensor);
        shooterCounter.setMaxPeriod(1.0); // min - 60 RPM (period = 1 sec)
        shooterCounter.setUpSourceEdge(false, true);
        shooterCounter.start();
        targetRPM = 0;
        actualPower = 0; 
        shooterTimer = new Timer();
        shooterTimer.start();
        shooterTalon = new Talon(6);
    }

    public void handler() {

        if(shooterCounter.getStopped()){
            shooterRPM = 0;
        }
        else {
            double p = shooterCounter.getPeriod();
            // filter out occasional glitches
            if(p > 0.010){
                shooterRPM = 60 / p;
            }
        }
        
        predictedPower = targetRPM * (1.0 / 2000.0);    // shold be too low!
        RPMError = targetRPM - shooterRPM;
        double powerCorrection = 0;
        if(shooterRPM < targetRPM){
            powerCorrection = 0.4;
        }
        else {
            powerCorrection = 0;
        }
        // powerCorrection = (RPMError * (1.0 / 1700.0)) * 5;
        actualPower = predictedPower + powerCorrection;
        if(actualPower > 1) {
            actualPower = 1;
        }

        if(targetRPM > 0){
            shooterTalon.set(actualPower); 
        }
        else {
            shooterTalon.set(0); 
        }
        // shooterTalon.set(predictedPower); 
    }

    public void ui() {
        boolean button;

        // handle speed increase button
        button = joystick.getRawButton(increaseRPMButton);
        if (button) {
            // push detection
            if (lastIncreaseButton == false) {
                targetRPM = targetRPM + 100;
                if (targetRPM > 1800) {
                    targetRPM = 1800;
                }
            }
        }
        lastIncreaseButton = button;

        // handle speed decrease button
        button = joystick.getRawButton(decreaseRPMButton);
        if (button) {
            // push detection
            if (lastDecreaseButton == false) {
                targetRPM = targetRPM - 100;
                if (targetRPM < 0) {
                    targetRPM = 0;
                }
            }
        }
        lastDecreaseButton = button;
        
        if(joystick.getRawButton(offButton)){
            targetRPM = 0; 
        }
        if(joystick.getRawButton(shootingRPMButton)){
            targetRPM = 1800; 
        }
    }

    public void setTargetRPM(double tRPM) {
        targetRPM = tRPM;
    }
}
