package jp.co.ntt.atrs.domain.service.b2;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.terasoluna.gfw.common.exception.BusinessException;
import org.terasoluna.gfw.common.exception.SystemException;

import jakarta.inject.Inject;
import jp.co.ntt.atrs.domain.common.exception.AtrsBusinessException;
import jp.co.ntt.atrs.domain.common.logging.LogMessages;
import jp.co.ntt.atrs.domain.common.util.FareUtil;
import jp.co.ntt.atrs.domain.model.FareType;
import jp.co.ntt.atrs.domain.model.FareTypeCd;
import jp.co.ntt.atrs.domain.model.Flight;
import jp.co.ntt.atrs.domain.model.Gender;
import jp.co.ntt.atrs.domain.model.Member;
import jp.co.ntt.atrs.domain.model.Passenger;
import jp.co.ntt.atrs.domain.model.Reservation;
import jp.co.ntt.atrs.domain.model.ReserveFlight;
import jp.co.ntt.atrs.domain.repository.flight.FlightRepository;
import jp.co.ntt.atrs.domain.repository.member.MemberRepository;
import jp.co.ntt.atrs.domain.repository.reservation.ReservationRepository;
import jp.co.ntt.atrs.domain.service.b0.TicketSharedService;

/**
 * チケット予約サービス実装クラス。
 * @author Dummy 電電三郎
 */
@Service
@Transactional
public class TicketReserveServiceImpl implements TicketReserveService {

    /**
     * 予約代表者に必要な最小年齢。
     */
    @Value("${atrs.representativeMinAge}")
    private int representativeMinAge;

    /**
     * 大人運賃が適用される最小年齢。
     */
    @Value("${atrs.adultPassengerMinAge}")
    private int adultPassengerMinAge;

    /**
     * 大人運賃に対する小児運賃の比率(%)。
     */
    @Value("${atrs.childFareRate}")
    private int childFareRate;

    /**
     * フライト情報リポジトリ。
     */
    @Inject
    FlightRepository flightRepository;

    /**
     * カード会員情報リポジトリ。
     */
    @Inject
    MemberRepository memberRepository;

    /**
     * 予約情報リポジトリ。
     */
    @Inject
    ReservationRepository reservationRepository;

    /**
     * チケット共通サービス。
     */
    @Inject
    TicketSharedService ticketSharedService;

    /**
     * 指定された会員番号に該当するカード会員情報を取得する。
     * @param membershipNumber 会員番号
     * @return カード会員情報
     */
    @Override
    public Member findMember(String membershipNumber) {
        // 1. Parameter validation
        if (membershipNumber == null || membershipNumber.isEmpty() || membershipNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("membershipNumber must have some text.");
        }
        // 2. Get member info from repository
        return memberRepository.findOne(membershipNumber);
    }

    /**
     * 予約代表者の年齢が、予約代表者に必要な最小年齢以上であることをチェックする。
     * @param age 年齢
     */
    private void validateRepresentativeAge(int age) {
        if (age < representativeMinAge) {
            throw new AtrsBusinessException(TicketReserveErrorCode.E_AR_B2_2004, representativeMinAge);
        }
    }

    /**
     * 予約フライト情報において、適用されている運賃種別の制限（レディース割、グループ割）を満たしているかを検証する。
     * @param reserveFlightList 予約フライト情報リスト
     * @throws AtrsBusinessException 業務例外
     */
    private void validateFareType(List<ReserveFlight> reserveFlightList) throws AtrsBusinessException {
        for (ReserveFlight reserveFlight : reserveFlightList) {
            if (reserveFlight == null) {
                throw new IllegalArgumentException("reserveFlight must not null.");
            }
            List<Passenger> passengerList = reserveFlight.getPassengerList();
            if (passengerList == null || passengerList.isEmpty()) {
                throw new IllegalArgumentException("passengerList must not empty.");
            }

            FareTypeCd fareTypeCd = reserveFlight.getFlight().getFareType().getFareTypeCd();

            if (FareTypeCd.LD == fareTypeCd) {
                // レディース割: 搭乗者の中に男性が含まれていないかチェック
                for (Passenger passenger : passengerList) {
                    if (passenger == null) {
                        throw new IllegalArgumentException("passenger must not null.");
                    }
                    if (Gender.M == passenger.getGender()) {
                        throw new AtrsBusinessException(TicketReserveErrorCode.E_AR_B2_2007);
                    }
                }
            } else if (FareTypeCd.GD == fareTypeCd) {
                // グループ割: 搭乗者数が利用可能最少人数以上かチェック
                FareType fareType = reserveFlight.getFlight().getFareType();
                int passengerMinNum = fareType.getPassengerMinNum();
                if (passengerList.size() < passengerMinNum) {
                    throw new AtrsBusinessException(TicketReserveErrorCode.E_AR_B2_2010,
                            fareType.getFareTypeName(), passengerMinNum);
                }
            }
        }
    }

