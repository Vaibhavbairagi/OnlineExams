package com.vaibhav.onlineexams.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vaibhav.onlineexams.MyConstants;
import com.vaibhav.onlineexams.R;
import com.vaibhav.onlineexams.adapters.SubmissionListRVAdapter;
import com.vaibhav.onlineexams.interfaces.OnSubmissionClickedListener;
import com.vaibhav.onlineexams.models.Exam;
import com.vaibhav.onlineexams.models.Submission;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class ExamFragment extends Fragment implements OnSubmissionClickedListener {
    private static final int REQUEST_UPLOAD = 200;
    Exam exam;

    TextView titleTV, startTimeTV, endTimeTV, qpDownloadTV, chooseAnswerScriptTV, subMissionStatusTV;
    RelativeLayout studentSubmissionRL, qpDownloadRL, chooseAnswerSheetRL, fullScreenProgressRL, submissionViewRL;
    Button submitBtn;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    SharedPreferences appPrefs;

    RecyclerView submissionListRV;
    SubmissionListRVAdapter submissionListRVAdapter;

    int type = -1;
    Submission submission;
    Uri imageuri;

    ProgressDialog dialog;

    String rollNo, submissionLink;

    ArrayList<Submission> submissions = new ArrayList<>();


    public ExamFragment() {
        // Required empty public constructor
    }

    public ExamFragment(Exam exam) {
        this.exam = exam;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exam, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        titleTV = view.findViewById(R.id.tv_fe_exam_title);
        startTimeTV = view.findViewById(R.id.tv_fe_exam_start_time);
        endTimeTV = view.findViewById(R.id.tv_fe_exam_end_time);
        qpDownloadTV = view.findViewById(R.id.tv_question_paper_pdf);
        chooseAnswerScriptTV = view.findViewById(R.id.tv_choose_answer_script);
        studentSubmissionRL = view.findViewById(R.id.rl_fe_student_submission);
        qpDownloadRL = view.findViewById(R.id.rl_question_paper_pdf);
        chooseAnswerSheetRL = view.findViewById(R.id.rl_choose_answer_sheet);
        submitBtn = view.findViewById(R.id.btn_fe_submit);
        fullScreenProgressRL = view.findViewById(R.id.fullscreen_progress_layout);
        subMissionStatusTV = view.findViewById(R.id.tv_submission_status);
        submissionViewRL = view.findViewById(R.id.rl_submission_view);
        submissionListRV = view.findViewById(R.id.rv_submissions_list);

        appPrefs = requireActivity().getSharedPreferences(MyConstants.APP_PREFS, Context.MODE_PRIVATE);
        rollNo = appPrefs.getString(MyConstants.STUDENT_ROLL_NO, "");

        type = appPrefs.getInt(MyConstants.USER_TYPE, -1);

        titleTV.setText(exam.getExamTitle());
        startTimeTV.setText("Starts at : " + exam.getStartTime());
        endTimeTV.setText("Due : " + exam.getEndTime());

        if (type == MyConstants.USER_TYPE_STUDENT) {
            handleFlowForStudent();
        } else if (type == MyConstants.USER_TYPE_TEACHER) {
            handleFlowForTeacher();
        }
    }

    private void handleFlowForTeacher() {
        qpDownloadRL.setVisibility(View.VISIBLE);
        qpDownloadTV.setText(exam.getExamTitle() + ".pdf");
        chooseAnswerSheetRL.setVisibility(View.VISIBLE);

        qpDownloadRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFile();
            }
        });
        studentSubmissionRL.setVisibility(View.GONE);
        submissionViewRL.setVisibility(View.VISIBLE);
        fullScreenProgressRL.setVisibility(View.VISIBLE);
        firestore.collection(MyConstants.SUBMISSIONS)
                .whereEqualTo(MyConstants.EXAM_ID, exam.getExamId())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                fullScreenProgressRL.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                        Submission s = documentSnapshot.toObject(Submission.class);
                        submissions.add(s);
                    }

                    setUpRecyclerView();
                } else {
                    showToast(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                }
            }
        });
    }

    private void setUpRecyclerView() {
        submissionListRVAdapter = new SubmissionListRVAdapter(submissions, this);
        submissionListRV.setAdapter(submissionListRVAdapter);

        submissionListRV.setLayoutManager(new LinearLayoutManager(getContext()));
        submissionListRVAdapter.notifyDataSetChanged();
    }

    private void handleFlowForStudent() {
        submissionViewRL.setVisibility(View.GONE);
        if (hasExamStarted()) {
            qpDownloadRL.setVisibility(View.VISIBLE);
            qpDownloadTV.setText(exam.getExamTitle() + ".pdf");
            chooseAnswerSheetRL.setVisibility(View.VISIBLE);

            qpDownloadRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadFile();
                }
            });
            firestore.collection(MyConstants.SUBMISSIONS)
                    .whereEqualTo(MyConstants.STUDENT_ID, firebaseAuth.getUid())
                    .whereEqualTo(MyConstants.EXAM_ID, exam.getExamId())
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots != null) {
                        ArrayList<Submission> subs = (ArrayList<Submission>) queryDocumentSnapshots.toObjects(Submission.class);
                        if (subs.size() != 0) {
                            submission = subs.get(0);
                        }
                        if (submission != null) {
                            chooseAnswerScriptTV.setText(submission.getRollNo() + ".pdf");
                            subMissionStatusTV.setText(MyConstants.SUBMITTED);
                        }
                    }
                }
            });

            chooseAnswerSheetRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (hasExamStarted() && !hasExamEnded()) {

                        Intent galleryIntent = new Intent();
                        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                        // We will be redirected to choose pdf
                        galleryIntent.setType("application/pdf");
                        startActivityForResult(galleryIntent, REQUEST_UPLOAD);
                    } else {
                        showToast("Exam has ended");
                    }
                }
            });

            submitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submitBtn.setVisibility(View.GONE);
                    if (hasExamStarted() && !hasExamEnded()) {
                        saveSubmissionToDatabase();
                    }
                }
            });

        } else {
            qpDownloadRL.setVisibility(View.GONE);
            chooseAnswerSheetRL.setVisibility(View.GONE);
        }
        if (hasExamEnded()) {
            Log.d("fetched", "exam ended");
            chooseAnswerSheetRL.setOnClickListener(null);
            firestore.collection(MyConstants.SUBMISSIONS)
                    .whereEqualTo(MyConstants.STUDENT_ID, firebaseAuth.getUid())
                    .whereEqualTo(MyConstants.EXAM_ID, exam.getExamId())
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots != null) {
                        ArrayList<Submission> subs = (ArrayList<Submission>) queryDocumentSnapshots.toObjects(Submission.class);
                        if (subs.size() != 0) {
                            submission = subs.get(0);
                        }
                        if (submission != null) {
                            chooseAnswerScriptTV.setText(submission.getRollNo() + ".pdf");
                            subMissionStatusTV.setText(MyConstants.SUBMITTED);
                        } else {
                            chooseAnswerSheetRL.setVisibility(View.GONE);
                            subMissionStatusTV.setText(MyConstants.NOT_SUBMITTED);
                        }
                    }
                }
            });
        }
    }

    private void saveSubmissionToDatabase() {
        String submissionId = System.currentTimeMillis() + rollNo;
        Submission s = new Submission(submissionId, exam.getExamId(), firebaseAuth.getUid(), submissionLink, rollNo);
        firestore.collection(MyConstants.SUBMISSIONS).document(submissionId)
                .set(s).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    showToast("Submitted successfully");
                    subMissionStatusTV.setText(MyConstants.SUBMITTED);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_UPLOAD) {

            // Here we are initialising the progress dialog box
            dialog = new ProgressDialog(getContext());
            dialog.setMessage("Uploading");

            dialog.show();
            imageuri = data.getData();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();

            Log.d("fetched", "select: " + imageuri);
            submissionLink = exam.getExamId() + "/submissons/" + rollNo + "." + "pdf";
            // Here we are uploading the pdf in firebase storage with the name of current time
            StorageReference filepath = storageReference.child(submissionLink);
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
                        showToast("Upload Successful");
                        StorageReference reference = FirebaseStorage.getInstance().getReference(submissionLink);
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                dialog.dismiss();
                                submissionLink = uri.toString();
                                submitBtn.setVisibility(View.VISIBLE);
                            }
                        });
                        chooseAnswerScriptTV.setText(imageuri.getLastPathSegment());
                    }
                }
            });
        }
    }

    private void downloadFile() {
        dialog = new ProgressDialog(getContext());
        dialog.setTitle("Downloading");
        dialog.show();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(exam.getQpLink());

        File rootPath = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), exam.getExamTitle());
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath, exam.getExamTitle() + ".pdf");
        storageRef.getFile(localFile)
                .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        dialog.setProgress((int) progress);
                    }
                }).addOnPausedListener(new OnPausedListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onPaused(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                dialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                dialog.dismiss();
                showLongToast("Download Successful, you can find downloaded files in Android/data/com.vaibhav.onlineexams");
                Log.e("fetched", ";local tem file created  created " + localFile.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                dialog.dismiss();
                Log.e("firebase ", ";local tem file not created  created " + exception.toString());
            }
        });
    }

    private void showLongToast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
    }

    private void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private boolean hasExamStarted() {
        boolean ans = false;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy kk:mm");
        try {
            Date sTime = sdf.parse(exam.getStartTime());
            Date cTime = Calendar.getInstance().getTime();
            Log.d("fetched", cTime.toString() + " " + sTime.toString());
            if (sTime.after(cTime)) {
                ans = false;
            } else {
                ans = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ans;
    }

    private boolean hasExamEnded() {
        boolean ans = false;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy kk:mm");
        try {
            Log.d("fetched", exam.getEndTime());
            Date eTime = sdf.parse(exam.getEndTime());
            Date cTime = Calendar.getInstance().getTime();
            Log.d("fetched", cTime.toString() + " " + Objects.requireNonNull(eTime).toString());
            if (cTime.after(eTime)) {
                ans = true;
            } else {
                ans = false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ans;
    }

    @Override
    public void onSubmissionClicked(Submission submission) {
        dialog = new ProgressDialog(getContext());
        dialog.setTitle("Downloading");
        dialog.show();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(submission.getSubmissionLink());

        File rootPath = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), exam.getExamTitle() + "/submissions/");
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath, submission.getRollNo() + ".pdf");
        storageRef.getFile(localFile)
                .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        dialog.setProgress((int) progress);
                    }
                }).addOnPausedListener(new OnPausedListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onPaused(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                dialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                dialog.dismiss();
                showLongToast("Download Successful, you can find downloaded files in Android/data/com.vaibhav.onlineexams");
                Log.e("fetched", ";local tem file created  created " + localFile.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                dialog.dismiss();
                Log.e("firebase ", ";local tem file not created  created " + exception.toString());
            }
        });
    }
}