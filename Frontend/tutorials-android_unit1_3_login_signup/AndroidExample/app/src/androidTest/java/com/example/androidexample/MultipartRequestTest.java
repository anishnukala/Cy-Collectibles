package com.example.androidexample;

import static org.junit.Assert.*;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class MultipartRequestTest {

    @Test
    public void bodyContainsFormFieldAndFilePart() throws Exception {
        MultipartRequest request = new MultipartRequest(
                "http://test.com/postings",
                response -> {},
                error -> {}
        );

        request.addFormField("data", "{\"title\":\"Test\"}", "application/json; charset=utf-8");
        request.addFilePart("image", "test.jpg", "image/jpeg", "abc".getBytes(StandardCharsets.UTF_8));

        String body = new String(request.getBody(), StandardCharsets.UTF_8);

        assertTrue(body.contains("Content-Disposition: form-data; name=\"data\""));
        assertTrue(body.contains("Content-Type: application/json; charset=utf-8"));
        assertTrue(body.contains("{\"title\":\"Test\"}"));

        assertTrue(body.contains("Content-Disposition: form-data; name=\"image\"; filename=\"test.jpg\""));
        assertTrue(body.contains("Content-Type: image/jpeg"));
        assertTrue(body.contains("Content-Transfer-Encoding: binary"));
        assertTrue(body.contains("abc"));
    }

    @Test
    public void defaultContentTypesAreUsedWhenNull() throws Exception {
        MultipartRequest request = new MultipartRequest(
                "http://test.com/postings",
                response -> {},
                error -> {}
        );

        request.addFormField("data", "hello", null);
        request.addFilePart("image", "file.bin", null, "data".getBytes(StandardCharsets.UTF_8));

        String body = new String(request.getBody(), StandardCharsets.UTF_8);

        assertTrue(body.contains("Content-Type: text/plain; charset=utf-8"));
        assertTrue(body.contains("Content-Type: application/octet-stream"));
    }

    @Test
    public void contentTypeIsMultipart() {
        MultipartRequest request = new MultipartRequest(
                "http://test.com/postings",
                response -> {},
                error -> {}
        );

        assertTrue(request.getBodyContentType().startsWith("multipart/form-data; boundary="));
    }

    @Test
    public void extractServerMessageFromMessageField() {
        VolleyError error = new VolleyError(new NetworkResponse(
                400,
                "{\"message\":\"Invalid input\"}".getBytes(StandardCharsets.UTF_8),
                new HashMap<>(),
                false,
                0
        ));

        assertEquals("Invalid input", MultipartRequest.tryExtractServerMessage(error));
    }

    @Test
    public void extractServerMessageFromFirstField() {
        VolleyError error = new VolleyError(new NetworkResponse(
                400,
                "{\"title\":\"Title required\"}".getBytes(StandardCharsets.UTF_8),
                new HashMap<>(),
                false,
                0
        ));

        assertEquals("Title required", MultipartRequest.tryExtractServerMessage(error));
    }

    @Test
    public void extractServerMessageReturnsNullForNoNetworkResponse() {
        assertNull(MultipartRequest.tryExtractServerMessage(new VolleyError()));
    }

    @Test
    public void extractServerMessageReturnsNullForBadJson() {
        VolleyError error = new VolleyError(new NetworkResponse(
                500,
                "not-json".getBytes(StandardCharsets.UTF_8),
                new HashMap<>(),
                false,
                0
        ));

        assertNull(MultipartRequest.tryExtractServerMessage(error));
    }

    @Test
    public void deliverErrorCallsErrorListener() {
        final boolean[] called = {false};

        MultipartRequest request = new MultipartRequest(
                "http://test.com/postings",
                response -> {},
                error -> called[0] = true
        );

        request.deliverError(new VolleyError());

        assertTrue(called[0]);
    }

    @Test
    public void deliverResponseCallsSuccessListener() {
        final boolean[] called = {false};

        MultipartRequest request = new MultipartRequest(
                "http://test.com/postings",
                response -> called[0] = true,
                error -> {}
        );

        request.deliverResponse(new NetworkResponse(
                200,
                "ok".getBytes(StandardCharsets.UTF_8),
                new HashMap<String, String>(),
                false,
                0
        ));

        assertTrue(called[0]);
    }

    @Test
    public void parseNetworkResponseReturnsSameResponse() {
        MultipartRequest request = new MultipartRequest(
                "http://test.com/postings",
                response -> {},
                error -> {}
        );

        NetworkResponse networkResponse = new NetworkResponse(
                200,
                "ok".getBytes(StandardCharsets.UTF_8),
                new HashMap<String, String>(),
                false,
                0
        );
        Response<NetworkResponse> parsed = request.parseNetworkResponse(networkResponse);

        assertTrue(parsed.isSuccess());
        assertSame(networkResponse, parsed.result);
    }
}