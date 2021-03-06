package ua.omelchenko.cinema.service;

import ua.omelchenko.cinema.entity.Session;
import ua.omelchenko.cinema.entity.Ticket;
import ua.omelchenko.cinema.entity.User;

import java.util.List;

public interface TicketService {
    List<Ticket> getTicketsBySessionId(int sessionId);
    boolean addTickets(Session session, User user, List<Integer> places);
}
