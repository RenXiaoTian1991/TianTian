package cy.studiodemo.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import cy.studiodemo.R;
import cy.studiodemo.base.BaseFragment;
import cy.studiodemo.view.ScrollingImageView;

/**
 * Created by cuiyue on 15/7/27.
 */
public class CarFragment extends BaseFragment implements View.OnClickListener {

    private FrameLayout fl;
    private Button bt_start;
    private Button bt_stop;
    private Button bt_speed_up;
    private Button bt_speed_down;
    private Button bt_up;
    private Button bt_down;

    private ScrollingImageView scroll_img;

    private boolean isRoadUpOrDown;

    @Override
    protected void initVariable() {
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_car;
    }

    @Override
    protected void findViews(View rootView) {
        fl = (FrameLayout) rootView.findViewById(R.id.fl);
        scroll_img = (ScrollingImageView) rootView.findViewById(R.id.scroll_img);
        bt_start = (Button) rootView.findViewById(R.id.bt_start);
        bt_stop = (Button) rootView.findViewById(R.id.bt_stop);
        bt_speed_up = (Button) rootView.findViewById(R.id.bt_speed_up);
        bt_speed_down = (Button) rootView.findViewById(R.id.bt_speed_down);
        bt_up = (Button) rootView.findViewById(R.id.bt_up);
        bt_down = (Button) rootView.findViewById(R.id.bt_down);
    }

    @Override
    protected void setListensers() {
        bt_start.setOnClickListener(this);
        bt_stop.setOnClickListener(this);
        bt_speed_up.setOnClickListener(this);
        bt_speed_down.setOnClickListener(this);
        bt_up.setOnClickListener(this);
        bt_down.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_start:
                carStart();
                break;
            case R.id.bt_stop:
                carStop();
                break;
            case R.id.bt_speed_up:
                speedUP();
                break;
            case R.id.bt_speed_down:
                speedDown();
                break;
            case R.id.bt_up:
                roadUp();
                break;
            case R.id.bt_down:
                roadDown();
                break;
        }
    }

    private void carStart() {
        scroll_img.start();
    }

    private void carStop() {
        scroll_img.stop();
    }

    private void speedUP() {
        scroll_img.setSpeed(scroll_img.getSpeed() + 5);
    }

    private void speedDown() {
        scroll_img.setSpeed(scroll_img.getSpeed() - 5);
    }

    private void roadUp() {
        if (isRoadUpOrDown) {
            return;
        }
        float rotation = fl.getRotation();
        ObjectAnimator animator = ObjectAnimator.ofFloat(fl, "rotation", rotation, (rotation - 15F));
        animator.setDuration(2000);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isRoadUpOrDown = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isRoadUpOrDown = false;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }

    private void roadDown() {
        if (isRoadUpOrDown) {
            return;
        }
        float rotation = fl.getRotation();
        ObjectAnimator animator = ObjectAnimator.ofFloat(fl, "rotation", rotation, (rotation + 15F));
        animator.setDuration(2000);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isRoadUpOrDown = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isRoadUpOrDown = false;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }
}
