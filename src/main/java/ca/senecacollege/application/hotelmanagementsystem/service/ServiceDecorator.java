package ca.senecacollege.application.hotelmanagementsystem.service;

public abstract class ServiceDecorator implements IBooking {
    protected IBooking decoratedBooking;

    public ServiceDecorator(IBooking booking) {
        this.decoratedBooking = booking;
    }

    @Override
    public double calculateTotal() {
        return decoratedBooking.calculateTotal();
    }

    @Override
    public String getDescription() {
        return decoratedBooking.getDescription();
    }
}