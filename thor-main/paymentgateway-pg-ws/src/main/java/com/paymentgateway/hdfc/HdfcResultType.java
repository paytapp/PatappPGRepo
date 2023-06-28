package com.paymentgateway.hdfc;


public enum HdfcResultType {

	HDFC001			("IPAY0100056" , "007" , "Rejected" , "Instrument not allowed in Terminal and Brand"),
	HDFC002			("IPAY0100203" , "022" , "Failed at Acquirer" , "Problem occured while doing perform transaction"),
	HDFC003			("IPAY0100290" , "022" , "Failed at Acquirer" , "Problem occured while validating original transaction"),
	HDFC004			("IPAY0100136" , "002" , "Denied by risk" , "Transaction denied due to previous capture check failure ( Validate Original Transaction )"),
	HDFC005			("IPAY0100199" , "002" , "Denied by risk" , "Transaction denied due to previous credit check failure ( Validate Original Transaction )"),
	HDFC006			("IPAY0100140" , "002" , "Denied by risk" , "Transaction denied due to previous void check failure ( Validate Original Transaction )"),
	HDFC007			("IPAY0100137" , "002" , "Denied by risk" ,"Transaction denied due to credit amount greater than debit amount check failure ( Validate Original Transaction )"),
	HDFC008			("IPAY0100138" , "002" , "Denied by risk" , "Transaction denied due to capture amount versus auth amount check failure ( Validate Original Transaction )"),
	HDFC009			("IPAY0100139" , "002" , "Denied by risk" , "Transaction denied due to void amount versus original amount check failure ( Validate Original Transaction )"),
	HDFC010			("IPAY0100141" , "002" , "Denied by risk" , "Transaction denied due to authorization already captured ( Validate Original Transaction )"),
	HDFC011			("IPAY0200079" , "002" , "Denied by risk" , "Chargeback transaction not allowed."),
	HDFC012			("IPAY0100163" , "022" , "Failed at Acquirer" , "Problem occured during transaction."),
	HDFC013			("IPAY0100180" , "022" , "Failed at Acquirer" , "Authentication not available."),
	HDFC014			("IPAY0100205" , "022" , "Failed at Acquirer" , "Problem occurred while getting PARES details."),
	HDFC015			("IPAY0200001" , "022" , "Failed at Acquirer" , "Problem occured while getting terminal."),
	HDFC016			("IPAY0200017" , "022" , "Failed at Acquirer" , "Problem occurred while getting payment instrument list"),
	HDFC017			("IPAY0200016" , "022" , "Failed at Acquirer" , "Problem occured while getting payment instrument."),
	HDFC018			("IPAY0100206" , "022" , "Failed at Acquirer" , "Problem occurred while getting currency minor digits"),
	HDFC019			("IPAY0200022" , "004" , "Declined" , "Problem occured while getting currency."),
	HDFC020			("IPAY0200041" , "004" , "Declined" , "Problem occured while getting institution configuration."),
	HDFC021			("IPAY0200004" , "004" , "Declined" ,"Problem occured while getting password security rules."),
	HDFC022			("IPAY0200042" , "004" , "Declined" ,  "Problem occured while getting brand."),
	HDFC023			("IPAY0200008" , "004" , "Declined" , "Problem occured while verifying payment details."),
	HDFC024			("IPAY0200043" , "004" , "Declined" , "Problem occured while getting bin range details."),
	HDFC025			("IPAY0200044" , "004" , "Declined" , "Problem occured while adding transaction log details."),
	HDFC026			("IPAY0100207" , "004" , "Declined" , "Bin range not enabled."),
	HDFC027			("IPAY0100208" , "022" , "Failed at Acquirer" , "Action not enabled."),
	HDFC028			("IPAY0100125" , "004" , "Declined" , "Payment instrument not enabled."),
	HDFC029			("IPAY0100186" , "022" , "Failed at Acquirer" , "Encryption enabled."),
	HDFC030			("IPAY0100034" , "022" , "Failed at Acquirer" , "Currency code not enabled."),
	HDFC031			("IPAY0100209" , "022" , "Failed at Acquirer" , "Institution config not enabled."),
	HDFC032			("IPAY0100016" , "022" , "Failed at Acquirer" , "Password security not enabled."),
	HDFC033			("IPAY0100042" , "004" , "Declined", "Transaction time limit exceeds."),
	HDFC034			("IPAY0100041" , "004" , "Declined" ,"Payment details missing."),
	HDFC035			("IPAY0100037" , "004" , "Declined" , "Payment id missing."),
	HDFC036			("IPAY0100039" , "004" , "Declined" , "Invalid payment id ."),
	HDFC037			("IPAY0100142" , "022" , "Failed at Acquirer" , "Problem occurred while validating original transaction"),
	HDFC038			("IPAY0100053" , "022" , "Failed at Acquirer" , "Problem occured while processing direct debit."),
	HDFC039			("IPAY0100038" , "022" , "Failed at Acquirer" , "Unable to process the request."),
	HDFC040			("IPAY0100160" , "022" , "Failed at Acquirer" ,  "Unable to process the transaction."),
	HDFC041			("IPAY0100100" , "022" , "Failed at Acquirer" , "Problem occured while authorize"),
	HDFC042			("IPAY0100210" , "022" , "Failed at Acquirer" , "Problem occured during veres process."),
	HDFC043			("IPAY0100211" , "022" , "Failed at Acquirer" , "Problem occured during pareq process."),
	HDFC044			("IPAY0100212" , "022" , "Failed at Acquirer" , "Problem occured while getting veres."),
	HDFC045			("IPAY0200023" , "022" , "Failed at Acquirer" , "Problem occured while determining payment instrument."),
	HDFC046			("IPAY0100213" , "022" , "Failed at Acquirer" , "Problem occured while processing the hosted transaction request."),
	HDFC047			("IPAY0100014" , "022" , "Failed at Acquirer" , "Terminal Authentication requested with invalid tranportal id data."),
	HDFC048			("IPAY0100015" , "002" , "Denied by risk" , "Invalid tranportal password."),
	HDFC049			("IPAY0100019" , "002" , "Denied by risk" , "Invalid login attempt."),
	HDFC050			("IPAY0100017" , "002" , "Denied by risk" , "Inactive terminal."),
	HDFC051			("IPAY0100018" , "002" , "Denied by risk" , "Terminal password expired."),
	HDFC052			("IPAY0200007" , "002" , "Denied by risk" , "Problem occured while validating payment details"),
	HDFC053			("IPAY0100214" , "002" , "Denied by risk" , "Problem occurred while verifying tranportal id."),
	HDFC054			("IPAY0200006" , "002" , "Denied by risk" , "Problem occurred while verifying tranportal password."),
	HDFC055			("IPAY0100215" , "002" , "Denied by risk" , "Invalid tranportal id."),
	HDFC056			("IPAY0100020" , "002" , "Denied by risk" , "Invalid action type."),
	HDFC057			("IPAY0100021" , "004" , "Declined" , "Missing currency."),
	HDFC058			("IPAY0100022" , "004" , "Declined" , "Invalid currency."),
	HDFC059			("IPAY0100216" , "004" , "Declined" , "Invalid data received."),
	HDFC060			("IPAY0100217" , "004" , "Declined" , "Invalid payment detail."),
	HDFC061			("IPAY0100023" , "004" , "Declined" , "Missing amount."),
	HDFC062			("IPAY0100024" , "004" , "Declined" , "Invalid amount."),
	HDFC063			("IPAY0100218" , "004" , "Declined" , "Invalid brand id."),
	HDFC064			("IPAY0100105" , "022" , "Failed at Acquirer" , "Action type not supported by maestro brand."),
	HDFC065			("IPAY0100219" , "004" , "Declined" , "Missing card number."),
	HDFC066			("IPAY0100220" , "004" , "Declined" , "Invalid card number."),
	HDFC067			("IPAY0100221" , "004" , "Declined" , "Missing card holder name."),
	HDFC068			("IPAY0100222" , "004" , "Declined" , "Invalid card holder name."),
	HDFC069			("IPAY0100069" , "004" , "Declined" , "Missing payment instrument."),
	HDFC070			("IPAY0100106" , "004" , "Declined" , "Invalid payment instrument."),
	HDFC071			("IPAY0100107" , "022" , "Failed at Acquirer" , "Instrument not enabled."),
	HDFC072			("IPAY0100223" , "004" , "Declined" , "Missing cvv."),
	HDFC073			("IPAY0100224" , "004" , "Declined" , "Invalid cvv."),
	HDFC074			("IPAY0100162" , "022" , "Failed at Acquirer" , "Merchant is not allowed for encryption process."),
	HDFC075			("IPAY0100011" , "004" , "Declined" , "Merchant has not enabled for encryption process."),
	HDFC076			("IPAY0100010" , "004" , "Declined" , "Institution has not enabled for the encryption process."),
	HDFC077			("IPAY0100225" , "004" , "Declined" , "Missing card expiry year."),
	HDFC078			("IPAY0100226" , "004" , "Declined" , "Invalid card expiry year."),
	HDFC079			("IPAY0100227" , "004" , "Declined" , "Missing card expiry month."),
	HDFC080			("IPAY0100228" , "004" , "Declined" , "Invalid card expiry month."),
	HDFC081			("IPAY0100291" , "004" , "Declined" , "Transaction denied due to invalid PIN"),
	HDFC082			("IPAY0100292" , "004" , "Declined" , "Transaction denied due to missing PIN"),
	HDFC083			("IPAY0100229" , "004" , "Declined" , "Invalid card expiry day."),
	HDFC084			("IPAY0100230" , "004" , "Declined" , "Card expired."),
	HDFC085			("IPAY0100027" , "004" , "Declined" , "Invalid track id."),
	HDFC086			("IPAY0100231" , "022" , "Failed at Acquirer" , "Invalid user defined field."),
	HDFC087			("IPAY0100028" , "022" , "Failed at Acquirer" , "Invalid user defined field1."),
	HDFC088			("IPAY0100029" , "022" , "Failed at Acquirer" , "Invalid user defined field2."),
	HDFC089			("IPAY0100030" , "022" , "Failed at Acquirer" , "Invalid user defined field3."),
	HDFC090			("IPAY0100031" , "022" , "Failed at Acquirer" , "Invalid user defined field4."),
	HDFC091			("IPAY0100032" , "022" , "Failed at Acquirer" , "Invalid user defined field5."),
	HDFC092			("IPAY0100026" , "022" , "Failed at Acquirer" , "Invalid language id"),
	HDFC093			("IPAY0100025" , "004" , "Declined" , "Invalid amount or currency."),
	HDFC094			("IPAY0100232" , "004" , "Declined" , "Missing original transaction id"),
	HDFC095			("IPAY0100233" , "004" , "Declined" , "Invalid original transaction id"),
	HDFC096			("IPAY0100001" , "004" , "Declined" , "Missing error url."),
	HDFC097			("IPAY0100002" , "004" , "Declined" , "Invalid error url."),
	HDFC098			("IPAY0100003" , "004" , "Declined" , "Missing response url."),
	HDFC099			("IPAY0100004" , "004" , "Declined" , "Invalid response url."),
	HDFC100			("IPAY0100005" , "004" , "Declined" , "Missing tranportal id."),
	HDFC101			("IPAY0100007" , "004" , "Declined" , "Missing transaction data."),
	HDFC102			("IPAY0100006" , "004" , "Declined" , "Invalid tranportal id."),
	HDFC103			("IPAY0100013" , "004" , "Declined" , "Invalid transaction data."),
	HDFC104			("IPAY0100095" , "022" , "Failed at Acquirer" , "Terminal inactive."),
	HDFC105			("IPAY0200018" , "004" , "Declined" , "Problem occurred while getting transaction details"),
	HDFC106			("IPAY0200009" , "004" , "Declined" , "Problem occurred while getting payment details."),
	HDFC107			("IPAY0100044" , "004" , "Declined" , "Problem occured while loading payment page."),
	HDFC108			("IPAY0200025" , "004" , "Declined" , "Problem occurred while getting terminal details."),
	HDFC109			("IPAY0200002" , "004" , "Declined" , "Problem occurred while getting institution details."),
	HDFC110			("IPAY0200003" , "004" , "Declined" , "Problem occurred while getting merchant details."),
	HDFC111			("IPAY0200056" , "004" , "Declined" , "Problem occurred while getting brand details."),
	HDFC112			("IPAY0200038" , "004" , "Declined" , "Problem occurred while getting vpas merchant details."),
	HDFC113			("IPAY0200024" , "004" , "Declined" , "Problem occurred while getting brand rules details."),
	HDFC114			("IPAY0200057" , "004" , "Declined" , "Problem occurred while getting external connection details."),
	HDFC115			("IPAY0200058" , "004" , "Declined" , "Problem occured while updating message log 2fa details."),
	HDFC116			("IPAY0200059" , "002" , "Denied by risk" , "Problem occured while updating vpas details."),
	HDFC117			("IPAY0200060" , "002" , "Denied by risk" , "Problem occured while adding vpas details."),
	HDFC118			("IPAY0200033" , "002" , "Denied by risk" , "Problem occured while getting vpas log details."),
	HDFC119			("IPAY0200061" , "002" , "Denied by risk" , "Problem occured during batch 2fa process."),
	HDFC120			("IPAY0200062" , "002" , "Denied by risk" , "Problem occured while getting brand rules details."),
	HDFC121			("IPAY0200029" , "002" , "Denied by risk" , "Problem occured while getting external connection details."),
	HDFC122			("IPAY0200012" , "002" , "Denied by risk" , "Problem occured while updating payment log ip details."),
	HDFC123			("IPAY0200063" , "002" , "Denied by risk" , "Problem occured while updating payment log process code details."),
	HDFC124			("IPAY0200064" , "002" , "Denied by risk" , "Problem occured while updating payment log process code and ip details."),
	HDFC125			("IPAY0200065" , "002" , "Denied by risk" , "Problem occured while updating payment log description details."),
	HDFC126			("IPAY0200066" , "002" , "Denied by risk" , "Problem occured while updating payment log instrument details."),
	HDFC127			("IPAY0200067" , "002" , "Denied by risk" , "Problem occured while updating payment log udf Fields."),
	HDFC128			("IPAY0200069" , "002" , "Denied by risk" , "Problem occured while updating payment log card details."),
	HDFC129			("IPAY0200011" , "002" , "Denied by risk" , "Problem occured while getting ipblock details."),
	HDFC130			("IPAY0200070" , "002" , "Denied by risk" , "Problem occured while updating ipblock details."),
	HDFC131			("IPAY0100178" , "022" , "Failed at Acquirer" , "Merchant encryption enabled."),
	HDFC132			("IPAY0100254" , "022" , "Failed at Acquirer" , "Merchant not enabled for performing transaction."),
	HDFC133			("IPAY0100255" , "022" , "Failed at Acquirer" , "External connection not enabled."),
	HDFC134			("IPAY0100126" , "022" , "Failed at Acquirer" , "Brand not enabled."),
	HDFC135			("IPAY0100257" , "022" , "Failed at Acquirer" , "Brand rules not enabled."),
	HDFC136			("IPAY0100182" , "022" , "Failed at Acquirer" , "Vpas merchant not enabled."),
	HDFC137			("IPAY0100008" , "022" , "Failed at Acquirer" , "Terminal not enabled."),
	HDFC138			("IPAY0200015" , "002" , "Denied by risk" , "Problem occured while getting terminal details."),
	HDFC139			("IPAY0100009" , "002" , "Denied by risk" , "Institution not enabled."),
	HDFC140			("IPAY0100046" , "022" , "Failed at Acquirer" , "Payment option not enabled."),
	HDFC141			("IPAY0100033" , "022" , "Failed at Acquirer" , "Terminal action not enabled."),
	HDFC142			("IPAY0100260" , "022" , "Failed at Acquirer" , "Payment option(s) not enabled"),
	HDFC143			("IPAY0100054" , "022" , "Failed at Acquirer" , "Payment details not available."),
	HDFC144			("IPAY0200072" , "022" , "Failed at Acquirer" , "Payment log details not available."),
	HDFC145			("IPAY0100263" , "022" , "Failed at Acquirer" , "Transaction details not available."),
	HDFC146			("IPAY0100243" , "007" , "Rejected" , "NOT SUPPORTED"),
	HDFC147			("IPAY0100242" , "007" , "Rejected" , "RC_UNAVAILABLE"),
	HDFC148			("IPAY0100036" , "007" , "Rejected" , "UDF MISMATCHED"),
	HDFC149			("IPAY0100045" , "002" , "Denied by risk" , "DENIED BY RISK"),
	HDFC150			("IPAY0100266" , "022" , "Failed at Acquirer" , "Brand directory unavailable."),
	HDFC151			("IPAY0100268" , "004" , "Declined" , "3d secure not enabled for the brand"),
	HDFC152			("IPAY0100269" , "022" , "Failed at Acquirer" , "Invalid card check digit"),
	HDFC153			("IPAY0100185" , "022" , "Failed at Acquirer" , "Problem occured while authentication"),
	HDFC154			("IPAY0100270" , "004" , "Declined" , "PARES not successfull"),
	HDFC155			("IPAY0100267" , "004" , "Declined" , "PARES status not sucessfull."),
	//HDFC156			("IPAY0100265" , "022" , "Failed at Acquirer" , "PARES validation failed."),
	HDFC157			("IPAY0100264" , "004" , "Declined" , "Signature validation failed."),
	HDFC158			("IPAY0100258" , "004" , "Declined" , "Certification verification failed."),
	HDFC159			("IPAY0100262" , "022" , "Failed at Acquirer" , "Problem occured during VEREQ process."),
	HDFC160			("IPAY0200071" , "022" , "Failed at Acquirer" , "Probelm occured during authentication."),
	HDFC161			("IPAY0100181" , "022" , "Failed at Acquirer" , "Card encryption failed."),
	HDFC162			("IPAY0100051" , "022" , "Failed at Acquirer" , "Missing terminal key."),
	HDFC163			("IPAY0100050" , "022" , "Failed at Acquirer" , "Invalid terminal key."),
	HDFC164			("IPAY0100176" , "022" , "Failed at Acquirer" , "Decrypting transaction data failed."),
	HDFC165			("IPAY0100256" , "022" , "Failed at Acquirer" , "Payment encryption failed."),
	HDFC166			("IPAY0100111" , "022" , "Failed at Acquirer" , "Card decryption failed."),
	HDFC167			("IPAY0100261" , "022" , "Failed at Acquirer" , "Payment hashing failed."),
	HDFC168			("IPAY0100178" , "022" , "Failed at Acquirer" , "Invalid input data received."),
	HDFC169			("IPAY0200014" , "022" , "Failed at Acquirer" , "Problem occured during merchant response."),
	HDFC170			("IPAY0100052" , "022" , "Failed at Acquirer" , "Problem occured during merchant response encryption."),
	HDFC171			("IPAY0100035" , "022" , "Failed at Acquirer" , "Problem occured during merchant hashing process."),
	HDFC172			("IPAY0100259" , "022" , "Failed at Acquirer" , "Problem occured during merchant hashing process."),
	HDFC173			("IPAY0100253" , "022" , "Failed at Acquirer" , "Problem occured while cancelling the transaction."),
	HDFC174			("IPAY0100252" , "004" , "Declined" , "Missing veres."),
	HDFC175			("IPAY0100251" , "004" , "Declined" , "Invalid payment data."),
	HDFC176			("IPAY0100204" , "004" , "Declined" , "Missing payment details."),
	HDFC177			("IPAY0100250" , "004" , "Declined" , "Payment details verification failed."),
	HDFC178	("IPAY0100249","022","Failed at Acquirer","Merchant response url is down."),
	HDFC179	("IPAY0100088","022","Failed at Acquirer","Empty mobile number."),
	HDFC180	("IPAY0100089","022","Failed at Acquirer","Invalid mobile number."),
	HDFC181	("IPAY0100090","022","Failed at Acquirer","Empty MMID."),
	HDFC182	("IPAY0100091","022","Failed at Acquirer","Invalid MMID."),
	HDFC183	("IPAY0100092","022","Failed at Acquirer","Empty OTP number."),
	HDFC184	("IPAY0100093","004","Declined","Invalid OTP number."),
	HDFC185	("IPAY0100272","022","Failed at Acquirer","Problem occured while validating xml message format."),
	HDFC186	("IPAY0100273","022","Failed at Acquirer","Problem occured while validation VERES message format"),
	HDFC187	("IPAY0100274","022","Failed at Acquirer","VERES message format is invalid"),
	HDFC188	("IPAY0100248","007","Rejected","Problem occured while validating PARES message format"),
	HDFC189	("IPAY0100247","007","Rejected","PARES message format is invalid"),
	HDFC190	("IPAY0100283","004","Declined","Problem occured in determine payment instrument"),
	HDFC191	("IPAY0100109","004","Declined","Invalid subsequent transaction, payment id is null or empty."),
	HDFC192	("IPAY0100110","004","Declined","Invalid subsequent transaction, Tran Ref id is null or empty."),
	HDFC193	("IPAY0100284","004","Declined","Invalid subsequent transaction, track id is null or empty."),
	HDFC194	("IPAY0100114","004","Declined","Duplicate Record"),
	HDFC195	("IPAY0100057","002","Denied by risk","Transaction denied due to invalid processing option action code"),
	HDFC196	("IPAY0100115","002","Denied by risk","Transaction denied due to missing original transaction id."),
	HDFC197	("IPAY0100116","002","Denied by risk","Transaction denied due to invalid original transaction id."),
	HDFC198	("IPAY0100058","002","Denied by risk","Transaction denied due to invalid instrument"),
	HDFC199	("IPAY0100059","002","Denied by risk","Transaction denied due to invalid currency code."),
	HDFC200	("IPAY0100060","002","Denied by risk","Transaction denied due to missing amount."),
	HDFC201	("IPAY0100061","002","Denied by risk","Transaction denied due to invalid amount."),
	HDFC202	("IPAY0100062","002","Denied by risk","Transaction denied due to invalid Amount/Currency."),
	HDFC203	("IPAY0100117","002","Denied by risk","Transaction denied due to missing card number."),
	HDFC204	("IPAY0100118","002","Denied by risk","Transaction denied due to card number length error"),
	HDFC205	("IPAY0100119","002","Denied by risk","Transaction denied due to invalid card number"),
	HDFC206	("IPAY0100071","002","Denied by risk","Transaction denied due to missing CVD2."),
	HDFC207	("IPAY0100072","002","Denied by risk","Transaction denied due to invalid CVD2."),
	HDFC208	("IPAY0100086","002","Denied by risk","Transaction denied due to missing CVV."),
	HDFC209	("IPAY0100073","002","Denied by risk","Transaction denied due to invalid CVV."),
	HDFC210	("IPAY0100074","002","Denied by risk","Transaction denied due to missing expiry year."),
	HDFC211	("IPAY0100075","002","Denied by risk","Transaction denied due to invalid expiry year."),
	HDFC212	("IPAY0100076","002","Denied by risk","Transaction denied due to missing expiry month."),
	HDFC213	("IPAY0100077","002","Denied by risk","Transaction denied due to invalid expiry month."),
	HDFC214	("IPAY0100078","002","Denied by risk","Transaction denied due to missing expiry day."),
	HDFC215	("IPAY0100079","002","Denied by risk","Transaction denied due to invalid expiry day."),
	HDFC216	("IPAY0100120","002","Denied by risk","Transaction denied due to invalid payment instrument for brand data."),
	HDFC217	("IPAY0100121","002","Denied by risk","Transaction denied due to invalid card holder name."),
	HDFC218	("IPAY0100122","002","Denied by risk","Transaction denied due to invalid address."),
	HDFC219	("IPAY0100123","002","Denied by risk","Transaction denied due to invalid postal code."),
	HDFC220	("IPAY0100063","002","Denied by risk","Transaction denied due to invalid trackID"),
	HDFC221	("IPAY0100064","002","Denied by risk","Transaction denied due to invalid UDF1"),
	HDFC222	("IPAY0100065","002","Denied by risk","Transaction denied due to invalid UDF2"),
	HDFC223	("IPAY0100066","002","Denied by risk","Transaction denied due to invalid UDF3"),
	HDFC224	("IPAY0100067","002","Denied by risk","Transaction denied due to invalid UDF4"),
	HDFC225	("IPAY0100068","002","Denied by risk","Transaction denied due to invalid UDF5"),
	HDFC226	("IPAY0100069","004","Declined","Missing payment instrument."),
	HDFC227	("IPAY0100070","004","Declined","Transaction denied due to failed card check digit calculation."),
	HDFC228	("IPAY0100082","002","Denied by risk","Card address is not present"),
	HDFC229	("IPAY0100083","002","Denied by risk","Card postal code is not present"),
	HDFC230	("IPAY0100084","022","Failed at Acquirer","AVS Check : Fail"),
	HDFC231	("IPAY0100087","022","Failed at Acquirer","Card pin number is not present"),
	HDFC232	("IPAY0100085","022","Failed at Acquirer","Electronic Commerce Indicator is invalid"),
	HDFC233	("IPAY0100080","004","Declined","Transaction denied due to invalid expiration date."),
	HDFC234	("IPAY0100081","004","Declined","Card holder name is not present"),
	HDFC235	("IPAY0200027","004","Declined","Missing encrypted card number."),
	HDFC236	("IPAY0100112","004","Declined","Problem occurred in method loading original transaction data(card number, exp month / year) for orig_tran_id"),
	HDFC237	("IPAY0100124","004","Declined","Problem occured while validating transaction data"),
	HDFC238	("IPAY0100094","007","Rejected","Sorry, this instrument is not handled"),
	HDFC239	("IPAY0100285","002","Denied by risk","Transaction denied due to invalid original transaction"),
	HDFC240	("IPAY0100127","004","Declined","Problem occured while doing validate original transaction"),
	HDFC241	("IPAY0100128","002","Denied by risk","Transaction denied due to Institution ID mismatch"),
	HDFC242	("IPAY0100129","002","Denied by risk","Transaction denied due to Merchant ID mismatch"),
	HDFC243	("IPAY0100130","002","Denied by risk","Transaction denied due to Terminal ID mismatch"),
	HDFC244	("IPAY0100131","002","Denied by risk","Transaction denied due to Payment Instrument mismatch"),
	HDFC245	("IPAY0100132","002","Denied by risk","Transaction denied due to Currency Code mismatch"),
	HDFC246	("IPAY0100133","002","Denied by risk","Transaction denied due to Card Number mismatch"),
	HDFC247	("IPAY0100134","002","Denied by risk","Transaction denied due to invalid Result Code"),
	HDFC248	("IPAY0200028","004","Declined","Problem occurred while loading default institution configuration (Validate Original Transaction)"),
	HDFC249	("IPAY0100108","007","Rejected","Perform risk check : Failed"),
	HDFC250	("IPAY0100101","002","Denied by risk","Denied by risk : Risk Profile does not exist"),
	HDFC251	("IPAY0200019","007","Rejected","Problem occurred while getting risk profile details"),
	HDFC252	("IPAY0100200","002","Denied by risk","Denied by risk : Negative BIN check - Fail"),
	HDFC253	("IPAY0100191","002","Denied by risk","Denied by risk : Negative Card check - Fail"),
	HDFC254	("IPAY0100201","002","Denied by risk","Denied by risk : Declined Card check - Fail"),
	HDFC255	("IPAY0100102","002","Denied by risk","Denied by risk : Maximum Floor Limit Check - Fail"),
	HDFC256	("IPAY0100198","002","Denied by risk","Transaction denied due to Risk : Transaction count limit exceeded for the IP"),
	HDFC257	("IPAY0100246","002","Denied by risk","Problem occurred while doing perform ip risk check"),
	HDFC258	("IPAY0200040","002","Denied by risk","Problem occurred while performing card risk check"),
	HDFC259	("IPAY0200021","002","Denied by risk","Problem occurred while performing risk check"),
	HDFC260	("IPAY0200020","002","Denied by risk","Problem occurred while performing transaction risk check"),
	HDFC261	("IPAY0100103","002","Denied by risk","Transaction denied due to Risk : Maximum transaction count"),
	HDFC262	("IPAY0100197","002","Denied by risk","Transaction denied due to Risk : Maximum debit amount"),
	HDFC263	("IPAY0100190","002","Denied by risk","Transaction denied due to Risk : Maximum floor limit transaction count"),
	HDFC264	("IPAY0100289","002","Denied by risk","Transaction denied due to Risk : Maximum credit amount"),
	HDFC265	("IPAY0100104","002","Denied by risk","Transaction denied due to Risk : Maximum processing amount"),
	HDFC266	("IPAY0100196","002","Denied by risk","Transaction denied due to Risk : Maximum processing amount"),
	HDFC267	("IPAY0100195","002","Denied by risk","Transaction denied due to Risk : Maximum credit processing amount"),
	HDFC268	("IPAY0100194","002","Denied by risk","Transaction denied due to Risk : Minimum Transaction Amount processing"),
	HDFC269	("IPAY0100144","007","Rejected","ISO MSG is null. See log for more details!"),
	HDFC270	("IPAY0100245","007","Rejected","Problem occurred while sending/receivinig ISO message"),
	HDFC271	("IPAY0200034","007","Rejected","Problem occurred while getting details from VPASLOG table for payment id : null"),
	HDFC272	("IPAY0200045","007","Rejected","Problem occurred while updating VPASLOG table"),
	HDFC273	("IPAY0200046","004","Declined","Unable to update VPASLOG table, payment id is null"),
	HDFC274	("IPAY0200047","007","Rejected","Problem occurred while getting details from VPASLOG table for payment id"),
	HDFC275	("IPAY0200048","007","Rejected","Problem occurred while getting details from VPASLOG table"),
	HDFC276	("IPAY0200049","004","Declined","Card number is null. Unable to update risk factors in negative card table & declined card table"),
	HDFC277	("IPAY0200050","004","Declined","Problem occurred while updating risk in negative card details"),
	HDFC278	("IPAY0100043","007","Rejected","IP address is blocked already"),
	HDFC279	("IPAY0200068","007","Rejected","Problem occured while validating IP address blocking"),
	HDFC280	("IPAY0200051","004","Declined","Problem occurred while updating risk in declined card table"),
	HDFC281	("IPAY0200052","007","Rejected","Problem occurred while updating risk factor"),
	HDFC282	("IPAY0100143","004","Declined","Transaction action is null"),
	HDFC283	("IPAY0100286","004","Declined","Unknown IMPS Tran Action Code encountered"),
	HDFC284	("IPAY0100097","004","Declined","IMPS for Terminal Not Active for Transaction request, Terminal"),
	HDFC285	("IPAY0100287","004","Declined","Terminal Action not enabled for Transaction request, Terminal"),
	HDFC286	("IPAY0100288","004","Declined","Terminal Payment Instrument not enabled for Transaction request, Terminal"),
	HDFC287	("IPAY0100096","004","Declined","IMPS for Institution Not Active for Transaction request, Institution"),
	HDFC288	("IPAY0100164","004","Declined","Transaction Not Processed due to Invalid ECI value"),
	HDFC289	("IPAY0100165","004","Declined","Transaction Not Processed due to Empty ECI value"),
	HDFC290	("IPAY0100167","004","Declined","Transaction Not Processed due to Invalid Authentication Status"),
	HDFC291	("IPAY0100166","004","Declined","Transaction Not Processed due to Empty Authentication Status"),
	HDFC292	("IPAY0100169","004","Declined","Transaction Not Processed due to Invalid Enrollment Status"),
	HDFC293	("IPAY0100170","004","Declined","Transaction Not Processed due to Invalid Cavv"),
	HDFC294	("IPAY0100171","004","Declined","Transaction Not Processed due to Empty Cavv"),
	HDFC295	("IPAY0100168","004","Declined","Transaction Not Processed due to Empty Enrollment Status"),
	HDFC296	("IPAY0100187","007","Rejected","Customer ID is missing for Faster Checkout"),
	HDFC297	("IPAY0100188","004","Declined","Transaction Mode(FC) is missing for Faster Checkout"),
	HDFC298	("IPAY0200039","004","Declined","Problem occured while getting Faster Checkout details"),
	HDFC299	("IPAY0100192","004","Declined","Transaction Not Processed due to Empty Xid"),
	HDFC300	("IPAY0100193","004","Declined","Transaction Not Processed due to Invalid Xid"),
	HDFC301	("IPAY0100189","002","Denied by risk","Transaction denied due to brand directory unavailable"),
	HDFC302	("IPAY0100048","010","Cancelled","CANCELLED"),
	HDFC303	("IPAY0100049","004","Declined","Transaction Declined Due To Exceeding OTP Attempts"),
	HDFC304	("IPAY0200030","007","Rejected","No external connection details for extr conn id "),
	HDFC305	("IPAY0200031","007","Rejected","Alternate external connection details not found for the alt extr conn id "),
	HDFC306	("IPAY0200032","007","Rejected","Problem occurred while getting external connection details for extr conn id "),
	HDFC307	("IPAY0100145","007","Rejected","Problem occurred while loading default messages in ISO Formatter"),
	HDFC308	("IPAY0100146","007","Rejected","Problem occurred while encrypting PIN"),
	HDFC309	("IPAY0100147","007","Rejected","Problem occurred while formatting purchase request in B24 ISO Message Formatter"),
	HDFC310	("IPAY0100148","007","Rejected","Problem occurred while hashing ecom pin."),
	HDFC311	("IPAY0100149","021","Invalid","Invalid PIN Type"),
	HDFC312	("IPAY0100150","004","Declined","Problem occurred while formatting Reverse purchase request in B24 ISO Message Formatter"),
	HDFC313	("IPAY0100151","004","Declined","Problem occurred while formatting Credit request in B24 ISO Message Formatter"),
	HDFC314	("IPAY0100152","004","Declined","Problem occurred while formatting authorization request in B24 ISO Message Formatter"),
	HDFC315	("IPAY0100153","004","Declined","Problem occurred while formatting Capture request in B24 ISO Message Formatter"),
	HDFC316	("IPAY0100154","004","Declined","Problem occurred while formatting Reverse Credit request in B24 ISO Message Formatter"),
	HDFC317	("IPAY0100155","004","Declined","Problem occurred while formatting reverse authorization request in B24 ISO Message Formatter"),
	HDFC318	("IPAY0100156","004","Declined","Problem occurred while formatting Reverse Capture request in B24 ISO Message Formatter"),
	HDFC319	("IPAY0100157","004","Declined","Problem occurred while formatting vpas capture request in B24 ISO Message Formatter"),
	HDFC320	("IPAY0200037","004","Declined","Error Occured while getting Merchant ID"),
	HDFC321	("IPAY0100183","022","Failed at Acquirer","Error Occured Due to bytePAReq is null"),
	HDFC322	("IPAY0100184","022","Failed at Acquirer","Error Occured while Parsing PAReq"),
	HDFC323	("IPAY0100158","003","Timed out at Acquirer","Host timeout"),
	HDFC324	("IPAY0100159","022","Failed at Acquirer","External message system error"),
	HDFC325	("IPAY0100241","004","Declined","Problem occurred while formatting purchase request in VISA ISO Message Formatter"),
	HDFC326	("IPAY0100240","004","Declined","Problem occurred while formatting Credit request in VISA ISO Message Formatter"),
	HDFC327	("IPAY0100239","004","Declined","Problem occurred while formatting authorization request in VISA ISO Message Formatter"),
	HDFC328	("IPAY0100238","004","Declined","Problem occurred while formatting Capture request in VISA ISO Message Formatter"),
	HDFC329	("IPAY0100237","004","Declined","Problem occurred while formatting Reverse purchase request in VISA ISO Message Formatter"),
	HDFC330	("IPAY0100236","004","Declined","Problem occurred while formatting Reverse Credit request in VISA ISO Message Formatter"),
	HDFC331	("IPAY0100235","004","Declined","Problem occurred while formatting reverse authorization request in VISA ISO Message Formatter"),
	HDFC332	("IPAY0100234","004","Declined","Problem occurred while formatting Reverse Capture request in VISA ISO Message Formatter"),
	HDFC333	("IPAY0100271","004","Declined","Problem occurred while formatting purchase request in MASTER ISO Message Formatter"),
	HDFC334	("IPAY0100275","004","Declined","Problem occurred while formatting Credit request in MASTER ISO Message Formatter"),
	HDFC335	("IPAY0100276","004","Declined","Problem occurred while formatting Reverse purchase request in MASTER ISO Message Formatter"),
	HDFC336	("IPAY0100277","004","Declined","Problem occurred while formatting Reverse Credit request in MASTER ISO Message Formatter"),
	HDFC337	("IPAY0100278","004","Declined","Problem occurred while formatting reverse authorization request in MASTER ISO Message Formatter"),
	HDFC338	("IPAY0100279","004","Declined","Problem occurred while formatting Reverse Capture request in MASTER ISO Message Formatter"),
	HDFC339	("IPAY0100280","004","Declined","Problem occurred while formatting Capture request in MASTER ISO Message Formatter"),
	HDFC340	("IPAY0200053","004","Declined","Problem occured while updating payment log currency details."),
	HDFC341	("IPAY0200054","004","Declined","Problem occured while inserting currency conversion currency details."),
	HDFC342	("IPAY0200055","004","Declined"," Problem occured while updating currency conversion currency details."),
	HDFC343	("IPAY0100281","002","Denied by risk","Transaction Denied due to missing Master Brand"),
	HDFC344	("IPAY0100282","002","Denied by risk","Transaction Denied due to missing Visa Brand"),
	HDFC345	("IPAY0100293","002","Denied by risk","Transaction denied due to duplicate Merchant trackid"),
	HDFC346	("IPAY0100294","002","Denied by risk","Transaction denied due to missing Merchant trackid"),
	HDFC347	("IPAY0200073","004","Declined","Country Code not available for the Card."),
	HDFC348	("IPAY0200074","004","Declined","Restricted Country Code for the Transaction."),
	HDFC349	("IPAY0200075","004","Declined","Problem occured while getting Original transaction log details."),
	HDFC350	("IPAY0100211","007","Rejected","Problem occured during EnStage process."),
	HDFC351	("IPAY0100267","007","Rejected","Enstage Response status not sucessfull."),
	//HDFC352	("IPAY0100265","010","Rejected","Enstage Response validation failed."),
	HDFC353	("IPAY0100072","002","Denied by risk","Transaction denied due to invalid CVD2 for rupay card."),
	HDFC354	("IPAY0100265","010","Rejected","enstage response validation failed"),
	HDFC355	("IPAY0100205","007","Rejected","Problem occurred while getting enstage response details."),
	HDFC356  ("IPAY0101265", "010", "Cancelled","Transaction cancelled by user"),
	HDFC357	("NOT CAPTURED","007","Rejected","Transaction rejected by acquirer."),
	HDFC358	("CM90000","007","Rejected","CM90000-Problem occured during pareq process."),
	HDFC359 ("FSS0001", "004","Declined", "FSS0001-Authentication Not Available"),
	HDFC360 ("GV00007", "022","Failed at Acquirer", "GV00007-Signature validation failed"),
	HDFC361 ("GV00008", "022","Failed at Acquirer", "GV00008-Signature validation failed"),
	HDFC362 ("GV00004", "022","Failed at Acquirer", "GV00004-PARes status not sucessful");
	
	
	private HdfcResultType(String bankCode, String paymentGatewayCode, String statusCode, String message) {
		this.bankCode = bankCode;
		this.paymentGatewayCode = paymentGatewayCode;
		this.statusCode = statusCode;
		this.message = message;
	}

	public static HdfcResultType getInstanceFromName(String code) {
		HdfcResultType[] statusTypes = HdfcResultType.values();
		for (HdfcResultType statusType : statusTypes) {
			if (String.valueOf(statusType.getBankCode()).toUpperCase().equals(code)) {
				return statusType;
			}
		}
		return null;
	}

	private final String bankCode;
	private final String paymentGatewayCode;
	private final String statusCode;
	private final String message;

	public String getBankCode() {
		return bankCode;
	}
	
	public String getStatusCode() {
		return statusCode;
	}

	public String getMessage() {
		return message;
	}

	public String getPaymentGatewayCode() {
		return paymentGatewayCode;
	}
}