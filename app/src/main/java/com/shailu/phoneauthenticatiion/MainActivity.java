package com.shailu.phoneauthenticatiion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
{
    //**************
    int RC_SIGN_IN=264;
    String TAG="GOOGLE SIGN IN";
    GoogleSignInClient mGoogleSignInClient;
    SignInButton googlesigninbtn;

    private EditText input_mo_no,input_otp;
    private Button send_otp, verify_otp;
    FirebaseAuth mAuth;
    String codesent;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.just);
        //*********GOOGLE
        googlesigninbtn=(SignInButton)findViewById(R.id.signinbtn);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient=GoogleSignIn.getClient(this,gso);

//        googlesigninbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                signIn();
//            }
//        });

        input_mo_no=(EditText) findViewById(R.id.User_phoneNo);
        input_otp=(EditText)findViewById(R.id.User_Verification_No);
        verify_otp=(Button)findViewById(R.id.verify_otp_btn);
        send_otp=(Button)findViewById(R.id.send_otp_btn);
        mAuth=FirebaseAuth.getInstance();

       send_otp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendvarificationcode();
            }
        });

        verify_otp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                verifiSingincode();
            }
        });
    }

    private void verifiSingincode()
    {
        String code=input_otp.getText().toString();
        if(code.isEmpty())
        {
            input_otp.setError("Enter Otp");
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codesent, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, "LOGIN SUCCESSFULL...", Toast.LENGTH_LONG).show();
                        }
                        else if(task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                        {
                            Toast.makeText(MainActivity.this, "ERROR...", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void sendvarificationcode()
    {
        String phone=input_mo_no.getText().toString();
        if (phone.isEmpty())
        {
            input_mo_no.setError("Enter Mobile No.");
            input_mo_no.requestFocus();
            return;
        }
        if(phone.length()<10)
        {
            input_mo_no.setError("Valid mo no");
            input_mo_no.requestFocus();
            return;
        }

        String MONO="+91"+phone;
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                MONO,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
    {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
        {
            Toast.makeText(MainActivity.this, "VERIFICATION COMPLETE", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onVerificationFailed(FirebaseException e)
        {
            String message=e.getMessage();
            Toast.makeText(MainActivity.this, "VARIFICATION FAILED :"+message, Toast.LENGTH_SHORT).show();
            input_mo_no.setVisibility(View.VISIBLE);
            send_otp.setVisibility(View.VISIBLE);
            input_otp.setVisibility(View.INVISIBLE);
            verify_otp.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken)
        {
            super.onCodeSent(s, forceResendingToken);
            Toast.makeText(MainActivity.this, "Code sent "+s, Toast.LENGTH_SHORT).show();
            codesent=s;
            input_mo_no.setVisibility(View.INVISIBLE);
            send_otp.setVisibility(View.INVISIBLE);
            input_otp.setVisibility(View.VISIBLE);
            verify_otp.setVisibility(View.VISIBLE);
        }
    };

    //**********************8
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(MainActivity.this, "Login Success Email", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }
}