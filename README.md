# このプロジェクトについて

## 位置づけ

設計書(markdown)からjavaコードを生成するためのGitHub Copilotカスタムエージェントのサンプル

## エージェントのポイント

- 設計書からメソッド仕様を読み取り、メソッドを「１つずつ」実装する
- コード生成は常に ** サブエージェント ** で実行する（メインコンテキストへの影響を抑える目的）

## エージェントの実行例

GitHub Copilot のチャットでエージェントに`java-codegen`を選択し、下記プロンプトを実行

```
design.md
./atrs-domain/src/main/java/jp/co/ntt/atrs/domain/common/exception/AtrsBusinessException.java
./atrs-domain/src/main/java/jp/co/ntt/atrs/domain/common/logging/LogMessages.java
./atrs-domain/src/main/java/jp/co/ntt/atrs/domain/common/util/FareUtil.java
./atrs-domain/src/main/java/jp/co/ntt/atrs/domain/model/FareType.java
./atrs-domain/src/main/java/jp/co/ntt/atrs/domain/model/FareTypeCd.java
./atrs-domain/src/main/java/jp/co/ntt/atrs/domain/model/Flight.java
./atrs-domain/src/main/java/jp/co/ntt/atrs/domain/model/Gender.java
./atrs-domain/src/main/java/jp/co/ntt/atrs/domain/model/Member.java
./atrs-domain/src/main/java/jp/co/ntt/atrs/domain/model/Passenger.java
./atrs-domain/src/main/java/jp/co/ntt/atrs/domain/model/Reservation.java
./atrs-domain/src/main/java/jp/co/ntt/atrs/domain/model/ReserveFlight.java
./atrs-domain/src/main/java/jp/co/ntt/atrs/domain/model/Route.java
./atrs-domain/src/main/java/jp/co/ntt/atrs/domain/repository/flight/FlightRepository.java
./atrs-domain/src/main/java/jp/co/ntt/atrs/domain/repository/member/MemberRepository.java
./atrs-domain/src/main/java/jp/co/ntt/atrs/domain/repository/reservation/ReservationRepository.java
./atrs-domain/src/main/java/jp/co/ntt/atrs/domain/service/b0/TicketSharedService.java
```

## 格納ファイルについて

- .github/agents/java-codegen.agent.md
  - カスタムエージェントの定義
- .github/instructions/java-codegen.instructions.md
  - 上記カスタムエージェント用のinstruction定義
- design.md
  - 設計書ドキュメント
  - [atrs](https://github.com/terasolunaorg/atrs)のオリジナルソースを元にGeminiで生成したもの
- atrs-domain/src/test/java/jp/co/ntt/atrs/domain/service/b2/TicketReserveServiceImplTest.java
  - 生成したjavaコードの動作確認用テストコード
  - [atrs](https://github.com/terasolunaorg/atrs)のオリジナルソースを元にGeminiで生成したもの
- atrs-domain/src/main/java/jp/co/ntt/atrs/domain/service/b2/TicketReserveServiceImpl.java
  - 上記`実行例`で生成されたjavaコード