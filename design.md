# クラス概要

本クラスは、チケット予約システムにおける予約処理および関連する検証、料金計算を行うサービス実装クラスである。
チケット予約画面等からの要求に応じ、以下の機能を提供する。

1. **合計運賃の計算**: 選択されたフライトおよび搭乗者の年齢構成（大人・小児）に基づき、往路・復路を合わせた合計運賃を計算する。
2. **予約情報の妥当性検証**: 予約代表者の年齢制限、運賃種別（レディース割、グループ割など）の適用条件、会員情報との照合（予約代表者および搭乗者）を行う。
3. **予約情報の登録**: フライトの空席状況を確認・更新（確保）した上で、予約情報、予約フライト情報、搭乗者情報をデータベースへ登録する。
4. **会員情報の検索**: 入力された会員番号を基に、カード会員情報を取得する。
# クラス仕様

|項目|内容|
|---|---|
|ファイルPATH|atrs-domain/src/main/java/jp/co/ntt/atrs/domain/service/b2/TicketReserveServiceImpl.java|
|クラスのpackage名|jp.co.ntt.atrs.domain.service.b2|
|クラス名|TicketReserveServiceImpl|
|クラスシグネチャ|@Service<br>@Transactional<br>public class TicketReserveServiceImpl implements TicketReserveService|

## メンバ変数仕様

設計ドキュメントからコーディングを可能とするため、本クラスで保持するメンバ変数の定義を以下に示す。

|型|変数名|アノテーション|設定値/インジェクション対象|説明|
|---|---|---|---|---|
|int|representativeMinAge|@Value("${atrs.representativeMinAge}")|プロパティ: atrs.representativeMinAge|予約代表者に必要な最小年齢（18歳）|
|int|adultPassengerMinAge|@Value("${atrs.adultPassengerMinAge}")|プロパティ: atrs.adultPassengerMinAge|大人運賃が適用される最小年齢（12歳）|
|int|childFareRate|@Value("${atrs.childFareRate}")|プロパティ: atrs.childFareRate|大人運賃に対する小児運賃の比率（%）（75%）|
|FlightRepository|flightRepository|@Inject|DIコンテナ経由|フライト情報リポジトリ|
|MemberRepository|memberRepository|@Inject|DIコンテナ経由|カード会員情報リポジトリ|
|ReservationRepository|reservationRepository|@Inject|DIコンテナ経由|予約情報リポジトリ|
|TicketSharedService|ticketSharedService|@Inject|DIコンテナ経由|チケット共通サービス|

## import対象クラス

- java.time.LocalDate
- java.util.List
- org.springframework.beans.factory.annotation.Value
- org.springframework.stereotype.Service
- org.springframework.transaction.annotation.Transactional
- org.springframework.util.Assert
- org.springframework.util.StringUtils
- org.terasoluna.gfw.common.exception.BusinessException
- org.terasoluna.gfw.common.exception.SystemException
- jakarta.inject.Inject
- jp.co.ntt.atrs.domain.common.exception.AtrsBusinessException
- jp.co.ntt.atrs.domain.common.logging.LogMessages
- jp.co.ntt.atrs.domain.common.util.FareUtil
- jp.co.ntt.atrs.domain.model.FareType
- jp.co.ntt.atrs.domain.model.FareTypeCd
- jp.co.ntt.atrs.domain.model.Flight
- jp.co.ntt.atrs.domain.model.Gender
- jp.co.ntt.atrs.domain.model.Member
- jp.co.ntt.atrs.domain.model.Passenger
- jp.co.ntt.atrs.domain.model.Reservation
- jp.co.ntt.atrs.domain.model.ReserveFlight
- jp.co.ntt.atrs.domain.model.Route
- jp.co.ntt.atrs.domain.repository.flight.FlightRepository
- jp.co.ntt.atrs.domain.repository.member.MemberRepository
- jp.co.ntt.atrs.domain.repository.reservation.ReservationRepository
- jp.co.ntt.atrs.domain.service.b0.TicketSharedService

# メソッド仕様

## findMember

### メソッド概要

指定された会員番号に該当するカード会員情報を取得する。

