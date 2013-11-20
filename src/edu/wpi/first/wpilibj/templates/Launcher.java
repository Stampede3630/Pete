/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;

/**
 *
 * @author Robotics
 */
public class Launcher {

    Joystick joystick;
    Talon hopper;
    Timer displayTimer;
    Servo pusher, tapper;
    Relay LightRelay;
    double lastRPM;
    boolean firingPosition;
    DigitalInput fresbeeSensor;
    DigitalInput mixerSensor;
    boolean shoot, load, trigger;
    boolean goodFeed;
    // launcher support variables
    boolean[] launcherSlots;   // 0 = loading slot, 3 = chamber, false = empty
    boolean launcherTurning;
    boolean launcherSettling;
    boolean launcherLoading;
    boolean launcherShooting;
    boolean launcherPausing;
    boolean launcherPastMark;
    boolean discInFeeder;
    boolean fresbeeDetected;
    boolean goodShot;
    boolean fresbeeDetectorActive;
    boolean fireRequest;    // firing requested by autonomous
    Timer shotTimer, turnTimer, settlingTimer, feederTimer, autoTimer;
    int fireButton, safetyButton, rePushButton, reTapButton, reShootButton;

    // loading event trigger flags
    boolean emptyFlag, fullFlag;

    public void init() {
        hopper = new Talon(8);
        pusher = new Servo(9);
        tapper = new Servo(10);
        pusherOut();
        tapperUp();
        fresbeeDetectorActive = true;
        fresbeeSensor = new DigitalInput(7);
        mixerSensor = new DigitalInput(6);
        LightRelay = new Relay(2, 1, Relay.Direction.kForward);

        // initial launcher configuration
        launcherSlots = new boolean[4];
        launcherSlots[0] = false;
        launcherSlots[1] = true;
        launcherSlots[2] = true;
        launcherSlots[3] = true;
        launcherTurning = false;
        launcherSettling = false;
        launcherShooting = false;
        launcherLoading = false;
        launcherPausing = false;
        launcherPastMark = false;
        discInFeeder = false;
        fresbeeDetected = false;
        turnTimer = new Timer();
        settlingTimer = new Timer();
        shotTimer = new Timer();
        feederTimer = new Timer();
        autoTimer = new Timer();

        shoot = false;
        load = false;
        trigger = false;
        fireRequest = false;
        

    }

