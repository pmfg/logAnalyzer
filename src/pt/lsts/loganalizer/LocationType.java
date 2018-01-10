/*
 * Copyright (c) 2004-2018 Universidade do Porto - Faculdade de Engenharia
 * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
 * All rights reserved.
 * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
 *
 * This file is part of Neptus, Command and Control Framework.
 *
 * Commercial Licence Usage
 * Licencees holding valid commercial Neptus licences may use this file
 * in accordance with the commercial licence agreement provided with the
 * Software or, alternatively, in accordance with the terms contained in a
 * written agreement between you and Universidade do Porto. For licensing
 * terms, conditions, and further information contact lsts@fe.up.pt.
 *
 * Modified European Union Public Licence - EUPL v.1.1 Usage
 * Alternatively, this file may be used under the terms of the Modified EUPL,
 * Version 1.1 only (the "Licence"), appearing in the file LICENCE.md
 * included in the packaging of this file. You may not use this work
 * except in compliance with the Licence. Unless required by applicable
 * law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific
 * language governing permissions and limitations at
 * https://github.com/LSTS/neptus/blob/develop/LICENSE.md
 * and http://ec.europa.eu/idabc/eupl.html.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: Paulo Dias, Ze Pinto
 * 2005/03/05
 */

package pt.lsts.loganalizer;

public class LocationType {

    private double depth = 0;
    protected double latitudeRads = 0;
    protected double longitudeRads = 0;
    public static final LocationType ABSOLUTE_ZERO = new LocationType();
    // offsets are in meters (m)
    private boolean isOffsetNorthUsed = true;
    private double offsetNorth = 0;
    private boolean isOffsetEastUsed = true;
    private double offsetEast = 0;
    private boolean isOffsetUpUsed = true;
    private double offsetDown = 0;
    protected double offsetDistance = 0;
    protected double azimuth = 0;
    protected double zenith = 90;

    public LocationType(LocationType anotherLocation) {
        super();
        setLocation(anotherLocation);
    }

    public LocationType() {
        super();
    }

    public double getHorizontalDistanceInMeters(LocationType anotherLocation) {
        double[] offsets = getOffsetFrom(anotherLocation);
        double sum = offsets[0] * offsets[0] + offsets[1] * offsets[1];
        return Math.sqrt(sum);
    }

    public double getDistanceInMeters(LocationType anotherLocation) {
        // NeptusLog.pub().info("<###>distance in meters from "+getLatitude()+","+getLongitude()+" to
        // "+anotherLocation.getLatitude()+","+getLongitude());
        double[] offsets = getOffsetFrom(anotherLocation);
        double sum = offsets[0] * offsets[0] + offsets[1] * offsets[1] + offsets[2] * offsets[2];
        return Math.sqrt(sum);
    }

    public double[] getOffsetFrom(LocationType otherLocation) {
        return CoordinateUtil.WGS84displacement(otherLocation, this);
    }

    /**
     * Translate this location by the offsets.
     * 
     * @param offsetNorth
     * @param offsetEast
     * @param offsetDown
     * @return This location.
     */
    @SuppressWarnings("unchecked")
    public <L extends LocationType> L translatePosition(double offsetNorth, double offsetEast, double offsetDown) {

        setOffsetNorth(getOffsetNorth() + offsetNorth);
        setOffsetEast(getOffsetEast() + offsetEast);
        setOffsetDown(getOffsetDown() + offsetDown);

        return (L) this;
    }