### メソッドシグネチャ

```java
@Override
public Member findMember(String membershipNumber)
```

### 処理内容

1. **パラメータのバリデーションチェック**:
   - `membershipNumber` が null、空文字、または空白文字のみの場合、`IllegalArgumentException` をスローする。 (Message: `"membershipNumber must have some text."`)

2. **会員情報の取得**:
   - カード会員情報リポジトリ `memberRepository.findOne(membershipNumber)` を呼び出し、取得結果を返却する。

---

## validateRepresentativeAge

### メソッド概要

予約代表者の年齢が、予約代表者に必要な最小年齢以上であることをチェックする。

### メソッドシグネチャ

```java
private void validateRepresentativeAge(int age)
```

### 処理内容

1. 引数 `age` がメンバ変数 `representativeMinAge` 未満である場合、業務例外 `AtrsBusinessException` をスローする。
   - エラーコード: `TicketReserveErrorCode.E_AR_B2_2004`
   - パラメータ: `representativeMinAge`

---

## validateFareType

### メソッド概要

予約フライト情報において、適用されている運賃種別の制限（レディース割、グループ割）を満たしているかを検証する。

### メソッドシグネチャ

```java
private void validateFareType(List<ReserveFlight> reserveFlightList) throws AtrsBusinessException
```

### 処理内容

1. `reserveFlightList` をループする。
   - `reserveFlight` が null の場合、`IllegalArgumentException` をスローする。 (Message: `"reserveFlight must not null."`)
   - `reserveFlight.getPassengerList()` が null または空リストである場合、`IllegalArgumentException` をスローする。 (Message: `"passengerList must not empty."`)

2. 各予約フライトに設定された運賃種別コード `fareTypeCd`（`reserveFlight.getFlight().getFareType().getFareTypeCd()`）に応じて以下の検証を行う。
   - **レディース割 (`FareTypeCd.LD`) の場合**:
     - `passengerList` をループし、搭乗者の中に男性（`Gender.M`）が含まれていないかチェックする。
       - `passenger` が null の場合、`IllegalArgumentException` をスローする。 (Message: `"passenger must not null."`)
       - 性別が男性（`passenger.getGender() == Gender.M`）である搭乗者が存在する場合、業務例外 `AtrsBusinessException` をスローする。
         - エラーコード: `TicketReserveErrorCode.E_AR_B2_2007`
   - **グループ割 (`FareTypeCd.GD`) の場合**:
     - 運賃種別の利用可能最少人数 `passengerMinNum`（`fareType.getPassengerMinNum()`）を取得する。
     - 搭乗者の総数 `passengerList.size()` が `passengerMinNum` 未満の場合、業務例外 `AtrsBusinessException` をスローする。
       - エラーコード: `TicketReserveErrorCode.E_AR_B2_2010`
       - パラメータ: `fareType.getFareTypeName()`, `passengerMinNum`

---

## validateRepresentativeMemberInfo

### メソッド概要

予約代表者の会員番号が設定されている場合、登録されているカード会員情報と一致しているかをチェックする。

### メソッドシグネチャ

```java
private void validateRepresentativeMemberInfo(Reservation reservation) throws AtrsBusinessException
```

### 処理内容

1. 予約代表者会員番号 `repMembershipNumber`（`reservation.getRepMember().getMembershipNumber()`）を取得する。

2. `repMembershipNumber` が入力されている場合（`StringUtils.hasLength(repMembershipNumber)` が true の場合）のみ、以下のチェックを実施する。
   - 会員情報リポジトリ `memberRepository.findOne(repMembershipNumber)` から予約代表者の会員情報 `repMember` を取得する。
   - 会員情報 `repMember` が存在しない（null の）場合、業務例外 `AtrsBusinessException` をスローする。
     - エラーコード: `TicketReserveErrorCode.E_AR_B2_2002`
   - 取得した会員情報と予約代表者の情報（カナ姓、カナ名、性別）が一致することを確認する。具体的には、以下のすべてを満たすか判定する。
     - `reservation.getRepFamilyName()` が `repMember.getKanaFamilyName()` と等しい
     - `reservation.getRepGivenName()` が `repMember.getKanaGivenName()` と等しい
     - `reservation.getRepGender()` が `repMember.getGender()` と等しい
   - 一致しない項目がある場合、業務例外 `AtrsBusinessException` をスローする。
     - エラーコード: `TicketReserveErrorCode.E_AR_B2_2003`

