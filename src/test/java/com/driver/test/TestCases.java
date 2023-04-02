package com.driver.test;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.*;
import com.driver.repository.*;
import com.driver.services.PassengerService;
import com.driver.services.TicketService;
import com.driver.services.TrainService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestCases {
    @InjectMocks
    TrainService trainService;
    @InjectMocks
    PassengerService passengerService;

    @InjectMocks
    TicketService ticketService;

    @Mock
    TicketRepository ticketRepository;

    @Mock
    TrainRepository trainRepository;

    @Mock
    PassengerRepository passengerRepository;
    @Test
    public void testAddTrain(){
        List<Station> stations = new ArrayList<>();
        stations.add(Station.DELHI);
        stations.add(Station.AGRA);
        stations.add(Station.KANPUR);
        stations.add(Station.GWALIOR);
        AddTrainEntryDto trainEntryDto = getTrainEntryDto(stations);
        when(trainRepository.save(any())).thenReturn(new Train(123,"delhi,agra,kanpur,gwalior",new ArrayList<>(),
                                                                LocalTime.now(),100));
        Integer trainId = trainService.addTrain(trainEntryDto);
        assertEquals(trainId,123);
    }

    @Test
    public void testCalculateAvailableSeats(){
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(getTicket(1,Station.AGRA,Station.GWALIOR,2000));
        tickets.add(getTicket(2,Station.DELHI,Station.AGRA,2000));
        tickets.add(getTicket(3,Station.KANPUR,Station.GWALIOR,2000));
        tickets.add(getTicket(3,Station.DELHI,Station.GWALIOR,2000));
        when(trainRepository.findById(any())).thenReturn(Optional.of(new Train(123,"DELHI,AGRA,KANPUR,GWALIOR",
                                                                             tickets,
                                                                             LocalTime.now(), 100)));
        Integer seatsAvailable = trainService.calculateAvailableSeats(new SeatAvailabilityEntryDto(0,Station.AGRA,Station.KANPUR));
        Assertions.assertEquals(96, seatsAvailable);

    }
    @Test
    public void testCalculatePeopleBoardingAtAStation() throws Exception {
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(getTicket(1,Station.AGRA,Station.GWALIOR,2000));
        when(trainRepository.findById(1)).thenReturn(Optional.of(new Train(123,"DELHI,AGRA,KANPUR,GWALIOR",
                                                                             tickets,
                                                                             LocalTime.now(), 100)));
        Integer integer = trainService.calculatePeopleBoardingAtAStation(1,Station.AGRA);
        Assertions.assertEquals(2,integer);
    }
    @Test
    public void testCalculatePeopleBoardingAtAStationWhenStationNotExist() throws Exception {
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(getTicket(1,Station.AGRA,Station.GWALIOR,2000));
        when(trainRepository.findById(1)).thenReturn(Optional.of(new Train(123,"DELHI,AGRA,KANPUR,GWALIOR",
                                                                           tickets,
                                                                           LocalTime.now(), 100)));
        Assertions.assertThrows(Exception.class , ()-> trainService.calculatePeopleBoardingAtAStation(1, Station.PRAYAGRAJ));
    }
    @Test
    public void testCalculateOldestPersonTravelling(){
        when(trainRepository.findById(1)).thenReturn(Optional.of(getTrain(1,"DELHI,AGRA,KANPUR,GWALIOR",
                                                                          LocalTime.of(12,34))));
        Integer integer = trainService.calculateOldestPersonTravelling(1);
        Assertions.assertEquals(integer,72);


    }
    @Test
    public void testTrainsBetweenAGivenTime(){
        List<Train> trains = new ArrayList<>();
        trains.add(getTrain(1,"DELHI,AGRA,KANPUR,GWALIOR",
                            LocalTime.of(12,34)));
        trains.add(getTrain(2,"DELHI,AGRA,KANPUR,GWALIOR",
                            LocalTime.of(11,34))
                  );
        trains.add(getTrain(3,"DELHI,AGRA,KANPUR,GWALIOR",
                            LocalTime.of(11,50))
                  );
        when(trainRepository.findAll()).thenReturn(trains);
        List<Integer> trainAvailable = trainService.trainsBetweenAGivenTime(Station.AGRA, LocalTime.of(10, 50),
                                                                     LocalTime.of(12, 50));
        Assertions.assertEquals(trainAvailable.size(),2);


    }

    @Test
    public void testAddPassenger(){
     when(passengerRepository.save(any())).thenReturn(getPassenger(1,"TUSAHR",22));
     Integer passengerId = passengerService.addPassenger(getPassenger(1,"TUSAHR",22));
     Assertions.assertEquals(passengerId,1);
    }


    @Test
    public void testBookATicketWhenLessTicketAvailable(){
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(getTicket(1,Station.AGRA,Station.GWALIOR,2000));
        tickets.add(getTicket(2,Station.DELHI,Station.AGRA,2000));
        tickets.add(getTicket(3,Station.KANPUR,Station.GWALIOR,2000));
        tickets.add(getTicket(3,Station.DELHI,Station.GWALIOR,2000));
        when(trainRepository.findById(any())).thenReturn(Optional.of(new Train(123,"DELHI,AGRA,KANPUR,GWALIOR",
                                                                               tickets,
                                                                               LocalTime.now(), 100)));
        Exception exception = Assertions.assertThrows(Exception.class,()->{
            ticketService.bookTicket(new BookTicketEntryDto(new ArrayList<>(),1,Station.AGRA,Station.KANPUR,98,1));
        });
        Assertions.assertEquals(exception.getMessage(), "Less tickets are available");
    }

    @Test
    public void testBookATicket() throws Exception {
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(getTicket(1,Station.AGRA,Station.GWALIOR,2000));
        tickets.add(getTicket(2,Station.DELHI,Station.AGRA,2000));
        tickets.add(getTicket(3,Station.KANPUR,Station.GWALIOR,2000));
        tickets.add(getTicket(3,Station.DELHI,Station.GWALIOR,2000));
        when(trainRepository.findById(any())).thenReturn(Optional.of(new Train(123,"DELHI,AGRA,KANPUR,GWALIOR",
                                                                               tickets,
                                                                               LocalTime.now(), 100)));
        when(passengerRepository.findById(1)).thenReturn(Optional.of(getPassenger(1,"TUSHAR",22)));
        when(passengerRepository.findById(2)).thenReturn(Optional.of(getPassenger(2,"SUPER V",22)));
        when(ticketRepository.save(any())).thenReturn(getTicket(123,Station.AGRA,Station.KANPUR,1221));

        List<Integer> passengerIds = new ArrayList<Integer>(){
            {
                add(1);
                add(2);
            }
        };
        Integer ticketId = ticketService.bookTicket(new BookTicketEntryDto(passengerIds,1,Station.AGRA,Station.KANPUR,4,
                                                                        1));
        Assertions.assertEquals(123,ticketId);

    }
    @Test
    public void testBookATicketWhenStationNotOnTrain() throws Exception {
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(getTicket(1,Station.AGRA,Station.GWALIOR,2000));
        tickets.add(getTicket(2,Station.DELHI,Station.AGRA,2000));
        tickets.add(getTicket(3,Station.KANPUR,Station.GWALIOR,2000));
        tickets.add(getTicket(3,Station.DELHI,Station.GWALIOR,2000));
        when(trainRepository.findById(any())).thenReturn(Optional.of(new Train(123,"DELHI,AGRA,KANPUR,GWALIOR",
                                                                               tickets,
                                                                               LocalTime.now(), 100)));
        List<Integer> passengerIds = new ArrayList<Integer>(){
            {
                add(1);
                add(2);
            }
        };
        Exception exception = Assertions.assertThrows(Exception.class,()->{
            ticketService.bookTicket(new BookTicketEntryDto(new ArrayList<>(),1,Station.PRAYAGRAJ,Station.KANPUR,8,1));
        });
        Assertions.assertEquals(exception.getMessage(), "Invalid stations");
    }
    private static AddTrainEntryDto getTrainEntryDto(List<Station> stations) {
        return new AddTrainEntryDto(stations,LocalTime.now(), 100);
    }

    public Ticket getTicket(int ticketId,Station from, Station to,int totalFare){
        Train train = new Train();
        List<Passenger> passengerList = new ArrayList<Passenger>() {{
            add(new Passenger(1,"12",12,new ArrayList<>()));
            add(new Passenger(1,"12",72,new ArrayList<>())); //Check this :
        }};
        Ticket ticket = new Ticket(ticketId,passengerList,train,from,to,totalFare);
        return  ticket;
    }

    public Train getTrain(int trainId, String route, LocalTime time){
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(getTicket(1,Station.AGRA,Station.GWALIOR,2000));
        tickets.add(getTicket(2,Station.DELHI,Station.AGRA,2000));
        Train train = new Train(trainId, route, tickets, time,100);
        return train;
    }

    public Passenger getPassenger(int id, String name, int age) {
        return new Passenger(id, name, age, new ArrayList<>());
    }
}

