package com.app.bemyrider.WebServices;

/**
 * Created by nct58 on 20/6/17.
 */

public class WebServiceUrl {

     private final static String hostname = "https://bemyrider.it/";
//    private final static String hostname = "https://gotasker.ncryptedprojects.com/";

    public final static String terms_and_conditions = "https://bemyrider.it/app/termini-e-condizioni-bemyrider/";
    private final static String baseUrl = hostname + "ws/";

    /*USER*/
    public final static String URL_GET_WALLET_DETAILS = baseUrl + "profile/walletdetails/";
    public final static String URL_GET_FAVORITE_LIST = baseUrl + "services/getfavoriteservice";
    public final static String URL_ADD_REVIEW = baseUrl + "services/addproviderreview";
    public final static String URL_BOOK_SERVICE = baseUrl + "finance/servicerequestpayment";
    public final static String URL_REDDEMRE_REQUEST = baseUrl + "finance/sendredeemrequest";
    public final static String URL_PROVIDERLIST = baseUrl + "services/providerlist";
    public final static String URL_BOOKSERVICE = baseUrl + "services/send_service_request";
    public final static String URL_FAVOURITETOGGLE = baseUrl + "services/likedislikeservices";
    public final static String URL_GETSERVICEHISTORY = baseUrl + "services/customerservices";
    public final static String URL_GETLIST = baseUrl + "services/getList";
    public final static String URL_DEACTIVATE_USER = baseUrl + "profile/deactiveuser";

    public final static String URL_SMALL = baseUrl + "services/small";
    public final static String URL_MEDIUM = baseUrl + "services/medium";
    public final static String URL_LARGE = baseUrl + "services/large";

    /*PARTNER*/
    public final static String URL_CATEGORYLIST = baseUrl + "services/categorylist";
    public final static String URL_MYSERVICE = baseUrl + "services/providerservices";
    public final static String URL_SERVICEDETAILS = baseUrl + "services/providerservice";
    public final static String URL_ADD_NEW_SERVICES = baseUrl + "services/addservices";
    public final static String MY_SERVICE_DELETE = baseUrl + "services/deleteservices";
    public final static String URL_PROVIDER_REVIEWS = baseUrl + "services/providerreviews";
    public final static String URL_DELETE_SERVICE_IMAGE = baseUrl + "services/deletemedia";
    public final static String URL_ACCEPT_SERVICE = baseUrl + "services/acceptservice";
    public final static String URL_GET_FINANCIAL_INFO = baseUrl + "finance/financialinfo";

    /*INFO*/
    public final static String URL_GET_CMS_INFO = baseUrl + "cms/getcmslist";

    /*COMMON*/
    public final static String URL_LOGIN = baseUrl + "profile/login";
    public final static String URL_SOCIAL_SIGNUP = baseUrl + "profile/socialsignup";
    public final static String URL_SOCIAL_LOGIN = baseUrl + "profile/sociallogin";
    public final static String URL_COUNTRY_CODE = baseUrl + "profile/countrycodelist";
    public final static String URL_SIGNUP = baseUrl + "profile/register";
    public final static String URL_PROFILE = baseUrl + "profile";
    public final static String URL_RESEND_ACTIVATION_MAIL = baseUrl + "profile/resend-activation";
    public final static String URL_SUBCATEGORYLIST = baseUrl + "services/subcategorylist";
    public final static String URL_SERVICELIST = baseUrl + "services/servicelist";
    public final static String URL_EDIT_PROFILE = baseUrl + "profile/editprofile";
    public final static String MY_SERVICE_DETAILS = baseUrl + "services/providerservicedetail";
    public final static String MY_SERVICE_DETAILS_HOME = baseUrl + "services/homeproviderservicedetail";
    public final static String URL_FORGET_PASSWORD = baseUrl + "profile/forgotpassword";
    public final static String URL_CANCEL_SERVICE = baseUrl + "services/cancelservice";
    public final static String URL_GET_MESSAGE_LIST = baseUrl + "messages/getmessagelist";
    public final static String URL_GET_MESSAGE_DETAILS = baseUrl + "messages/getmessage";
    public final static String URL_SEND_MESSAGE = baseUrl + "messages/sendmessage";
    public final static String URL_DISPUTE_DETAILS = baseUrl + "disputes/getdisputedetails";
    public final static String URL_SEND_DISPUTE_MESSAGE = baseUrl + "disputes/senddisputemessage";
    public final static String URL_ACCEPT_DISPUTE = baseUrl + "disputes/acceptdispute";
    public final static String URL_GET_NOTIFICATION = baseUrl + "profile/getnotificationlist";
    public final static String URL_UPDATE_NOTIFICATION = baseUrl + "profile/updatenotification";
    public final static String URL_SEND_PRAPOSAL = baseUrl + "services/sendproposal";
    public final static String URL_ESCALAPERTO_ADMIN = baseUrl + "disputes/escalatetoadmin";
    public final static String URL_LOGOUT = baseUrl + "profile/logout";
    public final static String URL_SEND_FEEDBACK = baseUrl + "messages/feedback";
    public final static String URL_SEND_CONTACTUS = baseUrl + "messages/contactus";
    public final static String URL_ACCEPT_PROPOSAL = baseUrl + "services/acceptproposal";
    public final static String URL_EXTEND_SERVICE = baseUrl + "services/extendservice";
    public final static String URL_GET_LANGUAGE = baseUrl + "language/getlanguages";
    public final static String URL_ACCEPT_EXTEND_REQUEST = baseUrl + "services/acceptextendservice";
    public final static String URL_EXTEND_SERVICE_PAYMENT = baseUrl + "services/extendservicepayment";
    public final static String URL_DEPOSITE_HISTORY = baseUrl + "finance/deposithistory";
    public final static String URL_REDEEM_HISTORY = baseUrl + "finance/redeemhistory";
    public final static String URL_PAYMENT_HISTORY = baseUrl + "finance/paymenthistory";
    public final static String URL_AVAILABLE_NOW = baseUrl + "profile/updateavailablestatus";
    public final static String URL_CHANGE_PASS = baseUrl + "profile/changepassword";
    public final static String URL_SERVICE_MIN_MAX_PRICE = baseUrl + "services/minmaxprice";
    public final static String URL_GETSITESETTINGDATA = baseUrl + "other/getSiteSettingData/";
    public final static String URL_PAYPALBUTTON = baseUrl + "payment-nct/paypal-button.php?user_id=";
    public final static String URL_DOWNLOAD_INVOICE = hostname + "download-invoice";
    public final static String URL_SERVICE_REQUEST_LIST = baseUrl + "services/providertasks";
    public final static String URL_RASEDISPUTE = baseUrl + "disputes/raisedispute";
    public final static String URL_GETDISPUTELIST = baseUrl + "disputes/getdisputelist";
    public final static String URL_GETNOTIFICATIONS = baseUrl + "notifications/getNotifications";
    public final static String URL_GETOFFLINEDATA = baseUrl + "offline/getOfflineData";

    public final static String URL_POPULARSERVICS = baseUrl + "services/popularservice";
    public final static String URL_POPULARTASKERS = baseUrl + "services/populartasker";
    public final static String URL_PROVIDERPAYMENTHISTORY = baseUrl + "services/transectionhistory";
    public final static String URL_FLAGUSER = baseUrl + "profile/flag_user";

    public final static String URL_STRIPE_CONNECT = baseUrl + "profile/stripe_connect";
    public final static String URL_STRIPE_PAYMENT = baseUrl + "finance/successpayment";

}
