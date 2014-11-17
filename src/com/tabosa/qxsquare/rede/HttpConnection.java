package com.tabosa.qxsquare.rede;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpConnection {

	public static String getDataWeb(String url, String metodo, String dado) {
		HttpClient httpCliente = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		String resposta = "";

		try {

			ArrayList<NameValuePair> valores = new ArrayList<NameValuePair>();
			valores.add(new BasicNameValuePair("metodo", metodo));
			valores.add(new BasicNameValuePair("json", dado));

			httpPost.setEntity(new UrlEncodedFormEntity(valores));
			HttpResponse answer = httpCliente.execute(httpPost);
			resposta = EntityUtils.toString(answer.getEntity());

		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resposta;
	}

}
