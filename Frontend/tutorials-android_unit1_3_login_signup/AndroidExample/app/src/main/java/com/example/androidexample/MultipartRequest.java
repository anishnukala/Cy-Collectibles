package com.example.androidexample;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class MultipartRequest extends Request<NetworkResponse> {

    // success callback
    private final Response.Listener<NetworkResponse> listener;

    // error callback
    private final Response.ErrorListener errorListener;

    // unique multipart boundary
    private final String boundary = "----CyCollectiblesBoundary" + UUID.randomUUID();

    // content type for multipart request
    private final String mimeType = "multipart/form-data; boundary=" + boundary;

    // stores normal form fields
    private final Map<String, FormField> formFields = new LinkedHashMap<>();

    // stores file parts
    private final Map<String, FilePart> fileParts = new LinkedHashMap<>();

    public MultipartRequest(
            String url,
            Response.Listener<NetworkResponse> listener,
            Response.ErrorListener errorListener
    ) {
        super(Method.POST, url, errorListener); // POST request
        this.listener = listener;
        this.errorListener = errorListener;
    }

    public void addFormField(String name, String value, String contentType) {
        formFields.put(name, new FormField(value, contentType)); // add text/json field
    }

    public void addFilePart(String name, String filename, String contentType, byte[] data) {
        fileParts.put(name, new FilePart(filename, contentType, data)); // add file field
    }

    @Override
    public String getBodyContentType() {
        return mimeType; // tell server this is multipart/form-data
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return super.getHeaders();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // text/json parts
            for (Map.Entry<String, FormField> e : formFields.entrySet()) {
                writeBoundary(out);
                writeLine(out, "Content-Disposition: form-data; name=\"" + e.getKey() + "\"");
                String ct = (e.getValue().contentType != null)
                        ? e.getValue().contentType
                        : "text/plain; charset=utf-8";
                writeLine(out, "Content-Type: " + ct);
                writeLine(out, "");
                out.write(e.getValue().value.getBytes(StandardCharsets.UTF_8));
                writeLine(out, "");
            }

            // write file parts
            for (Map.Entry<String, FilePart> e : fileParts.entrySet()) {
                FilePart f = e.getValue();
                writeBoundary(out);
                writeLine(out, "Content-Disposition: form-data; name=\"" + e.getKey() + "\"; filename=\"" + f.filename + "\"");
                writeLine(out, "Content-Type: " + (f.contentType != null ? f.contentType : "application/octet-stream"));
                writeLine(out, "Content-Transfer-Encoding: binary");
                writeLine(out, "");
                out.write(f.data);
                writeLine(out, "");
            }

            // end boundary
            writeLine(out, "--" + boundary + "--");

            return out.toByteArray(); // final request body
        } catch (Exception e) {
            throw new AuthFailureError("Failed to build multipart body", e);
        }
    }

    private void writeBoundary(ByteArrayOutputStream out) {
        writeLine(out, "--" + boundary); // start part boundary
    }

    private void writeLine(ByteArrayOutputStream out, String line) {
        try {
            out.write((line + "\r\n").getBytes(StandardCharsets.UTF_8)); // write line with CRLF
        } catch (Exception ignored) {}
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, getCacheEntry()); // pass raw response back
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        listener.onResponse(response); // success callback
    }

    @Override
    public void deliverError(VolleyError error) {
        errorListener.onErrorResponse(error); // error callback
    }

    // reads JSON error response and gets message
    public static String tryExtractServerMessage(VolleyError error) {
        try {
            if (error.networkResponse == null || error.networkResponse.data == null) return null;

            String json = new String(error.networkResponse.data, StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(json);

            // check for "message" field
            if (obj.has("message")) return obj.optString("message", null);

            // otherwise return first field message
            java.util.Iterator<String> keys = obj.keys();
            if (keys.hasNext()) {
                String k = keys.next();
                String msg = obj.optString(k, null);
                if (msg != null && !msg.isEmpty()) return msg;
            }

            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    // holds one normal form field
    private static class FormField {
        final String value;
        final String contentType;

        FormField(String value, String contentType) {
            this.value = value;
            this.contentType = contentType;
        }
    }

    // holds one uploaded file
    private static class FilePart {
        final String filename;
        final String contentType;
        final byte[] data;

        FilePart(String filename, String contentType, byte[] data) {
            this.filename = filename;
            this.contentType = contentType;
            this.data = data;
        }
    }
}