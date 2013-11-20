/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Talon;

/**
 *
 * @author Robotics
 */
public class Winch {

    private Talon w1;
    private Talon w2;
    private Encoder rightWinchEncoder;
    private Encoder leftWinchEncoder;
    
    boolean winchActive;    // safety flag
    
    int 
            leftWinchOutButton, leftWinchInButton,
            rightWinchOutButton, rightWinchInButton,
            winchActivateButton1, winchActivateButton2;
    
    double winchInSpeed, winchOutSpeed;
    
    Joystick gamepad;

    public void init() {
       rightWinchEncoder = new Encoder(1, 12, 1, 13);
        leftWinchEncoder = new Encoder(1, 10, 1, 11);
        w1 = new Talon(5); //pwm number
        w2 = new Talon(7); //pwm number
        rightWinchEncoder.start();
        
        leftWinchEncoder.start();
        
        winchActive = false;
    }
    
    public void teleopInit()
    {
        winchActive = false;
    }

    public void handler() {
        // check safety!
        if(!winchActive) return;
    }

    public void ui() {
        // activation (safety)
        if(gamepad.getRawButton(winchActivateButton1) && gamepad.getRawButton(winchActivateButton1)){
            winchActive = true;
        }
        // check safety!
        if(!winchActive) return;
        
     
        if(gamepad.getRawButton(leftWinchOutButton)) {
            w1.set(winchOutSpeed);
        } else if(gamepad.getRawButton(leftWinchInButton)) {
            w1.set(winchInSpeed);
        } else {
            w1.stopMotor();
        }

        if(gamepad.getRawButton(rightWinchOutButton)) {
            w2.set(winchOutSpeed);
        } else if(gamepad.getRawButton(rightWinchInButton)) {
            w2.set(winchInSpeed);
        } else {
            w2.stopMotor();
        }
    }
}
