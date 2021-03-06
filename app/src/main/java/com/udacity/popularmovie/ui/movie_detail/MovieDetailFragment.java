package com.udacity.popularmovie.ui.movie_detail;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.udacity.popularmovie.R;
import com.udacity.popularmovie.data.models.MovieResult;
import com.udacity.popularmovie.data.models.MovieReviewResult;
import com.udacity.popularmovie.data.models.MovieTrailerResult;
import com.udacity.popularmovie.databinding.MovieDetailBinding;
import com.udacity.popularmovie.ui.movie_landing.MovieListActivity;
import com.udacity.popularmovie.util.DateUtils;
import com.udacity.popularmovie.util.NetworkUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a single MovieResponse detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_ITEM_TRANSITION_ID = "item_transition_id";
    public static final String ARG_ITEM_COLOR_PALETTE = "movie_color_palette";

    /**
     * The dummy content this fragment is presenting.
     */
    private MovieResult mItem;
    private String mTransitionName;
    private int mColorPalette;
    private MovieTrailerRecyclerViewAdapter mAdapter;
    private MovieReviewRecyclerViewAdapter mAdapterReview;
    private MovieDetailViewModel mViewModel;

    @BindView(R.id.background_view)
    View mViewBackground;

    @BindView(R.id.iv_movie_poster)
    AppCompatImageView mMoviePoster;

    @BindView(R.id.tv_release_date)
    AppCompatTextView mTvReleaseDate;

    @BindView(R.id.tv_review)
    AppCompatTextView mTvReview;

    @BindView(R.id.tv_video)
    AppCompatTextView mTvVideo;

    @BindView(R.id.rv_movie_trailer)
    RecyclerView mRVMovieTrailer;

    @BindView(R.id.rv_movie_review)
    RecyclerView mRVMovieReview;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = getArguments().getParcelable(ARG_ITEM_ID);
            mTransitionName = getArguments().getString(ARG_ITEM_TRANSITION_ID);
            mColorPalette = getArguments().getInt(ARG_ITEM_COLOR_PALETTE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_movie_detail, menu);
        if (!mViewModel.isMovieAlreadyFavourite(mItem.id)) {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite_border_default_24dp));
        } else {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_favorite_selected_24dp));
        }
        mViewModel = ViewModelProviders.of(this).get(MovieDetailViewModel.class);
        mViewModel.getFavouriteDrawableId().observe(this, integer -> menu.getItem(0).setIcon(ContextCompat.getDrawable(getActivity(), integer)));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.favourite:
                if (mItem != null) {
                    if (!mViewModel.isMovieAlreadyFavourite(mItem.id)) {
                        mViewModel.addMovieFavourite(mItem.id);
                    } else {
                        mViewModel.removeMovieFavourite(mItem.id);
                    }
                } else {
                    Toast.makeText(getActivity(), "There's something wrong right now, please try again later", Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MovieDetailBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.movie_detail, container, false);
        binding.setMovie(mItem);
        View view = binding.getRoot();
        ButterKnife.bind(this, view);
        mTvReleaseDate.setText(DateUtils.formattedDateFromString(mItem.releaseDate));
        mMoviePoster.setTransitionName(mTransitionName);
        if ((mColorPalette != 0)) {
            mViewBackground.setBackgroundColor(mColorPalette);
        } else {
            if (getActivity() != null) {
                mViewBackground.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.movieDetailTitleBg));
            }
        }
        mViewModel = ViewModelProviders.of(this).get(MovieDetailViewModel.class);
        // Handle changes emitted by LiveData
        mViewModel.getApiResponse().observe(this, apiResponse -> {
            if (apiResponse != null) {
                if (apiResponse.getError() != null) {
                    Log.e("MovieDetailFragment", apiResponse.getError().getMessage());
                } else {
                    if (apiResponse.getMovieTrailerResponse() != null
                            && apiResponse.getMovieTrailerResponse().movieTrailerResultList != null) {
                        setupRecyclerViewMovieTrailer(mRVMovieTrailer, apiResponse.getMovieTrailerResponse().movieTrailerResultList);
                        mTvVideo.setVisibility(View.VISIBLE);
                    }
                    if (apiResponse.getMovieReviewResponse() != null
                            && apiResponse.getMovieReviewResponse().results != null) {
                        setupRecyclerViewMovieReview(mRVMovieReview, apiResponse.getMovieReviewResponse().results);
                        mTvReview.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        if (NetworkUtils.isOnline(getActivity())) {
            mViewModel.loadMovieTrailers(mItem.id);
            mViewModel.loadMovieReviews(mItem.id);
        }
        return view;
    }

    private void setupRecyclerViewMovieTrailer(@NonNull RecyclerView recyclerView, @NonNull List<MovieTrailerResult> movieTrailerResults) {
        mAdapter = new MovieTrailerRecyclerViewAdapter(getActivity(), movieTrailerResults);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }

    private void setupRecyclerViewMovieReview(@NonNull RecyclerView recyclerView, @NonNull List<MovieReviewResult> movieReviewResults) {
        mAdapterReview = new MovieReviewRecyclerViewAdapter(movieReviewResults);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapterReview);
    }
}