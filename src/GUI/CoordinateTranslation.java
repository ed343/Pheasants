package GUI;

public class CoordinateTranslation {
    
    final int MERCATOR_RANGE = 256;
    final int R = 6378137; // EARTH RADIUS (IN METERS)
    
    /**
     * Converts geographical coordinate (lat, lon) to Cartesian coordinate (x,y,z)
     * @param lat
     * @param lon
     * @return 
     */
    public Double[] lonLatToCartesian(double lat, double lon) {
        
        lon = degreesToRadians(lon);
        lat = degreesToRadians(lat);
        
        double x = R * Math.cos(lat) * Math.cos(lon);

        double y = R * Math.cos(lat) * Math.sin(lon);

        double z = R * Math.sin(lat);
        
        Double[] coords = {x,y,z};
        
        return coords;
    }
    
    /**
     * Converts Cartesian coordinate (x,y,z) to geographical coordinate (lat, lon).
     * @param cartCoords
     * @return 
     */
    public Double[] cartesianToLatLon(Double[] cartCoords) {
        
        double x = cartCoords[0];
        double y = cartCoords[1];
        double z = cartCoords[2];                
        
        double lat = Math.asin(z / R);
        double lon = Math.atan2(y, x);
        
        lat = radiansToDegrees(lat);
        lon = radiansToDegrees(lon);
        
        Double[] coords = {lat, lon};
           
        return coords;
    }
     
    public double bound( double value, double opt_min, double opt_max){

        if (opt_min>value) {
            value = opt_min;
        } 
        if (value>opt_max) {
            value = opt_max;
        }
        	
        return value;
    }
    
    public double degreesToRadians(double deg) {
        return deg * (Math.PI / 180);
    }
    
    public double radiansToDegrees (double rad) {
        return rad / (Math.PI /180);
    }

    class G_Point {
        double x;
        double y;
        
        public G_Point(double xx, double yy) {
            this.x = xx;
            this.y = yy;
        }
    }
    
    static class G_LatLng {        
        double lat;
        double lng; 
        
        public G_LatLng (double lt, double ln) {
            this.lat = lt;
            this.lng = ln;
        }
    }
    
    class MercatorProjection {
        
        G_Point pixelOrigin;
        double pixelsPerLonDegree;
        double pixelsPerLonRadian;
        
        public MercatorProjection() {

            pixelOrigin = new G_Point(MERCATOR_RANGE/2.0, MERCATOR_RANGE/2.0);
            pixelsPerLonDegree = MERCATOR_RANGE / 360.0;
            pixelsPerLonRadian = MERCATOR_RANGE/(2 * Math.PI);
        }
        
        public G_Point fromLatLngToPoint(G_LatLng latLng) {
            G_Point point = new G_Point(0,0);
            G_Point origin = this.pixelOrigin;
            point.x = origin.x + latLng.lng * this.pixelsPerLonDegree;
            
            double siny = bound(Math.sin(degreesToRadians(latLng.lat)), -0.9999, 0.9999);
            point.y = origin.y + 0.5 * Math.log((1 + siny) / (1 - siny))* - this.pixelsPerLonRadian;
            
            return point;
        }
        
        public G_LatLng fromPointToLatLng(G_Point point) {
            G_Point origin = this.pixelOrigin;
            double lng = (point.x - origin.x) / this.pixelsPerLonDegree;
            double latRadians = (point.y - origin.y) / -this.pixelsPerLonRadian;
            double lat = radiansToDegrees(2 * Math.atan(Math.exp(latRadians)) - Math.PI / 2);
            
            return new G_LatLng(lat, lng);
        }           
    }
    
    public Double[] getCorners (G_LatLng center, int zoom, int mapWidth, int mapHeight) {
        double scale = Math.pow(2, zoom);
        MercatorProjection proj = new MercatorProjection();
        
        G_Point centerPx = proj.fromLatLngToPoint(center);        
        G_Point SWPoint = new G_Point(centerPx.x-(mapWidth/2)/scale, centerPx.y+(mapHeight/2)/scale);        
	G_LatLng SWLatLon = proj.fromPointToLatLng(SWPoint);        
	G_Point NEPoint = new G_Point(centerPx.x+(mapWidth/2)/scale, centerPx.y-(mapHeight/2)/scale);        
	G_LatLng NELatLon = proj.fromPointToLatLng(NEPoint);
        
        Double[] coords= {NELatLon.lat, NELatLon.lng, SWLatLon.lat, SWLatLon.lng};
        
        return coords;
    }
    
    @SuppressWarnings("empty-statement")
    public static void main(String[] args) {
        
        CoordinateTranslation ct = new CoordinateTranslation();
        
        double centerLat = 50.728392;
        double centerLon = -3.5253615;
        int zoom = 16;
        int mapWidth = 480;
        int mapHeight = 280;
        G_LatLng centerPoint = new G_LatLng(centerLat, centerLon);
        Double[] corners = ct.getCorners(centerPoint, zoom, mapWidth, mapHeight);
        
        System.out.println("N:" + corners[0]);
        System.out.println("E:" + corners[1]);
        System.out.println("S:" + corners[2]);
        System.out.println("W:" + corners[3]);
        
        double x = ct.lonLatToCartesian(centerLat, centerLon)[0];
        double y = ct.lonLatToCartesian(centerLat, centerLon)[1];
        double z = ct.lonLatToCartesian(centerLat, centerLon)[2];

        Double[] c = {x,y,z};
        System.out.println(c[0]); // x
        System.out.println(c[1]); // y
        System.out.println(c[2]); // z
        
        System.out.println("---------");
        
        //Double[] t = {450, 13, 1.5};
        //Double[] re = ct.cartesianToLatLon(t);
        //System.out.println(re[0]);
        //System.out.println(re[1]);
        
        Double[] te = { 237.24549, -262.59965, -6562.23780};
        Double[] res = ct.cartesianToLatLon(te);
        System.out.println("-----------");
        System.out.println(res[0]);
        System.out.println(res[1]);
        
        Double[] latlon = ct.cartesianToLatLon(c);
        System.out.println("-----------");
        System.out.println(latlon[0]); //latitude
        System.out.println(latlon[1]); //longitude

        
    }
}
