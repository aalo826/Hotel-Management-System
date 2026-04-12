package ca.senecacollege.application.hotelmanagementsystem.service;

public class ExtraServices {

    // Wi-Fi
    private static class WifiDecorator extends ServiceDecorator {
        public WifiDecorator(IBooking booking) { super(booking); }
        @Override public double calculateTotal() { return super.calculateTotal() + 10.0; }
        @Override public String getDescription() { return super.getDescription() + ", Wi-Fi"; }
    }

    // Spa
    private static class SpaDecorator extends ServiceDecorator {
        public SpaDecorator(IBooking booking) { super(booking); }
        @Override public double calculateTotal() { return super.calculateTotal() + 50.0; }
        @Override public String getDescription() { return super.getDescription() + ", Spa"; }
    }

    // Breakfast
    private static class BreakfastDecorator extends ServiceDecorator {
        private final long nights;
        public BreakfastDecorator(IBooking booking, long nights) {
            super(booking);
            this.nights = Math.max(1, nights);
        }
        @Override public double calculateTotal() { return super.calculateTotal() + (20.0 * nights); }
        @Override public String getDescription() { return super.getDescription() + ", Breakfast"; }
    }

    // Parking
    private static class ParkingDecorator extends ServiceDecorator {
        private final long nights;
        public ParkingDecorator(IBooking booking, long nights) {
            super(booking);
            this.nights = Math.max(1, nights);
        }
        @Override public double calculateTotal() { return super.calculateTotal() + (15.0 * nights); }
        @Override public String getDescription() { return super.getDescription() + ", Parking"; }
    }

    // Flat-rate add methods (used by admin preview where nights are unknown)
    public static IBooking addWifi(IBooking b)      { return new WifiDecorator(b); }
    public static IBooking addSpa(IBooking b)        { return new SpaDecorator(b); }
    public static IBooking addBreakfast(IBooking b)  { return new BreakfastDecorator(b, 1); }
    public static IBooking addParking(IBooking b)    { return new ParkingDecorator(b, 1); }

    // Night-aware add methods (used by kiosk and booking service)
    public static IBooking addWifi(IBooking b, long nights)      { return new WifiDecorator(b); }
    public static IBooking addSpa(IBooking b, long nights)       { return new SpaDecorator(b); }
    public static IBooking addBreakfast(IBooking b, long nights) { return new BreakfastDecorator(b, nights); }
    public static IBooking addParking(IBooking b, long nights)   { return new ParkingDecorator(b, nights); }
}