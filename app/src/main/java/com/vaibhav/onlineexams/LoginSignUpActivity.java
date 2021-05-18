package com.vaibhav.onlineexams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vaibhav.onlineexams.models.Student;
import com.vaibhav.onlineexams.models.Teacher;
import com.vaibhav.onlineexams.models.User;

import java.util.Calendar;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginSignUpActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 300;
    RelativeLayout loginUIParentRL, signUpUIParentRL, fullScreenProgressRL, aftParentRL, tsiParentRL;
    EditText loginEmailET, loginPasswordET, signUpNameET, signUPEmailET, signUpPasswordET, signUpCnfPasswordET, signUpInstituteNameET, tsiBranchET, tsiGraduationYearET,
            tsiRollNoET;
    Button login, signUp, aftContinueBtn, tsiContinueBtn;
    TextView dontHaveAcct, alreadyHaveAcct;
    FirebaseAuth firebaseAuth;
    Context context;
    FirebaseFirestore firestore;
    String userID, userName, userEmail, userInstituteName, studentBranch, studentGraduationYear, studentRollNo;
    int acctType = -1;

    SharedPreferences appPrefs;

    RadioGroup aftAcctTypeRG;

    private static final String APP_PREFS = "appPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
            }
        }

        appPrefs = getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);
        acctType = appPrefs.getInt(MyConstants.USER_TYPE, -1);
        userName = appPrefs.getString(MyConstants.USER_NAME, "");
        userEmail = appPrefs.getString(MyConstants.EMAIL, "");
        userInstituteName = appPrefs.getString(MyConstants.INSTITUTE_NAME, "");
        studentBranch = appPrefs.getString(MyConstants.STUDENT_BRANCH, "");
        studentGraduationYear = appPrefs.getString(MyConstants.STUDENT_GRADUATION_YEAR, "");
        studentRollNo = appPrefs.getString(MyConstants.STUDENT_ROLL_NO, "");

        context = this;
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        userID = firebaseAuth.getUid();

        mapUI();
        handleLogin();
        handleSignUp();

