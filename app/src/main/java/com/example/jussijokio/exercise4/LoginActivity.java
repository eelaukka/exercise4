package com.example.jussijokio.exercise4;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements AsyncResponse {

    private ProgressBar spinner;
    public TextView username;
    public TextView password;
    public Button loginBtn;
    public Button registerBtn;
    public JSONObject payload;
    public JSONObject response;
    private TextView mInfoText;
    static final String baseApiUrl = "https://hangouts-mobisocial-18.herokuapp.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = (TextView) findViewById(R.id.UsernameField);
        password = (TextView) findViewById(R.id.PasswordField);
        username.setText("testi");
        password.setText("testi");
        loginBtn = (Button) findViewById(R.id.LoginButton);
        registerBtn = (Button) findViewById(R.id.RegisterButton);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
        mInfoText = findViewById(R.id.tv_Login_info);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(username.getText().length()>= 1) {
                    if (password.getText().length() >= 1) {
                        spinner.setVisibility(View.VISIBLE);
                        CheckValidRegister();
                    }
                }
            }
        });



        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(username.getText().length()>= 1){
                    if(password.getText().length()>= 1){
                        spinner.setVisibility(View.VISIBLE);
                        CheckValidLogin();
                            //GoToMain();
                    }
                    else{
                        Toast.makeText(getBaseContext(),"Give password please!",
                                Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getBaseContext(),"Give username please!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        CallAPI asyncTask = new CallAPI();
        asyncTask.delegate = this;
        asyncTask.execute("users/createuser");


    }

    private boolean CheckValidRegister() {
        CallAPI apihelper = new CallAPI();
        if(username.getText().length()>= 1) {
            if (password.getText().length() >= 1) {
                Log.e("ApiHelper", "check registering...");
                Log.e("ApiHelper", "Spinner on");
                //call API
                JSONObject jsonParam = new JSONObject();
                try {
                    jsonParam.put("username",username.getText());
                    jsonParam.put("password",password.getText());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("ApiHelper", e.toString());
                }

                apihelper.setPayload(jsonParam, "POST");

                apihelper.delegate = this;
                apihelper.execute("users/createuser");
                Log.e("ApiHelper", "spinner off");
            }
        }
        return false;
    }


    public boolean CheckValidLogin() {
        CallAPI apihelper = new CallAPI();
        //call API
        Log.e("ApiHelper ", "login call started");
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("username",username.getText());
            jsonParam.put("password",password.getText());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ApiHelper", e.toString());
        }

        apihelper.setPayload(jsonParam, "GET");

        apihelper.delegate = this;
        apihelper.execute("users/login?username="+username.getText()+"&password="+password.getText());
        if (apihelper.delegate != null){
            return true;
        }
        spinner.setVisibility(View.GONE);
        return false;
    }

    private void GoToMain(){
        Intent gotoMain = new Intent(this, MainActivity.class);
        startActivity(gotoMain);
    }

    @Override
    public void processFinish(String output) {
        JSONObject obj = null;
        spinner.setVisibility(View.GONE);
        //response ID:t ja esimerkki responset
        // 1 - Create user - {"responseid":1,"status":"success","id":41,"username":"testi","msg":"Successfully created new account. Welcome testi"}
        // 2 - Login - {"responseid":2,"status":"success","id":34,"username":"testi","msg":"Successfully logged in."}
        // 3 - Location update - {"responseid":3,"status":"success","nearbyUsers":["Testikakkone"],"msg":"Location updated."}
        try {
            obj = new JSONObject(output);
            Log.e("ApiHelper responsejson",obj.toString());
            Log.e("ApiHelper responseid", String.valueOf(obj.getInt("responseid")));
            Toast.makeText(this, obj.getString("msg"), Toast.LENGTH_SHORT).show();
            //mInfoText.setText(obj.getString("msg"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {

            if(obj != null) {
                if(obj.getString("status").toLowerCase().equals("failed")){
                    Toast.makeText(this, obj.getString("msg"), Toast.LENGTH_SHORT).show();
                }
            }

            switch(obj != null ? obj.getInt("responseid") : 0){
                case 1:
                    Log.e("ApiHelperhandleresponse","user creation response");
                    break;
                case 2:
                    //Do this and this
                    if (Objects.equals(obj != null ? obj.getString("status") : null, "success")){
                        SharedPreferences sharedPref = this.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("id", obj.getInt("id"));
                        editor.commit();
                        GoToMain();
                    }
                    break;
                case 3:
                    //Do this and this:
                    break;
                default: //For all other cases, do this
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "An error occured!"+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
