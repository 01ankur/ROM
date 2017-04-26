package trainedge.rom.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.DeviceLoginButton;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import trainedge.rom.Login;
import trainedge.rom.R;
import trainedge.rom.RegisterActivity;
import trainedge.rom.UserListingActivity;
import trainedge.rom.core.login.LoginContract;
import trainedge.rom.core.login.LoginPresenter;

import static trainedge.rom.HomeActivity.TAG;

public class LoginFragment extends Fragment implements View.OnClickListener, LoginContract.View {
    private LoginPresenter mLoginPresenter;

    private EditText mETxtEmail, mETxtPassword;
    private Button mBtnLogin, mBtnRegister;

    private ProgressDialog mProgressDialog;
    private SignInButton mBtnGoogle;
    private LoginButton mBtnFacebook;
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;

    public static LoginFragment newInstance() {
        Bundle args = new Bundle();
        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_login, container, false);

        bindViews(fragmentView);
        return fragmentView;
    }

    private void bindViews(View view) {
        mETxtEmail = (EditText) view.findViewById(R.id.edit_text_email_id);
        mETxtPassword = (EditText) view.findViewById(R.id.edit_text_password);
        mBtnLogin = (Button) view.findViewById(R.id.button_login);
        mBtnGoogle = (SignInButton) view.findViewById(R.id.btnGoogle);
        mBtnFacebook = (LoginButton) view.findViewById(R.id.btnFbLogin);
        mBtnRegister = (Button) view.findViewById(R.id.button_register);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        mLoginPresenter = new LoginPresenter(this);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(getString(R.string.loading));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setIndeterminate(true);

        mBtnLogin.setOnClickListener(this);
        mBtnRegister.setOnClickListener(this);
        mBtnGoogle.setOnClickListener(this);

        setDummyCredentials();
        mCallbackManager = CallbackManager.Factory.create();
        mBtnFacebook.setReadPermissions("email", "public_profile");
        mBtnFacebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(getActivity(), "Cancelled Facebook login", Toast.LENGTH_SHORT).show();
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getActivity(), "error :" + error.getMessage(), Toast.LENGTH_SHORT).show();
                // ...
            }
        });
    }

    private void setDummyCredentials() {

    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId) {
            case R.id.button_login:
                onLogin(view);
                break;
            case R.id.button_register:
                onRegister(view);
                break;
        }
    }

    private void onLogin(View view) {
        String emailId = mETxtEmail.getText().toString();
        String password = mETxtPassword.getText().toString();

        mLoginPresenter.login(getActivity(), emailId, password);
        mProgressDialog.show();
    }

    private void onRegister(View view) {
        RegisterActivity.startActivity(getActivity());
    }

    @Override
    public void onLoginSuccess(String message) {
        mProgressDialog.dismiss();
        Toast.makeText(getActivity(), "Logged in successfully", Toast.LENGTH_SHORT).show();
        UserListingActivity.startActivity(getActivity(),
                Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Override
    public void onLoginFailure(String message) {
        mProgressDialog.dismiss();
        Toast.makeText(getActivity(), "Error: " + message, Toast.LENGTH_SHORT).show();
    }

    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Authentication failed " + task.getException(), Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
}
