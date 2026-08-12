package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        when(inputReaderUtil.readSelection()).thenReturn(1);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket);
        assertEquals("ABCDEF", ticket.getVehicleRegNumber());
        assertFalse(ticket.getParkingSpot().isAvailable());
        assertNotNull(ticket.getInTime());
    }

    @Test
    public void testParkingLotExit(){
        Ticket ticket = new Ticket();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        ticket.setParkingSpot(parkingSpot);
        // Setup inTime 1h before
        ticket.setInTime(new Date(System.currentTimeMillis() - 1000 * 60 * 60));
        ticket.setOutTime(new Date());
        ticket.setVehicleRegNumber("ABCDEF");
        ticketDAO.saveTicket(ticket);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        Ticket updatedTicket = ticketDAO.getTicket("ABCDEF");

        assertTrue(updatedTicket.getPrice() >= 0);
        assertNotNull(updatedTicket.getOutTime());
    }

    @Test
    public void testParkingLotExitRecurringUser(){
        Ticket oldTicket = new Ticket();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        oldTicket.setParkingSpot(parkingSpot);
        // Setup oldTicket inTime 5h before
        oldTicket.setInTime(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 5));
        // Setup oldTicket outTime 4h before
        oldTicket.setOutTime(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 4));
        oldTicket.setVehicleRegNumber("ABCDEF");
        ticketDAO.saveTicket(oldTicket);

        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        // Setup ticket inTime 1h before
        ticket.setInTime(new Date(System.currentTimeMillis() - 1000 * 60 * 60));
        ticket.setVehicleRegNumber("ABCDEF");
        ticketDAO.saveTicket(ticket);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        Ticket lastTicket = ticketDAO.getTicket("ABCDEF");

        assertNotNull(lastTicket.getOutTime());
        assertEquals(Fare.CAR_RATE_PER_HOUR * 0.95, lastTicket.getPrice(), 0.01);
    }
}
