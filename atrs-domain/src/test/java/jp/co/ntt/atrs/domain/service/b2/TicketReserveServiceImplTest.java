package jp.co.ntt.atrs.domain.service.b2;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jp.co.ntt.atrs.domain.model.BoardingClass;
import jp.co.ntt.atrs.domain.model.BoardingClassCd;
import jp.co.ntt.atrs.domain.model.FareType;
import jp.co.ntt.atrs.domain.model.FareTypeCd;
import jp.co.ntt.atrs.domain.model.Flight;
import jp.co.ntt.atrs.domain.model.FlightMaster;
import jp.co.ntt.atrs.domain.model.Gender;
import jp.co.ntt.atrs.domain.model.Member;
import jp.co.ntt.atrs.domain.model.Passenger;
import jp.co.ntt.atrs.domain.model.Reservation;
import jp.co.ntt.atrs.domain.model.ReserveFlight;
import jp.co.ntt.atrs.domain.model.Route;
import jp.co.ntt.atrs.domain.repository.flight.FlightRepository;
import jp.co.ntt.atrs.domain.repository.flight.VacantSeatSearchCriteriaDto;
import jp.co.ntt.atrs.domain.repository.member.MemberRepository;
import jp.co.ntt.atrs.domain.repository.reservation.ReservationRepository;
import jp.co.ntt.atrs.domain.repository.reservation.ReservationHistoryDto;
import jp.co.ntt.atrs.domain.service.b0.TicketSharedService;
import jp.co.ntt.atrs.domain.common.exception.AtrsBusinessException;
import org.terasoluna.gfw.common.exception.BusinessException;
import org.terasoluna.gfw.common.exception.SystemException;

/**
 * TicketReserveServiceImplのテストランナークラス。
 * ライブラリを使用せずJavaランタイムのみで実行できるようにしている。
 */
public class TicketReserveServiceImplTest {

    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("=== TicketReserveServiceImpl Unit Tests Start ===");

        try {
            runTests();
        } catch (Throwable t) {
            System.err.println("Fatal error during test execution:");
            t.printStackTrace();
            System.exit(1);
        }

        System.out.println("\n=== Test Run Summary ===");
        System.out.printf("Total Executed: %d\n", totalTests);
        System.out.printf("Passed        : %d\n", passedTests);
        System.out.printf("Failed        : %d\n", totalTests - passedTests);