---

## validatePassengerMemberInfo

### メソッド概要

搭乗者情報に会員番号が設定されている場合、登録されているカード会員情報と一致しているかを照合する。

### メソッドシグネチャ

```java
private void validatePassengerMemberInfo(List<ReserveFlight> reserveFlightList) throws AtrsBusinessException
```

### 処理内容

1. `reserveFlightList` をループする。
   - `reserveFlight` が null の場合、`IllegalArgumentException` をスローする。 (Message: `"reserveFlight must not null."`)
   - `reserveFlight.getPassengerList()` が null または空リストである場合、`IllegalArgumentException` をスローする。 (Message: `"passengerList must not empty."`)

2. 各予約フライトの搭乗者リスト `passengerList` をループする。ループの処理順（1始まり）を `position` 変数で追跡する。
   - `passenger` が null の場合、`IllegalArgumentException` をスローする。 (Message: `"passenger must not null."`)
   - 搭乗者の会員番号 `membershipNumber`（`passenger.getMember().getMembershipNumber()`）を取得する。
   - `membershipNumber` が入力されている場合（`StringUtils.hasLength(membershipNumber)` が true の場合）のみ、以下のチェックを実施する。
     - 会員情報リポジトリ `memberRepository.findOne(membershipNumber)` から搭乗者のカード会員情報 `passengerMember` を取得する。
     - 会員情報 `passengerMember` が存在しない（null の）場合、業務例外 `AtrsBusinessException` をスローする。
       - エラーコード: `TicketReserveErrorCode.E_AR_B2_2005`
       - パラメータ: `position`
     - 取得した会員情報と搭乗者情報（カナ姓、カナ名、性別）が一致することを確認する。具体的には、以下のすべてを満たすか判定する。
       - `passenger.getFamilyName()` が `passengerMember.getKanaFamilyName()` と等しい
       - `passenger.getGivenName()` が `passengerMember.getKanaGivenName()` と等しい
       - `passenger.getGender()` が `passengerMember.getGender()` と等しい
     - 一致しない項目がある場合、業務例外 `AtrsBusinessException` をスローする。
       - エラーコード: `TicketReserveErrorCode.E_AR_B2_2006`
       - パラメータ: `position`
   - ループの最後で `position` を 1 加算する。

---

## validateReservation

### メソッド概要

予約代表者の年齢、運賃種別の適用条件、および会員情報の一致検証を一括して行う。

### メソッドシグネチャ

```java
@Override
public void validateReservation(Reservation reservation) throws BusinessException
```

### 処理内容

1. **パラメータのバリデーションチェック**:
   - `reservation` が null の場合、`IllegalArgumentException` をスローする。 (Message: `"reservation must not null."`)
   - `reservation.getReserveFlightList()` が null または空リストである場合、`IllegalArgumentException` をスローする。 (Message: `"reserveFlightList must not empty."`)

2. **予約代表者年齢チェック**:
   - 予約代表者の年齢 `reservation.getRepAge()` を引数として、プライベートメソッド `validateRepresentativeAge` を呼び出す。

3. **運賃種別適用チェック**:
   - 予約フライト情報一覧 `reserveFlightList` を取得し、プライベートメソッド `validateFareType` を呼び出す。

4. **予約代表者会員情報チェック**:
   - 予約情報 `reservation` を引数として、プライベートメソッド `validateRepresentativeMemberInfo` を呼び出す。

5. **搭乗者会員情報チェック**:
   - 予約フライト情報一覧 `reserveFlightList` を引数として、プライベートメソッド `validatePassengerMemberInfo` を呼び出す。

---

## calculateTotalFare

### メソッド概要

フライト情報リストと搭乗者リストを基に、予約全体の合計運賃を算出する。算出された金額の100円未満は切り上げて返却する。

### メソッドシグネチャ