    public void handler() {
        // handle user interface
        if((joystick.getRawButton(fireButton) || fireRequest) && !(launcherShooting || launcherLoading || launcherPausing)) {
            launcherLoading = true;
            launcherShooting = true;
            fireRequest = false;
        } else if((joystick.getRawButton(reShootButton)) && !(launcherShooting || launcherLoading || launcherPausing)) {
            // override the slot array - preted one is about to be loaded
            launcherSlots[0] = false;
            launcherSlots[1] = false;
            launcherSlots[2] = true;
            launcherSlots[3] = false;
            
            // shoot as if fire button pushed
            launcherLoading = true;
            launcherShooting = true;
            fireRequest = false;
        }


        // handle disc detector
        if(fresbeeSensor.get()) {
            // make sure this is a real signal, not tower!
            if(fresbeeDetectorActive){
                if (!fresbeeDetected) {
                    fresbeeDetected = true;
                    feederTimer.reset();
                    feederTimer.start();
                    // 4th frisbee detection
                    if(getDiscCount() == 3){
                        fullFlag = true;
                    }
                } else {
                    if (!discInFeeder && feederTimer.get() > 0.25) {
                        feederTimer.stop();
                        feederTimer.reset();
                        discInFeeder = true;
                    }
                }
            }
        } else {
            discInFeeder = false;
            fresbeeDetected = false;
        }

        // mark feeder slot as occupied
        if (discInFeeder) {
            launcherSlots[0] = true;
        }

        // turn if chamber empty or slot 2 empty
        if (launcherSlots[0] && (!launcherSlots[3] || !launcherSlots[2]) && !launcherTurning && !launcherSettling && !launcherLoading && !launcherShooting) {
            // if(gamepad.getRawButton(10)){
            startTurning(); // DO NOT JUST SET launcherTurning to true
        }

        // now handle the launcher state machine
        if (launcherTurning) {
            if (turnTimer.get() < 0.10) {
                launcherPastMark = false;
                // lift tapper servo
                tapperUp();
            } else if (turnTimer.get() < 1.0) {
                // start turning
                hopper.set(0.65);
                turnTimer.stop();
            } else if (turnTimer.get() < 3.0) {
            }

            if (!launcherPastMark) {
                // spinning - wait to hit the mark...
                if (!mixerSensor.get()) {
                    launcherPastMark = true;
                }
            } else {
                // stop when past the mark
                if (mixerSensor.get()) {
                    int i;
                    hopper.set(0.0);
                    launcherTurning = false;
                    launcherSettling = true;
                    settlingTimer.reset();
                    settlingTimer.start();
                    // lower tapper servo
                    tapperDown();

                    // done turning - shift slots
                    // note: we do NOT shift emptiness into slot 3, only a fresbee (if there was one in slot 2!)
                    // chamber can only be loaded, gets emptied via a shot
                    if (launcherSlots[2]) {
                        launcherSlots[3] = true;
                    }
                    launcherSlots[2] = launcherSlots[1];
                    launcherSlots[1] = launcherSlots[0];
                    launcherSlots[0] = false;   // feeder slot
                }
            }
        } else if (launcherSettling) {
            if (settlingTimer.get() > 0.3) {
                settlingTimer.stop();
                launcherSettling = false;
            }
        } else if (launcherLoading) {
            // turn if disc available but not in the chamber
            if ((!launcherSlots[3]) && (launcherSlots[2] || launcherSlots[1] || launcherSlots[0])) {
                startTurning();
            } else {
                // loading done
                launcherLoading = false;
                // check if load successful - if not cancel a shoot
                if ((!launcherSlots[3]) && launcherShooting) {
                    launcherShooting = false;
                }
            }
        } else if (launcherShooting) {
            // set the servo
            pusherIn();

            // start the shotTimer
            shotTimer.reset();
            shotTimer.start();
            launcherShooting = false;
            launcherPausing = true;
        } else if (launcherPausing) {
            // may do some more here to move the second shooter servo
            if (shotTimer.get() > 0.6) {
                // shot complete
                shotTimer.stop();
                launcherPausing = false;
                launcherSlots[3] = false;    // disc out!
                pusherOut();
                // load next - remove this if we don't want to load right after shot!
                launcherLoading = true;
                if(getDiscCount() == 0){
                    emptyFlag = true;
                }
            }
        } else {
            // idle - handle pusher-tapper overrides, otherwise just withdraw
            if(joystick.getRawButton(reTapButton)){
                tapperUp();
            }
            else {
                tapperDown();
            }
            if(joystick.getRawButton(rePushButton)){
                pusherIn();
            }
            else {
                pusherOut();
            }
        }
    }

    public void ui() {
        
        
    }

    public void autonomousInit() {
        launcherSlots[0] = false;
        launcherSlots[1] = true;
        launcherSlots[2] = true;
        launcherSlots[3] = true;

        launcherTurning = false;
        launcherSettling = false;
        launcherShooting = false;
        launcherLoading = false;
        launcherPausing = false;
        launcherPastMark = false;
        discInFeeder = launcherSlots[0];
        fresbeeDetected = false;
        LightRelay.set(Relay.Value.kOn);
        
        pusherOut();
        tapperDown();
    }

    public void teleopInit() {
        launcherSlots[0] = false;
        launcherSlots[1] = false;
        launcherSlots[2] = false;
        launcherSlots[3] = false;
        
        emptyFlag = false;
        fullFlag = false;

        launcherTurning = false;
        launcherSettling = false;
        launcherShooting = false;
        launcherLoading = false;
        launcherPausing = false;
        launcherPastMark = false;
        discInFeeder = launcherSlots[0];
        fresbeeDetected = false;
        LightRelay.set(Relay.Value.kOn);
        pusherOut();
        tapperDown();
    }

    public void startTurning() {
        turnTimer.reset();
        turnTimer.start();
        launcherTurning = true;
    }

    private void tapperUp() {
        tapper.set(1.0);
    }

    private void tapperDown() {
        tapper.set(0.55);
    }

    private void pusherOut() {
        pusher.set(0.0);
    }

    private void pusherIn() {
        pusher.set(0.4);
    }

    public void rePush() {
        if (joystick.getRawButton(rePushButton)) {
            pusherIn();
        } 
        else {
        }

    }

    public void reTap() {
    }

    public void fire(){
        fireRequest = true;
    }

    public int getDiscCount(){
        return (((launcherSlots[0])?1:0) + ((launcherSlots[1])?1:0) + ((launcherSlots[2])?1:0) + ((launcherSlots[3])?1:0));
    }
}