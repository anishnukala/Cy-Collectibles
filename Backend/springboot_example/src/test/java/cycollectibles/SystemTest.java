package cycollectibles;

//import static java.lang.System.err;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cycollectibles.Comments.CommentRequest;
import cycollectibles.Messages.Channel;
import cycollectibles.Messages.ChannelRequestDTO;
import cycollectibles.Users.LoginRequest;
import cycollectibles.Users.SignUpRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class SystemTest {

    @LocalServerPort
    int port;

    private static final long timestamp = System.currentTimeMillis();
    private static final String TEST_USERNAME = "testuser_" + timestamp;
    private static final String TEST_EMAIL = "test_" + timestamp + "@test.com";

    @Before
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    // ===== USER TESTS =====

    @Test
    public void testUserSignupInvalidPassword() {
        SignUpRequest sr = new SignUpRequest();
        sr.setUserType("buyer");
        sr.setEmailId(TEST_EMAIL);
        sr.setUsername(TEST_USERNAME);
        sr.setPassword("123"); // too short

        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(sr)
                .when()
                .post("/users");
        int statusCode = response.getStatusCode();

        assertEquals(400, statusCode);

        // check the error message
        String body = response.getBody().asString();
        try {
            JSONObject responseBody = new JSONObject(body);
            assertEquals("Password must be at least 7 characters long", responseBody.getString("password"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUserLogin() {
        // POST /users/login
        LoginRequest lr = new LoginRequest();
        lr.setUsername("not_a_user");
        lr.setPassword("wrongwrong");

        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(lr)
                .when()
                .post("/users/login");
        int statusCode = response.getStatusCode();

        assertEquals(401, statusCode);

        // check the error message
        String body = response.getBody().asString();
        try {
            JSONObject responseBody = new JSONObject(body);
            assertEquals("Incorrect username or password", responseBody.getString("message"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetUserNotFound(){
        Response response = RestAssured.given()
                .when()
                .get("/users/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testPatchUser(){
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body("{}")
                .when()
                .patch("/users/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testDeleteUser(){
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .when()
                .delete("/users/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testPutUser(){
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body("{}")
                .when()
                .put("/users/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testGetAllUsers(){
        Response response = RestAssured.given().when().get("/users");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testGetUserCount(){
        Response response = RestAssured.given().when().get("/users/count");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testGetUserStats(){
        Response response = RestAssured.given().when().get("/users/stats");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testSignupDuplicateUsername(){
        SignUpRequest sr = new SignUpRequest();
        sr.setUserType("buyer");
        sr.setEmailId("unique_" + timestamp + "@test.com");
        sr.setUsername("buyer");
        sr.setPassword("1234567");

        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(sr)
                .when()
                .post("/users");
        assertEquals(409, response.getStatusCode());
    }

    // ===== POSTING TESTS =====

    @Test
    public void testCreatePostingInvalidSeller() {
        Response response = RestAssured.given()
                .contentType("multipart/form-data")
                .multiPart("data", "{\"sellerId\":-1,\"title\":\"test\",\"description\":\"test\",\"price\":10,\"genre\":\"Comics\"}", "application/json")
                .when()
                .post("/postings");

        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testGetPostingsSeller(){
        Response response = RestAssured.given()
                .when()
                .get("/postings/seller/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testReportPostings(){
        Response response = RestAssured.given()
                .when()
                .patch("/postings/report/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testGetPostings(){
        Response response = RestAssured.given()
                .when()
                .get("/postings/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testDeletePosting(){
        Response response = RestAssured.given()
                .when()
                .delete("/postings/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testAIPosting(){
        Response response = RestAssured.given()
                .when()
                .get("/postings/ai/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testGetAllPostings(){
        Response response = RestAssured.given().when().get("/postings");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testGetPostingCount(){
        Response response = RestAssured.given().when().get("/postings/count");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testGetGenres(){
        Response response = RestAssured.given().when().get("/postings/genre");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testGetPostingsWithFilters(){
        Response response = RestAssured.given()
                .queryParam("genre", "Comics")
                .queryParam("minPrice", "10")
                .queryParam("maxPrice", "100")
                .queryParam("titlePartial", "test")
                .when()
                .get("/postings");
        assertEquals(200, response.getStatusCode());
    }
    // ===== TRANSACTION TESTS =====

    @Test
    public void testPurchasePosting() {
        Response response = RestAssured.given()
                .when()
                .post("/transactions/-1/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testBuyerTransactions(){
        Response response = RestAssured.given()
                .when()
                .get("/transactions/buyer/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testSellerTransactions(){
        Response response = RestAssured.given()
                .when()
                .get("/transactions/seller/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testBuyerTransactionsExisting(){
        Response response = RestAssured.given()
                .when()
                .get("/transactions/buyer/31");
        assertEquals(200, response.getStatusCode());
    }

    // ===== NOTIFICATIONS =====

    @Test
    public void testGetUserNotif(){
        Response response = RestAssured.given()
                .when()
                .get("/notifications/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testGetNotif(){
        Response response = RestAssured.given()
                .when()
                .get("/notifications/notif/-1");
        assertEquals(404, response.getStatusCode());
    }


    // ===== CHANNEL TESTS ====

    @Test
    public void testCreateChannel(){
        ChannelRequestDTO err = new ChannelRequestDTO();
        err.setName("not_user");
        err.setMemberIds(List.of(-1,-2));
        err.setType(Channel.ChannelType.DIRECT);

        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(err)
                .when()
                .post("/channel");
        int statusCode = response.getStatusCode();

        assertEquals(404, statusCode);
    }

    @Test
    public void testGetChannel(){
        Response response = RestAssured.given()
                .when()
                .get("/channel/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testGetUserChannel(){
        Response response = RestAssured.given()
                .when()
                .get("/channel/user/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testGetChannelInfo(){
        Response response = RestAssured.given()
                .when()
                .get("/channel/9");
        assertEquals(200, response.getStatusCode());
    }

    //===== FAVOURITE TESTS =====

    @Test
    public void testGetUserFav(){
        Response response = RestAssured.given()
                .when()
                .get("/favourites/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testGetAddFav(){
        Response response = RestAssured.given()
                .when()
                .post("/favourites/-1/Comics");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testGetRemoveFav(){
        Response response = RestAssured.given()
                .when()
                .delete("/favourites/-1/Comics");
        assertEquals(404, response.getStatusCode());
    }

    //==== COMMENT TESTS
    @Test
    public void testGetPostingComments(){
        Response response = RestAssured.given()
                .when()
                .get("/comments/posting/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testAddComment(){
        CommentRequest err = new CommentRequest();
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(err)
                .when()
                .post("/comments/posting/-1");
        assertEquals(404, response.getStatusCode());
    }

    // ===== CART TESTS =====

    @Test
    public void testAddToCart() {
        Response response = RestAssured.given()
                .when()
                .post("/cart/-1/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testGetCart(){
        Response response = RestAssured.given()
                .when()
                .get("/cart/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testRemoveFromCart(){
        Response response = RestAssured.given()
                .when()
                .delete("/cart/-1/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testClearCart(){
        Response response = RestAssured.given()
                .when()
                .delete("/cart/-1");
        assertEquals(404, response.getStatusCode());
    }

    // ====== AI TEST======
    @Test
    public void testGetAIHistory(){
        Response response = RestAssured.given()
                .when()
                .get("/ai/chat/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void sendMessageAI(){
        Map<String, String> body = new HashMap<String, String>();
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .post("/ai/chat/-1");
        assertEquals(404, response.getStatusCode());
    }
//
//    // ===== ADMIN TESTS =====
//
    @Test
    public void testBanUser() {
        Response response = RestAssured.given()
                .when()
                .patch("/ban/user/-1/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testBanPosting() {
        Response response = RestAssured.given()
                .when()
                .patch("/ban/posting/-1/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testFlagUser() {
        Response response = RestAssured.given()
                .when()
                .patch("/flag/user/-1/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testVerifyPosting() {
        Response response = RestAssured.given()
                .when()
                .patch("/verify/posting/-1/-1");
        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void testGetReportedPostings(){
        Response response = RestAssured.given()
                .when()
                .get("/admin/postings/reported");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testGetBannedUsers(){
        Response response = RestAssured.given()
                .when()
                .get("/admin/users/banned");
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testBanUserNotAdmin(){
        Response response = RestAssured.given()
                .when()
                .patch("/ban/user/31/31");
        assertEquals(403, response.getStatusCode());
    }

    @Test
    public void testBanAlreadyBannedUser(){
        Response response = RestAssured.given()
                .when()
                .patch("/ban/user/42/43");
        assertEquals(409, response.getStatusCode());
    }

    @Test
    public void testBanAlreadyBannedPosting(){
        Response response = RestAssured.given()
                .when()
                .patch("/ban/posting/42/25");
        assertEquals(409, response.getStatusCode());
    }

}