```java
@Override
public int calculateTotalFare(List<Flight> flightList, List<Passenger> passengerList)
```

### 処理内容

1. **パラメータのバリデーションチェック**:
   - `flightList` が null または空リストである場合、`IllegalArgumentException` をスローする。 (Message: `"flightList must not empty."`)
   - `passengerList` が null または空リストである場合、`IllegalArgumentException` をスローする。 (Message: `"passengerList must not empty."`)

2. **搭乗者区分の集計**:
   - 小児搭乗者数（12歳未満の搭乗者数） `childNum` を 0 で初期化する。
   - `passengerList` をループし、各搭乗者の年齢をチェックする。
     - 搭乗者オブジェクト `passenger` が null の場合、`IllegalArgumentException` をスローする。 (Message: `"passenger must not null."`)
     - `passenger.getAge()` がメンバ変数 `adultPassengerMinAge` 未満の場合、`childNum` を 1 加算する。
   - 全搭乗者数から小児搭乗者数を引いた数を、大人（12歳以上）搭乗者数 `adultNum` とする。

3. **運賃の計算**:
   - 合計金額 `totalFare` を 0 で初期化する。
   - `flightList` をループし、各フライトの運賃を計算して `totalFare` に加算する。
     - フライトオブジェクト `flight` が null の場合、`IllegalArgumentException` をスローする。 (Message: `"flight must not null."`)
     - 対象フライトの区間・搭乗クラス・搭乗日に基づく基本運賃 `baseFare` を、チケット共通サービス `ticketSharedService.calculateBasicFare` を呼び出して算出する。
       - 引数: `flight.getFlightMaster().getRoute().getBasicFare()`, `flight.getBoardingClass().getBoardingClassCd()`, `flight.getDepartureDate()`
     - 対象フライトの運賃種別の割引率 `discountRate` を `flight.getFareType().getDiscountRate()` から取得する。
     - 割引適用後の大人運賃 `boardingFare` を、チケット共通サービス `ticketSharedService.calculateFare(baseFare, discountRate)` を呼び出して算出する。
     - 以下の計算式により、フライト単位の運賃 `fare` を算出する。
       - `fare = (boardingFare * adultNum) + (baseFare * (childFareRate - discountRate) / 100 * childNum)`
     - `totalFare` に `fare` を加算する。

4. **端数処理**:
   - `FareUtil.ceilFare(totalFare)` を呼び出し、合計運賃の100円未満を切り上げる。
   - 切り上げ後の `totalFare` を返却する。

---

## validateAndUpdateVacancy

### メソッド概要

予約フライト情報に設定されたフライトの空席数を悲観的ロックで確認・更新する。また、搭乗日が運賃種別の予約可能時期範囲内かチェックする。

### メソッドシグネチャ

```java
private void validateAndUpdateVacancy(ReserveFlight reserveFlight) throws AtrsBusinessException
```

### 処理内容

1. **パラメータのバリデーションチェック**:
   - `reserveFlight` が null の場合、`IllegalArgumentException` をスローする。 (Message: `"reserveFlight must not null."`)
   - `reserveFlight.getFlight()` が null の場合、`IllegalArgumentException` をスローする。 (Message: `"flight must not null."`)

2. **予約可能時期チェック**:
   - チケット共通サービス `ticketSharedService.isAvailableFareType` を呼び出し、対象フライトの搭乗日が運賃種別の予約可能時期範囲内か確認する。
     - 引数: `flight.getFareType()`, `flight.getDepartureDate()`
   - 範囲外（戻り値が false）の場合、業務例外 `AtrsBusinessException` をスローする。
     - エラーコード: `TicketReserveErrorCode.E_AR_B2_2008`

