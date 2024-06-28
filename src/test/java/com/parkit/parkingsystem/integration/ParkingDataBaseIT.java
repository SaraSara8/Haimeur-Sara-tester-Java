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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.Date;

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
    	lenient().when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

  
    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
        
        // Il faut recurper le ticket de la base de donnée puis recuprer le spot et lire l'attribut Available.
        
        
        
        
    }

  
    @Test
    public void testParkingLotExit(){
       
    	//testParkingACar();
    	
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        //parkingService.processIncomingVehicle();
        
        
        
        // On créé un ticket pour simuler l'entrée avec une heure intime egale à lheure actuelle - 60 min
        Ticket newTicket = new Ticket();
        
        newTicket.setId(1);
        newTicket.setVehicleRegNumber("ABCDEF");
        newTicket.setPrice(0);
        Date inTime = new Date(System.currentTimeMillis() - (  60 * 60 * 1000)); 
        newTicket.setInTime(inTime);
        newTicket.setOutTime(null);
        
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false); 
        
        newTicket.setParkingSpot(parkingSpot);
        // On verouille le ticket dans la base de donnée.
       
        parkingSpotDAO.updateParking(parkingSpot);
        // on enregiste de ticket dans la base de donnée.
        // nous avons simuler l'entrée dans le parking.
        
        ticketDAO.saveTicket(newTicket);
        
        
        
        
       
        
       
        
        //Le vehicule quitte le parking
        
        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database
        
        
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        
        assertAll(
        		() -> assertEquals(true,  ticket.getOutTime()!=null),
        		() -> assertEquals(true, ticket.getPrice()>0)
        		);
        
        
        
        
    }
    
    @Test
    public void testParkingLotExitRecurringUser() {
    	
    	
    	 ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
         
    	 
    	 // On créé un ticket pour simuler l'entrée avec une heure intime egale à lheure actuelle - 60 min
         Ticket newTicket = new Ticket();
         
         newTicket.setId(1);
         newTicket.setVehicleRegNumber("ABCDEF");
         newTicket.setPrice(1.5);
         
         newTicket.setInTime(new Date(System.currentTimeMillis() - (  25*60 * 60 * 1000)));
         newTicket.setOutTime(new Date(System.currentTimeMillis() - (  24*60 * 60 * 1000)));
         
         ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true); 
         
         newTicket.setParkingSpot(parkingSpot);
         // On verouille le ticket dans la base de donnée.
        
         parkingSpotDAO.updateParking(parkingSpot);
         // on enregiste de ticket dans la base de donnée.
         // nous avons simuler l'entrée dans le parking.
         
         ticketDAO.saveTicket(newTicket);
    	 
    	 
    	 
    	 parkingService.processIncomingVehicle();
    	 
    	 
    	 
    	 Connection connection = null;
         try{
             connection = dataBaseTestConfig.getConnection();

             //set parking entries to available
             connection.prepareStatement("update ticket set in_time='2024-06-28 10:00:00' where id=2").execute();

             

         }catch(Exception e){
             e.printStackTrace();
         }finally {
             dataBaseTestConfig.closeConnection(connection);
         }
    	 
    	 
    	 
         
    	 parkingService.processExitingVehicle();
    	 
    	 Ticket ticket = ticketDAO.getTicket("ABCDEF");
    	 
    	 double inMilli = ticket.getInTime().getTime();
    	 double outMilli = ticket.getOutTime().getTime();

         //TODO: Some tests are failing here. Need to check if this logic is correct
         
    	 
    	 
    	 double duration = outMilli - inMilli;
         
         
         
         duration = duration/(1000*60*60); // conversion des millisecondes en heures
         duration = duration *0.95;
		 duration = duration *Fare.CAR_RATE_PER_HOUR;
         
		 DecimalFormat f = new DecimalFormat();
    	 f.setMaximumFractionDigits(2);
		 
         assertEquals(f.format(duration), f.format(ticket.getPrice()));
    	 
    	 
    	
    }

    
}
