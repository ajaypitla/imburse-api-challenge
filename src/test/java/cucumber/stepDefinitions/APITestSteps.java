package cucumber.stepDefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.time.Instant;
import java.util.Random;
import java.util.logging.Logger;


public class APITestSteps {
    public static String basePath = "https://sandbox-api.imbursepayments.com/";
    public static String accountId = "782f1b71-7ca4-4465-917f-68d58ffbec8b";
    public static String tenantId = "6423ae63-59b6-4986-a949-c910ac622471";
    public static String schemeId = "654EB81FF7F07F7CF5A1EE3FF6972E90";
    public static String STATUS_CODE;
    RequestSpecification request = RestAssured.given();
    public static ResponseBody responseBody;
    public static Response response;
    public String apiPath;
    String hmacToken;
    String bearerToken;
    String orderRef = RandomStringUtils.randomAlphabetic(10);
    String instructionRef = RandomStringUtils.randomAlphabetic(10);
    String invalidInstructionRef = RandomStringUtils.randomAlphabetic(55) + "'$5%^sdfgsfgsfvsefv@";
    String customerRef = RandomStringUtils.randomAlphabetic(49);
    String direction = "DEBIT";
    String amount = "7.77";
    String currency = "EUR";
    String country = "IE";

    @Given("^generate an hmac token$")
    public String returnHMACToken() {

        String nonce = RandomStringUtils.randomAlphanumeric(21);

        String publicKey = "7934d5e6-260c-46d5-9309-e72a59cb90cd";
        String privateKey = "aWRpTN9tRsf2EyM8rcvz7bohO/ fAg6IF+daZ1JYE9AM=";

        byte[] privateKeyBytes = Base64.decodeBase64(privateKey);
        String bodySignature = "";

        long epoch = Instant.now().getEpochSecond();

        String unsignedSignature = publicKey + ":" + nonce + ":" + epoch + ":" + bodySignature;

        byte[] utf8Signature = unsignedSignature.getBytes(StandardCharsets.UTF_8);
        byte[] hashedSignature = HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_256, privateKeyBytes).doFinal(utf8Signature);
        String signedSignature = new String(Base64.encodeBase64(hashedSignature));
        hmacToken = publicKey + ":" + nonce + ":" + epoch + ":" + signedSignature;

        return hmacToken;
    }

    @Given("^generate a bearer token using hmac token$")
    public String returnBearerTokenUsingHmacToken() {

        apiPath = "v1/identity/hmac";
        RequestSpecification request = RestAssured.given();
        request.header("Authorization", "Hmac " + returnHMACToken());
        response = request.post(basePath + apiPath);
        STATUS_CODE = String.valueOf(response.getStatusCode());
        Assert.assertEquals("201", STATUS_CODE);
        responseBody = response.body();
        JsonPath jsonpathEvaluator = responseBody.jsonPath();
        bearerToken = jsonpathEvaluator.get("accessToken");
        return bearerToken;
    }

    @When("create an order via rest call")
    public void createAnOrderViaRestCall() {

        apiPath = "v1/order-management";
        request.header("Authorization", "bearer " + returnBearerTokenUsingHmacToken());
        request.header("x-account-id", accountId);
        request.header("x-tenant-id", tenantId);

        String payLoadOrderCreation = "{\n" +
                "  \"orderRef\": "+orderRef+" \n" +
                "  \"instructions\": [\n" +
                "    {\n" +
                "      \"instructionRef\": "+instructionRef+" \n" +
                "      \"customerRef\": "+customerRef+" \n" +
                "      \"direction\": "+direction+" \n" +
                "      \"amount\": "+amount+" \n" +
                "      \"currency\": "+currency+" \n" +
                "      \"country\": "+country+" \n" +
                "      \"settledByDate\": \"2021-03-05\",\n" +
                "      \"schemeId\": "+schemeId+" \n" +
                "    }\n" +
                "  ]\n" +
                "}";

        response = request.body(payLoadOrderCreation).post(basePath + apiPath);

        STATUS_CODE = String.valueOf(response.getStatusCode());
        Assert.assertEquals("201", STATUS_CODE);
    }

    @When("create an instruction via rest call")
    public void createAnInstructionViaRestCall() {

        apiPath = "v1/order-management/"+orderRef+"/instruction";
        request.header("Authorization", "bearer " + returnBearerTokenUsingHmacToken());
        request.header("x-account-id", accountId);
        request.header("x-tenant-id", tenantId);

        String payLoadInstructionCreation = "{\n" +
                "      \"instructionRef\": "+instructionRef+", \n" +
                "      \"customerRef\": "+customerRef+", \n" +
                "      \"direction\": "+direction+", \n" +
                "      \"amount\": "+amount+", \n" +
                "      \"currency\": "+currency+", \n" +
                "      \"country\": "+country+", \n" +
                "      \"settledByDate\": \"2021-03-05\",\n" +
                "      \"schemeId\": "+schemeId+" \n" +
                "  }\n" +
                "}";

        response = request.body(payLoadInstructionCreation).post(basePath + apiPath);

        STATUS_CODE = String.valueOf(response.getStatusCode());

        Assert.assertEquals("201", STATUS_CODE);
    }

    @And("create an instruction with invalid instruction ref via rest call")
    public void createAnInstructionWithInvalidIntructionRefViaRestCall() {
        apiPath = "v1/order-management/"+orderRef+"/instruction";
        request.header("Authorization", "bearer " + returnBearerTokenUsingHmacToken());
        request.header("x-account-id", accountId);
        request.header("x-tenant-id", tenantId);
        System.out.println("invalidInstructionRef:: " + invalidInstructionRef);
        String payLoadInstructionCreation = "{\n" +
                "      \"instructionRef\": "+invalidInstructionRef+" \n" +
                "      \"customerRef\": "+customerRef+" \n" +
                "      \"direction\": "+direction+" \n" +
                "      \"amount\": "+amount+" \n" +
                "      \"currency\": "+currency+" \n" +
                "      \"country\": "+country+" \n" +
                "      \"settledByDate\": \"2021-03-05\",\n" +
                "      \"schemeId\": "+schemeId+" \n" +
                "  }\n" +
                "}";

        response = request.body(payLoadInstructionCreation).post(basePath + apiPath);
        responseBody = response.body();

        STATUS_CODE = String.valueOf(response.getStatusCode());

        Assert.assertEquals("401", STATUS_CODE);



        JsonPath jsonpathEvaluator = responseBody.jsonPath();
        String actualErrorCode = jsonpathEvaluator.get("errors[0].errorCode");
        Assert.assertEquals("INSTRUCTION_REF_LENGTH_OUT_OF_RANGE",actualErrorCode);
    }
}
