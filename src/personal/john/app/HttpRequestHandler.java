
package personal.john.app;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class HttpRequestHandler {
    private HttpClient mHttpClient;

    private HttpParams mParams;

    private HttpResponse mHttpResponse;

    public HttpRequestHandler() {
        mHttpClient = new DefaultHttpClient();
        mParams = mHttpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(mParams, 1000);
        HttpConnectionParams.setSoTimeout(mParams, 1000);
        mHttpResponse = null;
    }

    public boolean checkHttpGET(String uri) {
        try {
            mHttpResponse = mHttpClient.execute(new HttpGet(uri));
        } catch (ClientProtocolException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }

        if (mHttpResponse != null
                && mHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return true;
        }

        return false;
    }

    public String getJSONData(String uri) {
        String sJSONData = "";
        if (checkHttpGET(uri)) {
            HttpEntity httpEntity = mHttpResponse.getEntity();
            try {
                sJSONData = EntityUtils.toString(httpEntity);
            } catch (ParseException e) {
                // TODO 自動生成された catch ブロック
                e.printStackTrace();
            } catch (IOException e) {
                // TODO 自動生成された catch ブロック
                e.printStackTrace();
            } finally {
                try {
                    httpEntity.consumeContent();
                } catch (IOException e) {
                    // 例外処理
                }
            }
        }

        return sJSONData;
    }

    public void httpClientClose() {
        mHttpClient.getConnectionManager().shutdown();
    }
}
