<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- <title>Invoice Details</title>
    <link rel="icon" href="../image/favicon-32x32.png"> -->
    <!-- <link href="../css/default.css" rel="stylesheet" type="text/css" /> -->
    <!-- <link href="../css/custom.css" rel="stylesheet" type="text/css" /> -->

    <script>
        if (self == top) {
            var theBody = document.getElementsByTagName('body')[0];
            theBody.style.display = "block";
        } else {
            top.location = self.location;
        }

        function copyInvoiceLink(copyLinkElement) {
            document.getElementById("invoiceShortLinkBtn").disabled = !document.queryCommandSupported('copy');
            // document.getElementById("invoiceLinkBtn").disabled = !document.queryCommandSupported('copy');
            var copiedLink = document.getElementById(copyLinkElement);
            copiedLink.select();
            document.execCommand('copy');
        }
    </script>

    <style>
        .navbar-header {
            background-color: #fff;
        }
        
        .btn-custom {
            margin-top: 5px;
            height: 27px;
            border: 1px solid #5e68ab;
            width: 73px;
            padding: 5px;
            background: url(../image/textF.jpg) repeat-x bottom #5e68ab;
            font: bold 12px Tahoma;
            color: #fff;
            cursor: pointer;
            border-radius: 5px;
            z-index: 999;
            position: relative;
        }
    </style>
</head>
<body>
    <div class="scrollDR">
        <div style="width: 700px;">
            <div>
                <h3 class="font-weight-bold font-size-14 mb-10">Detail Information</h3>
                <div class="lpay_table mb-20">
                    <table class="display" cellspacing="0" width="100%">
                        <thead class="bg-primary">
                            <tr>
                                <th>Invoice Id</th>
                                <th>Name</th>
                                <th>City</th>
                                <th>Country</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr class="odd">
                                <td><s:property value="%{invoice.invoiceId}" /></td>
                                <td><s:property value="%{invoice.name}" /></td>
                                <td><s:property value="%{invoice.city}" /></td>
                                <td><s:property value="%{invoice.country}" /></td>
                            </tr>
                            <tr class="bg-primary">
                                <th>State</th>
                                <th>Zip</th>
                                <th>Phone</th>
                                <th>Email</th>
                            </tr>
                            <tr class="odd">
                                <td><s:property value="%{invoice.state}" /></td>
                                <td><s:property value="%{invoice.zip}" /></td>
                                <td><s:property value="%{invoice.phone}" /></td>
                                <td><s:property value="%{invoice.email}" /></td>
                            </tr>
                            <tr class="bg-primary"><th colspan="4">Address</th></tr>
                            <tr class="odd">
                                <td colspan="4"><s:property value="%{invoice.address}" /></td>                                
                            </tr>
                        </tbody>
                    </table>
                </div>                
            </div>

            <div>
                <h3 class="font-weight-bold font-size-14 mb-10">Product Information</h3>
                <div class="lpay_table mb-20">
                    <table class="display" cellspacing="0" width="100%">
                        <thead class="bg-primary">
                            <tr>
                                <th>Name</th>
                                <th>Description</th>
                                <th>Quantity</th>
                                <th>Amount</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr class="odd">
                                <td><s:property value="%{invoice.productName}" /></td>
                                <td><s:property value="%{invoice.productDesc}" /></td>
                                <td><s:property value="%{invoice.quantity}" /></td>
                                <td><s:property value="%{invoice.amount}" /></td>
                            </tr>
                            <tr class="bg-primary">
                                <td colspan="3"></td>
                                <td class="text-white">Service Charge</td>
                            </tr>
                            <tr>
                                <td colspan="3"></td>
                                <td><s:property value="%{invoice.serviceCharge}" /></td>
                            </tr>
                            <tr class="bg-primary">
                                <th>All prices are in</th>
                                <th colspan="2">Expire in days</th>                                
                                <th>Total Amount</th>
                            </tr>
                            <tr class="odd">
                                <td><s:property value="%{currencyName}" /></td>
                                <td colspan="2"><s:property value="%{invoice.expiresDay}" /></td>
                                <td><s:property value="%{invoice.totalAmount}" /></td>                                
                            </tr>                            
                        </tbody>
                    </table>
                </div>
            </div>

            <s:if test="%{invoice.invoiceType=='PROMOTIONAL PAYMENT'}">
                <div>
                    <h3 class="font-weight-bold font-size-14 mb-10">Sender Information</h3>
                    <table class="display" cellspacing="0" width="100%">
                        <thead class="bg-primary">
                            <tr>
                                <th>Recipient Mobile</th>
                                <th>Message Body</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr class="odd">
                                <td><s:property value="%{invoice.recipientMobile}" /></td>
                                <td><s:property value="%{invoice.messageBody}" /></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </s:if>

            <div class="lpay_table mb-20">
                <table class="display" cellspacing="0" width="100%">
                    <thead class="bg-primary">
                        <tr>
                            <th colspan="2">Payment Links</th>
                        </tr>
                    </thead>
                    <tbody>
                        <!-- <tr class="odd">
                            <td class="lpay_input_group">
                                <input
                                    id="invoiceLink"
                                    onkeydown="document.getElementById('invoiceLinkBtn').focus();"
                                    type="text"
                                    class="lpay_input"
                                    value=<s:property value="invoiceUrl" />>
                                </input>
                            </td>
                            <td>
                                <button id="invoiceLinkBtn" onclick="copyInvoiceLink('invoiceLink')" class="btn-custom">Copy Link</button>
                            </td>
                        </tr> -->
                        <tr>
                            <td class="lpay_input_group">
                                <input
                                    id="invoiceShortLink"
                                    onkeydown="document.getElementById('invoiceLinkBtn').focus();"
                                    type="text"
                                    class="lpay_input"
                                    value=<s:property value="%{invoice.shortUrl}" />>
                                </input>
                            </td>
                            <td>
                                <button id="invoiceShortLinkBtn" onclick="copyInvoiceLink('invoiceShortLink')" class="btn-custom">Copy Link</button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            
        </div>
    </div>
</body>
</html>