    /**
     * 予約代表者の会員番号が設定されている場合、登録されているカード会員情報と一致しているかをチェックする。
     * @param reservation 予約情報
     * @throws AtrsBusinessException 業務例外
     */
    private void validateRepresentativeMemberInfo(Reservation reservation) throws AtrsBusinessException {
        // 予約代表者会員番号を取得
        String repMembershipNumber = reservation.getRepMember().getMembershipNumber();

        // 会員番号が入力されている場合のみチェック
        if (StringUtils.hasLength(repMembershipNumber)) {
            // 会員情報を取得
            Member repMember = memberRepository.findOne(repMembershipNumber);
            if (repMember == null) {
                throw new AtrsBusinessException(TicketReserveErrorCode.E_AR_B2_2002);
            }

            // 会員情報と予約代表者情報の一致を確認
            if (!reservation.getRepFamilyName().equals(repMember.getKanaFamilyName())
                    || !reservation.getRepGivenName().equals(repMember.getKanaGivenName())
                    || reservation.getRepGender() != repMember.getGender()) {
                throw new AtrsBusinessException(TicketReserveErrorCode.E_AR_B2_2003);
            }
        }
    }

    /**
     * 搭乗者情報に会員番号が設定されている場合、登録されているカード会員情報と一致しているかを照合する。
     * @param reserveFlightList 予約フライト情報リスト
     * @throws AtrsBusinessException 業務例外
     */
    private void validatePassengerMemberInfo(List<ReserveFlight> reserveFlightList) throws AtrsBusinessException {
        for (ReserveFlight reserveFlight : reserveFlightList) {
            if (reserveFlight == null) {
                throw new IllegalArgumentException("reserveFlight must not null.");
            }
            List<Passenger> passengerList = reserveFlight.getPassengerList();
            if (passengerList == null || passengerList.isEmpty()) {
                throw new IllegalArgumentException("passengerList must not empty.");
            }

            int position = 1;
            for (Passenger passenger : passengerList) {
                if (passenger == null) {
                    throw new IllegalArgumentException("passenger must not null.");
                }

                String membershipNumber = passenger.getMember().getMembershipNumber();
                if (StringUtils.hasLength(membershipNumber)) {
                    Member passengerMember = memberRepository.findOne(membershipNumber);
                    if (passengerMember == null) {
                        throw new AtrsBusinessException(TicketReserveErrorCode.E_AR_B2_2005, position);
                    }

                    if (!passenger.getFamilyName().equals(passengerMember.getKanaFamilyName())
                            || !passenger.getGivenName().equals(passengerMember.getKanaGivenName())
                            || passenger.getGender() != passengerMember.getGender()) {
                        throw new AtrsBusinessException(TicketReserveErrorCode.E_AR_B2_2006, position);
                    }
                }
                position++;
            }
        }
    }

    /**
     * 予約代表者の年齢、運賃種別の適用条件、および会員情報の一致検証を一括して行う。
     * @param reservation 予約情報
     * @throws BusinessException 業務例外
     */
    @Override
    public void validateReservation(Reservation reservation) throws BusinessException {
        // 1. パラメータのバリデーションチェック
        if (reservation == null) {
            throw new IllegalArgumentException("reservation must not null.");
        }
        List<ReserveFlight> reserveFlightList = reservation.getReserveFlightList();
        if (reserveFlightList == null || reserveFlightList.isEmpty()) {
            throw new IllegalArgumentException("reserveFlightList must not empty.");
        }

        // 2. 予約代表者年齢チェック
        validateRepresentativeAge(reservation.getRepAge());

        // 3. 運賃種別適用チェック
        validateFareType(reserveFlightList);

        // 4. 予約代表者会員情報チェック
        validateRepresentativeMemberInfo(reservation);

        // 5. 搭乗者会員情報チェック
        validatePassengerMemberInfo(reserveFlightList);
    }

    /**
     * 予約チケットの合計金額を計算する。
     * @param flightList 予約するフライトのリスト
     * @param passengerList 搭乗者リスト
     * @return 予約チケットの合計金額
     */
    @Override
    public int calculateTotalFare(List<Flight> flightList, List<Passenger> passengerList) {
        // 1. パラメータのバリデーションチェック
        if (flightList == null || flightList.isEmpty()) {
            throw new IllegalArgumentException("flightList must not empty.");
        }
        if (passengerList == null || passengerList.isEmpty()) {
            throw new IllegalArgumentException("passengerList must not empty.");
        }

        // 2. 搭乗者区分の集計
        int childNum = 0;
        for (Passenger passenger : passengerList) {
            if (passenger == null) {
                throw new IllegalArgumentException("passenger must not null.");
            }
            if (passenger.getAge() < adultPassengerMinAge) {
                childNum++;
            }
        }
        int adultNum = passengerList.size() - childNum;

        // 3. 運賃の計算
        int totalFare = 0;
        for (Flight flight : flightList) {
            if (flight == null) {
                throw new IllegalArgumentException("flight must not null.");
            }

            // 基本運賃を算出
            int baseFare = ticketSharedService.calculateBasicFare(
                    flight.getFlightMaster().getRoute().getBasicFare(),
                    flight.getBoardingClass().getBoardingClassCd(),
                    flight.getDepartureDate());

            // 割引率を取得
            int discountRate = flight.getFareType().getDiscountRate();

            // 割引適用後の大人運賃を算出
            int boardingFare = ticketSharedService.calculateFare(baseFare, discountRate);

            // フライト単位の運賃を算出
            int fare = (boardingFare * adultNum) + (baseFare * (childFareRate - discountRate) / 100 * childNum);
            totalFare += fare;
        }

        // 4. 端数処理（100円未満切り上げ）
        return FareUtil.ceilFare(totalFare);
    }

