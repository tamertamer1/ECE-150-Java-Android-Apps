package edu.ucsb.ece150.locationplus;
import java.io.Serializable;

/*
 * This class is provided as a way for you to store information about a single satellite. It can
 * be helpful if you would like to maintain the list of satellites using an ArrayList (i.e.
 * ArrayList<Satellite>). As in Homework 3, you can then use an Adapter to update the list easily.
 *
 * You are not required to implement this if you want to handle satellite information in using
 * another method.
 */
public class Satellite implements Serializable{
    private String name;
    private double azimuth;
    private double elevation;
    private double carrierFrequency;
    private double carrierNoiseDensity;
    private String constellationName;
    private int svid;

    private boolean isUsedInFix;
    private static int counter = 0;
    private int satelliteNumber;
    // Constructor
    public Satellite(String name, double azimuth, double elevation, double carrierFrequency,
                     double carrierNoiseDensity, String constellationName, int svid, boolean isUsedInFix) {
        this.name = name;
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.carrierFrequency = carrierFrequency;
        this.carrierNoiseDensity = carrierNoiseDensity;
        this.constellationName = constellationName;
        this.svid = svid;
        this.isUsedInFix = isUsedInFix;
        satelliteNumber = ++counter;
    }
    public static void resetCounter() {
        counter = 0;
    }
    // Getter methods
    public String getName() { return name; }
    public boolean isUsedInFix() {
        return isUsedInFix;
    }
    public double getAzimuth() { return azimuth; }
    public double getElevation() { return elevation; }
    public double getCarrierFrequency() { return carrierFrequency; }
    public double getCarrierNoiseDensity() { return carrierNoiseDensity; }
    public String getConstellationName() { return constellationName; }
    public int getSvid() { return svid; }
    public int getSatelliteNumber() {return satelliteNumber;}

    // toString method used for ArrayAdapter in ListView
    @Override
    public String toString() {
        return "Satellite " + satelliteNumber; // This will be the text representation in the ListView
    }
}
