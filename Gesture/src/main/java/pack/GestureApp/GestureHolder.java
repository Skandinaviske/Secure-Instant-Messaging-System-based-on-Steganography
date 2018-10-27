package pack.GestureApp;

import android.gesture.Gesture;

class GestureHolder {
    private Gesture gesture;
    private String gestureNaam;

    GestureHolder(Gesture gesture, String naam){
        this.gesture = gesture;
        this.gestureNaam = naam;
    }

    Gesture getGesture(){
        return gesture;
    }

    public void setGesture(Gesture gesture){
        this.gesture = gesture;
    }

    String getNaam(){
        return gestureNaam;
    }

    public void setNaam(String naam){
        this.gestureNaam = naam;
    }

}
