package uk.gov.dwp.uc.pairtest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidAccountException;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.ArrayList;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceImplTest {

    @Mock
    private SeatReservationService reservationService;

    @Mock
    private TicketPaymentService paymentService;

    @InjectMocks
    private TicketService service = new TicketServiceImpl();

    @Test
    public void purchaseTickets_validRequest() {
        var requestList = new ArrayList<TicketTypeRequest>();
        requestList.add(new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 3));
        requestList.add(new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2));
        requestList.add(new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2));

        service.purchaseTickets(1L, requestList.toArray(new TicketTypeRequest[3]));

        verify(paymentService, times(1)).makePayment(1L, 60 + 20);

        verify(reservationService, times(1)).reserveSeat(1L, 5);
    }

    @Test(expected = InvalidAccountException.class)
    public void purchaseTickets_invalidAccount() {
        service.purchaseTickets(-1L);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void purchaseTickets_invalidPurchase_noRequest() {
        service.purchaseTickets(1L);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void purchaseTickets_invalidPurchaseRequest_noAdult() {
        var requestList = new ArrayList<TicketTypeRequest>();
        requestList.add(new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2));
        requestList.add(new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1));

        service.purchaseTickets(1L, requestList.toArray(new TicketTypeRequest[2]));
    }

    @Test(expected = InvalidPurchaseException.class)
    public void purchaseTickets_invalidPurchaseRequest_numberOfAllowedTicketsExceeded() {
        var requestList = new ArrayList<TicketTypeRequest>();
        requestList.add(new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10));
        requestList.add(new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 10));
        requestList.add(new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1));

        service.purchaseTickets(1L, requestList.toArray(new TicketTypeRequest[3]));
    }

    @Test(expected = InvalidPurchaseException.class)
    public void purchaseTickets_invalidPurchaseRequest_numberOfInfantExceedsNumberOfAdults() {
        var requestList = new ArrayList<TicketTypeRequest>();
        requestList.add(new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 4));
        requestList.add(new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 10));
        requestList.add(new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 6));

        service.purchaseTickets(1L, requestList.toArray(new TicketTypeRequest[3]));
    }
}