    public void setOffsetDown(double offsetDown) {
        this.offsetDown = offsetDown;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public void setLatitudeRads(double latitudeRads) {
        this.latitudeRads = latitudeRads;
    }

    public void setLongitudeRads(double longitudeRads) {
        this.longitudeRads = longitudeRads;
    }

    /**
     * Converts this Location to absolute (Lat/Lon/Depth without offsets).
     * 
     * @return The Location itself.
     */
    @SuppressWarnings("unchecked")
    public <L extends LocationType> L convertToAbsoluteLatLonDepth() {
        if (offsetNorth == 0 && offsetEast == 0 && offsetDown == 0 && offsetDistance == 0) {
            return (L) this;
        }

        double latlondepth[] = getAbsoluteLatLonDepth();

        setLocation(ABSOLUTE_ZERO);
        setLatitudeDegs(latlondepth[0]);
        setLongitudeDegs(latlondepth[1]);
        setDepth(latlondepth[2]);

        return (L) this;
    }

    public void setLongitudeDegs(double longitude) {
        setLongitudeRads(Math.toRadians(longitude));
    }

    public void setLatitudeDegs(double latitude) {
        setLatitudeRads(Math.toRadians(latitude));
    }

    public double[] getAbsoluteLatLonDepth() {
        double[] totalLatLonDepth = new double[] { 0d, 0d, 0d };
        totalLatLonDepth[0] = getLatitudeDegs();
        totalLatLonDepth[1] = getLongitudeDegs();
        totalLatLonDepth[2] = getDepth();

        double[] tmpDouble = CoordinateUtil.sphericalToCartesianCoordinates(getOffsetDistance(), getAzimuth(),
                getZenith());
        double north = getOffsetNorth() + tmpDouble[0];
        double east = getOffsetEast() + tmpDouble[1];
        double down = getOffsetDown() + tmpDouble[2];

        if (north != 0.0 || east != 0.0 || down != 0.0)
            return CoordinateUtil.WGS84displace(totalLatLonDepth[0], totalLatLonDepth[1], totalLatLonDepth[2], north,
                    east, down);
        else
            return totalLatLonDepth;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public double getZenith() {
        return zenith;
    }

    public double getOffsetDistance() {
        return offsetDistance;
    }

    public double getLatitudeRads() {
        return latitudeRads;
    }

    public double getLongitudeRads() {
        return longitudeRads;
    }

    public double getDepth() {
        if (depth == 0)
            return 0;
        return depth;
    }

    public double getOffsetEast() {
        return offsetEast;
    }

    public double getOffsetNorth() {
        return offsetNorth;
    }

    public void setOffsetEast(double offsetEast) {
        this.offsetEast = offsetEast;
    }

    public void setOffsetNorth(double offsetNorth) {
        this.offsetNorth = offsetNorth;
    }

    public double getOffsetDown() {
        return offsetDown;
    }

    /**
     * Copies the given location to this one. (Does not link them together.)
     * 
     * @param anotherPoint
     */
    public void setLocation(LocationType anotherPoint) {
        if (anotherPoint == null)
            return;

        this.setLatitudeRads(anotherPoint.getLatitudeRads());
        this.setLongitudeRads(anotherPoint.getLongitudeRads());
        this.setDepth(anotherPoint.getDepth());

        this.setAzimuth(anotherPoint.getAzimuth());
        this.setZenith(anotherPoint.getZenith());
        this.setOffsetDistance(anotherPoint.getOffsetDistance());

        this.setOffsetDown(anotherPoint.getOffsetDown());
        this.setOffsetEast(anotherPoint.getOffsetEast());
        this.setOffsetNorth(anotherPoint.getOffsetNorth());

        this.setOffsetEastUsed(anotherPoint.isOffsetEastUsed());
        this.setOffsetNorthUsed(anotherPoint.isOffsetNorthUsed());
        this.setOffsetUpUsed(anotherPoint.isOffsetUpUsed());
    }

    public void setOffsetUpUsed(boolean isOffsetUpUsed) {
        this.isOffsetUpUsed = isOffsetUpUsed;
    }

    public void setOffsetNorthUsed(boolean isOffsetNorthUsed) {
        this.isOffsetNorthUsed = isOffsetNorthUsed;
    }

    public void setOffsetEastUsed(boolean isOffsetEastUsed) {
        this.isOffsetEastUsed = isOffsetEastUsed;
    }

    public boolean isOffsetUpUsed() {
        return isOffsetUpUsed;
    }

    public boolean isOffsetNorthUsed() {
        return isOffsetNorthUsed;
    }

    public boolean isOffsetEastUsed() {
        return isOffsetEastUsed;
    }

    public void setOffsetDistance(double offsetDistance) {
        this.offsetDistance = offsetDistance;
    }

    public void setZenith(double zenith) {
        this.zenith = zenith;
    }

    public void setAzimuth(double azimuth) {
        this.azimuth = azimuth;
    }

    /**
     * Converts a copy of this Location to absolute (Lat/Lon/Depth without offsets).
     * 
     * @return A copy of the location.
     */
    @SuppressWarnings("unchecked")
    public <L extends LocationType> L getNewAbsoluteLatLonDepth() {
        double latlondepth[] = getAbsoluteLatLonDepth();
        L loc;
        try {
            loc = (L) this.getClass().newInstance();
        }
        catch (Exception e) {
            loc = (L) new LocationType();
        }
        loc.setLatitudeDegs(latlondepth[0]);
        loc.setLongitudeDegs(latlondepth[1]);
        loc.setDepth(latlondepth[2]);

        return loc;
    }

    public double getLatitudeDegs() {
        return Math.toDegrees(latitudeRads);
    }

    public double getLongitudeDegs() {
        return Math.toDegrees(this.longitudeRads);
    }

}
