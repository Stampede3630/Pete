/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Talon;

/**
 *
 * @author Robotics deck controls:
 */
public class Deck {

    Joystick joystick;
    int raiseDeckButton,
            lowerDeckButton,
            shootingPositionButton,
            deckTopButton, 
            deckBottomButton; 
    Talon angulator;
    double lastDeckAngle;
    DigitalInput lowerLimit;
    DigitalInput upperLimit;
    Encoder angleEncoder;
    private boolean moveUp,
            moveDown,
            deckTopRequest,
            deckBottomRequest;
    private boolean offsetSet;
    public double deckAngle,
            deckAngleOffset,
            targetAngle;
    private static final double deckUpAngle = 61;
    private static final double deckDownAngle = -28;
    private static final double shootingPositionAngle = -4;
    // private static final double towerPositionAngle = 23.2;

    public void init() {
        deckAngle = 0;
        deckAngleOffset = 0;
        targetAngle = 0; 
        offsetSet = false;

        angulator = new Talon(2, 1);
        lowerLimit = new DigitalInput(2, 2);
        upperLimit = new DigitalInput(2, 1);
        angleEncoder = new Encoder(2, 3, 2, 4, false, Encoder.EncodingType.k4X);
        angleEncoder.start();
        angleEncoder.setDistancePerPulse(-41.0 / 26.0 * 4);
    }

    public void teleopInit(){
        targetAngle = 0; 
    }
    
    public void autonomousInit(){
        targetAngle = 0; 
         offsetSet = false;
    }
    
    public void handler() {

        //handle angle reading 
        if (!lowerLimit.get()) {
            // deckAngleOffset = deckDownAngle - angleEncoder.getDistance();
        } else if (!upperLimit.get() && !offsetSet) {
            // tripped top switch for the first time - record angle offset
            offsetSet = true; 
            deckAngleOffset = deckUpAngle - angleEncoder.getDistance();
        }
        deckAngle = angleEncoder.getDistance() + deckAngleOffset;

        double angulatorPower;

        // firingPosition = 

        // move up?
        if (moveUp || deckTopRequest) {
            if (upperLimit.get()) {
                angulatorPower = 1;
            } else {
                angulatorPower = 0;
                deckTopRequest = false;
            }
        } else if (moveDown || deckBottomRequest) {
            if (lowerLimit.get()) {
                angulatorPower = -1;
            } else {
                angulatorPower = 0;
                deckBottomRequest = false;
            }
        } else if (targetAngle != 0) {
            if (deckAngle > (targetAngle + 1)) {
                // move down
                angulatorPower = -1;
            } else if (deckAngle < (targetAngle - 1)) {
                // move up
                angulatorPower = 1;
            } else {
                // target angle reached - stop following!
                angulatorPower = 0;
                targetAngle = 0;
            }
        } else {
            angulatorPower = 0;
        }
        angulator.set(angulatorPower);
    }

    public void ui() {
        moveUp = joystick.getRawButton(raiseDeckButton);
        moveDown = joystick.getRawButton(lowerDeckButton);
        if (joystick.getRawButton(shootingPositionButton)) {
            targetAngle = shootingPositionAngle;
        }

        if(joystick.getRawButton(deckTopButton)){ 
            deckTopRequest = true; 
        }
        
        if(joystick.getRawButton(deckBottomButton)){ 
            deckBottomRequest = true; 
        }
            
        // move down wins
        if (moveDown) {
            moveUp = false;
        }
        // any manual request overrides top/down requests
        if (moveUp || moveDown) {
            deckTopRequest = false;
            deckBottomRequest = false;
            targetAngle = 0;
        }
        
    }

    boolean isAtTop() {
        return !upperLimit.get();
    }

    boolean isAtTargetAngle() {
        return ((Math.abs(targetAngle - deckAngle) <= 1) || targetAngle == 0);
        
    }

    void moveToTop() {
        deckTopRequest = true;
        deckBottomRequest = false; 
        targetAngle = 0; 
    }

    void moveToBottom() {
        deckBottomRequest = true;
        deckTopRequest = false; 
        targetAngle = 0; 
    }

    void moveToAngle(double a) {
        targetAngle = a;
    }
}
