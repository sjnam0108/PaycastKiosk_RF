package kr.co.bbmc.paycast.presentation.paymentKicc;

public class KiccErrReason {
    public static String getReasonErr(String code)
    {
        String reason = "";
        switch (code)
        {
            case "0000" :
                reason = "정상완료";
                break;
            case "1203" :
                reason = " 전산 작업중.. 추후전송요망";
                break;
            case "1204" :
                reason = "  02-3416-2900     문의 요망";
                break;
            case "1205" :
                reason = " 테스트 ID 거래  문의:1600-1234";
                break;
            case "1206" :
                reason = "농협재승인";
                break;
            case "1207" :
                reason = "국민LMS MS거래    EMVS로 전환";
                break;
            case "1208" :
                reason = "MS 거래 요망";
                break;
            case "1209" :
                reason = "거래불가 POS거래 아님";
                break;
            case "1211" :
                reason = "  재거래 요망";
                break;
            case "2016" :
                reason = "미등록가맹점       ";
                break;
            case "4201" :
                reason = " 요청 전문 오류 ";
                break;
            case "4215" :
                reason = "SIGN KEY값 오류";
                break;
            case "4216" :
                reason = "  재조회  요망  ";
                break;
            case "4217" :
                reason = " SIGN FLAG 오류 ";
                break;
            case "4218" :
                reason = "인증구분필드오류";
                break;
            case "4224" :
                reason = " 통신구분 오류";
                break;
            case "4250" :
                reason = "전문구분코드오류";
                break;
            case "4251" :
                reason = "단말기 번호 오류";
                break;
            case "4252" :
                reason = "  WCC필드 오류  ";
                break;
            case "4253" :
                reason = "카드번호필드오류";
                break;
            case "4254" :
                reason = "유효기간필드오류";
                break;
            case "4255" :
                reason = "거래금액필드오류";
                break;
            case "4256" :
                reason = "할부개월필드오류";
                break;
            case "4257" :
                reason = "봉사료 필드 오류";
                break;
            case "4258" :
                reason = " 세금 필드 오류 ";
                break;
            case "4259" :
                reason = "가맹점 번호 오류";
                break;
            case "4260" :
                reason = "사업자 번호 오류";
                break;
            case "4261" :
                reason = "정산건수필드오류";
                break;
            case "4262" :
                reason = "정산금액필드오류";
                break;
            case "4263" :
                reason = "R거래가 허용안됨";
                break;
            case "4264" :
                reason = "포인트율필드오류";
                break;
            case "4265" :
                reason = "주유량 필드 오류";
                break;
            case "4266" :
                reason = "원거래 일자 오류";
                break;
            case "4267" :
                reason = "거래일련번호오류";
                break;
            case "4268" :
                reason = "Point카드 Key-In거래불가";
                break;
            case "4269" :
                reason = "원승인 번호 오류";
                break;
            case "4271" :
                reason = "추후 서비스 예정";
                break;
            case "4272" :
                reason = " 사용할 수 없는   단말기 번호";
                break;
            case "4274" :
                reason = "단가를입력하세요";
                break;
            case "4275" :
                reason = "유종단가입력오류";
                break;
            case "4276" :
                reason = "R거래가 허용안됨";
                break;
            case "4277" :
                reason = "R거래가 허용안됨";
                break;
            case "4278" :
                reason = "R거래가 허용안됨";
                break;
            case "4279" :
                reason = "  재조회  요망  ";
                break;
            case "4280" :
                reason = "  재조회  요망  ";
                break;
            case "4287" :
                reason = "   재조회  요망  ";
                break;
            case "4286" :
                reason = "  재조회  요망  ";
                break;
            case "4285" :
                reason = "  재조회  요망  ";
                break;
            case "4284" :
                reason = "   재조회  요망  ";
                break;
            case "4281" :
                reason = "상품코드입력오류";
                break;
            case "4282" :
                reason = "조회일자입력오류";
                break;
            case "4288" :
                reason = "  재조회  요망  ";
                break;
            case "4289" :
                reason = "  재조회  요망  ";
                break;
            case "4290" :
                reason = "  재조회  요망  ";
                break;
            case "4291" :
                reason = " VAN CODE 오류  ";
                break;
            case "4292" :
                reason = "할인금액입력오류";
                break;
            case "4293" :
                reason = "당첨번호입력오류";
                break;
            case "4294" :
                reason = "당첨번호  불일치";
                break;
            case "4295" :
                reason = "당첨번호가맹점아님";
                break;
            case "4296" :
                reason = "당첨 금액 불일치";
                break;
            case "4297" :
                reason = "유효기간 경과   ";
                break;
            case "4298" :
                reason = "면세유 입력 오류";
                break;
            case "4299" :
                reason = "기 승인거래존재 ";
                break;
            case "4300" :
                reason = "당첨거래조회장애일반신용거래요망";
                break;
            case "4310" :
                reason = "  조회기간오류  ";
                break;
            case "4311" :
                reason = " 매입사코드오류 ";
                break;
            case "4312" :
                reason = " 조회PAGE 오류  ";
                break;
            case "4319" :
                reason = "  SIS 거래 불가 ";
                break;
            case "4320" :
                reason = " NEWPOS 포멧오류";
                break;
            case "4322" :
                reason = "  조회기간 오류 ";
                break;
            case "4324" :
                reason = "   포인트카드    할부거래  불가 ";
                break;
            case "4332" :
                reason = "  통화코드 오류 ";
                break;
            case "4333" :
                reason = " EMV DATA NULL  ";
                break;
            case "4334" :
                reason = "EMV요청DATA 오류";
                break;
            case "4335" :
                reason = "카드번호2 오류  ";
                break;
            case "4336" :
                reason = "유효기간2 오류  ";
                break;
            case "4337" :
                reason = " WCC2 필드오류  ";
                break;
            case "4338" :
                reason = "거래금액필드오류 ";
                break;
            case "4339" :
                reason = "    현금거래        구분오류    ";
                break;
            case "4340" :
                reason = "요청 전문 오류";
                break;
            case "4341" :
                reason = "  재조회  요망(4341)";
                break;
            case "4802" :
                reason = "  V.A.K. Error  ";
                break;
            case "4804" :
                reason = "CheckValue Error";
                break;
            case "6310" :
                reason = "  재조회  요망(6310) ";
                break;
            case "6610" :
                reason = "  재조회  요망(6610) ";
                break;
            case "6620" :
                reason = "  재조회  요망(6620) ";
                break;
            case "6630" :
                reason = "  재조회  요망(6630) ";
                break;
            case "6640" :
                reason = "  재조회  요망(6640) ";
                break;
            case "6650" :
                reason = "  재조회  요망(6650) ";
                break;
            case "6660" :
                reason = "  재조회  요망(6660) ";
                break;
            case "6661" :
                reason = "  재조회  요망(6661) ";
                break;
            case "6670" :
                reason = "  재조회  요망(6670) ";
                break;
            case "6680" :
                reason = "  재조회  요망(6680) ";
                break;
            case "6690" :
                reason = "  재조회  요망(6690) ";
                break;
            case "6802" :
                reason = "  재조회  요망(6802) ";
                break;
            case "6803" :
                reason = "  재조회  요망(6803) ";
                break;
            case "6804" :
                reason = "  재조회  요망(6804) ";
                break;
            case "6805" :
                reason = "  재조회  요망(6805) ";
                break;
            case "6806" :
                reason = "  재조회  요망(6806) ";
                break;
            case "6807" :
                reason = "  재조회  요망(6807) ";
                break;
            case "6808" :
                reason = "  재조회  요망(6808) ";
                break;
            case "6809" :
                reason = "  재조회  요망(6809) ";
                break;
            case "6999" :
                reason = "이미  취소원복된      거래";
                break;
            case "7401" :
                reason = "   수표  발행      취소  수표";
                break;
            case "7403" :
                reason = " 기 지급된 수표 ";
                break;
            case "7405" :
                reason = " 기 조회된 수표";
                break;
            case "7406" :
                reason = " 수표 금액 상이 ";
                break;
            case "7407" :
                reason = "수표 발행일 상이";
                break;
            case "7408" :
                reason = " 수표 번호 오류 ";
                break;
            case "7409" :
                reason = "  수표권종코드        오류";
                break;
            case "7410" :
                reason = "  수표발행지점        오류";
                break;
            case "7412" :
                reason = "   해당  은행      통신  장애";
                break;
            case "7415" :
                reason = "  미발행  수표  ";
                break;
            case "7416" :
                reason = "   발행  은행      문의  요망";
                break;
            case "7501" :
                reason = " 단말기  미등록 TEL (02)761-3000";
                break;
            case "7502" :
                reason = "단말기번호  오류   ";
                break;
            case "7503" :
                reason = "TPK 미등록가맹점";
                break;
            case "7504" :
                reason = "관리 대리점      교체 요망!    ";
                break;
            case "7505" :
                reason = "   사업자번호        불일치 ";
                break;
            case "7570" :
                reason = "   할부  거래    불가능  가맹점";
                break;
            case "7571" :
                reason = "   할부  거래    불가능  가맹점";
                break;
            case "7581" :
                reason = "  승인 완료된     거래입니다.";
                break;
            case "7768" :
                reason = "  등록  작업시  당초매출일  오류";
                break;
            case "7769" :
                reason = "  등록  작업시   승인번호  오류";
                break;
            case "7781" :
                reason = "  정산  거래시     정산  오류 ";
                break;
            case "7792" :
                reason = " 전문구분  오류 ";
                break;
            case "7803" :
                reason = "  재조회  요망       (7803)   ";
                break;
            case "7974" :
                reason = "가맹점 등록 요망TEL (02)761-3000";
                break;
            case "7975" :
                reason = "국세청 전화 요망  (126)";
                break;
            case "7979" :
                reason = " 해당  카드사에 가맹점정보  확인";
                break;
            case "8000" :
                reason = "조회 불가능 카드";
                break;
            case "8001" :
                reason = " 허가된  거래가       아님";
                break;
            case "8003" :
                reason = "  FORMAT  오류    재시도 요망";
                break;
            case "8004" :
                reason = " 해외/법인 카드       아님";
                break;
            case "8005" :
                reason = " 국내카드 승인    불가";
                break;
            case "8006" :
                reason = "   거래  금액      너무  적음";
                break;
            case "8007" :
                reason = "   설치  거래     1,234원 변경";
                break;
            case "8008" :
                reason = " 한도초과  금액   ";
                break;
            case "8009" :
                reason = "거래금액  미입력   ";
                break;
            case "8010" :
                reason = " *금액입력오류*";
                break;
            case "8011" :
                reason = " 승인이  제한된    금액입니다";
                break;
            case "8012" :
                reason = " 회원 이용실적     기준 미달";
                break;
            case "8013" :
                reason = "원거래 주문번호      불일치";
                break;
            case "8014" :
                reason = "배송비 할인금액    한도 초과";
                break;
            case "8015" :
                reason = "청구시 할인대상 (현장할인 아님)";
                break;
            case "8016" :
                reason = "   해당서비스    미연계 가맹점";
                break;
            case "8017" :
                reason = "회원은행 불일치";
                break;
            case "8032" :
                reason = "   할부  금액      잘못  입력";
                break;
            case "8034" :
                reason = " 거래 정지 계좌";
                break;
            case "8035" :
                reason = "   잔액  부족";
                break;
            case "8036" :
                reason = "  잔액 20%이내      사용가능";
                break;
            case "8037" :
                reason = "   카드  번호      입력  오류";
                break;
            case "8038" :
                reason = "  신분확인정보     확인  요망";
                break;
            case "8110" :
                reason = "   수동  조회     불가능  카드";
                break;
            case "8120" :
                reason = "   수동  조회    불가능  가맹점 ";
                break;
            case "8121" :
                reason = "당월중  수동조회   한도  초과";
                break;
            case "8122" :
                reason = " 자기매출  불가   ";
                break;
            case "8123" :
                reason = "   수동  조회    불가능  가맹점";
                break;
            case "8124" :
                reason = "해당카드 거래 제한 업종";
                break;
            case "8125" :
                reason = "  PG  입점업체   사업자번호오류";
                break;
            case "8126" :
                reason = " 미등록 PG SUB   가맹점";
                break;
            case "8127" :
                reason = " 불량 PG SUB     가맹점";
                break;
            case "8178" :
                reason = "   연체  카드       ";
                break;
            case "8179" :
                reason = "타 카드/은행 연체";
                break;
            case "8310" :
                reason = " 비밀번호  오류   ";
                break;
            case "8311" :
                reason = " 비밀번호  오류    회수  초과";
                break;
            case "8312" :
                reason = " 비밀번호  오류     ";
                break;
            case "8313" :
                reason = "가맹점  한도초과   ";
                break;
            case "8314" :
                reason = "   유효  기간      경과  카드";
                break;
            case "8315" :
                reason = " 유효 기간 오류";
                break;
            case "8316" :
                reason = "   유효  기간      경과  카드";
                break;
            case "8318" :
                reason = "   비밀  번호      입력  요망";
                break;
            case "8319" :
                reason = "   비밀  번호      등록  요망";
                break;
            case "8321" :
                reason = "   1 일  사용     한도액  초과";
                break;
            case "8322" :
                reason = "   일반  구매      한도  초과";
                break;
            case "8323" :
                reason = "   할부  구매      한도  초과";
                break;
            case "8324" :
                reason = " 거래정지  카드    ";
                break;
            case "8325" :
                reason = "   승인  대행      한도  초과";
                break;
            case "8326" :
                reason = "     월  사용      한도액  초과";
                break;
            case "8327" :
                reason = "   1 회  사용     한도액  초과";
                break;
            case "8328" :
                reason = " 사용횟수  초과          ";
                break;
            case "8329" :
                reason = "  무통장  거래     횟수  초과";
                break;
            case "8330" :
                reason = " 사용시간 초과";
                break;
            case "8331" :
                reason = " 회원/가족 요청 거래 정지";
                break;
            case "8332" :
                reason = "일일사용횟수초과";
                break;
            case "8333" :
                reason = "월간사용횟수초과";
                break;
            case "8334" :
                reason = "연간사용횟수초과";
                break;
            case "8335" :
                reason = "분기사용횟수초과";
                break;
            case "8350" :
                reason = "도난 / 분실 카드  카드사  신고";
                break;
            case "8351" :
                reason = " 사고등록  계좌          ";
                break;
            case "8352" :
                reason = " 법적등록  계좌          ";
                break;
            case "8353" :
                reason = "카드사/은행 시스템 장애";
                break;
            case "8354" :
                reason = "탈회/해지 카드";
                break;
            case "8355" :
                reason = "미교부 카드";
                break;
            case "8356" :
                reason = "카드사/은행 시스템 점검중";
                break;
            case "8371" :
                reason = "카드사  전화요망       ";
                break;
            case "8372" :
                reason = "카드사  전화요망       ";
                break;
            case "8373" :
                reason = "카드사  전화요망       ";
                break;
            case "8374" :
                reason = " 취소기한  경과";
                break;
            case "8375" :
                reason = " 전화승인  요망          ";
                break;
            case "8376" :
                reason = "IC카드 승인 거절";
                break;
            case "8377" :
                reason = "카드사/은행 확인 후 거래 요망";
                break;
            case "8380" :
                reason = "   통신  장애     재조회  요망";
                break;
            case "8381" :
                reason = "  재조회  요망    카드사  장애";
                break;
            case "8391" :
                reason = "   할부  개월      입력  오류";
                break;
            case "8392" :
                reason = "     할부는       12개월  이내";
                break;
            case "8393" :
                reason = "   할부  개월      입력  오류";
                break;
            case "8395" :
                reason = " 즉시입금/      즉시입금취소오류";
                break;
            case "8396" :
                reason = "   할인 금액       미 존재";
                break;
            case "8397" :
                reason = "   승인 금액       미 존재";
                break;
            case "8398" :
                reason = "    원 거래       주문번호 틀림";
                break;
            case "8399" :
                reason = "   기준 실적         미달";
                break;
            case "8400" :
                reason = "  해당  카드사     통신  장애";
                break;
            case "8401" :
                reason = "  인증  서비스   대행승인  불가";
                break;
            case "8410" :
                reason = "신용 조기경보 등재 회원";
                break;
            case "8411" :
                reason = "카드발급상태이상";
                break;
            case "8412" :
                reason = "  신용  불량자";
                break;
            case "8413" :
                reason = " 위변조 카드";
                break;
            case "8414" :
                reason = " 승인 거래 없음         ";
                break;
            case "8415" :
                reason = " 거래 내역 없음     ";
                break;
            case "8416" :
                reason = "   할부  거래     불가능  카드";
                break;
            case "8417" :
                reason = "   할부  거래     불가능  카드";
                break;
            case "8418" :
                reason = "     서비스       불가능  카드";
                break;
            case "8419" :
                reason = " MS정보  불일치";
                break;
            case "8420" :
                reason = "이미  EDI 매입된  거래  입니다";
                break;
            case "8431" :
                reason = "승인 취소 불가";
                break;
            case "8432" :
                reason = "  이미  매입된    거래  입니다";
                break;
            case "8433" :
                reason = "  이미  취소된    거래  입니다";
                break;
            case "8434" :
                reason = " PIN  DOWNLOAD    재시도  요망";
                break;
            case "8435" :
                reason = " IC 거래 요망";
                break;
            case "8501" :
                reason = "  전문ID 오류";
                break;
            case "8502" :
                reason = "  상품코드오류   080-5000-011 ";
                break;
            case "8503" :
                reason = "취소승인번호없음 080-5000-011 ";
                break;
            case "8504" :
                reason = "취소할금액이다름 080-5000-011 ";
                break;
            case "8505" :
                reason = " Check기준오류 ";
                break;
            case "8506" :
                reason = " VIP아님         080-5000-011 ";
                break;
            case "8507" :
                reason = " 취소불가 기취소 080-5000-011 ";
                break;
            case "8508" :
                reason = " Pos entry 오류  080-5000-011 ";
                break;
            case "8509" :
                reason = " 가맹점코드오류  080-5000-011 ";
                break;
            case "8510" :
                reason = " 통화코드오류";
                break;
            case "8511" :
                reason = " 거래금액오류    080-5000-011 ";
                break;
            case "8512" :
                reason = " 취소구분오류";
                break;
            case "8513" :
                reason = "취소승인번호오류 080-5000-011 ";
                break;
            case "8514" :
                reason = " 도난/분실카드   080-5000-011 ";
                break;
            case "8515" :
                reason = " 거래정지카드    080-5000-011 ";
                break;
            case "8516" :
                reason = " 조회불능카드    080-5000-011 ";
                break;
            case "8517" :
                reason = " 불량가맹점 ";
                break;
            case "8518" :
                reason = " 해지가맹점      080-5000-011 ";
                break;
            case "8519" :
                reason = " 등록VAN사다름   080-5000-011 ";
                break;
            case "8520" :
                reason = " 말소단말기 ";
                break;
            case "8521" :
                reason = " 한도초과  ";
                break;
            case "8522" :
                reason = " 해당회원아님    080-5000-011 ";
                break;
            case "8523" :
                reason = "재조회카드사장애 080-5000-011 ";
                break;
            case "8524" :
                reason = "보너스포인트    사용불가 가맹점";
                break;
            case "8525" :
                reason = "VIP 한도초과     080-555-0017";
                break;
            case "8526" :
                reason = "재발급 대상카드 LGT문의1544-0019";
                break;
            case "8527" :
                reason = "포인트잔액 부족";
                break;
            case "8531" :
                reason = "  멤버쉽 번호      Update 필요";
                break;
            case "8532" :
                reason = "  상 환  방 법    입 력  오 류";
                break;
            case "8533" :
                reason = " 미 등 록 카 드";
                break;
            case "8534" :
                reason = "포인트 사용불가";
                break;
            case "8535" :
                reason = "   인증서비스    불가  가맹점";
                break;
            case "8536" :
                reason = "  상품권  월     구매한도 초과";
                break;
            case "8537" :
                reason = " 포인트  버튼을 눌러 거래하세요";
                break;
            case "8538" :
                reason = "   배송 금액       한도 초과";
                break;
            case "8539" :
                reason = "  매출 캐쉬백      탑재 카드";
                break;
            case "8540" :
                reason = "   대상 카드         아님";
                break;
            case "8541" :
                reason = "   기 처리         데이터";
                break;
            case "8542" :
                reason = "   총매출금액      미 존재";
                break;
            case "8543" :
                reason = "    요청 매수       미 존재 ";
                break;
            case "8544" :
                reason = " 서비스관리번호    미 존재";
                break;
            case "8545" :
                reason = "   결제구분값        틀림";
                break;
            case "8546" :
                reason = " 최저사용포인트      미만";
                break;
            case "8547" :
                reason = "  최저거래금액       미만";
                break;
            case "8548" :
                reason = "  개인본인회원       아님";
                break;
            case "8549" :
                reason = "포인트사용가맹점     아님";
                break;
            case "8550" :
                reason = "  서비스 대상   회원사카드  아님";
                break;
            case "8551" :
                reason = " 서비스  대상일      아님";
                break;
            case "8552" :
                reason = "서비스대상브랜드     아님";
                break;
            case "8553" :
                reason = " 서비스 대상요일     아님";
                break;
            case "8554" :
                reason = " 건별대행       불가 거래";
                break;
            case "8600" :
                reason = "체크카드    사용불가 가맹점";
                break;
            case "8601" :
                reason = "체크카드      서비스 불가";
                break;
            case "8611" :
                reason = "온라인 가맹점     거래 불가";
                break;
            case "8621" :
                reason = "안심클릭으로      거래요망";
                break;
            case "8631" :
                reason = "CAVV값 불일치";
                break;
            case "8641" :
                reason = "CAVV 취소후      재거래 요망";
                break;
            case "8651" :
                reason = "CAVV 갱신 요망";
                break;
            case "8661" :
                reason = "CAVV 전문 오류";
                break;
            case "8721" :
                reason = "공인인증서   비밀번호등록요망";
                break;
            case "8722" :
                reason = "ISP승인대상      가맹점임";
                break;
            case "8723" :
                reason = "ISP거래승인 불가  대행 VAN사임";
                break;
            case "8724" :
                reason = "인터넷 승인    인증결제 오류";
                break;
            case "8725" :
                reason = "충전불가  카드";
                break;
            case "8726" :
                reason = "카드수령후     미등록 카드";
                break;
            case "8727" :
                reason = "구카드, 신규수령카드 이용요망";
                break;
            case "8730" :
                reason = "해외카드사  거절";
                break;
            case "8731" :
                reason = " 선불카드       거래 오류";
                break;
            case "8732" :
                reason = "기프트카드     서비스 불가";
                break;
            case "8733" :
                reason = "서비스불가        시간대";
                break;
            case "8734" :
                reason = "사용가능        쿠폰없음";
                break;
            case "8735" :
                reason = "서비스미등록       가맹점";
                break;
            case "8736" :
                reason = "약정서 미등록   카드사 요망";
                break;
            case "8737" :
                reason = " 바우처승인         오류";
                break;
            case "8738" :
                reason = " POS 보안모듈     미설치       ";
                break;
            case "8739" :
                reason = "참가기관업무종료";
                break;
            case "8740" :
                reason = "해당카드 거래 제한 시간대";
                break;
            case "8741" :
                reason = "약정할부중복이용      불가";
                break;
            case "9001" :
                reason = "결제예정일       입력 오류";
                break;
            case "9002" :
                reason = "2회이상 분할금지";
                break;
            case "9003" :
                reason = " 매출취소불가   카드사 전화요망";
                break;
            case "9050" :
                reason = "승인누적횟수초과";
                break;
            case "9051" :
                reason = "특정시간승인불가";
                break;
            case "9052" :
                reason = " 주유전용가맹점       아님";
                break;
            case "9053" :
                reason = "CVC오류 횟수초과";
                break;
            case "9054" :
                reason = "중복승인불가";
                break;
            case "9055" :
                reason = "사용불가승인채널";
                break;
            case "9090" :
                reason = "수기전표  작성후 매입은행  제출";
                break;
            case "9950" :
                reason = "  주민  번호 /  사업자번호 오류";
                break;
            case "9970" :
                reason = "가맹점 다운로드가 필요합니다";
                break;
            case "9971" :
                reason = "가맹점 다운로드한 단말기 번호와 다른 단말기 번호입니다.";
                break;
            case "9973" :
                reason = "서명 대기 시간이 종료되었습니다.";
                break;
            case "9974" :
                reason = "USB연결 에러";
                break;
            case "9975" :
                reason = "모듈통신 실패";
                break;
            case "9976" :
                reason = "통신취소 처리 시간이 종료되었습니다.";
                break;
            case "9977" :
                reason = "승인 처리 시간이 종료되었습니다.";
                break;
            case "9978" :
                reason = "통신취소 실패 (※각 상황에 따라 메시지 틀림)";
                break;
            case "9979" :
                reason = "통신취소 되었습니다";
                break;
            case "9980" :
                reason = "거래구분코드(TRAN_TYPE)가 잘못되었습니다.";
                break;
            case "9981" :
                reason = "단말기구분코드(TERMINAL_TYPE)가 잘못되었습니다";
                break;
            case "9982" :
                reason = "총 결제 금액이 잘못되었습니다";
                break;
            case "9983" :
                reason = "부가세 금액이 잘못되었습니다";
                break;
            case "9984" :
                reason = "봉사료 금액이 잘못되었습니다";
                break;
            case "9985" :
                reason = "원 승인번호가 잘못되었습니다";
                break;
            case "9988" :
                reason = "루팅 디바이스는 이용할 수 없습니다";
                break;
            case "9990" :
                reason = "DUPLICATE CALL";
                break;
            case "9991" :
                reason = "통신장애 (※각 상황에 따라 메시지 틀림)";
                break;
            case "9993" :
                reason = "리더기가 분리되었습니다. 관리자에게 문의하세요";
                break;
            case "9994" :
                reason = "리딩 시간이 종료되었습니다";
                break;
            case "9995" :
                reason = "리더기 연결을 확인해주세요";
                break;
            case "9996" :
                reason = "무결성 실패로 결제를 진행할 수 없습니다. 관리자에게 문의하세요";
                break;
            case "9997" :
                reason = "카드 정보 읽기에 실패했습니다.";
//                reason = "Reading Error (※각 상황에 따라 메시지 틀림)";
                break;
            case "9999" :
                reason = "USER CANCEL";
                break;
            default:
                reason = code;
                break;
        }
        return reason;
    }
}
