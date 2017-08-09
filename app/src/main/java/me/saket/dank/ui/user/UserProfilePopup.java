package me.saket.dank.ui.user;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.IdRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import java.util.Date;
import me.saket.dank.R;
import me.saket.dank.data.RedditLink;
import me.saket.dank.data.ResolvedError;
import me.saket.dank.di.Dank;
import me.saket.dank.utils.RxUtils;
import me.saket.dank.utils.Strings;

public class UserProfilePopup extends PopupWindowWithTransition {

  @BindView(R.id.userprofilepopup_cover_image) ImageView coverImageView;
  @BindView(R.id.userprofilepopup_profile_image) ImageView profileImageView;
  @BindView(R.id.userprofilepopup_username) TextView usernameView;
  @BindView(R.id.userprofilepopup_stats_viewflipper) ViewFlipper statsViewFlipper;
  @BindView(R.id.userprofilepopup_account_age) TextView accountAgeView;
  @BindView(R.id.userprofilepopup_link_karma) TextView linkKarmaView;
  @BindView(R.id.userprofilepopup_comment_karma) TextView commentKarmaView;
  @BindView(R.id.userprofilepopup_stats_load_error_message) TextView errorStateMessageView;
  @BindView(R.id.userprofilepopup_stats_load_error_retry) Button errorStateRetryButton;

  private CompositeDisposable onDismissDisposables = new CompositeDisposable();

  private enum StatsLoadState {
    IN_FLIGHT,
    ERROR,
    FETCHED
  }

  @SuppressLint("InflateParams")
  public UserProfilePopup(Context context) {
    super(context);

    View popupView = LayoutInflater.from(context).inflate(R.layout.popup_user_profile, null);
    ButterKnife.bind(this, popupView);
    setContentView(popupView);

    setOnDismissListener(() -> onDismissDisposables.clear());
  }

  public void loadUserProfile(RedditLink.User userLink) {
    String username = userLink.name;
    coverImageView.setContentDescription(coverImageView.getResources().getString(R.string.cd_userprofilepopup_cover_image, username));
    profileImageView.setContentDescription(profileImageView.getResources().getString(R.string.cd_userprofilepopup_profile_image, username));
    usernameView.setText(usernameView.getResources().getString(R.string.user_name_u_prefix, username));

    coverImageView.setVisibility(View.GONE);
    profileImageView.setVisibility(View.GONE);
    showStatsLoadState(StatsLoadState.IN_FLIGHT);

    onDismissDisposables.add(
        Dank.reddit().userProfile(username)
            .compose(RxUtils.applySchedulersSingle())
            .subscribe(
                userAccount -> {
                  showStatsLoadState(StatsLoadState.FETCHED);

                  accountAgeView.setText(constructAccountAgeSinceText(userAccount.getCreated()));
                  linkKarmaView.setText(Strings.abbreviateScore(userAccount.getLinkKarma()));
                  commentKarmaView.setText(Strings.abbreviateScore(userAccount.getCommentKarma()));
                },
                error -> {
                  showStatsLoadState(StatsLoadState.ERROR);

                  ResolvedError resolvedError = Dank.errors().resolve(error);
                  errorStateMessageView.setText(resolvedError.errorMessageRes());
                  errorStateRetryButton.setOnClickListener(v -> loadUserProfile(userLink));
                })
    );
  }

  private String constructAccountAgeSinceText(Date createdDate) {
    // TODO: Implement this.
    //DateTime accountCreationDate = DateTime.forInstant(createdDate.getTime(), TimeZone.getTimeZone("UTC"));
    //DateTime nowDate = DateTime.forInstant(System.currentTimeMillis(), TimeZone.getTimeZone("UTC"));
    //int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
    //int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
    return "2m";
  }

  private void showStatsLoadState(StatsLoadState loadState) {
    @IdRes int stateViewIdRes;

    switch (loadState) {
      case IN_FLIGHT:
        stateViewIdRes = R.id.userprofilepopup_stats_load_progress;
        break;

      case ERROR:
        stateViewIdRes = R.id.userprofilepopup_stats_load_error;
        break;

      case FETCHED:
        stateViewIdRes = R.id.userprofilepopup_stats_container;
        break;

      default:
        throw new AssertionError("Unknown state: " + loadState);
    }

    statsViewFlipper.setDisplayedChild(statsViewFlipper.indexOfChild(ButterKnife.findById(statsViewFlipper, stateViewIdRes)));
  }

  @OnClick(R.id.userprofilepopup_send_private_message)
  void onClickSendPrivateMessage() {
    // TODO.
  }

  @OnClick(R.id.userprofilepopup_view_full_profile)
  void onClickViewFullProfile(Button b) {
    Toast.makeText(b.getContext(), R.string.work_in_progress, Toast.LENGTH_SHORT).show();
  }
}