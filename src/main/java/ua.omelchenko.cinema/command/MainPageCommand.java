package ua.omelchenko.cinema.command;

import ua.omelchenko.cinema.entity.Session;
import ua.omelchenko.cinema.servlet.TemporaryAttributes;
import ua.omelchenko.cinema.dao.DaoFactory;
import ua.omelchenko.cinema.util.ConfigurationManager;
import ua.omelchenko.cinema.service.SessionService;
import ua.omelchenko.cinema.service.impl.SessionServiceImpl;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Load the index.jsp page
 */
public class MainPageCommand implements Command {
    public static final int NUMBERS_OF_OBJECTS = 4;
    public static final int ONE_MORE_OBJECT = NUMBERS_OF_OBJECTS + 1;
    public static final int NUMBER_OF_PLACES = ConfigurationManager.getInstance()
            .getNumberProperty(ConfigurationManager.HALL_CAPACITY);
    public static final int NUMBER_OF_PLACES_OVER = NUMBER_OF_PLACES + 10;
    SessionService sessionService = new SessionServiceImpl(DaoFactory.getInstance());

    public String execute(HttpServletRequest request, HttpServletResponse response) {
        Integer number = (Integer) request.getSession().getAttribute("currentMainPage");

        if (request.getParameter("mainPage") != null) {
            number = Integer.valueOf(request.getParameter("mainPage"));
        }

        pagination(request, number);
        request.getSession().setAttribute("currentMainPage", number);

        return ConfigurationManager.getInstance()
                .getProperty(ConfigurationManager.MAIN_PAGE_PATH);
    }

    private Session extract(List<Session> sessionList) {
        if (sessionList.iterator().hasNext()) {
            Session session = sessionList.iterator().next();
            sessionList.remove(0);
            return session;
        }
        return null;

    }

    /**
     * Implementation of pagination for 4 cells
     */
    public void pagination(HttpServletRequest request, Integer previousPage) {

        int start = 0;
        if (previousPage != null) {
            start = previousPage - 1;
        }
        //Limit property
        String limitRequest = request.getParameter("limitPlaces");
        if (!Objects.isNull(limitRequest)) {
            request.getSession().setAttribute("limitPlaces", Boolean.valueOf(limitRequest));
        }
        Optional<Object> limitSession = Optional.ofNullable(request.getSession().getAttribute("limitPlaces"));
        int limit = (limitSession.isPresent()) &&
                (boolean) limitSession.get() ? NUMBER_OF_PLACES : NUMBER_OF_PLACES_OVER;
        //Sort property
        String sort = request.getParameter("sort");
        String paginationSort = "dateTime";
        if (sort != null) {
            switch (sort) {
                case "dateTime" -> request.getSession().setAttribute("sort", "dateTime");
                case "name" -> request.getSession().setAttribute("sort", "title");
                case "places" -> request.getSession().setAttribute("sort", "numberOfTickets");
            }
        }
        if (request.getSession().getAttribute("sort") != null) {
            paginationSort = (String) request.getSession().getAttribute("sort");
        }

        int end = start + ONE_MORE_OBJECT;
        List<Session> sessionList = sessionService.getServicesOrderByLimits(paginationSort, start, end, limit);
        extracted(request, end, sessionList);
    }

    public void extracted(HttpServletRequest request, int end, List<Session> sessionList) {
        TemporaryAttributes tA = (TemporaryAttributes) request.getSession().getAttribute("temp");
        tA.setAttributes("mainPreviousPage", end - NUMBERS_OF_OBJECTS * 2);
        if (sessionList.size() >= ONE_MORE_OBJECT) {
            tA.setAttributes("mainNextPage", end);
        }

        request.getSession().setAttribute("temp", tA);

        Session movieCell1 = extract(sessionList);
        Session movieCell2 = extract(sessionList);
        Session movieCell3 = extract(sessionList);
        Session movieCell4 = extract(sessionList);

        request.getSession().setAttribute("mainCell1", movieCell1);
        request.getSession().setAttribute("mainCell2", movieCell2);
        request.getSession().setAttribute("mainCell3", movieCell3);
        request.getSession().setAttribute("mainCell4", movieCell4);
    }
}