//        if (firebaseAuth.getCurrentUser() != null) {
//
//            if (acctType == MyConstants.USER_TYPE_TEACHER) {
//                startTeacherActivity();
//            } else if (acctType == MyConstants.USER_TYPE_STUDENT) {
//                if (studentRollNo.isEmpty()) {
//                    getBranchAndGYFromStudent();
//                } else {
//                    startStudentActivity();
//                }
//            } else {
//                getAcctTypeFromUser();
//            }
//        }
    }

    private void handleSignUp() {
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userEmail = signUPEmailET.getText().toString();
                String password = signUpPasswordET.getText().toString();
                userName = signUpNameET.getText().toString();
                userInstituteName = signUpInstituteNameET.getText().toString();
                String cnfPassword = signUpCnfPasswordET.getText().toString();
                if (userName.isEmpty()) {
                    showToast("Please enter your name");
                } else if (userEmail.isEmpty()) {
                    showToast("Please enter email");
                } else if (password.isEmpty()) {
                    showToast("Please enter you password");
                } else if (cnfPassword.isEmpty()) {
                    showToast("Please enter your password again in confirm password field");
                } else if (userInstituteName.isEmpty()) {
                    showToast("Please enter you institute name");
                } else if (!isEmailValid(userEmail)) {
                    showToast("Please enter a valid email address");
                } else if (!password.equals(cnfPassword)) {
                    showToast("Passwords do not match");
                } else {
                    fullScreenProgressRL.setVisibility(View.VISIBLE);
                    firebaseAuth.createUserWithEmailAndPassword(userEmail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            fullScreenProgressRL.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                showToast("User registered");
                                userID = firebaseAuth.getUid();
                                //uncomment and delete startMainActivity() for email verification
                                //sendVerificationEmail();
                                SharedPreferences.Editor editor = appPrefs.edit();
                                editor.putString(MyConstants.EMAIL, userEmail);
                                editor.putString(MyConstants.USER_NAME, userName);
                                editor.putString(MyConstants.INSTITUTE_NAME, userInstituteName);
                                editor.apply();
                                getAcctTypeFromUser();
                            } else {
                                showToast("Error: " + Objects.requireNonNull(task.getException()).getMessage());
                            }
                        }
                    });
                }
            }
        });

        alreadyHaveAcct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUIParentRL.setVisibility(View.GONE);
                loginUIParentRL.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getAcctTypeFromUser() {
        signUpUIParentRL.setVisibility(View.GONE);
        aftParentRL.setVisibility(View.VISIBLE);
        aftContinueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aftAcctTypeRG.getCheckedRadioButtonId() == R.id.rb_aft_teacher) {
                    acctType = 0;
                } else {
                    acctType = 1;
                }
                fullScreenProgressRL.setVisibility(View.VISIBLE);
                DocumentReference documentReference = firestore.collection("users").document(userID);
                documentReference.set(new User(userID, acctType)).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        fullScreenProgressRL.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            SharedPreferences.Editor editor = appPrefs.edit();
                            editor.putInt(MyConstants.USER_TYPE, acctType);
                            editor.apply();
                            if (acctType == 0) {
                                saveTeacherDataToDatabase();
                            } else {
                                getBranchAndGYFromStudent();
                            }
                        } else {
                            showToast(Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });
            }
        });
    }

    private void getBranchAndGYFromStudent() {
        loginUIParentRL.setVisibility(View.GONE);
        signUpUIParentRL.setVisibility(View.GONE);
        aftParentRL.setVisibility(View.GONE);
        tsiParentRL.setVisibility(View.VISIBLE);
        tsiContinueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                studentGraduationYear = tsiGraduationYearET.getText().toString();
                studentBranch = tsiBranchET.getText().toString();
                studentRollNo = tsiRollNoET.getText().toString();
                int year = Integer.parseInt(studentGraduationYear);
                int cYear = Calendar.getInstance().get(Calendar.YEAR);
                if (studentGraduationYear.isEmpty()) {
                    showToast("Please enter your graduation year");
                } else if (studentBranch.isEmpty()) {
                    showToast("Please enter your branch");
                } else if (year < cYear) {
                    showToast("Please enter valid graduation year");
                } else if (studentRollNo.isEmpty()) {
                    showToast("Please enter your roll number");
                } else {
                    SharedPreferences.Editor editor = appPrefs.edit();
                    editor.putString(MyConstants.STUDENT_GRADUATION_YEAR, studentGraduationYear);
                    editor.putString(MyConstants.STUDENT_BRANCH, studentBranch);
                    editor.putString(MyConstants.STUDENT_ROLL_NO, studentRollNo);
                    editor.apply();
                    saveStudentDataToDatabase();
                }
            }
        });
    }

    private void saveStudentDataToDatabase() {
        fullScreenProgressRL.setVisibility(View.VISIBLE);
        DocumentReference documentReference = firestore.collection(MyConstants.STUDENTS).document(userID);
        documentReference.set(new Student(userID, userName, userEmail, userInstituteName, Integer.parseInt(studentGraduationYear), studentBranch, studentRollNo))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        fullScreenProgressRL.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            startStudentActivity();
                        } else {
                            showToast(Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });
    }


    private void saveTeacherDataToDatabase() {
        fullScreenProgressRL.setVisibility(View.VISIBLE);
        DocumentReference documentReference = firestore.collection(MyConstants.TEACHERS).document(userID);
        documentReference.set(new Teacher(userID, userName, userEmail, userInstituteName)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                fullScreenProgressRL.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    startTeacherActivity();
                } else {
                    showToast(Objects.requireNonNull(task.getException()).getMessage());
                }
            }
        });
    }

    private void startTeacherActivity() {

        Intent intent = new Intent(LoginSignUpActivity.this, TeacherActivity.class);
        //uncomment below line and delete startActivity(intent) for email verification check;
        //checkEmailVerifiedAndOpenActivity(intent);
        startActivity(intent);
    }

    private void sendVerificationEmail() {
        Objects.requireNonNull(firebaseAuth.getCurrentUser()).sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    showToast("Verification Email sent");
                    if (acctType == 0) {
                        startTeacherActivity();
                    } else {
                        startStudentActivity();
                    }
                } else {
                    showToast(Objects.requireNonNull(task.getException()).getMessage());
                }
            }
        });
    }

    private void startStudentActivity() {
        Intent intent = new Intent(LoginSignUpActivity.this, StudentActivity.class);
        //checkEmailVerifiedAndOpenActivity(intent);
        startActivity(intent);
    }

    public boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    private void handleLogin() {
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmailET.getText().toString();
                String password = loginPasswordET.getText().toString();
                if (email.isEmpty()) {
                    showToast("Please enter email");
                } else if (password.isEmpty()) {
                    showToast("Please enter you password");
                } else if (!isEmailValid(email)) {
                    showToast("Please enter a valid email address");
                } else {
                    fullScreenProgressRL.setVisibility(View.VISIBLE);
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                DocumentReference documentReference = firestore.collection(MyConstants.USERS).document(Objects.requireNonNull(firebaseAuth.getUid()));
                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            User user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                                            acctType = Objects.requireNonNull(user).getUserType();
                                            if (acctType == MyConstants.USER_TYPE_TEACHER) {
                                                DocumentReference mref = firestore.collection(MyConstants.TEACHERS).document(firebaseAuth.getUid());
                                                mref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            showToast("Logged In");
                                                            Teacher s = Objects.requireNonNull(task.getResult()).toObject(Teacher.class);

                                                            SharedPreferences.Editor editor = appPrefs.edit();
                                                            editor.putString(MyConstants.USER_NAME, Objects.requireNonNull(s).getName());
                                                            editor.putString(MyConstants.INSTITUTE_NAME, s.getInstituteName());
                                                            editor.putInt(MyConstants.USER_TYPE, acctType);
                                                            editor.apply();
                                                            userName = s.getName();
                                                            userInstituteName = s.getInstituteName();
                                                            startTeacherActivity();
                                                        } else {
                                                            firebaseAuth.signOut();
                                                            showToast(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                                                        }
                                                    }
                                                });
                                            } else if (acctType == MyConstants.USER_TYPE_STUDENT) {
                                                DocumentReference mref = firestore.collection(MyConstants.STUDENTS).document(firebaseAuth.getUid());
                                                mref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            showToast("Logged In");
                                                            Student s = Objects.requireNonNull(task.getResult()).toObject(Student.class);

                                                            SharedPreferences.Editor editor = appPrefs.edit();
                                                            editor.putString(MyConstants.STUDENT_ROLL_NO, s.getRollNo());
                                                            editor.putString(MyConstants.STUDENT_GRADUATION_YEAR, String.valueOf(s.getGraduationYear()));
                                                            editor.putString(MyConstants.INSTITUTE_NAME, s.getInstituteName());
                                                            editor.putString(MyConstants.STUDENT_BRANCH, s.getBranch());
                                                            editor.putString(MyConstants.USER_NAME, s.getName());
                                                            editor.putInt(MyConstants.USER_TYPE, acctType);
                                                            editor.apply();
                                                            studentGraduationYear = String.valueOf(s.getGraduationYear());
                                                            studentBranch = s.getBranch();
                                                            studentRollNo = s.getRollNo();
                                                            userName = s.getName();

                                                            if (studentRollNo.isEmpty()) {
                                                                getBranchAndGYFromStudent();
                                                            } else {
                                                                startStudentActivity();
                                                            }
                                                        } else {
                                                            firebaseAuth.signOut();
                                                            showToast(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                                                        }
                                                    }
                                                });
                                            } else {
                                                getAcctTypeFromUser();
                                            }
                                        } else {
                                            firebaseAuth.signOut();
                                            showToast(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                                        }
                                        fullScreenProgressRL.setVisibility(View.GONE);
                                    }
                                });
                            } else {
                                fullScreenProgressRL.setVisibility(View.GONE);
                                showToast(Objects.requireNonNull(task.getException()).getMessage());
                            }
                        }
                    });
                }
            }
        });

        dontHaveAcct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUIParentRL.setVisibility(View.GONE);
                signUpUIParentRL.setVisibility(View.VISIBLE);
            }
        });
    }

    private void mapUI() {
        loginUIParentRL = findViewById(R.id.rl_login_ui_parent);
        signUpUIParentRL = findViewById(R.id.rl_signup_ui_parent);
        fullScreenProgressRL = findViewById(R.id.fullscreen_progress_layout);
        loginEmailET = findViewById(R.id.et_login_email);
        loginPasswordET = findViewById(R.id.et_login_password);
        signUpNameET = findViewById(R.id.et_signup_name);
        signUPEmailET = findViewById(R.id.et_signup_email);
        signUpPasswordET = findViewById(R.id.et_signup_password);
        signUpCnfPasswordET = findViewById(R.id.et_signup_cnf_password);
        signUpInstituteNameET = findViewById(R.id.et_signup_institute_name);
        login = findViewById(R.id.btn_login_login);
        signUp = findViewById(R.id.btn_signup_signup);
        dontHaveAcct = findViewById(R.id.tv_login_dont_have_an_acct);
        alreadyHaveAcct = findViewById(R.id.tv_signup_already_have_an_acct);
        aftParentRL = findViewById(R.id.rl_aft_parent);
        aftAcctTypeRG = findViewById(R.id.rg_aft_acctype);
        aftContinueBtn = findViewById(R.id.btn_aft_continue);
        tsiContinueBtn = findViewById(R.id.btn_tsi_continue);
        tsiBranchET = findViewById(R.id.et_tsi_branch);
        tsiGraduationYearET = findViewById(R.id.et_tsi_graduation_year);
        tsiParentRL = findViewById(R.id.rl_tsi_parent);
        tsiRollNoET = findViewById(R.id.et_tsi_roll_no);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) {
            if (acctType == MyConstants.USER_TYPE_TEACHER) {
                startTeacherActivity();
            } else if (acctType == MyConstants.USER_TYPE_STUDENT) {
                if (studentRollNo.isEmpty()) {
                    getBranchAndGYFromStudent();
                } else {
                    startStudentActivity();
                }
            } else {
                getAcctTypeFromUser();
            }
        }
    }

    public void checkEmailVerifiedAndOpenActivity(Intent intent) {
        if (Objects.requireNonNull(firebaseAuth.getCurrentUser()).isEmailVerified()) {
            startActivity(intent);
        } else {
            showToast("An verification mail is sent to your email, Please tap on the link in email to verify account");
        }
    }
}