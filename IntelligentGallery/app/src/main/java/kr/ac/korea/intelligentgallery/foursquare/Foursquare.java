package kr.ac.korea.intelligentgallery.foursquare;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;


public class Foursquare {
    // the foursquare client_id and the client_secret
    static final String CLIENT_ID = "UOLCVQTJCQZ12XNPCKLQZNFGNX3WWFUFPW3VQ3F4C3OTL1QC";
    static final String CLIENT_SECRET = "TQGC142SN3HT0VPYGUX4OSGJSVATOA1P2IMYF05TREAQ44X4";


    public static String getQueryFromLocation(String latitude, String longtitude) {
        ArrayList<FoursquareVenue> venuesList;
        String foursquareResponse = "";
        String queryString = "";

        if (latitude==null || longtitude==null) {
            // case without location
            return "";
        } else {
            // call API and get response
            foursquareResponse = callFoursquareAPI(latitude, longtitude);

            // parseFoursquare venues search result
            venuesList = (ArrayList) parseFoursquare(foursquareResponse);
            for (int i = 0; i < venuesList.size(); i++) {
                // show the name, the category and the city
                queryString += venuesList.get(i).getCategory() + " ";
            }

            return queryString;
        }
    }


    private static String callFoursquareAPI(String latitude, String longtitude) {
        String url = "https://api.foursquare.com/v2/venues/search?client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET
                + "&v=20130815&ll=" + latitude + "," + longtitude;

        // string buffers the url
        StringBuffer buffer_string = new StringBuffer(url);
        String replyString = "";

        // instanciate an HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        // instanciate an HttpGet
        HttpGet httpget = new HttpGet(buffer_string.toString());

        try {
            // get the responce of the httpclient execution of the url
            HttpResponse response = httpclient.execute(httpget);
            InputStream is = response.getEntity().getContent();

            // buffer input stream the result
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(20);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            // the result as a string is ready for parsing
            replyString = new String(baf.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // trim the whitespaces
        return replyString.trim();
    }


    private static ArrayList parseFoursquare(final String response) {
        ArrayList results = new ArrayList();

        try {
            // make an jsonObject in order to parse the response
            JSONObject jsonObject = new JSONObject(response);

            // make an jsonObject in order to parse the response
            if (jsonObject.has("response")) {

                if (jsonObject.getJSONObject("response").has("venues")) {
                    JSONArray jsonArray = jsonObject.getJSONObject("response").getJSONArray("venues");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        FoursquareVenue poi = new FoursquareVenue();
                        if (jsonArray.getJSONObject(i).getJSONArray("categories").length() > 0 && jsonArray.getJSONObject(i).getJSONArray("categories").getJSONObject(0).has("icon")) {
                            poi.setCategory(jsonArray.getJSONObject(i).getJSONArray("categories").getJSONObject(0).getString("name"));
                        }
                        if (jsonArray.getJSONObject(i).has("name")) {
                            poi.setName(jsonArray.getJSONObject(i).getString("name"));
                        }
                        if (jsonArray.getJSONObject(i).has("location")) {
                            if (jsonArray.getJSONObject(i).getJSONObject("location").has("city")) {
                                poi.setCity(jsonArray.getJSONObject(i).getJSONObject("location").getString("city"));
                            }
                        }
                        results.add(poi);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList();
        }
        return results;
    }


}
