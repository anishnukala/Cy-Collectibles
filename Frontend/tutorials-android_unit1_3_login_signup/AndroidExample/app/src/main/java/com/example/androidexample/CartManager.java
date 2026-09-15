package com.example.androidexample;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

public class CartManager {

    private static final String BASE_URL = BuildConfig.BASE_URL + "/cart";

    public static void addToCart(Context context, long userId, int postingId,
                                 Response.Listener<String> listener,
                                 Response.ErrorListener errorListener) {

        String url = BASE_URL + "/" + userId + "/" + postingId;

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                listener,
                errorListener
        );

        Volley.newRequestQueue(context).add(request);
    }

    public static void getCart(Context context, long userId,
                               Response.Listener<JSONArray> listener,
                               Response.ErrorListener errorListener) {

        String url = BASE_URL + "/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                listener,
                errorListener
        );

        Volley.newRequestQueue(context).add(request);
    }


    public static void clearCart(Context context, long userId,
                                 Response.Listener<String> listener,
                                 Response.ErrorListener errorListener) {

        String url = BASE_URL + "/" + userId;

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                listener,
                errorListener
        );

        Volley.newRequestQueue(context).add(request);
    }

    public static void removeFromCart(Context context, long userId, int postingId,
                                      Response.Listener<String> listener,
                                      Response.ErrorListener errorListener) {

        String url = BASE_URL + "/" + userId + "/" + postingId;

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                listener,
                errorListener
        );

        Volley.newRequestQueue(context).add(request);
    }
}