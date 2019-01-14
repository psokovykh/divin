package io.github.psokovykh.divin.demo;

import io.github.psokovykh.divin.util.FlexibleJFXThread;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class VkDummy {
	public static final String REDIRECT_URL = "https://oauth.vk.com/blank.html";
	public static final String VK_AUTH_URL = "https://oauth.vk.com/authorize?v=5.9&" +
			"redirect_uri="+REDIRECT_URL+"&display=page&response_type=token&scope=messages";

	public static String getTokenUrl(FlexibleJFXThread jfxThread, String appcode){
		final String[] token = new String[1];
		final Object obj = new Object();
		synchronized (obj) {
			jfxThread.performJfxForeground(() -> {
				final WebView view = new WebView();
				final WebEngine engine = view.getEngine();
				engine.load(VK_AUTH_URL+"&client_id="+appcode);

				Stage stage = new Stage();
				stage.setMinHeight(600);
				stage.setMinWidth(800);
				stage.setScene(new Scene(view));
				stage.show();

				engine.locationProperty().addListener(new ChangeListener<String>() {
					@Override
					public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
						if (newValue.startsWith(REDIRECT_URL)) {
							token[0] = newValue;
							stage.close();
							synchronized (obj){
								obj.notify();
							}
						}
					}

				});
			});
			try {
				obj.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return token[0].split("access_token=")[1].split("&")[0];
	}

	public static String invokeMethod(String methodName, String params, String token) throws IOException{
		var urlstr = "https://api.vk.com/method/"+methodName+"?"+params+
				"&access_token="+token+"&v=5.9";
		final StringBuilder result = new StringBuilder();
		URL url = new URL(urlstr);

		try (InputStream is = url.openStream()) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			reader.lines().forEach(result::append);
		}

		return result.toString();
	}
}
