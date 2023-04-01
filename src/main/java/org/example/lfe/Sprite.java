package org.example.lfe;

public class Sprite {

    //ANIMATION
    byte[] image;
    int frame;
    int wait;
    int timerWait;
    int dx;
    int dy;
    int dz;
    String sound;
    int mpCost;
    int nextId;
    int nextFrame;
    int nextMpCost;

    //COMMANDS
    int ja;
    int jj;
    int daf;
    int dau;
    int dad;
    int djf;
    int dju;
    int djd;
    int dja;

    //3D VOLUME
    ObjectBodyBox[] bodies = new ObjectBodyBox[4];
    ObjectInteractionBox[] interactions = new ObjectInteractionBox[4];
    ObjectCreationPoint[] creations = new ObjectCreationPoint[4];

    //2D STICKS
    ObjectStickPoint prey;
    ObjectStickPoint item;
    ObjectStickPoint blood;
    //org.example.lfe.ObjectStickPoint[] sticks = new org.example.lfe.ObjectStickPoint[2]; TODO: implementar

    //GET TOTAL BODY VOLUME
    public ObjectBodyBox overallBody() {
        int xMin = Integer.MAX_VALUE;
        int yMin = Integer.MAX_VALUE;
        int wMax = Integer.MIN_VALUE;
        int hMax = Integer.MIN_VALUE;
        int zMin = Integer.MAX_VALUE;
        int zMax = Integer.MIN_VALUE;
        for(ObjectBodyBox body : bodies) {
            xMin = Math.min(xMin, body.getX());
            yMin = Math.min(yMin, body.getY());
            wMax = Math.max(wMax, body.getW());
            hMax = Math.max(hMax, body.getH());
            zMin = Math.min(zMin, body.getZ1());
            zMax = Math.max(zMax, body.getZ2());
        }
        return ObjectBodyBox.builder()
            .x(xMin)
            .y(yMin)
            .w(wMax)
            .h(hMax)
            .z1(zMin)
            .z2(zMax)
            .build();
    }

}
