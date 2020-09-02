package ru.loginov.chemistryapplication;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class JavaTest {

    @Test
    public void test() throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL("http://localhost:8080").openConnection();
        con.setConnectTimeout(1000);
        con.setDoInput(true);
        byte[] bytes = IOUtils.toByteArray(con.getInputStream());
        System.out.println(bytes.length);
    }
}