3. **空席状況の確認および確保(排他ロック)**:
   - フライト情報リポジトリから、条件に合致するフライト情報を悲観的ロックをかけて取得し、変数 `flight` に再設定する。
     - 呼び出しメソッド: `flightRepository.findOneForUpdate`
     - 引数: `flight.getDepartureDate()`, `flight.getFlightMaster().getFlightName()`, `flight.getBoardingClass()`, `flight.getFareType()`
   - 取得したフライトの空席数 `vacantNum`（`flight.getVacantNum()`）を取得する。
   - 今回予約する搭乗者数 `passengerNum`（`reserveFlight.getPassengerList().size()`）を取得する。
   - `vacantNum` が `passengerNum` 未満である場合、空席不足として業務例外 `AtrsBusinessException` をスローする。
     - エラーコード: `TicketReserveErrorCode.E_AR_B2_2009`
   - `flight` の空席数を `vacantNum - passengerNum` に更新する。

4. **フライト情報の更新**:
   - フライト情報リポジトリ `flightRepository.update(flight)` を呼び出して更新を反映する。
   - 更新件数が 1 件でない場合、システム例外 `SystemException` をスローする。
     - エラーコード: `LogMessages.E_AR_A0_L9002.getCode()`
     - パラメータ: 実際の更新件数（`flightUpdateCount`）, `1`

---

## registerReservation

### メソッド概要

予約の空席確認と確保、およびデータベースへの予約登録を行い、予約完了後の情報（予約番号と支払期限）を含む DTO を返却する。

### メソッドシグネチャ

```java
@Override
public TicketReserveDto registerReservation(Reservation reservation) throws BusinessException
```

### 処理内容

1. **パラメータのバリデーションチェック**:
   - `reservation` が null の場合、`IllegalArgumentException` をスローする。 (Message: `"reservation must not null."`)

2. **予約フライト情報のチェック**:
   - `reservation.getReserveFlightList()` が null または空リストである場合、`IllegalArgumentException` をスローする。 (Message: `"reserveFlightList must not empty."`)

3. **空席状況の確認および更新**:
   - `reserveFlightList` をループし、各予約フライト `reserveFlight` について、プライベートメソッド `validateAndUpdateVacancy` を呼び出して空席の確保を行う。

4. **予約情報の登録**:
   - 予約リポジトリ `reservationRepository.insert(reservation)` を呼び出し、予約ヘッダ情報を登録する。
     - 登録が成功すると、パラメータの `reservation` オブジェクトに自動採番された予約番号が設定される。
   - 戻り値の登録件数が 1 件でない場合、システム例外 `SystemException` をスローする。
     - エラーコード: `LogMessages.E_AR_A0_L9002.getCode()`
     - パラメータ: 実際の登録件数（`reservationInsertCount`）, `1`

5. **予約番号の取得**:
   - `reservation.getReserveNo()` から採番された予約番号 `reserveNo` を取得する。

6. **予約フライト情報および搭乗者情報の登録**:
   - `reserveFlightList` をループする。
     - `reserveFlight` に予約番号 `reserveNo` を設定する (`reserveFlight.setReserveNo(reserveNo)`)。
     - 予約フライト情報を登録する: `reservationRepository.insertReserveFlight(reserveFlight)`。
       - 登録が成功すると、パラメータの `reserveFlight` オブジェクトに自動採番された予約フライト番号が設定される。
       - 登録件数が 1 件でない場合、システム例外 `SystemException` をスローする。
         - エラーコード: `LogMessages.E_AR_A0_L9002.getCode()`
         - パラメータ: 実際の登録件数（`reserveFlightInsertCount`）, `1`
     - 予約フライトの搭乗者リスト `reserveFlight.getPassengerList()` をループする。
       - 搭乗者情報 `passenger` に親となる予約フライト番号を設定する (`passenger.setReserveFlightNo(reserveFlight.getReserveFlightNo())`)。
       - 搭乗者情報を登録する: `reservationRepository.insertPassenger(passenger)`。
       - 登録件数が 1 件でない場合、システム例外 `SystemException` をスローする。
         - エラーコード: `LogMessages.E_AR_A0_L9002.getCode()`
         - パラメータ: 実際の登録件数（`passengerInsertCount`）, `1`

7. **戻り値の構築**:
   - 支払期限 `paymentDate` として、往路フライトの搭乗日を設定する。
     - `paymentDate = reserveFlightList.get(0).getFlight().getDepartureDate()`
   - `TicketReserveDto(reserveNo, paymentDate)` をインスタンス化して返却する。
