package unics.okeffect.sample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import unics.okeffect.Effects;

/**
 * Create by luochao
 * on 2023/12/12
 */
public class JavaSampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effect_drawable_layout);

        View layout1 = findViewById(R.id.layout1);
        layout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(JavaSampleActivity.this, EffectLayoutSampleActivity.class)) ;
            }
        });

        View layout12 = findViewById(R.id.layout12);

        //阴影 + viewPadding
        Effects.withDraw().setShadow(20f, Color.RED).buildParams().create();// .into(layout1);
        //阴影
        Effects.withNinePatch(this, R.drawable.bg_shadow).into(layout12);
    }
}