        if (passedTests == totalTests) {
            System.out.println("ALL TESTS PASSED SUCCESSFUL!");
            System.exit(0);
        } else {
            System.err.println("SOME TESTS FAILED!");
            System.exit(1);
        }
    }

    private static void runTests() throws Exception {
        // --- calculateTotalFare tests ---
        testCalculateTotalFare_FlightListNull();
        testCalculateTotalFare_FlightListEmpty();
        testCalculateTotalFare_PassengerListNull();
        testCalculateTotalFare_PassengerListEmpty();
        testCalculateTotalFare_PassengerNullElement();
        testCalculateTotalFare_FlightNullElement();
        testCalculateTotalFare_HappyPath();

        // --- validateReservation tests ---
        testValidateReservation_NullReservation();
        testValidateReservation_EmptyReserveFlightList();
        testValidateReservation_RepAgeLessThanMin();
        testValidateReservation_NullReserveFlightInList();
        testValidateReservation_EmptyPassengerListInReserveFlight();
        testValidateReservation_LadiesDiscountMalePassenger();
        testValidateReservation_LadiesDiscountNullPassenger();
        testValidateReservation_GroupDiscountLessThanMin();
        testValidateReservation_RepMemberNotFound();
        testValidateReservation_RepMemberMismatchFamilyName();
        testValidateReservation_RepMemberMismatchGivenName();
        testValidateReservation_RepMemberMismatchGender();
        testValidateReservation_PassengerMemberNotFound();
        testValidateReservation_PassengerMemberMismatchFamilyName();
        testValidateReservation_PassengerMemberMismatchGivenName();
        testValidateReservation_PassengerMemberMismatchGender();
        testValidateReservation_HappyPath();

        // --- registerReservation tests ---
        testRegisterReservation_NullReservation();
        testRegisterReservation_EmptyReserveFlightList();
        testRegisterReservation_NullReserveFlight();
        testRegisterReservation_NullFlight();
        testRegisterReservation_NotAvailableFareType();
        testRegisterReservation_VacancyLessThanPassengerNum();
        testRegisterReservation_FlightUpdateCountNotOne();
        testRegisterReservation_ReservationInsertCountNotOne();
        testRegisterReservation_ReserveFlightInsertCountNotOne();
        testRegisterReservation_PassengerInsertCountNotOne();
        testRegisterReservation_HappyPath();

        // --- findMember tests ---
        testFindMember_EmptyMembershipNo();
        testFindMember_NullMembershipNo();
        testFindMember_HappyPath();
    }

    // ==========================================
    // Test Case Methods
    // ==========================================

    private static void testCalculateTotalFare_FlightListNull() {
        startTest("calculateTotalFare - FlightList is null");
        TicketReserveServiceImpl service = createService();
        try {
            service.calculateTotalFare(null, new ArrayList<>());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertContains("flightList must not empty", e.getMessage());
            pass();
        }
    }

    private static void testCalculateTotalFare_FlightListEmpty() {
        startTest("calculateTotalFare - FlightList is empty");
        TicketReserveServiceImpl service = createService();
        try {
            service.calculateTotalFare(new ArrayList<>(), new ArrayList<>());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertContains("flightList must not empty", e.getMessage());
            pass();
        }
    }

    private static void testCalculateTotalFare_PassengerListNull() {
        startTest("calculateTotalFare - PassengerList is null");
        TicketReserveServiceImpl service = createService();
        List<Flight> flights = new ArrayList<>();
        flights.add(new Flight());
        try {
            service.calculateTotalFare(flights, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertContains("passengerList must not empty", e.getMessage());
            pass();
        }
    }

    private static void testCalculateTotalFare_PassengerListEmpty() {
        startTest("calculateTotalFare - PassengerList is empty");
        TicketReserveServiceImpl service = createService();
        List<Flight> flights = new ArrayList<>();
        flights.add(new Flight());
        try {
            service.calculateTotalFare(flights, new ArrayList<>());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertContains("passengerList must not empty", e.getMessage());
            pass();
        }
    }

    private static void testCalculateTotalFare_PassengerNullElement() {
        startTest("calculateTotalFare - PassengerList contains null element");
        TicketReserveServiceImpl service = createService();
        List<Flight> flights = new ArrayList<>();
        flights.add(new Flight());
        List<Passenger> passengers = new ArrayList<>();
        passengers.add(null);
        try {
            service.calculateTotalFare(flights, passengers);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertContains("passenger must not null", e.getMessage());
            pass();
        }
    }

    private static void testCalculateTotalFare_FlightNullElement() {
        startTest("calculateTotalFare - FlightList contains null element");
        TicketReserveServiceImpl service = createService();
        List<Flight> flights = new ArrayList<>();
        flights.add(null);
        List<Passenger> passengers = new ArrayList<>();
        Passenger p = new Passenger();
        p.setAge(20);
        passengers.add(p);
        try {
            service.calculateTotalFare(flights, passengers);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertContains("flight must not null", e.getMessage());
            pass();
        }
    }

    private static void testCalculateTotalFare_HappyPath() {
        startTest("calculateTotalFare - Happy Path (Adults and Children calculation & round up)");
        TicketReserveServiceImpl service = createService();

        // Configure min ages & rates
        try {
            setField(service, "adultPassengerMinAge", 12);
            setField(service, "childFareRate", 50);
        } catch (Exception e) {
            fail("Reflection setup failed: " + e.getMessage());
            return;
        }

        // Mock TicketSharedService methods
        MockTicketSharedService mockSharedService = new MockTicketSharedService();
        mockSharedService.calculateBasicFareFunc = args -> {
            int basic = (Integer) args[0];
            return basic; // 10000
        };
        mockSharedService.calculateFareFunc = (basic, discount) -> {
            return basic - (basic * discount / 100); // 10000 - 1000 = 9000
        };
        try {
            setField(service, "ticketSharedService", mockSharedService);
        } catch (Exception e) {
            fail("Reflection injection failed: " + e.getMessage());
            return;
        }

        List<Flight> flights = new ArrayList<>();
        Flight flight1 = new Flight();
        BoardingClass boardingClass = new BoardingClass();
        boardingClass.setBoardingClassCd(BoardingClassCd.N);
        flight1.setBoardingClass(boardingClass);
        flight1.setDepartureDate(LocalDate.now());

        FlightMaster flightMaster = new FlightMaster();
        Route route = new Route();
        route.setBasicFare(10000);
        flightMaster.setRoute(route);
        flight1.setFlightMaster(flightMaster);

        FareType fareType = new FareType();
        fareType.setDiscountRate(10);
        flight1.setFareType(fareType);
        flights.add(flight1);

        List<Passenger> passengers = new ArrayList<>();
        // Passenger 1: Adult (20 years)
        Passenger p1 = new Passenger();
        p1.setAge(20);
        passengers.add(p1);

        // Passenger 2: Child (10 years)
        Passenger p2 = new Passenger();
        p2.setAge(10);
        passengers.add(p2);

        // Calculation check:
        // childNum = 1, adultNum = 1
        // boardingFare (adult) = 9000
        // childFare = baseFare * (childFareRate - discountRate) / 100 * childNum
        //           = 10000 * (50 - 10) / 100 * 1 = 4000
        // total = 9000 * 1 + 4000 = 13000
        int fare = service.calculateTotalFare(flights, passengers);
        assertEquals(13000, fare);

        // Test with round up (total fare is 13001, round up to 13100)
        mockSharedService.calculateFareFunc = (basic, discount) -> {
            return 9001;
        };
        int roundedFare = service.calculateTotalFare(flights, passengers);
        assertEquals(13100, roundedFare);

        pass();
    }

    private static void testValidateReservation_NullReservation() {
        startTest("validateReservation - Reservation is null");
        TicketReserveServiceImpl service = createService();
        try {
            service.validateReservation(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertContains("reservation must not null", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail("Did not expect BusinessException: " + e.getMessage());
        }
    }

    private static void testValidateReservation_EmptyReserveFlightList() {
        startTest("validateReservation - ReserveFlightList is empty");
        TicketReserveServiceImpl service = createService();
        Reservation res = new Reservation();
        res.setReserveFlightList(new ArrayList<>());
        try {
            service.validateReservation(res);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertContains("reserveFlightList must not empty", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail("Did not expect BusinessException: " + e.getMessage());
        }
    }

    private static void testValidateReservation_RepAgeLessThanMin() {
        startTest("validateReservation - Representative age is less than minimum");
        TicketReserveServiceImpl service = createService();
        try {
            setField(service, "representativeMinAge", 18);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        List<ReserveFlight> rfl = new ArrayList<>();
        rfl.add(new ReserveFlight());
        res.setReserveFlightList(rfl);
        res.setRepAge(17);

        try {
            service.validateReservation(res);
            fail("Expected AtrsBusinessException");
        } catch (AtrsBusinessException e) {
            assertContains("e.ar.b2.2004", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail("Expected AtrsBusinessException instead of general BusinessException: " + e.getMessage());
        }
    }

    private static void testValidateReservation_NullReserveFlightInList() {
        startTest("validateReservation - ReserveFlightList contains null element");
        TicketReserveServiceImpl service = createService();
        try {
            setField(service, "representativeMinAge", 18);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        List<ReserveFlight> rfl = new ArrayList<>();
        rfl.add(null);
        res.setReserveFlightList(rfl);
        res.setRepAge(18);

        try {
            service.validateReservation(res);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertContains("reserveFlight must not null", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testValidateReservation_EmptyPassengerListInReserveFlight() {
        startTest("validateReservation - PassengerList in ReserveFlight is empty");
        TicketReserveServiceImpl service = createService();
        try {
            setField(service, "representativeMinAge", 18);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        rf.setPassengerList(new ArrayList<>());
        Flight flight = new Flight();
        FareType ft = new FareType();
        ft.setFareTypeCd(FareTypeCd.OW);
        flight.setFareType(ft);
        rf.setFlight(flight);

        rfl.add(rf);
        res.setReserveFlightList(rfl);
        res.setRepAge(18);

        try {
            service.validateReservation(res);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertContains("passengerList must not empty", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testValidateReservation_LadiesDiscountMalePassenger() {
        startTest("validateReservation - Ladies Discount with male passenger");
        TicketReserveServiceImpl service = createService();
        try {
            setField(service, "representativeMinAge", 18);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        res.setRepAge(18);

        // Rep member is empty (bypass rep member check)
        Member repMember = new Member();
        res.setRepMember(repMember);

        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        FareType ft = new FareType();
        ft.setFareTypeCd(FareTypeCd.LD);
        flight.setFareType(ft);
        rf.setFlight(flight);

        List<Passenger> passengers = new ArrayList<>();
        Passenger p = new Passenger();
        p.setGender(Gender.M); // Male passenger
        // Empty membership number to bypass passenger member check
        Member pm = new Member();
        p.setMember(pm);
        passengers.add(p);
        rf.setPassengerList(passengers);

        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.validateReservation(res);
            fail("Expected AtrsBusinessException");
        } catch (AtrsBusinessException e) {
            assertContains("e.ar.b2.2007", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testValidateReservation_LadiesDiscountNullPassenger() {
        startTest("validateReservation - Ladies Discount with null passenger");
        TicketReserveServiceImpl service = createService();
        try {
            setField(service, "representativeMinAge", 18);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        res.setRepAge(18);
        Member repMember = new Member();
        res.setRepMember(repMember);

        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        FareType ft = new FareType();
        ft.setFareTypeCd(FareTypeCd.LD);
        flight.setFareType(ft);
        rf.setFlight(flight);

        List<Passenger> passengers = new ArrayList<>();
        passengers.add(null);
        rf.setPassengerList(passengers);
        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.validateReservation(res);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertContains("passenger must not null", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testValidateReservation_GroupDiscountLessThanMin() {
        startTest("validateReservation - Group Discount passenger size less than min size");
        TicketReserveServiceImpl service = createService();
        try {
            setField(service, "representativeMinAge", 18);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        res.setRepAge(18);
        Member repMember = new Member();
        res.setRepMember(repMember);

        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        FareType ft = new FareType();
        ft.setFareTypeCd(FareTypeCd.GD);
        ft.setPassengerMinNum(5);
        ft.setFareTypeName("グループ割");
        flight.setFareType(ft);
        rf.setFlight(flight);

        List<Passenger> passengers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Passenger p = new Passenger();
            p.setMember(new Member());
            passengers.add(p);
        }
        rf.setPassengerList(passengers);
        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.validateReservation(res);
            fail("Expected AtrsBusinessException");
        } catch (AtrsBusinessException e) {
            assertContains("e.ar.b2.2010", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testValidateReservation_RepMemberNotFound() {
        startTest("validateReservation - Rep member not found in repository");
        TicketReserveServiceImpl service = createService();
        try {
            setField(service, "representativeMinAge", 18);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        MockMemberRepository mockMemberRepo = new MockMemberRepository();
        mockMemberRepo.findOneFunc = membershipNo -> null;
        try {
            setField(service, "memberRepository", mockMemberRepo);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        res.setRepAge(18);
        Member repMember = new Member();
        repMember.setMembershipNumber("MEMBER001");
        res.setRepMember(repMember);

        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        FareType ft = new FareType();
        ft.setFareTypeCd(FareTypeCd.OW);
        flight.setFareType(ft);
        rf.setFlight(flight);
        List<Passenger> passengers = new ArrayList<>();
        Passenger p = new Passenger();
        p.setMember(new Member());
        passengers.add(p);
        rf.setPassengerList(passengers);
        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.validateReservation(res);
            fail("Expected AtrsBusinessException");
        } catch (AtrsBusinessException e) {
            assertContains("e.ar.b2.2002", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testValidateReservation_RepMemberMismatchFamilyName() {
        startTest("validateReservation - Rep member mismatch in Family Name");
        TicketReserveServiceImpl service = createService();
        try {
            setField(service, "representativeMinAge", 18);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        MockMemberRepository mockMemberRepo = new MockMemberRepository();
        mockMemberRepo.findOneFunc = membershipNo -> {
            Member m = new Member();
            m.setKanaFamilyName("NTT");
            m.setKanaGivenName("Taro");
            m.setGender(Gender.M);
            return m;
        };
        try {
            setField(service, "memberRepository", mockMemberRepo);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        res.setRepAge(18);
        Member repMember = new Member();
        repMember.setMembershipNumber("MEMBER001");
        res.setRepMember(repMember);
        res.setRepFamilyName("MISMATCH");
        res.setRepGivenName("Taro");
        res.setRepGender(Gender.M);

        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        FareType ft = new FareType();
        ft.setFareTypeCd(FareTypeCd.OW);
        flight.setFareType(ft);
        rf.setFlight(flight);
        List<Passenger> passengers = new ArrayList<>();
        Passenger p = new Passenger();
        p.setMember(new Member());
        passengers.add(p);
        rf.setPassengerList(passengers);
        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.validateReservation(res);
            fail("Expected AtrsBusinessException");
        } catch (AtrsBusinessException e) {
            assertContains("e.ar.b2.2003", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testValidateReservation_RepMemberMismatchGivenName() {
        startTest("validateReservation - Rep member mismatch in Given Name");
        TicketReserveServiceImpl service = createService();
        try {
            setField(service, "representativeMinAge", 18);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        MockMemberRepository mockMemberRepo = new MockMemberRepository();
        mockMemberRepo.findOneFunc = membershipNo -> {
            Member m = new Member();
            m.setKanaFamilyName("NTT");
            m.setKanaGivenName("Taro");
            m.setGender(Gender.M);
            return m;
        };
        try {
            setField(service, "memberRepository", mockMemberRepo);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        res.setRepAge(18);
        Member repMember = new Member();
        repMember.setMembershipNumber("MEMBER001");
        res.setRepMember(repMember);
        res.setRepFamilyName("NTT");
        res.setRepGivenName("MISMATCH");
        res.setRepGender(Gender.M);

        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        FareType ft = new FareType();
        ft.setFareTypeCd(FareTypeCd.OW);
        flight.setFareType(ft);
        rf.setFlight(flight);
        List<Passenger> passengers = new ArrayList<>();
        Passenger p = new Passenger();
        p.setMember(new Member());
        passengers.add(p);
        rf.setPassengerList(passengers);
        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.validateReservation(res);
            fail("Expected AtrsBusinessException");
        } catch (AtrsBusinessException e) {
            assertContains("e.ar.b2.2003", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testValidateReservation_RepMemberMismatchGender() {
        startTest("validateReservation - Rep member mismatch in Gender");
        TicketReserveServiceImpl service = createService();
        try {
            setField(service, "representativeMinAge", 18);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        MockMemberRepository mockMemberRepo = new MockMemberRepository();
        mockMemberRepo.findOneFunc = membershipNo -> {
            Member m = new Member();
            m.setKanaFamilyName("NTT");
            m.setKanaGivenName("Taro");
            m.setGender(Gender.M);
            return m;
        };
        try {
            setField(service, "memberRepository", mockMemberRepo);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        res.setRepAge(18);
        Member repMember = new Member();
        repMember.setMembershipNumber("MEMBER001");
        res.setRepMember(repMember);
        res.setRepFamilyName("NTT");
        res.setRepGivenName("Taro");
        res.setRepGender(Gender.F); // Mismatch

        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        FareType ft = new FareType();
        ft.setFareTypeCd(FareTypeCd.OW);
        flight.setFareType(ft);
        rf.setFlight(flight);
        List<Passenger> passengers = new ArrayList<>();
        Passenger p = new Passenger();
        p.setMember(new Member());
        passengers.add(p);
        rf.setPassengerList(passengers);
        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.validateReservation(res);
            fail("Expected AtrsBusinessException");
        } catch (AtrsBusinessException e) {
            assertContains("e.ar.b2.2003", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testValidateReservation_PassengerMemberNotFound() {
        startTest("validateReservation - Passenger member not found in repository");
        TicketReserveServiceImpl service = createService();
        try {
            setField(service, "representativeMinAge", 18);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        MockMemberRepository mockMemberRepo = new MockMemberRepository();
        // Rep member is found, passenger member returns null
        mockMemberRepo.findOneFunc = membershipNo -> {
            if ("REP001".equals(membershipNo)) {
                Member m = new Member();
                m.setKanaFamilyName("NTT");
                m.setKanaGivenName("Taro");
                m.setGender(Gender.M);
                return m;
            }
            return null;
        };
        try {
            setField(service, "memberRepository", mockMemberRepo);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        res.setRepAge(18);
        Member repMember = new Member();
        repMember.setMembershipNumber("REP001");
        res.setRepMember(repMember);
        res.setRepFamilyName("NTT");
        res.setRepGivenName("Taro");
        res.setRepGender(Gender.M);

        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        FareType ft = new FareType();
        ft.setFareTypeCd(FareTypeCd.OW);
        flight.setFareType(ft);
        rf.setFlight(flight);

        List<Passenger> passengers = new ArrayList<>();
        Passenger p = new Passenger();
        Member pm = new Member();
        pm.setMembershipNumber("PASS001");
        p.setMember(pm);
        passengers.add(p);
        rf.setPassengerList(passengers);

        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.validateReservation(res);
            fail("Expected AtrsBusinessException");
        } catch (AtrsBusinessException e) {
            assertContains("e.ar.b2.2005", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testValidateReservation_PassengerMemberMismatchFamilyName() {
        startTest("validateReservation - Passenger member mismatch in Family Name");
        TicketReserveServiceImpl service = createService();
        try {
            setField(service, "representativeMinAge", 18);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        MockMemberRepository mockMemberRepo = new MockMemberRepository();
        mockMemberRepo.findOneFunc = membershipNo -> {
            Member m = new Member();
            m.setKanaFamilyName("PAS_FAM");
            m.setKanaGivenName("PAS_GIV");
            m.setGender(Gender.F);
            return m;
        };
        try {
            setField(service, "memberRepository", mockMemberRepo);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        res.setRepAge(18);
        Member repMember = new Member(); // empty membership bypasses rep check
        res.setRepMember(repMember);

        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        FareType ft = new FareType();
        ft.setFareTypeCd(FareTypeCd.OW);
        flight.setFareType(ft);
        rf.setFlight(flight);

        List<Passenger> passengers = new ArrayList<>();
        Passenger p = new Passenger();
        p.setFamilyName("MISMATCH");
        p.setGivenName("PAS_GIV");
        p.setGender(Gender.F);
        Member pm = new Member();
        pm.setMembershipNumber("PASS001");
        p.setMember(pm);
        passengers.add(p);
        rf.setPassengerList(passengers);

        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.validateReservation(res);
            fail("Expected AtrsBusinessException");
        } catch (AtrsBusinessException e) {
            assertContains("e.ar.b2.2006", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testValidateReservation_PassengerMemberMismatchGivenName() {
        startTest("validateReservation - Passenger member mismatch in Given Name");
        TicketReserveServiceImpl service = createService();
        try {
            setField(service, "representativeMinAge", 18);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        MockMemberRepository mockMemberRepo = new MockMemberRepository();
        mockMemberRepo.findOneFunc = membershipNo -> {
            Member m = new Member();
            m.setKanaFamilyName("PAS_FAM");
            m.setKanaGivenName("PAS_GIV");
            m.setGender(Gender.F);
            return m;
        };
        try {
            setField(service, "memberRepository", mockMemberRepo);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        res.setRepAge(18);
        Member repMember = new Member();
        res.setRepMember(repMember);

        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        FareType ft = new FareType();
        ft.setFareTypeCd(FareTypeCd.OW);
        flight.setFareType(ft);
        rf.setFlight(flight);

        List<Passenger> passengers = new ArrayList<>();
        Passenger p = new Passenger();
        p.setFamilyName("PAS_FAM");
        p.setGivenName("MISMATCH");
        p.setGender(Gender.F);
        Member pm = new Member();
        pm.setMembershipNumber("PASS001");
        p.setMember(pm);
        passengers.add(p);
        rf.setPassengerList(passengers);

        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.validateReservation(res);
            fail("Expected AtrsBusinessException");
        } catch (AtrsBusinessException e) {
            assertContains("e.ar.b2.2006", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testValidateReservation_PassengerMemberMismatchGender() {
        startTest("validateReservation - Passenger member mismatch in Gender");
        TicketReserveServiceImpl service = createService();
        try {
            setField(service, "representativeMinAge", 18);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        MockMemberRepository mockMemberRepo = new MockMemberRepository();
        mockMemberRepo.findOneFunc = membershipNo -> {
            Member m = new Member();
            m.setKanaFamilyName("PAS_FAM");
            m.setKanaGivenName("PAS_GIV");
            m.setGender(Gender.F);
            return m;
        };
        try {
            setField(service, "memberRepository", mockMemberRepo);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        res.setRepAge(18);
        Member repMember = new Member();
        res.setRepMember(repMember);

        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        FareType ft = new FareType();
        ft.setFareTypeCd(FareTypeCd.OW);
        flight.setFareType(ft);
        rf.setFlight(flight);

        List<Passenger> passengers = new ArrayList<>();
        Passenger p = new Passenger();
        p.setFamilyName("PAS_FAM");
        p.setGivenName("PAS_GIV");
        p.setGender(Gender.M); // Mismatch
        Member pm = new Member();
        pm.setMembershipNumber("PASS001");
        p.setMember(pm);
        passengers.add(p);
        rf.setPassengerList(passengers);

        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.validateReservation(res);
            fail("Expected AtrsBusinessException");
        } catch (AtrsBusinessException e) {
            assertContains("e.ar.b2.2006", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testValidateReservation_HappyPath() {
        startTest("validateReservation - Happy Path");
        TicketReserveServiceImpl service = createService();
        try {
            setField(service, "representativeMinAge", 18);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        MockMemberRepository mockMemberRepo = new MockMemberRepository();
        mockMemberRepo.findOneFunc = membershipNo -> {
            Member m = new Member();
            m.setKanaFamilyName("TEST_FAM");
            m.setKanaGivenName("TEST_GIV");
            m.setGender(Gender.F);
            return m;
        };
        try {
            setField(service, "memberRepository", mockMemberRepo);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        res.setRepAge(18);
        Member repMember = new Member();
        repMember.setMembershipNumber("REP001");
        res.setRepMember(repMember);
        res.setRepFamilyName("TEST_FAM");
        res.setRepGivenName("TEST_GIV");
        res.setRepGender(Gender.F);

        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        FareType ft = new FareType();
        ft.setFareTypeCd(FareTypeCd.OW);
        flight.setFareType(ft);
        rf.setFlight(flight);

        List<Passenger> passengers = new ArrayList<>();
        Passenger p = new Passenger();
        p.setFamilyName("TEST_FAM");
        p.setGivenName("TEST_GIV");
        p.setGender(Gender.F);
        Member pm = new Member();
        pm.setMembershipNumber("PASS001");
        p.setMember(pm);
        passengers.add(p);
        rf.setPassengerList(passengers);

        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.validateReservation(res);
            pass();
        } catch (Exception e) {
            fail("Expected validation to pass but got: " + e.getMessage());
        }
    }

    private static void testRegisterReservation_NullReservation() {
        startTest("registerReservation - Reservation is null");
        TicketReserveServiceImpl service = createService();
        try {
            service.registerReservation(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertContains("reservation must not null", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testRegisterReservation_EmptyReserveFlightList() {
        startTest("registerReservation - ReserveFlightList is empty");
        TicketReserveServiceImpl service = createService();
        Reservation res = new Reservation();
        res.setReserveFlightList(new ArrayList<>());
        try {
            service.registerReservation(res);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertContains("reserveFlightList must not empty", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testRegisterReservation_NullReserveFlight() {
        startTest("registerReservation - ReserveFlight is null");
        TicketReserveServiceImpl service = createService();
        Reservation res = new Reservation();
        List<ReserveFlight> rfl = new ArrayList<>();
        rfl.add(null);
        res.setReserveFlightList(rfl);
        try {
            service.registerReservation(res);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertContains("reserveFlight must not null", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testRegisterReservation_NullFlight() {
        startTest("registerReservation - Flight in ReserveFlight is null");
        TicketReserveServiceImpl service = createService();
        Reservation res = new Reservation();
        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        rf.setFlight(null);
        rfl.add(rf);
        res.setReserveFlightList(rfl);
        try {
            service.registerReservation(res);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertContains("flight must not null", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testRegisterReservation_NotAvailableFareType() {
        startTest("registerReservation - FareType not available on departure date");
        TicketReserveServiceImpl service = createService();

        MockTicketSharedService mockSharedService = new MockTicketSharedService();
        mockSharedService.isAvailableFareTypeFunc = (ft, date) -> false;
        try {
            setField(service, "ticketSharedService", mockSharedService);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        flight.setFareType(new FareType());
        flight.setDepartureDate(LocalDate.now());
        rf.setFlight(flight);
        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.registerReservation(res);
            fail("Expected AtrsBusinessException");
        } catch (AtrsBusinessException e) {
            assertContains("e.ar.b2.2008", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testRegisterReservation_VacancyLessThanPassengerNum() {
        startTest("registerReservation - Vacant seats less than passenger count");
        TicketReserveServiceImpl service = createService();

        MockTicketSharedService mockSharedService = new MockTicketSharedService();
        mockSharedService.isAvailableFareTypeFunc = (ft, date) -> true;

        MockFlightRepository mockFlightRepo = new MockFlightRepository();
        mockFlightRepo.findOneForUpdateFunc = (date, name) -> {
            Flight f = new Flight();
            f.setVacantNum(2); // Only 2 vacant seats
            return f;
        };

        try {
            setField(service, "ticketSharedService", mockSharedService);
            setField(service, "flightRepository", mockFlightRepo);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        flight.setFareType(new FareType());
        flight.setDepartureDate(LocalDate.now());
        FlightMaster fm = new FlightMaster();
        fm.setFlightName("NH241");
        flight.setFlightMaster(fm);
        rf.setFlight(flight);

        List<Passenger> passengers = new ArrayList<>();
        passengers.add(new Passenger());
        passengers.add(new Passenger());
        passengers.add(new Passenger()); // 3 passengers
        rf.setPassengerList(passengers);

        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.registerReservation(res);
            fail("Expected AtrsBusinessException");
        } catch (AtrsBusinessException e) {
            assertContains("e.ar.b2.2009", e.getMessage());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testRegisterReservation_FlightUpdateCountNotOne() {
        startTest("registerReservation - Flight update count is not 1");
        TicketReserveServiceImpl service = createService();

        MockTicketSharedService mockSharedService = new MockTicketSharedService();
        mockSharedService.isAvailableFareTypeFunc = (ft, date) -> true;

        MockFlightRepository mockFlightRepo = new MockFlightRepository();
        mockFlightRepo.findOneForUpdateFunc = (date, name) -> {
            Flight f = new Flight();
            f.setVacantNum(5);
            return f;
        };
        mockFlightRepo.updateFunc = flight -> 0; // Update fails

        try {
            setField(service, "ticketSharedService", mockSharedService);
            setField(service, "flightRepository", mockFlightRepo);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        flight.setFareType(new FareType());
        flight.setDepartureDate(LocalDate.now());
        FlightMaster fm = new FlightMaster();
        fm.setFlightName("NH241");
        flight.setFlightMaster(fm);
        rf.setFlight(flight);

        List<Passenger> passengers = new ArrayList<>();
        passengers.add(new Passenger());
        rf.setPassengerList(passengers);

        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.registerReservation(res);
            fail("Expected SystemException");
        } catch (SystemException e) {
            assertContains("e.ar.a0.l9002", e.getCode());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testRegisterReservation_ReservationInsertCountNotOne() {
        startTest("registerReservation - Reservation insert count is not 1");
        TicketReserveServiceImpl service = createService();

        MockTicketSharedService mockSharedService = new MockTicketSharedService();
        mockSharedService.isAvailableFareTypeFunc = (ft, date) -> true;

        MockFlightRepository mockFlightRepo = new MockFlightRepository();
        mockFlightRepo.findOneForUpdateFunc = (date, name) -> {
            Flight f = new Flight();
            f.setVacantNum(5);
            return f;
        };
        mockFlightRepo.updateFunc = flight -> 1;

        MockReservationRepository mockResRepo = new MockReservationRepository();
        mockResRepo.insertFunc = r -> 0; // Insert fails

        try {
            setField(service, "ticketSharedService", mockSharedService);
            setField(service, "flightRepository", mockFlightRepo);
            setField(service, "reservationRepository", mockResRepo);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        flight.setFareType(new FareType());
        flight.setDepartureDate(LocalDate.now());
        FlightMaster fm = new FlightMaster();
        fm.setFlightName("NH241");
        flight.setFlightMaster(fm);
        rf.setFlight(flight);

        List<Passenger> passengers = new ArrayList<>();
        passengers.add(new Passenger());
        rf.setPassengerList(passengers);

        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.registerReservation(res);
            fail("Expected SystemException");
        } catch (SystemException e) {
            assertContains("e.ar.a0.l9002", e.getCode());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testRegisterReservation_ReserveFlightInsertCountNotOne() {
        startTest("registerReservation - ReserveFlight insert count is not 1");
        TicketReserveServiceImpl service = createService();

        MockTicketSharedService mockSharedService = new MockTicketSharedService();
        mockSharedService.isAvailableFareTypeFunc = (ft, date) -> true;

        MockFlightRepository mockFlightRepo = new MockFlightRepository();
        mockFlightRepo.findOneForUpdateFunc = (date, name) -> {
            Flight f = new Flight();
            f.setVacantNum(5);
            return f;
        };
        mockFlightRepo.updateFunc = flight -> 1;

        MockReservationRepository mockResRepo = new MockReservationRepository();
        mockResRepo.insertFunc = r -> {
            r.setReserveNo("RSV0001");
            return 1;
        };
        mockResRepo.insertReserveFlightFunc = rf -> 0; // Insert fails

        try {
            setField(service, "ticketSharedService", mockSharedService);
            setField(service, "flightRepository", mockFlightRepo);
            setField(service, "reservationRepository", mockResRepo);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        flight.setFareType(new FareType());
        flight.setDepartureDate(LocalDate.now());
        FlightMaster fm = new FlightMaster();
        fm.setFlightName("NH241");
        flight.setFlightMaster(fm);
        rf.setFlight(flight);

        List<Passenger> passengers = new ArrayList<>();
        passengers.add(new Passenger());
        rf.setPassengerList(passengers);

        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.registerReservation(res);
            fail("Expected SystemException");
        } catch (SystemException e) {
            assertContains("e.ar.a0.l9002", e.getCode());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testRegisterReservation_PassengerInsertCountNotOne() {
        startTest("registerReservation - Passenger insert count is not 1");
        TicketReserveServiceImpl service = createService();

        MockTicketSharedService mockSharedService = new MockTicketSharedService();
        mockSharedService.isAvailableFareTypeFunc = (ft, date) -> true;

        MockFlightRepository mockFlightRepo = new MockFlightRepository();
        mockFlightRepo.findOneForUpdateFunc = (date, name) -> {
            Flight f = new Flight();
            f.setVacantNum(5);
            return f;
        };
        mockFlightRepo.updateFunc = flight -> 1;

        MockReservationRepository mockResRepo = new MockReservationRepository();
        mockResRepo.insertFunc = r -> {
            r.setReserveNo("RSV0001");
            return 1;
        };
        mockResRepo.insertReserveFlightFunc = rf -> {
            rf.setReserveFlightNo(99);
            return 1;
        };
        mockResRepo.insertPassengerFunc = p -> 0; // Insert fails

        try {
            setField(service, "ticketSharedService", mockSharedService);
            setField(service, "flightRepository", mockFlightRepo);
            setField(service, "reservationRepository", mockResRepo);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        flight.setFareType(new FareType());
        flight.setDepartureDate(LocalDate.now());
        FlightMaster fm = new FlightMaster();
        fm.setFlightName("NH241");
        flight.setFlightMaster(fm);
        rf.setFlight(flight);

        List<Passenger> passengers = new ArrayList<>();
        passengers.add(new Passenger());
        rf.setPassengerList(passengers);

        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            service.registerReservation(res);
            fail("Expected SystemException");
        } catch (SystemException e) {
            assertContains("e.ar.a0.l9002", e.getCode());
            pass();
        } catch (BusinessException e) {
            fail(e.getMessage());
        }
    }

    private static void testRegisterReservation_HappyPath() {
        startTest("registerReservation - Happy Path");
        TicketReserveServiceImpl service = createService();

        MockTicketSharedService mockSharedService = new MockTicketSharedService();
        mockSharedService.isAvailableFareTypeFunc = (ft, date) -> true;

        MockFlightRepository mockFlightRepo = new MockFlightRepository();
        mockFlightRepo.findOneForUpdateFunc = (date, name) -> {
            Flight f = new Flight();
            f.setVacantNum(5);
            return f;
        };
        mockFlightRepo.updateFunc = flight -> 1;

        MockReservationRepository mockResRepo = new MockReservationRepository();
        mockResRepo.insertFunc = r -> {
            r.setReserveNo("RSV0001");
            return 1;
        };
        mockResRepo.insertReserveFlightFunc = rf -> {
            rf.setReserveFlightNo(99);
            return 1;
        };
        mockResRepo.insertPassengerFunc = p -> 1;

        try {
            setField(service, "ticketSharedService", mockSharedService);
            setField(service, "flightRepository", mockFlightRepo);
            setField(service, "reservationRepository", mockResRepo);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Reservation res = new Reservation();
        List<ReserveFlight> rfl = new ArrayList<>();
        ReserveFlight rf = new ReserveFlight();
        Flight flight = new Flight();
        flight.setFareType(new FareType());
        LocalDate departureDate = LocalDate.of(2026, 7, 10);
        flight.setDepartureDate(departureDate);
        FlightMaster fm = new FlightMaster();
        fm.setFlightName("NH241");
        flight.setFlightMaster(fm);
        rf.setFlight(flight);

        List<Passenger> passengers = new ArrayList<>();
        passengers.add(new Passenger());
        rf.setPassengerList(passengers);

        rfl.add(rf);
        res.setReserveFlightList(rfl);

        try {
            TicketReserveDto dto = service.registerReservation(res);
            assertEquals("RSV0001", dto.getReserveNo());
            assertEquals(departureDate, dto.getPaymentDate());
            pass();
        } catch (Exception e) {
            fail("Expected registration to pass but got: " + e.getMessage());
        }
    }

    private static void testFindMember_EmptyMembershipNo() {
        startTest("findMember - membershipNumber is empty");
        TicketReserveServiceImpl service = createService();
        try {
            service.findMember("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertContains("membershipNumber must have some text", e.getMessage());
            pass();
        }
    }

    private static void testFindMember_NullMembershipNo() {
        startTest("findMember - membershipNumber is null");
        TicketReserveServiceImpl service = createService();
        try {
            service.findMember(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertContains("membershipNumber must have some text", e.getMessage());
            pass();
        }
    }

    private static void testFindMember_HappyPath() {
        startTest("findMember - Happy Path");
        TicketReserveServiceImpl service = createService();

        MockMemberRepository mockMemberRepo = new MockMemberRepository();
        mockMemberRepo.findOneFunc = membershipNo -> {
            Member m = new Member();
            m.setMembershipNumber(membershipNo);
            return m;
        };

        try {
            setField(service, "memberRepository", mockMemberRepo);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        Member m = service.findMember("MEM999");
        assertEquals("MEM999", m.getMembershipNumber());
        pass();
    }

    // ==========================================
    // Mocks / Stubs
    // ==========================================

    public static class MockFlightRepository implements FlightRepository {
        public java.util.function.BiFunction<LocalDate, String, Flight> findOneForUpdateFunc;
        public java.util.function.Function<Flight, Integer> updateFunc;

        @Override
        public Flight findOneForUpdate(LocalDate departureDate, String flightName, BoardingClass boardingClass, FareType fareType) {
            if (findOneForUpdateFunc != null) {
                return findOneForUpdateFunc.apply(departureDate, flightName);
            }
            return null;
        }

        @Override
        public int update(Flight flight) {
            if (updateFunc != null) {
                return updateFunc.apply(flight);
            }
            return 0;
        }

        @Override
        public List<Flight> findByVacantSeatSearchCriteria(VacantSeatSearchCriteriaDto criteria) {
            return null;
        }

        @Override
        public List<FlightMaster> findAllFlightMaster() {
            return null;
        }

        @Override
        public boolean exists(LocalDate departureDate, String flightName, BoardingClass boardingClass, FareType fareType) {
            return false;
        }
    }

    public static class MockMemberRepository implements MemberRepository {
        public java.util.function.Function<String, Member> findOneFunc;

        @Override
        public Member findOne(String membershipNumber) {
            if (findOneFunc != null) {
                return findOneFunc.apply(membershipNumber);
            }
            return null;
        }

        @Override
        public Member findOneForLogin(String membershipNumber) { return null; }
        @Override
        public int updateToLoginStatus(Member member) { return 0; }
        @Override
        public int updateToLogoutStatus(Member member) { return 0; }
        @Override
        public int insert(Member member) { return 0; }
        @Override
        public int insertMemberLogin(Member member) { return 0; }
        @Override
        public int update(Member member) { return 0; }
        @Override
        public int updateMemberLogin(Member member) { return 0; }
    }

    public static class MockReservationRepository implements ReservationRepository {
        public java.util.function.Function<Reservation, Integer> insertFunc;
        public java.util.function.Function<ReserveFlight, Integer> insertReserveFlightFunc;
        public java.util.function.Function<Passenger, Integer> insertPassengerFunc;

        @Override
        public int insert(Reservation reservation) {
            if (insertFunc != null) {
                return insertFunc.apply(reservation);
            }
            return 0;
        }

        @Override
        public int insertReserveFlight(ReserveFlight reserveFlight) {
            if (insertReserveFlightFunc != null) {
                return insertReserveFlightFunc.apply(reserveFlight);
            }
            return 0;
        }

        @Override
        public int insertPassenger(Passenger passenger) {
            if (insertPassengerFunc != null) {
                return insertPassengerFunc.apply(passenger);
            }
            return 0;
        }

        @Override
        public List<ReservationHistoryDto> findAllByMembershipNumberForReport(String membershipNumber) {
            return null;
        }
    }

    public static class MockTicketSharedService implements TicketSharedService {
        public java.util.function.BiFunction<FareType, LocalDate, Boolean> isAvailableFareTypeFunc;
        public java.util.function.Function<Object[], Integer> calculateBasicFareFunc;
        public java.util.function.BiFunction<Integer, Integer, Integer> calculateFareFunc;

        @Override
        public boolean isAvailableFareType(FareType fareType, LocalDate depDate) {
            if (isAvailableFareTypeFunc != null) {
                return isAvailableFareTypeFunc.apply(fareType, depDate);
            }
            return false;
        }

        @Override
        public int calculateBasicFare(int basicFareOfRoute, BoardingClassCd boardingClassCd, LocalDate depDate) {
            if (calculateBasicFareFunc != null) {
                return calculateBasicFareFunc.apply(new Object[]{basicFareOfRoute, boardingClassCd, depDate});
            }
            return 0;
        }

        @Override
        public int calculateFare(int basicFare, int discountRate) {
            if (calculateFareFunc != null) {
                return calculateFareFunc.apply(basicFare, discountRate);
            }
            return 0;
        }

        @Override
        public LocalDate getSearchLimitDate() { return null; }
        @Override
        public void validateFlightList(List<Flight> flightList) {}
        @Override
        public void validateDepatureDate(LocalDate departureDate) {}
        @Override
        public boolean existsFlight(Flight flight) { return false; }
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    private static TicketReserveServiceImpl createService() {
        return new TicketReserveServiceImpl();
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void startTest(String testName) {
        totalTests++;
        System.out.printf("Test %02d: %s ... ", totalTests, testName);
    }

    private static void pass() {
        passedTests++;
        System.out.println("PASS");
    }

    private static void fail(String message) {
        System.out.println("FAIL");
        System.err.println("  => Failure reason: " + message);
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(String.format("Expected: %s, but actual was: %s", expected, actual));
    }

    private static void assertContains(String expectedSubstring, String actualString) {
        if (actualString == null || !actualString.toLowerCase().contains(expectedSubstring.toLowerCase())) {
            throw new AssertionError(String.format("Expected to contain: \"%s\", but actual was: \"%s\"", expectedSubstring, actualString));
        }
    }
}
