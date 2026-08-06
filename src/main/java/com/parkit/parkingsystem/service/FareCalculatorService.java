package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, Boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        double inHour = ticket.getInTime().getTime();
        double outHour = ticket.getOutTime().getTime();

        double duration = (outHour - inHour) / 1000 / 60 / 60;

        if (duration < 0.5) {
            ticket.setPrice(0);
        } else {
            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    if (discount) {
                        ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR * 0.95);
                        break;
                    }
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                    break;
                }
                case BIKE: {
                    if (discount) {
                        ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR * 0.95);
                        break;
                    }
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                    break;
                }
                default: throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}