package com.aaa.aaa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class Signup extends AppCompatActivity {
    private FirebaseAuth mAuth;
    TextView back;
    EditText name, id, pw, pw2, email, phone_number;
    Button pwcheck, submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();

        //뒤로 가기 버튼
        back = findViewById(R.id.back);
        back.setOnClickListener(v -> onBackPressed());

        //기입 항목
        name = findViewById(R.id.signName);
        pw = findViewById(R.id.signPW);
        pw2 = findViewById(R.id.signPWtest);
        email = findViewById(R.id.signmail);
        phone_number = findViewById(R.id.signPhonenumber);

        //비밀번호 확인 버튼
        pwcheck = findViewById(R.id.pwcheckbutton);
        pwcheck.setOnClickListener(v -> {
            if (pw.getText().toString().equals(pw2.getText().toString())) {
                pwcheck.setText("일치");
            } else {
                Toast.makeText(Signup.this, "비밀번호가 다릅니다.", Toast.LENGTH_LONG).show();
            }
        });

        //회원가입 완료 버튼
        submit = findViewById(R.id.signupbutton);
        submit.setOnClickListener(v -> {
            signup();
        });
    }

    public void signup() {
        //가입 정보 가져오기
        mAuth = FirebaseAuth.getInstance();
        String email = ((EditText) findViewById(R.id.signmail)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.signPW)).getText().toString().trim();
        String name = ((EditText) findViewById(R.id.signName)).getText().toString().trim();
        String passwordtest = ((EditText) findViewById(R.id.signPWtest)).getText().toString().trim();
        String phone_number = ((EditText) findViewById(R.id.signPhonenumber)).getText().toString().trim();

        if (email.length() > 0 && password.length() > 0 && passwordtest.length() > 0
                && phone_number.length() > 0) {
            if (password.equals(passwordtest) && phone_number.matches("[+-]?\\d*(\\.\\d+)?") == true) {
                //파이어베이스에 신규계정 등록하기
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(Signup.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //가입 성공시
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            int p_number = Integer.parseInt(phone_number);
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            MemberInfo memberInfo = new MemberInfo(name, p_number);
                            db.collection("user").document(user.getUid()).set(memberInfo);

                            //가입이 이루어져을시 가입 화면을 빠져나감.
                            StartMyActivity(LoginActivity.class);
                            finish();
                            toast("회원가입 성공");

                        } else {
                            toast("회원가입 실패");
                            return;  //해당 메소드 진행을 멈추고 빠져나감.

                        }

                    }
                });
                //비밀번호 오류시
            } else if (password.equals(passwordtest) == false) {
                toast("비밀번호가 맞지 않습니다.");
                return;
            } else if (phone_number.matches("[+-]?\\d*(\\.\\d+)?") == false) {
                toast("전화번호는 숫자만 입력 해 주세요.");
            }
            //한 항목이라도 비어 있을 시
        } else {
            toast("모든 항목을 입력해주세요.");
        }

    }


    //토스트 메세지
    public void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void StartMyActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }
}