    /**
     * 予約フライト情報に設定されたフライトの空席数を悲観的ロックで確認・更新する。また、搭乗日が運賃種別の予約可能時期範囲内かチェックする。
     * @param reserveFlight 予約フライト情報
     * @throws AtrsBusinessException 業務例外
     */
    private void validateAndUpdateVacancy(ReserveFlight reserveFlight) throws AtrsBusinessException {
        // 1. パラメータのバリデーションチェック
        if (reserveFlight == null) {
            throw new IllegalArgumentException("reserveFlight must not null.");
        }
        Flight flight = reserveFlight.getFlight();
        if (flight == null) {
            throw new IllegalArgumentException("flight must not null.");
        }

        // 2. 予約可能時期チェック
        if (!ticketSharedService.isAvailableFareType(flight.getFareType(), flight.getDepartureDate())) {
            throw new AtrsBusinessException(TicketReserveErrorCode.E_AR_B2_2008);
        }

        // 3. 空席状況の確認および確保(排他ロック)
        flight = flightRepository.findOneForUpdate(
                flight.getDepartureDate(),
                flight.getFlightMaster().getFlightName(),
                flight.getBoardingClass(),
                flight.getFareType());

        int vacantNum = flight.getVacantNum();
        int passengerNum = reserveFlight.getPassengerList().size();

        if (vacantNum < passengerNum) {
            throw new AtrsBusinessException(TicketReserveErrorCode.E_AR_B2_2009);
        }

        flight.setVacantNum(vacantNum - passengerNum);

        // 4. フライト情報の更新
        int flightUpdateCount = flightRepository.update(flight);
        if (flightUpdateCount != 1) {
            throw new SystemException(LogMessages.E_AR_A0_L9002.getCode(),
                    LogMessages.E_AR_A0_L9002.getMessage(flightUpdateCount, 1));
        }
    }

    /**
     * 予約の空席確認と確保、およびデータベースへの予約登録を行い、予約完了後の情報（予約番号と支払期限）を含む DTO を返却する。
     * @param reservation 予約情報
     * @return 予約完了後の情報（予約番号と支払期限）を含む DTO
     * @throws BusinessException 空席数が搭乗者数未満の場合にスローする例外
     */
    @Override
    public TicketReserveDto registerReservation(Reservation reservation) throws BusinessException {
        // 1. パラメータのバリデーションチェック
        if (reservation == null) {
            throw new IllegalArgumentException("reservation must not null.");
        }
        List<ReserveFlight> reserveFlightList = reservation.getReserveFlightList();
        if (reserveFlightList == null || reserveFlightList.isEmpty()) {
            throw new IllegalArgumentException("reserveFlightList must not empty.");
        }

        // 2. 空席状況の確認および更新
        for (ReserveFlight reserveFlight : reserveFlightList) {
            validateAndUpdateVacancy(reserveFlight);
        }

        // 3. 予約情報の登録
        int reservationInsertCount = reservationRepository.insert(reservation);
        if (reservationInsertCount != 1) {
            throw new SystemException(LogMessages.E_AR_A0_L9002.getCode(),
                    LogMessages.E_AR_A0_L9002.getMessage(reservationInsertCount, 1));
        }

        // 4. 予約番号の取得
        String reserveNo = reservation.getReserveNo();

        // 5. 予約フライト情報および搭乗者情報の登録
        for (ReserveFlight reserveFlight : reserveFlightList) {
            reserveFlight.setReserveNo(reserveNo);

            int reserveFlightInsertCount = reservationRepository.insertReserveFlight(reserveFlight);
            if (reserveFlightInsertCount != 1) {
                throw new SystemException(LogMessages.E_AR_A0_L9002.getCode(),
                        LogMessages.E_AR_A0_L9002.getMessage(reserveFlightInsertCount, 1));
            }

            for (Passenger passenger : reserveFlight.getPassengerList()) {
                passenger.setReserveFlightNo(reserveFlight.getReserveFlightNo());

                int passengerInsertCount = reservationRepository.insertPassenger(passenger);
                if (passengerInsertCount != 1) {
                    throw new SystemException(LogMessages.E_AR_A0_L9002.getCode(),
                            LogMessages.E_AR_A0_L9002.getMessage(passengerInsertCount, 1));
                }
            }
        }

        // 6. 戻り値の構築
        LocalDate paymentDate = reserveFlightList.get(0).getFlight().getDepartureDate();
        return new TicketReserveDto(reserveNo, paymentDate);
    }
}
