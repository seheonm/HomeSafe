// CS 460 Team 01

import java.util.ArrayList;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class SafeController {

    private SafeState currentState;
    private Screen screen;
    private PINManager pinManager;
    private SafeGUI safeGUI;
    private List<User> users = new ArrayList<>();
    private User currentUser = null;


    public SafeController(Screen screen, SafeGUI safeGUI) {
        this.screen = screen;
        this.safeGUI = safeGUI;
        currentState = SafeState.OFF;
    }


    public void setPINManager(PINManager pinManager) {
        this.pinManager = pinManager;
    }


    public void setState(SafeState newState) {
        currentState = newState;
        System.out.println("Current state: " + currentState);
        switch (currentState) {
            case WAITING_FOR_IRIS -> handleWaitingForIris();
            case SETTING_IRIS -> handleSettingIris();
            case INITIAL_PIN_SETUP -> handleInitialPinSetup();
            case NORMAL -> handleNormalState();
            case CLOSED -> handleCloseSafe();
            case ADD_NEW_USER -> handleAddNewUser();
            case LOCKED -> handleLockedState();
            case MASTER_VERIFICATION -> handleMasterVerification();
        }

    }

    private void handleMasterVerification() {
        screen.displayMessage("Enter Master PIN");
    }

    private void handleWaitingForIris() {
        screen.displayMessage("Waiting on Iris scan");
        // Additional setup logic here
    }
    private void handleSettingIris() {
        screen.displayMessage("Scan your Iris");
        // Additional setup logic here
    }
    
    private void handleInitialPinSetup() {
        screen.displayMessage("Enter Set up PIN");
        // Additional setup logic here
    }

    private void handleNormalState() {
        screen.displayMessage("Enter PIN");
        // Normal state logic here
    }

    private void handleAddNewUser() {
        screen.displayMessage("Add User");
        // Logic to add new user here
    }

    private void handleUnlockedState() {
        screen.displayMessage("Safe Unlocked");
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> safeGUI.openSafe());
        delay.play();
    }

    public void handleCloseSafe() {
        setState(SafeState.NORMAL);
    }


    private void handleLockedState() {
        screen.displayMessage("Safe Locked");
    }

    public SafeState getCurrentState() {
        return currentState;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void checkPIN(String enteredPIN) {
        if (currentState == SafeState.INITIAL_PIN_SETUP) {
            if ("00000".equals(enteredPIN)) {
                screen.displayMessage("Enter Master PIN");
                setState(SafeState.SETTING_NEW_PIN);
            } else {
                screen.displayMessage("Wrong Setup PIN. Try again.");
            }
        } else if (currentState == SafeState.SETTING_NEW_PIN) {
            if("00000".equals(enteredPIN)){
                screen.displayMessage("Cannot use setup pin");
            }
            else if (pinExists(enteredPIN)) {
                screen.displayMessage("PIN already in use. Choose a different PIN.");
            }
            else {
                screen.displayMessage("Scan Your Iris");
                currentUser = new User(enteredPIN, null); // Save the entered PIN temporarily
                setState(SafeState.SETTING_IRIS); // New state to wait for the iris scan
            }
        }
        else if (currentState == SafeState.NORMAL) {
            if ("00000".equals(enteredPIN)) {
                // If user enters setup PIN during NORMAL state, prompt to set up a new account.
                setState(SafeState.MASTER_VERIFICATION);
                return; // Exit the method to prevent further checks
            }

            boolean pinMatchFound = false;

            for (User user : users) {
                if (user.getPin() != null && user.getPin().equals(enteredPIN)) {
                    currentUser = user;
                    pinMatchFound = true;
                    screen.displayMessage("Scan Your Iris");
                    setState(SafeState.WAITING_FOR_IRIS);
                    break;  // Exit the loop once a match is found
                }
            }

            if (!pinMatchFound) {
                screen.displayMessage("Wrong PIN");
            }
        }

        else if (currentState == SafeState.MASTER_VERIFICATION) {

            User masterUser = users.get(0);  // Assuming 'users' is a list of all users.

            //pinInput.equals(masterUser.getPin()) && irisInput.equals(masterUser.getIris())

            if (enteredPIN.equals(masterUser.getPin())) {
                screen.displayMessage("Enter New PIN");
                setState(SafeState.SETTING_NEW_PIN);
            }

            else {
                // Notify that the verification failed.
                screen.displayMessage("Master user verification failed!");
                setState(SafeState.NORMAL);
            }

        }

    }

    public void resetUser(){
        currentUser = null;
    }

    public void checkIris(String irisName) {
        if (currentState == SafeState.WAITING_FOR_IRIS && currentUser != null) {
            if (currentUser.getIrisName().equals(irisName)) {
                handleUnlockedState();
            } else {
                screen.displayMessage("Wrong Iris");
                setState(SafeState.NORMAL);
            }
        }
    }

    public void setIrisForCurrentUser(String irisName) {
        if(currentUser != null) {
            currentUser.setIrisName(irisName);
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean usersIsEmpty() {
        return users.isEmpty();
    }

    // Helper method to check if a PIN already exists
    private boolean pinExists(String pin) {
        for (User user : users) {
            if (pin.equals(user.getPin())) {
                return true;
            }
        }
        return false;
    }

    // Helper method to check if an iris scan already exists
    boolean irisScanExists(String irisName) {
        for (User user : users) {
            if (irisName.equals(user.getIrisName())) {
                return true;
            }
        }
        return false;
    }

    public Screen getScreen() {
        return this.screen;
    }


}
