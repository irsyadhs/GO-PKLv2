package go_pkl.irsyaddhs.cs.upi.edu.go_pkl;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class RegisterAct extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void klikButton(View v){
        EditText edNama = (EditText) findViewById(R.id.etUsername);
        EditText edPass = (EditText) findViewById(R.id.etPassword);

        String nama = edNama.getText().toString();
        String pass = edPass.getText().toString();


        String[] mystr = new String[]{nama, pass};

        Intent i = getIntent();
        i.putExtra("strings", mystr);
        setResult(RESULT_OK, i);
        finish();
    }
}
