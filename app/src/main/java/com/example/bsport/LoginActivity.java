package com.example.bsport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.mtp.MtpConstants;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bsport.Model.Users;
import com.example.bsport.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.regex.Pattern;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;
    public static final Pattern  EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");
    public static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9]+$");
    private Button LoginButton;
    public static EditText UserName;
    public static EditText UserPassword;
    private TextView NeedNewAccountLink ,ForgetPasswordLink;
    private DatabaseReference UserRef;
    private TextView AdminLink, NotAdminLink;
    private CheckBox chkBoxRememberMe;
    private String parentDbName = "Users";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__login);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        UserRef = FirebaseDatabase.getInstance().getReference();

        initilizedFields();
        Paper.init(this);
        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLRegisterActivity();
            }
        });
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });
        AdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginButton.setText("התחבר כמנהל");
                AdminLink.setVisibility(View.INVISIBLE);
                NotAdminLink.setVisibility(View.VISIBLE);
                NeedNewAccountLink.setVisibility(View.INVISIBLE);
                parentDbName = "Admin";
            }
        });
        NotAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginButton.setText("התחבר");
                NotAdminLink.setVisibility(View.INVISIBLE);
                AdminLink.setVisibility(View.VISIBLE);
                parentDbName = "Users";
            }
        });

    }


    private void AllowUserToLogin() {

        final String userName = UserName.getText().toString();
        final String password = UserPassword.getText().toString();
        if(chkBoxRememberMe.isChecked()){
            Paper.book().write(Prevalent.UserNameKey, userName);
            Paper.book().write(Prevalent.UserPasswordKey, password);

        }
        if(TextUtils.isEmpty(userName)){
            Toast.makeText(this,"אנא הכנס שם משתמש",Toast.LENGTH_SHORT).show();
        }
        else if(!USERNAME_PATTERN.matcher(userName).matches()){
            Toast.makeText(this,"אנא הכנס שם משמש חוקי - אותיות ומספרים בלבד",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"אנא הכנס סיסמה",Toast.LENGTH_SHORT).show();
        }
        else if(password.length() < 6){
            Toast.makeText(this,"הסיסמה צריכה להיות באורך 6 ומעלה",Toast.LENGTH_SHORT).show();
        }
        else
        {

            loadingbar.setTitle("Sign In");
            loadingbar.setMessage("Please Wait..");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();

            UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(parentDbName).child(userName).exists()){
                        Users usersData = dataSnapshot.child(parentDbName).child(userName).getValue(Users.class);
                        if(usersData.getUsername().equals(userName)){

                            if(usersData.getPassword().equals(password)){
                                if(parentDbName.equals("Admin")){
                                    Paper.book().write(Prevalent.UserAdminKey, "true");

                                    Toast.makeText(LoginActivity.this,"ברוך הבא מנהל",Toast.LENGTH_SHORT).show();
                                    loadingbar.dismiss();
                                    SendUserToLMainActivityAdmin();
                                }
                                else if(parentDbName.equals("Users")){
                                    Toast.makeText(LoginActivity.this,"התחברת בהצלחה",Toast.LENGTH_SHORT).show();
                                    loadingbar.dismiss();
                                    SendUserToLMainActivity();
                                }
                                else{
                                    Toast.makeText(LoginActivity.this,"התחברות לא הצליחה, נסה שנית",Toast.LENGTH_SHORT).show();
                                    loadingbar.dismiss();
                                }
                            }
                            else{
                                Toast.makeText(LoginActivity.this, "Password is incorrect", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "UserName is incorrect", Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();
                        }
                    }
                    else{
                        Toast.makeText(LoginActivity.this, "Account with this " + userName + " do not exists.", Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    private void initilizedFields() {
        LoginButton = (Button) findViewById(R.id.login_button);
        UserName = ( EditText) findViewById(R.id.login_userName);
        UserPassword = ( EditText) findViewById(R.id.login_password);
        ForgetPasswordLink = ( TextView) findViewById(R.id.forget_password_link);
        NeedNewAccountLink = ( TextView) findViewById(R.id.Sign_Up_link);
        loadingbar = new ProgressDialog(this);
        AdminLink = (TextView) findViewById(R.id.Im_Admin);
        NotAdminLink = (TextView) findViewById(R.id.Im_not_Admin);
        chkBoxRememberMe = (CheckBox) findViewById(R.id.checkRemember);

    }


    private void SendUserToLMainActivity() {

        Intent MainIntent = new Intent(  LoginActivity.this,HomeActivity.class );
        startActivity(MainIntent);
        finish();
    }
    private void SendUserToLMainActivityAdmin() {
        Intent MainIntent = new Intent(  LoginActivity.this,HomeActivity.class );
        startActivity(MainIntent);
        finish();
    }
    private void SendUserToLRegisterActivity() {
        Intent RegisterIntent = new Intent(  LoginActivity.this,RegisterActivity.class );
        startActivity(RegisterIntent);
    }

}