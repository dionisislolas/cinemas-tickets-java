package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidAccountException;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private static final String INVALID_ACCOUNT_ID = "Invalid account id";

    private static final String NO_ADULT_TICKET = "No adult ticket";

    private static final String MAX_NUMBER_OF_ALLOWED_TICKETS_EXCEEDED = "Max number of allowed tickets exceeded";

    private static final String NUMBER_OF_INFANT_EXCEEDS_NUMBER_OF_ADULT = "Number of infant tickets exceeds number of adult tickets";

    private TicketPaymentService paymentService = new TicketPaymentServiceImpl();

    private SeatReservationService reservationService = new SeatReservationServiceImpl();

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        if (accountId <= 0) {
            throw new InvalidAccountException(INVALID_ACCOUNT_ID);
        }

        Arrays.stream(ticketTypeRequests)
                .filter(ticketTypeRequest -> ticketTypeRequest.getTicketType().equals(TicketTypeRequest.Type.ADULT))
                .findFirst()
                .orElseThrow(() -> new InvalidPurchaseException(NO_ADULT_TICKET));

        int totalNumberOfTickets = Arrays.stream(ticketTypeRequests)
                .map(TicketTypeRequest::getNoOfTickets)
                .mapToInt(Integer::intValue)
                .sum();

        if (totalNumberOfTickets > 20) {
            throw new InvalidPurchaseException(MAX_NUMBER_OF_ALLOWED_TICKETS_EXCEEDED);
        }

        if (calculateNumberOfInfantTickets(ticketTypeRequests) > calculateNumberOfAdultTickets(ticketTypeRequests)) {
            throw new InvalidPurchaseException(NUMBER_OF_INFANT_EXCEEDS_NUMBER_OF_ADULT);
        }

        paymentService.makePayment(accountId, calculateTotalPayableAmount(ticketTypeRequests));

        reservationService.reserveSeat(accountId, calculateTotalSeatsToAllocate(ticketTypeRequests));

    }

    private int calculateTotalPayableAmount(TicketTypeRequest... ticketTypeRequests) {
        int numberOfAdultTickets = calculateNumberOfAdultTickets(ticketTypeRequests);
        int numberOfChildTickets = calculateNumberOfChildTickets(ticketTypeRequests);
        return numberOfAdultTickets * 20 + numberOfChildTickets * 10;
    }

    private int calculateTotalSeatsToAllocate(TicketTypeRequest... ticketTypeRequests) {
        int numberOfAdultTickets = calculateNumberOfAdultTickets(ticketTypeRequests);
        int numberOfChildTickets = calculateNumberOfChildTickets(ticketTypeRequests);
        return numberOfAdultTickets + numberOfChildTickets;
    }

    private int calculateNumberOfAdultTickets(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .filter(ticketTypeRequest -> ticketTypeRequest.getTicketType().equals(TicketTypeRequest.Type.ADULT))
                .map(TicketTypeRequest::getNoOfTickets)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private int calculateNumberOfChildTickets(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .filter(ticketTypeRequest -> ticketTypeRequest.getTicketType().equals(TicketTypeRequest.Type.CHILD))
                .map(TicketTypeRequest::getNoOfTickets)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private int calculateNumberOfInfantTickets(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .filter(ticketTypeRequest -> ticketTypeRequest.getTicketType().equals(TicketTypeRequest.Type.INFANT))
                .map(TicketTypeRequest::getNoOfTickets)
                .mapToInt(Integer::intValue)
                .sum();
    }

}
