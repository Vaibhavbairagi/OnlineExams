package com.vaibhav.onlineexams.fragments;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vaibhav.onlineexams.MyConstants;
import com.vaibhav.onlineexams.R;
import com.vaibhav.onlineexams.TeacherActivity;
import com.vaibhav.onlineexams.interfaces.OnExamCreatedListener;
import com.vaibhav.onlineexams.models.Exam;

import java.util.Calendar;
import java.util.Objects;

public class CreateExamFragment extends Fragment {
    String classroomId;
    OnExamCreatedListener onExamCreatedListener;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    EditText titleET, startDateET, dueDateET, startTimeET, dueTimeET, chooseQpET;
    String startDate, dueDate, title, examId, qpLink;
    Button createNewExamBtn;
    int mYear, mMonth, mDay, mHour, mMinute;
    RelativeLayout fullscreenProgress;

    Uri imageuri;
    ProgressDialog dialog;

    boolean isQpUploaded = false;
    private int REQUEST_UPLOAD = 100;

    public CreateExamFragment() {
        // Required empty public constructor
    }

    public CreateExamFragment(String classroomId, OnExamCreatedListener onExamCreatedListener) {
        this.classroomId = classroomId;
        this.onExamCreatedListener = onExamCreatedListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_exam, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Objects.requireNonNull(((TeacherActivity) requireActivity()).getSupportActionBar()).setTitle("Create Exam");
        titleET = view.findViewById(R.id.et_fcd_title);
        startDateET = view.findViewById(R.id.et_fcd_start_date);
        startTimeET = view.findViewById(R.id.et_fcd_start_time);
        dueTimeET = view.findViewById(R.id.et_fcd_due_time);
        dueDateET = view.findViewById(R.id.et_fcd_due_date);
        createNewExamBtn = view.findViewById(R.id.btn_dce_create_new_exam);
        chooseQpET = view.findViewById(R.id.et_fcd_question_paper);
        fullscreenProgress = view.findViewById(R.id.fullscreen_progress_layout);

        createAndAttachDateAndTimePickers();
        attachAndChooseQuestionPaper();

        createNewExamBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = titleET.getText().toString();
                String sdate = startDateET.getText().toString();
                String ddate = dueDateET.getText().toString();
                String stime = startTimeET.getText().toString();
                String dtime = dueTimeET.getText().toString();
                if (title.isEmpty()) {
                    showToast("Please enter exam title");
                } else if (sdate.isEmpty()) {
                    showToast("Please select start date");
                } else if (stime.isEmpty()) {
                    showToast("Please select start time");
                } else if (ddate.isEmpty()) {
                    showToast("Please select due date");
                } else if (dtime.isEmpty()) {
                    showToast("Please select due time");
                } else if (!isQpUploaded) {
                    showToast("Please select and upload question paper");
                } else {
                    createNewExam();
                }
            }
        });
    }

    private boolean checkValidityOfAboveEntries() {
        title = titleET.getText().toString();
        String sdate = startDateET.getText().toString();
        String ddate = dueDateET.getText().toString();
        String stime = startTimeET.getText().toString();
        String dtime = dueTimeET.getText().toString();
        if (title.isEmpty()) {
            showToast("Please enter exam title");
            return false;
        } else if (sdate.isEmpty()) {
            showToast("Please select start date");
            return false;
        } else if (stime.isEmpty()) {
            showToast("Please select start time");
            return false;
        } else if (ddate.isEmpty()) {
            showToast("Please select due date");
            return false;
        } else if (dtime.isEmpty()) {
            showToast("Please select due time");
            return false;
        } else {
            startDate = sdate + " " + stime;
            dueDate = ddate + " " + dtime;
            return true;
        }
    }

    private void attachAndChooseQuestionPaper() {
        chooseQpET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidityOfAboveEntries()) {
                    examId = System.currentTimeMillis() + title + classroomId;

                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                    // We will be redirected to choose pdf
                    galleryIntent.setType("application/pdf");
                    startActivityForResult(galleryIntent, REQUEST_UPLOAD);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("fetched", "inresult");
        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_UPLOAD) {

            // Here we are initialising the progress dialog box
            dialog = new ProgressDialog(getContext());
            dialog.setMessage("Uploading");

            // this will show message uploading
            // while pdf is uploading
            dialog.show();
            imageuri = data.getData();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            Log.d("fetched", "select: " + imageuri);
            qpLink = examId + "/questionPaper/" + title + "." + "pdf";
            // Here we are uploading the pdf in firebase storage with the name of current time
            StorageReference filepath = storageReference.child(qpLink);
            Log.d("fetched", filepath.getName());
            filepath.putFile(imageuri)
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            dialog.setProgress((int) progress);
                            Log.d("fetched", "Upload is " + progress + "% done");
                        }
                    }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Log.d("fetched", "Upload is paused");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    dialog.dismiss();
                    showToast("Upload failed");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        showToast("Upload Successfull");
                        dialog.dismiss();
                        isQpUploaded = true;
                        StorageReference ref = FirebaseStorage.getInstance().getReference(qpLink);
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                qpLink = uri.toString();
                            }
                        });
                        chooseQpET.setText(imageuri.getLastPathSegment());
                    }
                }
            });
        }
    }

    private void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void createNewExam() {
        fullscreenProgress.setVisibility(View.VISIBLE);
        Exam e = new Exam(examId, classroomId, title, startDate, dueDate, qpLink);
        firestore.collection(MyConstants.EXAMS).document(examId)
                .set(e).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                fullscreenProgress.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    onExamCreatedListener.onExamCreated(e);
                    requireActivity().onBackPressed();
                } else {
                    showToast("Please try again");
                }
            }
        });
    }

    private void createAndAttachDateAndTimePickers() {
        Calendar c = Calendar.getInstance();
        startDateET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog startDatePicker = new DatePickerDialog(requireContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                mYear = year;
                                mMonth = monthOfYear;
                                mDay = dayOfMonth;
                                monthOfYear = monthOfYear + 1;
                                String date = ((dayOfMonth < 10) ? "0" + dayOfMonth : dayOfMonth) + "/" + ((monthOfYear < 10) ? "0" + monthOfYear : monthOfYear) + "/" + year;
                                startDateET.setText(date);
                            }
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                startDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                startDatePicker.show();
            }
        });

        dueDateET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog endDatePicker = new DatePickerDialog(requireContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                monthOfYear = monthOfYear + 1;
                                String date = ((dayOfMonth < 10) ? "0" + dayOfMonth : dayOfMonth) + "/" + ((monthOfYear < 10) ? "0" + monthOfYear : monthOfYear) + "/" + year;
                                dueDateET.setText(date);
                            }
                        }, mYear, mMonth, mDay);
                endDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                endDatePicker.show();
            }
        });

        Calendar c2 = Calendar.getInstance();

        startTimeET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimePickerDialog startTimePicker = new TimePickerDialog(getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                String time = ((hourOfDay < 10) ? "0" + hourOfDay : hourOfDay) + ":" + ((minute < 10) ? "0" + minute : minute);
                                startTimeET.setText(time);
                                mHour = hourOfDay;
                                mMinute = minute;
                            }
                        }, c2.get(Calendar.HOUR_OF_DAY), c2.get(Calendar.MINUTE), true);
                startTimePicker.show();
            }
        });

        dueTimeET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog endTimePicker = new TimePickerDialog(getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                String time = ((hourOfDay < 10) ? "0" + hourOfDay : hourOfDay) + ":" + ((minute < 10) ? "0" + minute : minute);
                                dueTimeET.setText(time);
                            }
                        }, mHour, mMinute, true);
                endTimePicker.show();
            }
        });
    }
}