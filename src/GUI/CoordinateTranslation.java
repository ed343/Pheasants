package GUI;

public class CoordinateTranslation {
    
    final int MERCATOR_RANGE = 256;
    
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
    
    public double[] getCorners (G_LatLng center, int zoom, int mapWidth, int mapHeight) {
        double scale = Math.pow(2, zoom);
        MercatorProjection proj = new MercatorProjection();
        
        G_Point centerPx = proj.fromLatLngToPoint(center);        
        G_Point SWPoint = new G_Point(centerPx.x-(mapWidth/2)/scale, centerPx.y+(mapHeight/2)/scale);        
	G_LatLng SWLatLon = proj.fromPointToLatLng(SWPoint);        
	G_Point NEPoint = new G_Point(centerPx.x+(mapWidth/2)/scale, centerPx.y-(mapHeight/2)/scale);        
	G_LatLng NELatLon = proj.fromPointToLatLng(NEPoint);
        
        double[] coords= {NELatLon.lat, NELatLon.lng, SWLatLon.lat, SWLatLon.lng};
        
        return coords;
    }
    
    public static void main(String[] args) {
        
        CoordinateTranslation ct = new CoordinateTranslation();
        
        double centerLat = 50.728392;
        double centerLon = -3.5253615;
        int zoom = 16;
        int mapWidth = 480;
        int mapHeight = 280;
        G_LatLng centerPoint = new G_LatLng(centerLat, centerLon);
        double[] corners = ct.getCorners(centerPoint, zoom, mapWidth, mapHeight);
        
        System.out.println("N:" + corners[0]);
        System.out.println("E:" + corners[1]);
        System.out.println("S:" + corners[2]);
        System.out.println("W:" + corners[3]);
        
    }
}
