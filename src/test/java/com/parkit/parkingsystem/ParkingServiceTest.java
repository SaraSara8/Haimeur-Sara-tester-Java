package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
        	lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            lenient().when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
            
            //mocker l’appel à la méthode getNbTicket().
            lenient().when(ticketDAO.getNbTicket(anyString())).thenReturn(1);
            
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest(){
        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    
    
        // les ajouts fait dans le cadre du test..
    	
    	verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
        
    
    }
    
    @Test
    public void testProcessIncomingVehicle() {
    	
    	//GIVEN
    	
    	when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
    	
    	//WHEN
    	parkingService.processIncomingVehicle();
    	
    	//THEN
    	verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
    	verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    	verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
    }
    
    
    
    @Test
    public void processExitingVehicleTestUnableUpdate() {
   
    	// GIVEN
    	
    	when (ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
    	
    	//WHEN
    	parkingService.processExitingVehicle();
    	
    	//THEN
    	verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
    	
    }
    
    @Test
    public void testGetNextParkingNumberIfAvailable () {
    	
    	// GIVEN
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
    	
    	//WHEN
    	ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
    	
    	//THEN
    	verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.CAR);
    	
    	assertAll(() -> assertEquals(parkingSpot.getId(), 1), () -> assertEquals(parkingSpot.isAvailable(), true));
    	
    	
    }
    
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
    	
    	//GIVEN
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	lenient().when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);
    	
    	//WHEN
    	ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
    	//THEN
    	verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.CAR);
    	
    }
    
    
    
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
    	
    	when(inputReaderUtil.readSelection()).thenReturn(3);
    	lenient().when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
    	
    	
    	ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
    	
    	
    	assertEquals(null, parkingSpot);

    	
    	
    	
    }



    	


}
